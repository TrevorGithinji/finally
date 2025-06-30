package com.trevor.final_resort

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UserManager {
    private val firebaseService = FirebaseService()
    
    suspend fun registerUser(email: String, password: String, firstName: String, secondName: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val firebaseUser = firebaseService.registerUser(email, password, firstName, secondName).getOrThrow()
                val user = User(
                    email = email,
                    firstName = firstName,
                    secondName = secondName,
                    password = password
                )
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun loginUser(email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val firebaseUser = firebaseService.loginUser(email, password).getOrThrow()
                val userData = firebaseService.getUserData(firebaseUser.uid).getOrThrow()
                if (userData != null) {
                    Result.success(userData)
                } else {
                    Result.failure(Exception("User data not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getCurrentUser(): User? {
        val firebaseUser = firebaseService.getCurrentUser()
        return firebaseUser?.let { user ->
            // Fetch the full user data from Firestore
            firebaseService.getUserData(user.uid).getOrNull()
        }
    }
    
    fun logout() {
        firebaseService.logout()
    }
    
    suspend fun getUserByEmail(email: String): User? {
        return withContext(Dispatchers.IO) {
            try {
                val firebaseUser = firebaseService.getCurrentUser()
                if (firebaseUser?.email == email) {
                    firebaseService.getUserData(firebaseUser.uid).getOrNull()
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
} 