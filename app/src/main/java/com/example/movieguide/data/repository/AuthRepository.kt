package com.example.movieguide.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun isUserLoggedIn(): Boolean = currentUser != null

    private fun getErrorMessage(exception: Exception): String {
        return when {
            exception is FirebaseAuthException -> {
                when (exception.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "Неверный формат email"
                    "ERROR_USER_DISABLED" -> "Аккаунт отключен"
                    "ERROR_USER_NOT_FOUND" -> "Пользователь не найден"
                    "ERROR_WRONG_PASSWORD" -> "Неверный пароль"
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "Email уже используется"
                    "ERROR_WEAK_PASSWORD" -> "Пароль слишком слабый"
                    "ERROR_OPERATION_NOT_ALLOWED" -> "Операция не разрешена"
                    "ERROR_INVALID_CREDENTIAL" -> "Неверные учетные данные"
                    "ERROR_NETWORK_REQUEST_FAILED" -> "Ошибка сети. Проверьте подключение к интернету"
                    else -> {
                        // Проверяем сообщение об ошибке на CONFIGURATION_NOT_FOUND
                        val message = exception.message ?: ""
                        when {
                            message.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) -> 
                                "Firebase Authentication не настроен. Пожалуйста, включите Authentication в Firebase Console"
                            message.contains("network", ignoreCase = true) -> 
                                "Ошибка сети. Проверьте подключение к интернету"
                            else -> exception.message ?: "Произошла ошибка при аутентификации"
                        }
                    }
                }
            }
            exception.message?.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) == true -> 
                "Firebase Authentication не настроен. Пожалуйста, включите Authentication в Firebase Console"
            exception.message?.contains("network", ignoreCase = true) == true -> 
                "Ошибка сети. Проверьте подключение к интернету"
            else -> exception.message ?: "Произошла ошибка"
        }
    }

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    suspend fun signUp(email: String, password: String, displayName: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.updateProfile(
                com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
            )?.await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(getErrorMessage(e)))
        }
    }
}

