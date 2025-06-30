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
            android.util.Log.d("FirebaseService", "Adding product: ${product.name}")
            val productId = UUID.randomUUID().toString()
            val uploadedImageUrls = mutableListOf<String>()
            
            // Upload images to Firebase Storage (with improved error handling)
            if (imageUris.isNotEmpty()) {
                android.util.Log.d("FirebaseService", "Starting image upload for ${imageUris.size} images")
                
                for ((index, imageUri) in imageUris.withIndex()) {
                    try {
                        android.util.Log.d("FirebaseService", "Uploading image ${index + 1}/${imageUris.size}: $imageUri")
                        
                        // Create a unique filename for each image
                        val imageFileName = "image_${System.currentTimeMillis()}_$index.jpg"
                        val imageRef = storage.reference.child("products/$productId/$imageFileName")
                        
                        // Upload the image
                        val uploadTask = imageRef.putFile(imageUri).await()
                        android.util.Log.d("FirebaseService", "Image upload completed for: $imageFileName")
                        
                        // Get the download URL
                        val downloadUrl = uploadTask.storage.downloadUrl.await()
                        uploadedImageUrls.add(downloadUrl.toString())
                        android.util.Log.d("FirebaseService", "Image uploaded successfully: $downloadUrl")
                        
                    } catch (e: Exception) {
                        android.util.Log.e("FirebaseService", "Failed to upload image ${index + 1}: ${e.message}", e)
                        // Continue with other images rather than failing the entire product
                        // The product will be saved without this specific image
                    }
                }
                
                android.util.Log.d("FirebaseService", "Image upload process completed. Successfully uploaded: ${uploadedImageUrls.size}/${imageUris.size} images")
            } else {
                android.util.Log.d("FirebaseService", "No images to upload")
            }
            
            // Save product data to Firestore (regardless of image upload success)
            val productData = hashMapOf<String, Any?>(
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
                "createdAt" to System.currentTimeMillis(),
                "isDeleted" to false,
                "deletedAt" to null
            )
            
            android.util.Log.d("FirebaseService", "Saving product to Firestore: $productId")
            firestore.collection("products").document(productId).set(productData as Map<String, Any?>).await()
            android.util.Log.d("FirebaseService", "Product saved successfully: $productId with ${uploadedImageUrls.size} images")
            Result.success(productId)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseService", "Failed to add product: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getAllProducts(): Result<List<Product>> {
        return try {
            android.util.Log.d("FirebaseService", "Fetching all products from Firestore")
            val snapshot = firestore.collection("products")
                .whereEqualTo("isDeleted", false) // Only get non-deleted products
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get().await()
            
            android.util.Log.d("FirebaseService", "Found ${snapshot.documents.size} active products in Firestore")
            
            val products = snapshot.documents.mapNotNull { document ->
                val data = document.data
                if (data != null) {
                    android.util.Log.d("FirebaseService", "Processing product: ${data["name"]}")
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
                        createdAt = (data["createdAt"] as Number).toLong(),
                        isDeleted = data["isDeleted"] as? Boolean ?: false,
                        deletedAt = (data["deletedAt"] as? Number)?.toLong()
                    )
                } else {
                    android.util.Log.w("FirebaseService", "Document data is null")
                    null
                }
            }
            android.util.Log.d("FirebaseService", "Successfully processed ${products.size} products")
            Result.success(products)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseService", "Failed to get products: ${e.message}", e)
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
            val ratingData = hashMapOf<String, Any>(
                "id" to rating.id,
                "userId" to rating.userId,
                "userName" to rating.userName,
                "rating" to rating.rating,
                "comment" to rating.comment,
                "createdAt" to rating.createdAt
            )
            
            firestore.collection("products").document(productId)
                .update("ratings", com.google.firebase.firestore.FieldValue.arrayUnion(ratingData as Map<String, Any>))
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
    
    suspend fun deleteProduct(productId: String, sellerId: String): Result<Unit> {
        return try {
            android.util.Log.d("FirebaseService", "Soft deleting product: $productId")
            
            // First, get the product to check if user is the seller
            val productDoc = firestore.collection("products").document(productId).get().await()
            if (!productDoc.exists()) {
                return Result.failure(Exception("Product not found"))
            }
            
            val productData = productDoc.data
            if (productData == null) {
                return Result.failure(Exception("Product data is null"))
            }
            
            // Check if current user is the seller
            val productSellerId = productData["sellerId"] as String
            if (productSellerId != sellerId) {
                return Result.failure(Exception("You can only delete your own products"))
            }
            
            // Soft delete: Mark product as deleted instead of removing it
            val updateData = hashMapOf<String, Any>(
                "isDeleted" to true,
                "deletedAt" to System.currentTimeMillis()
            )
            
            firestore.collection("products").document(productId).update(updateData as Map<String, Any>).await()
            android.util.Log.d("FirebaseService", "Product soft deleted successfully: $productId")
            
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseService", "Failed to soft delete product: ${e.message}", e)
            Result.failure(e)
        }
    }
} 