package com.trevor.final_resort

import java.util.UUID

data class Product(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val price: Double,
    val sellerId: String,
    val sellerName: String,
    val sellerPhone: String,
    val images: List<String> = emptyList(),
    val ratings: List<Rating> = emptyList(),
    val category: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

data class Rating(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val userName: String,
    val rating: Float, // 1.0 to 5.0
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
) 