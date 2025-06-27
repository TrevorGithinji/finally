package com.trevor.final_resort

object UserManager {
    private val registeredUsers = mutableListOf<User>()
    
    fun registerUser(user: User): Boolean {
        // Check if user with this email already exists
        if (registeredUsers.any { it.email == user.email }) {
            return false
        }
        registeredUsers.add(user)
        return true
    }
    
    fun loginUser(email: String, password: String): User? {
        return registeredUsers.find { it.email == email && it.password == password }
    }
    
    fun getUserByEmail(email: String): User? {
        return registeredUsers.find { it.email == email }
    }
    
    fun getAllUsers(): List<User> {
        return registeredUsers.toList()
    }
} 