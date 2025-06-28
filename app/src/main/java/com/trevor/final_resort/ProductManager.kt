package com.trevor.final_resort

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ProductManager {
    private val firebaseService = FirebaseService()
    
    suspend fun addProduct(product: Product, imageUris: List<Uri>): Result<String> {
        return withContext(Dispatchers.IO) {
            firebaseService.addProduct(product, imageUris)
        }
    }
    
    suspend fun getAllProducts(): List<Product> {
        return withContext(Dispatchers.IO) {
            firebaseService.getAllProducts().getOrElse { emptyList() }
        }
    }
    
    suspend fun getProductsBySeller(sellerId: String): List<Product> {
        return withContext(Dispatchers.IO) {
            firebaseService.getProductsBySeller(sellerId).getOrElse { emptyList() }
        }
    }
    
    suspend fun getProductById(productId: String): Product? {
        return withContext(Dispatchers.IO) {
            val allProducts = firebaseService.getAllProducts().getOrElse { emptyList() }
            allProducts.find { it.id == productId }
        }
    }
    
    suspend fun addRating(productId: String, rating: Rating): Boolean {
        return withContext(Dispatchers.IO) {
            firebaseService.addRating(productId, rating).isSuccess
        }
    }
    
    suspend fun getAverageRating(productId: String): Float {
        return withContext(Dispatchers.IO) {
            firebaseService.getAverageRating(productId)
        }
    }
    
    suspend fun deleteProduct(productId: String, sellerId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // For now, we'll just return false as deletion from Firestore
                // requires additional implementation
                // In a real app, you would implement the delete functionality
                false
            } catch (e: Exception) {
                false
            }
        }
    }
} 