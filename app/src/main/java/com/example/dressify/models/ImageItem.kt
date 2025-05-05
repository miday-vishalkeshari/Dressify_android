package com.example.dressify.models

data class ImageItem(
    val imageUrl: String,
    val collectionName: String,//typeof dress like jeans...
    val documentId: String//documentid of teh product inside dresstype collection
)