package com.trevor.final_resort

object ProductManager {
    private val products = mutableListOf<Product>()
    
    fun addProduct(product: Product): Boolean {
        products.add(product)
        return true
    }
    
    fun getAllProducts(): List<Product> {
        return products.sortedByDescending { it.createdAt }
    }
    
    fun getProductsBySeller(sellerId: String): List<Product> {
        return products.filter { it.sellerId == sellerId }
    }
    
    fun getProductById(productId: String): Product? {
        return products.find { it.id == productId }
    }
    
    fun addRating(productId: String, rating: Rating): Boolean {
        val product = getProductById(productId)
        return if (product != null) {
            val updatedProduct = product.copy(
                ratings = product.ratings + rating
            )
            val index = products.indexOfFirst { it.id == productId }
            if (index != -1) {
                products[index] = updatedProduct
                true
            } else {
                false
            }
        } else {
            false
        }
    }
    
    fun getAverageRating(productId: String): Float {
        val product = getProductById(productId)
        return if (product != null && product.ratings.isNotEmpty()) {
            product.ratings.map { it.rating }.average().toFloat()
        } else {
            0f
        }
    }
    
    fun deleteProduct(productId: String, sellerId: String): Boolean {
        val product = getProductById(productId)
        return if (product != null && product.sellerId == sellerId) {
            products.removeAll { it.id == productId }
            true
        } else {
            false
        }
    }
} 