package com.xzd.motherboardguider.bean

import com.google.gson.annotations.SerializedName

data class CollectionItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("user_id")
    val user_id: String,

    @SerializedName("collect_name")
    val collect_name: String,

    @SerializedName("cpu_id")
    val cpu_id: String,

    @SerializedName("gpu_id")
    val gpu_id: String,

    @SerializedName("disk_count")
    val disk_count: String,

    @SerializedName("cpu_name")
    val cpu_name: String,

    @SerializedName("gpu_name")
    val gpu_name: String,

    @SerializedName("total_powerConsumption")
    val total_powerConsumption: String,

    @SerializedName("supportedMotherboard")
    val supportedMotherboard: String,

    @SerializedName("suggestMotherboard")
    val suggestMotherboard: String,

    @SerializedName("create_time")
    val create_time: String,

    @SerializedName("update_time")
    val update_time: String
)
