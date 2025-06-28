package com.trevor.final_resort

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseTest {
    private const val TAG = "FirebaseTest"
    
    fun testFirebaseInitialization() {
        try {
            // Test Firebase App initialization
            val app = FirebaseApp.getInstance()
            Log.d(TAG, "Firebase App initialized successfully: ${app.name}")
            
            // Test Firebase Auth
            val auth = FirebaseAuth.getInstance()
            Log.d(TAG, "Firebase Auth initialized successfully")
            
            // Test Firestore
            val firestore = FirebaseFirestore.getInstance()
            Log.d(TAG, "Firestore initialized successfully")
            
            // Test Storage
            val storage = FirebaseStorage.getInstance()
            Log.d(TAG, "Firebase Storage initialized successfully")
            
            Log.d(TAG, "All Firebase services initialized successfully!")
            
        } catch (e: Exception) {
            Log.e(TAG, "Firebase initialization failed: ${e.message}", e)
        }
    }
    
    fun testFirebaseConnection() {
        // Test if we can access Firestore
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("test").document("test")
            .get()
            .addOnSuccessListener {
                Log.d(TAG, "Firestore connection successful")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Firestore connection failed: ${e.message}", e)
            }
    }
} 