package com.example.aimsandroid.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Review(
    @PrimaryKey
    val id: Int,
    val desc: String,
    val lat: Double,
    val long: Double,
    val review: String
)