package com.example.aimsandroid.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.lang.Exception

interface Dispatcher {
    @GET("{Id}")
    @Throws(Exception::class)
    fun getTripsAsync(
        @Path("Id") driverId: String,
        @Query("apiKey") apiKey: String
    ): Deferred<TripDataContainer>
}

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()


object Network{
    private var retrofit: Retrofit? = null
    init {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        val httpClient = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()
        retrofit= Retrofit.Builder()
            .baseUrl("https://api.appery.io/rest/1/apiexpress/api/DispatcherMobileApp/GetTripListDetailByDriver/")
//            .baseUrl("http://edeb808c032b.ngrok.io")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(httpClient)
            .build()
    }

    val dispatcher: Dispatcher = retrofit!!.create(Dispatcher::class.java)
}
