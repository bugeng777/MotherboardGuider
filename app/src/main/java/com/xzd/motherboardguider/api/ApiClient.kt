package com.xzd.motherboardguider.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Retrofit 服务
object ApiClient {
//    private const val BASE_URL = "http://192.168.50.110:8000/"
    private const val BASE_URL = "http://43.153.145.167:7227/"

    val collectionApi: CollectionApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CollectionApi::class.java)
    }
}

