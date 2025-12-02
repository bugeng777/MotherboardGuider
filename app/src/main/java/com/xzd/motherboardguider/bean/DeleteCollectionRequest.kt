package com.xzd.motherboardguider.bean

import com.google.gson.annotations.SerializedName

// 删除收藏请求数据类
data class DeleteCollectionRequest(
    @SerializedName("token") val token: String,
    @SerializedName("collection_id") val collection_id: Int
)

