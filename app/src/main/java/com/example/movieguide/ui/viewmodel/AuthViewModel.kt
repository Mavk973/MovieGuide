package com.example.movieguide.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private val _authResult = MutableStateFlow<AuthResult?>(null)
    val authResult: StateFlow<AuthResult?> = _authResult.asStateFlow()

    init {
        checkAuthState()
        observeAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            val currentUser = authRepository.currentUser
            if (currentUser != null) {
                loadUserProfile(currentUser.uid)
            } else {
                _authState.value = AuthUiState.Unauthenticated
            }
        }
    }

    private fun observeAuthState() {
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            viewModelScope.launch {
                val user = auth.currentUser
                if (user != null) {
                    loadUserProfile(user.uid)
                } else {
                    _authState.value = AuthUiState.Unauthenticated
                }
            }
        }
    }

    private suspend fun loadUserProfile(userId: String) {
        userRepository.getUserProfile(userId)
            .onSuccess { user ->
                _authState.value = AuthUiState.Authenticated(user)
            }
            .onFailure {
                // Если профиль не найден, создаем базовый профиль
                val firebaseUser = authRepository.currentUser
                if (firebaseUser != null) {
                    val newUser = User(
                        id = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        displayName = firebaseUser.displayName ?: ""
                    )
                    userRepository.createUserProfile(newUser)
                    _authState.value = AuthUiState.Authenticated(newUser)
                } else {
                    _authState.value = AuthUiState.Unauthenticated
                }
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
}

