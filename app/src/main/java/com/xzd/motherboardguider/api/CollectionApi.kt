package com.xzd.motherboardguider.api

import com.xzd.motherboardguider.bean.ApiResponse
import com.xzd.motherboardguider.bean.CollectionListResponse
import com.xzd.motherboardguider.bean.CreateCollectionRequest
import com.xzd.motherboardguider.bean.DeleteCollectionRequest
import com.xzd.motherboardguider.bean.LoadCollectionListRequest
import com.xzd.motherboardguider.bean.ForgetPwdRequest
import com.xzd.motherboardguider.bean.LoginRequest
import com.xzd.motherboardguider.bean.LoginResponse
import com.xzd.motherboardguider.bean.RegisterRequest
import com.xzd.motherboardguider.bean.SendVerificationCodeRequest
import retrofit2.http.Body
import retrofit2.http.POST

// API 接口
interface CollectionApi {
    @POST("hardware/create_collection_api/")
    suspend fun createCollection(@Body request: CreateCollectionRequest): ApiResponse
    
    @POST("hardware/get_collection_list_api/")
    suspend fun loadCollectionList(@Body request: LoadCollectionListRequest): CollectionListResponse
    
    @POST("hardware/delete_collection_api/")
    suspend fun deleteCollection(@Body request: DeleteCollectionRequest): ApiResponse
    
    @POST("accounts/login_check/")
    suspend fun login(@Body request: LoginRequest): LoginResponse
    
    @POST("accounts/register_api/")
    suspend fun register(@Body request: RegisterRequest): LoginResponse
    
    @POST("accounts/change_pwd_check/")
    suspend fun changePassword(@Body request: ForgetPwdRequest): LoginResponse
    
    @POST("accounts/pwd_miss_email_send/")
    suspend fun sendVerificationCode(@Body request: SendVerificationCodeRequest): LoginResponse
}

