package com.xzd.motherboardguider.bean

import com.google.gson.annotations.SerializedName

// 发送验证码请求数据类
data class SendVerificationCodeRequest(
    @SerializedName("contact_ad") val contactAd: String,
    @SerializedName("app_name") val appName: String
)

