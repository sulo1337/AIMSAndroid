package com.example.aimsandroid.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ReviewDao {
    @Query("select * from review_table order by id desc")
    fun getReviews(): LiveData<List<Review>>

    @Insert
    fun insertReview(review: Review)
}

@Database(entities = [Review::class], version = 1)
abstract class ReviewDatabase: RoomDatabase() {
    abstract val reviewDao: ReviewDao
}

private lateinit var INSTANCE: ReviewDatabase

fun getDatabase(context: Context): ReviewDatabase {
    synchronized(ReviewDatabase::class.java) {
        if(!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
            ReviewDatabase::class.java, "reviews").build()
        }
    }
    return INSTANCE
}