package com.xzd.motherboardguider.bean

import com.google.gson.annotations.SerializedName

// 收藏列表响应数据类
data class CollectionListResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("data") val data: List<CollectionItem>
)

