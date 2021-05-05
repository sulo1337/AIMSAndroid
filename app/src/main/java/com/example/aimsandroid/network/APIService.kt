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
    @GET("GetDetailedTripListByDriver/{Id}")
    @Throws(Exception::class)
    fun getTripsAsync(
        @Path("Id") driverId: String,
        @Query("apiKey") apiKey: String
    ): Deferred<ResponseContainer>

    @GET("TripStatusPut/{Id}/{tripId}/{statusCode}/{statusMessage}/true/{statusDate}")
    fun putTripEventStatusAsync(
        @Path("Id") driverId: String,
        @Path("tripId") tripId: String,
        @Path("statusCode") statusCode: String,
        @Path("statusMessage") statusMessage: String,
        @Path("statusDate") statusDate: String,
        @Query("apiKey") apiKey: String
    ): Deferred<PutTripStatusResponseContainer>

    @GET("TripProductPickupPut/{Id}/{tripId}/{sourceId}/{productId}/{bolNum}/{startTime}/{endTime}/{grossQty}/{netQty}")
    fun putTripProductPickupAsync(
        @Path("Id") driverId: String,
        @Path("tripId") tripId: String,
        @Path("sourceId") sourceId: String,
        @Path("productId") productId: String,
        @Path("bolNum") bolNum: String,
        @Path("startTime") startTime: String,
        @Path("endTime") endTime: String,
        @Path("grossQty") grossQty: String,
        @Path("netQty") netQty: String,
        @Query("apiKey") apiKey: String
    ): Deferred<PutTripProductPickupContainer>

    @GET("/rest/1/apiexpress/api/DispatcherMobileApp/")
    fun getDriverInfoAsync(
        @Query("apiKey") apiKey: String,
        @Query("Code") code: String,
        @Query("Active") active: String
    ): Deferred<GetDriverInfoResponseContainer>

    @GET("TripProductDeliveryInsert/{Id}/{tripId}/{siteId}/{productId}/{startTime}/{grossQty}/{netQty}/{remainingQty}")
    fun putTripProductDeliveryAsync(
        @Path("Id") driverId: String,
        @Path("tripId") tripId: String,
        @Path("siteId") siteId: String,
        @Path("productId") productId: String,
        @Path("startTime") startTime: String,
        @Path("grossQty") grossQty: String,
        @Path("netQty") netQty: String,
        @Path("remainingQty") remainingQty: String,
        @Query("apiKey") apiKey: String
    ): Deferred<PutTripStatusResponseContainer>
}

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()


object Network{
    private var retrofit: Retrofit? = null
    init {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC
        val httpClient = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()
        retrofit= Retrofit.Builder()
            .baseUrl("https://api.appery.io/rest/1/apiexpress/api/DispatcherMobileApp/")
//            .baseUrl("http://a4b36076744b.ngrok.io")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(httpClient)
            .build()
    }

    val dispatcher: Dispatcher = retrofit!!.create(Dispatcher::class.java)
}
