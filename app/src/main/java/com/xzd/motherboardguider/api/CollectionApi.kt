package com.xzd.motherboardguider.api

import com.xzd.motherboardguider.bean.ApiResponse
import com.xzd.motherboardguider.bean.CollectionListResponse
import com.xzd.motherboardguider.bean.CreateCollectionRequest
import com.xzd.motherboardguider.bean.LoadCollectionListRequest
import retrofit2.http.Body
import retrofit2.http.POST

// API 接口
interface CollectionApi {
    @POST("hardware/create_collection_api/")
    suspend fun createCollection(@Body request: CreateCollectionRequest): ApiResponse
    
    @POST("hardware/get_collection_list_api/")
    suspend fun loadCollectionList(@Body request: LoadCollectionListRequest): CollectionListResponse
}

