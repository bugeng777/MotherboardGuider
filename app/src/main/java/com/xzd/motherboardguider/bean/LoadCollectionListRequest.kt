package com.xzd.motherboardguider.bean

import com.google.gson.annotations.SerializedName

// 加载收藏列表请求数据类
data class LoadCollectionListRequest(
    @SerializedName("token") val token: String
)

