package com.trevor.final_resort

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseErrorChecker {
    private const val TAG = "FirebaseErrorChecker"
    
    fun checkAllFirebaseServices(context: Context): String {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        try {
            // Check 1: Firebase App initialization
            try {
                val app = FirebaseApp.getInstance()
                Log.d(TAG, "✅ Firebase App: Initialized successfully")
            } catch (e: Exception) {
                errors.add("Firebase App initialization failed: ${e.message}")
                Log.e(TAG, "❌ Firebase App: ${e.message}", e)
            }
            
            // Check 2: Firebase Auth
            try {
                val auth = FirebaseAuth.getInstance()
                Log.d(TAG, "✅ Firebase Auth: Initialized successfully")
            } catch (e: Exception) {
                errors.add("Firebase Auth initialization failed: ${e.message}")
                Log.e(TAG, "❌ Firebase Auth: ${e.message}", e)
            }
            
            // Check 3: Firestore
            try {
                val firestore = FirebaseFirestore.getInstance()
                Log.d(TAG, "✅ Firestore: Initialized successfully")
            } catch (e: Exception) {
                errors.add("Firestore initialization failed: ${e.message}")
                Log.e(TAG, "❌ Firestore: ${e.message}", e)
            }
            
            // Check 4: Storage
            try {
                val storage = FirebaseStorage.getInstance()
                Log.d(TAG, "✅ Firebase Storage: Initialized successfully")
            } catch (e: Exception) {
                errors.add("Firebase Storage initialization failed: ${e.message}")
                Log.e(TAG, "❌ Firebase Storage: ${e.message}", e)
            }
            
            // Check 5: Configuration file
            try {
                val configFile = context.assets.list("")?.contains("google-services.json")
                if (configFile == true) {
                    Log.d(TAG, "✅ Configuration: google-services.json found in assets")
                } else {
                    warnings.add("google-services.json not found in assets (this is normal if using app-level config)")
                }
            } catch (e: Exception) {
                warnings.add("Could not check assets: ${e.message}")
            }
            
            // Check 6: Package name
            val packageName = context.packageName
            if (packageName == "com.trevor.final_resort") {
                Log.d(TAG, "✅ Package name: Correct ($packageName)")
            } else {
                errors.add("Package name mismatch: Expected 'com.trevor.final_resort', got '$packageName'")
                Log.e(TAG, "❌ Package name: $packageName")
            }
            
        } catch (e: Exception) {
            errors.add("General Firebase check failed: ${e.message}")
            Log.e(TAG, "❌ General check failed: ${e.message}", e)
        }
        
        // Generate report
        val report = StringBuilder()
        report.appendLine("=== Firebase Configuration Report ===")
        
        if (errors.isEmpty() && warnings.isEmpty()) {
            report.appendLine("✅ All Firebase services are properly configured!")
        } else {
            if (errors.isNotEmpty()) {
                report.appendLine("\n❌ ERRORS:")
                errors.forEach { error ->
                    report.appendLine("  • $error")
                }
            }
            
            if (warnings.isNotEmpty()) {
                report.appendLine("\n⚠️ WARNINGS:")
                warnings.forEach { warning ->
                    report.appendLine("  • $warning")
                }
            }
        }
        
        val reportText = report.toString()
        Log.i(TAG, reportText)
        return reportText
    }
    
    fun testFirebaseConnection(): String {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("test").document("test")
                .get()
                .addOnSuccessListener {
                    Log.d(TAG, "✅ Firestore connection test: SUCCESS")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "❌ Firestore connection test: FAILED - ${e.message}", e)
                }
            
            "Firestore connection test initiated. Check logs for results."
        } catch (e: Exception) {
            val error = "Firestore connection test failed: ${e.message}"
            Log.e(TAG, "❌ $error", e)
            error
        }
    }
} 