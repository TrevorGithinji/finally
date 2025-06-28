package com.trevor.final_resort

import android.net.Uri
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseService {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    
    init {
        // Verify Firebase is initialized
        try {
            FirebaseApp.getInstance()
            android.util.Log.d("FirebaseService", "Firebase is properly initialized")
        } catch (e: Exception) {
            android.util.Log.e("FirebaseService", "Firebase not initialized: ${e.message}", e)
        }
    }
    
    // Authentication methods
    suspend fun registerUser(email: String, password: String, firstName: String, secondName: String): Result<FirebaseUser> {
        return try {
            android.util.Log.d("FirebaseService", "Attempting to register user: $email")
            
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            android.util.Log.d("FirebaseService", "User registration successful: ${result.user?.uid}")
            
            result.user?.let { user ->
                // Save additional user data to Firestore (without password for security)
                val userData = hashMapOf(
                    "email" to email,
                    "firstName" to firstName,
                    "secondName" to secondName,
                    "createdAt" to System.currentTimeMillis()
                )
                firestore.collection("users").document(user.uid).set(userData).await()
                android.util.Log.d("FirebaseService", "User data saved to Firestore")
            }
            Result.success(result.user!!)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseService", "User registration failed: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun loginUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    
    fun logout() = auth.signOut()
    
    // User data methods
    suspend fun getUserData(userId: String): Result<User?> {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            if (document.exists()) {
                val userData = document.data!!
                val user = User(
                    email = userData["email"] as String,
                    firstName = userData["firstName"] as String,
                    secondName = userData["secondName"] as String,
                    password = "" // Don't store passwords in Firestore for security
                )
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Product methods
    suspend fun addProduct(product: Product, imageUris: List<Uri>): Result<String> {
        return try {
            val productId = UUID.randomUUID().toString()
            val uploadedImageUrls = mutableListOf<String>()
            
            // Upload images to Firebase Storage
            for (imageUri in imageUris) {
                val imageRef = storage.reference.child("products/$productId/${UUID.randomUUID()}.jpg")
                val uploadTask = imageRef.putFile(imageUri).await()
                val downloadUrl = uploadTask.storage.downloadUrl.await()
                uploadedImageUrls.add(downloadUrl.toString())
            }
            
            // Save product data to Firestore
            val productData = hashMapOf(
                "id" to productId,
                "name" to product.name,
                "description" to product.description,
                "price" to product.price,
                "sellerId" to product.sellerId,
                "sellerName" to product.sellerName,
                "sellerPhone" to product.sellerPhone,
                "category" to product.category,
                "images" to uploadedImageUrls,
                "ratings" to product.ratings,
                "createdAt" to System.currentTimeMillis()
            )
            
            firestore.collection("products").document(productId).set(productData).await()
            Result.success(productId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllProducts(): Result<List<Product>> {
        return try {
            val snapshot = firestore.collection("products")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get().await()
            
            val products = snapshot.documents.mapNotNull { document ->
                val data = document.data
                if (data != null) {
                    Product(
                        id = data["id"] as String,
                        name = data["name"] as String,
                        description = data["description"] as String,
                        price = (data["price"] as Number).toDouble(),
                        sellerId = data["sellerId"] as String,
                        sellerName = data["sellerName"] as String,
                        sellerPhone = data["sellerPhone"] as String,
                        category = data["category"] as String? ?: "",
                        images = (data["images"] as? List<String>) ?: emptyList(),
                        ratings = (data["ratings"] as? List<Map<String, Any>>)?.map { ratingData ->
                            Rating(
                                id = ratingData["id"] as String,
                                userId = ratingData["userId"] as String,
                                userName = ratingData["userName"] as String,
                                rating = (ratingData["rating"] as Number).toFloat(),
                                comment = ratingData["comment"] as String? ?: "",
                                createdAt = (ratingData["createdAt"] as Number).toLong()
                            )
                        } ?: emptyList(),
                        createdAt = (data["createdAt"] as Number).toLong()
                    )
                } else null
            }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getProductsBySeller(sellerId: String): Result<List<Product>> {
        return try {
            val snapshot = firestore.collection("products")
                .whereEqualTo("sellerId", sellerId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get().await()
            
            val products = snapshot.documents.mapNotNull { document ->
                val data = document.data
                if (data != null) {
                    Product(
                        id = data["id"] as String,
                        name = data["name"] as String,
                        description = data["description"] as String,
                        price = (data["price"] as Number).toDouble(),
                        sellerId = data["sellerId"] as String,
                        sellerName = data["sellerName"] as String,
                        sellerPhone = data["sellerPhone"] as String,
                        category = data["category"] as String? ?: "",
                        images = (data["images"] as? List<String>) ?: emptyList(),
                        ratings = (data["ratings"] as? List<Map<String, Any>>)?.map { ratingData ->
                            Rating(
                                id = ratingData["id"] as String,
                                userId = ratingData["userId"] as String,
                                userName = ratingData["userName"] as String,
                                rating = (ratingData["rating"] as Number).toFloat(),
                                comment = ratingData["comment"] as String? ?: "",
                                createdAt = (ratingData["createdAt"] as Number).toLong()
                            )
                        } ?: emptyList(),
                        createdAt = (data["createdAt"] as Number).toLong()
                    )
                } else null
            }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun addRating(productId: String, rating: Rating): Result<Unit> {
        return try {
            val ratingData = hashMapOf(
                "id" to rating.id,
                "userId" to rating.userId,
                "userName" to rating.userName,
                "rating" to rating.rating,
                "comment" to rating.comment,
                "createdAt" to rating.createdAt
            )
            
            firestore.collection("products").document(productId)
                .update("ratings", com.google.firebase.firestore.FieldValue.arrayUnion(ratingData))
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAverageRating(productId: String): Float {
        return try {
            val document = firestore.collection("products").document(productId).get().await()
            val data = document.data
            val ratings = (data?.get("ratings") as? List<Map<String, Any>>) ?: emptyList()
            
            if (ratings.isEmpty()) {
                0f
            } else {
                val totalRating = ratings.sumOf { (it["rating"] as Number).toFloat().toDouble() }
                (totalRating / ratings.size).toFloat()
            }
        } catch (e: Exception) {
            0f
        }
    }
} 