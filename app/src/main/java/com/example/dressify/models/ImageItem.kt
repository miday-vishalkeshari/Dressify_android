package com.example.dressify.models

data class ImageItem(
    val imageUrl: String,
    val styleType: String,//typeof dress like jeans...
    val styleColour: String,//documentid of teh product inside dresstype collection
    val productDocId: String
)