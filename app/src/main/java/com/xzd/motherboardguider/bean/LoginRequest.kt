package com.xzd.motherboardguider.bean

import com.google.gson.annotations.SerializedName

// 登录请求数据类
data class LoginRequest(
    @SerializedName("contact_ad") val contactAd: String,
    @SerializedName("pwd") val pwd: String
)

