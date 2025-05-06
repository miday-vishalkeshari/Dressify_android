package com.example.dressify.models

import java.io.Serializable

data class UserRole(
    var name: String,
    val id: String,
    val emoji: String
) : Serializable
