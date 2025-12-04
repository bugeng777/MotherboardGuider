package com.xzd.motherboardguider.bean

import com.google.gson.annotations.SerializedName

// 忘记密码请求数据类
data class ForgetPwdRequest(
    @SerializedName("contact_ad") val contactAd: String,
    @SerializedName("reinput_pwd") val reinputPwd: String,
    @SerializedName("pwd") val pwd: String,
    @SerializedName("verfi_word") val verfiWord: String
)



