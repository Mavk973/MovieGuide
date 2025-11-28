package com.example.movieguide.data.repository

import com.example.movieguide.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun createUserProfile(user: User): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(user.id)
                .set(user)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(userId: String): Result<User> {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                val user = document.toObject(User::class.java)
                if (user != null && user.id.isNotEmpty()) {
                    Result.success(user)
                } else {
                    // Если документ существует, но не может быть преобразован, возвращаем базовый профиль
                    Result.success(User(id = userId))
                }
            } else {
                Result.success(User(id = userId))
            }
        } catch (e: Exception) {
            // В случае ошибки (например, нет сети или Firebase не инициализирован),
            // возвращаем базовый профиль вместо ошибки
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(user.id)
                .set(user, com.google.firebase.firestore.SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid
}

