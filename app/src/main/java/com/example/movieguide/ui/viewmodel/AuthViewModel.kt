package com.example.movieguide.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.movieguide.data.model.User
import com.example.movieguide.data.repository.AuthRepository
import com.example.movieguide.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Loading : AuthUiState()
    object Unauthenticated : AuthUiState()
    data class Authenticated(val user: User) : AuthUiState()
    object Guest : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthViewModel(
    application: Application,
    private val authRepository: AuthRepository = AuthRepository(application),
    private val userRepository: UserRepository = UserRepository()
) : AndroidViewModel(application) {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private val _authResult = MutableStateFlow<AuthResult?>(null)
    val authResult: StateFlow<AuthResult?> = _authResult.asStateFlow()

    init {
        try {
            Log.d(TAG, "AuthViewModel init: начало инициализации")
            checkAuthState()
            observeAuthState()
            Log.d(TAG, "AuthViewModel init: успешно")
        } catch (e: Exception) {
            // Если Firebase не инициализирован или произошла другая ошибка,
            // устанавливаем состояние как неавторизованный
            Log.e(TAG, "ОШИБКА в AuthViewModel init", e)
            _authState.value = AuthUiState.Unauthenticated
            e.printStackTrace()
        }
    }
    
    companion object {
        private const val TAG = "AuthViewModel"
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "checkAuthState: проверка текущего пользователя")
                val currentUser = authRepository.currentUser
                if (currentUser != null) {
                    Log.d(TAG, "checkAuthState: пользователь найден, загрузка профиля")
                    loadUserProfile(currentUser.uid)
                } else {
                    Log.d(TAG, "checkAuthState: пользователь не найден")
                    _authState.value = AuthUiState.Unauthenticated
                }
            } catch (e: Exception) {
                // В случае ошибки устанавливаем состояние как неавторизованный
                Log.e(TAG, "ОШИБКА в checkAuthState", e)
                _authState.value = AuthUiState.Unauthenticated
                e.printStackTrace()
            }
        }
    }

    private fun observeAuthState() {
        try {
            Log.d(TAG, "observeAuthState: добавление listener")
            FirebaseAuth.getInstance().addAuthStateListener { auth ->
                viewModelScope.launch {
                    try {
                        Log.d(TAG, "observeAuthState: изменение состояния аутентификации")
                        val user = auth.currentUser
                        if (user != null) {
                            Log.d(TAG, "observeAuthState: пользователь авторизован")
                            loadUserProfile(user.uid)
                        } else {
                            Log.d(TAG, "observeAuthState: пользователь не авторизован")
                            _authState.value = AuthUiState.Unauthenticated
                        }
                    } catch (e: Exception) {
                        // В случае ошибки устанавливаем состояние как неавторизованный
                        Log.e(TAG, "ОШИБКА в observeAuthState listener", e)
                        _authState.value = AuthUiState.Unauthenticated
                        e.printStackTrace()
                    }
                }
            }
            Log.d(TAG, "observeAuthState: listener добавлен успешно")
        } catch (e: Exception) {
            // Если не удалось добавить listener, устанавливаем состояние как неавторизованный
            Log.e(TAG, "ОШИБКА при добавлении observeAuthState listener", e)
            _authState.value = AuthUiState.Unauthenticated
            e.printStackTrace()
        }
    }

    private suspend fun loadUserProfile(userId: String) {
        try {
            userRepository.getUserProfile(userId)
                .onSuccess { user ->
                    _authState.value = AuthUiState.Authenticated(user)
                }
                .onFailure { exception ->
                    // Если профиль не найден или произошла ошибка, создаем базовый профиль
                    try {
                        val firebaseUser = authRepository.currentUser
                        if (firebaseUser != null) {
                            val newUser = User(
                                id = firebaseUser.uid,
                                email = firebaseUser.email ?: "",
                                displayName = firebaseUser.displayName ?: ""
                            )
                            // Пытаемся создать профиль, но не ждем результата
                            userRepository.createUserProfile(newUser)
                            _authState.value = AuthUiState.Authenticated(newUser)
                        } else {
                            _authState.value = AuthUiState.Unauthenticated
                        }
                    } catch (e: Exception) {
                        // Если не удалось создать профиль, все равно устанавливаем состояние
                        val firebaseUser = authRepository.currentUser
                        if (firebaseUser != null) {
                            val newUser = User(
                                id = firebaseUser.uid,
                                email = firebaseUser.email ?: "",
                                displayName = firebaseUser.displayName ?: ""
                            )
                            _authState.value = AuthUiState.Authenticated(newUser)
                        } else {
                            _authState.value = AuthUiState.Unauthenticated
                        }
                        e.printStackTrace()
                    }
                }
        } catch (e: Exception) {
            // В случае критической ошибки устанавливаем состояние как неавторизованный
            _authState.value = AuthUiState.Unauthenticated
            e.printStackTrace()
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authResult.value = null
            authRepository.signIn(email, password)
                .onSuccess { firebaseUser ->
                    loadUserProfile(firebaseUser.uid)
                    _authResult.value = AuthResult.Success
                }
                .onFailure { exception ->
                    _authResult.value = AuthResult.Error(
                        exception.message ?: "Ошибка входа"
                    )
                }
        }
    }

    fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _authResult.value = null
            authRepository.signUp(email, password, displayName)
                .onSuccess { firebaseUser ->
                    val user = User(
                        id = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        displayName = displayName
                    )
                    userRepository.createUserProfile(user)
                        .onSuccess {
                            _authState.value = AuthUiState.Authenticated(user)
                            _authResult.value = AuthResult.Success
                        }
                        .onFailure { exception ->
                            _authResult.value = AuthResult.Error(
                                exception.message ?: "Ошибка создания профиля"
                            )
                        }
                }
                .onFailure { exception ->
                    _authResult.value = AuthResult.Error(
                        exception.message ?: "Ошибка регистрации"
                    )
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            val currentState = _authState.value
            if (currentState is AuthUiState.Guest) {
                // Если пользователь был гостем, просто переводим в неавторизованное состояние
                _authState.value = AuthUiState.Unauthenticated
            } else {
                // Если пользователь был авторизован через Firebase, выходим из Firebase
                authRepository.signOut()
                    .onSuccess {
                        _authState.value = AuthUiState.Unauthenticated
                    }
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authResult.value = null
            authRepository.resetPassword(email)
                .onSuccess {
                    _authResult.value = AuthResult.Success
                }
                .onFailure { exception ->
                    _authResult.value = AuthResult.Error(
                        exception.message ?: "Ошибка отправки письма"
                    )
                }
        }
    }

    fun clearAuthResult() {
        _authResult.value = null
    }

    fun signInAsGuest() {
        viewModelScope.launch {
            _authResult.value = null
            _authState.value = AuthUiState.Guest
            _authResult.value = AuthResult.Success
        }
    }

    fun isGuest(): Boolean {
        return _authState.value is AuthUiState.Guest
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authResult.value = null
            authRepository.signInWithGoogle(idToken)
                .onSuccess { firebaseUser ->
                    loadUserProfile(firebaseUser.uid)
                    _authResult.value = AuthResult.Success
                }
                .onFailure { exception ->
                    _authResult.value = AuthResult.Error(
                        exception.message ?: "Ошибка входа через Google"
                    )
                }
        }
    }
}

