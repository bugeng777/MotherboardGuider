package com.xzd.motherboardguider.bean

import com.google.gson.annotations.SerializedName

// 注册请求数据类
data class RegisterRequest(
    @SerializedName("nick_name") val nickName: String,
    @SerializedName("contact_ad") val contactAd: String,
    @SerializedName("pwd") val pwd: String,
    @SerializedName("app_name") val appName: String
)

