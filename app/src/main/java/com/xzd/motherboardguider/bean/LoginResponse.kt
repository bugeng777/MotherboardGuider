package com.xzd.motherboardguider.bean

import com.google.gson.annotations.SerializedName

// 登录响应数据类
data class LoginResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("data") val data: String,
    @SerializedName("token") val token: String? = null,
    @SerializedName("contact_ad") val contactAd: String? = null
)

