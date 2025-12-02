package com.xzd.motherboardguider.bean

import com.google.gson.annotations.SerializedName

// 响应数据类
data class ApiResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("data") val data: String
)



