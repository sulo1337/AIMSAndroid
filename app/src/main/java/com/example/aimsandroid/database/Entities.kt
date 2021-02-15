package com.example.aimsandroid.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "review_table")
data class Review(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    @ColumnInfo(name="desc")
    var desc: String = "",
    @ColumnInfo(name="lat")
    var lat: Double = 0.0,
    @ColumnInfo(name="long")
    var long: Double = 0.0,
    @ColumnInfo(name="review")
    var review: String = ""
)