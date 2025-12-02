package com.xzd.motherboardguider.bean

import com.google.gson.annotations.SerializedName

// 请求数据类
data class CreateCollectionRequest(
    @SerializedName("token") val token: String,
    @SerializedName("collect_name") val collectName: String,
    @SerializedName("cpu_id") val cpuId: String,
    @SerializedName("gpu_id") val gpuId: String,
    @SerializedName("disk_count") val diskCount: String,
    @SerializedName("cpu_name") val cpuName: String = "",
    @SerializedName("gpu_name") val gpuName: String = "",
    @SerializedName("total_powerConsumption") val totalPowerConsumption: String = "",
    @SerializedName("supportedMotherboard") val supportedMotherboard: String = "",
    @SerializedName("suggestMotherboard") val suggestMotherboard: String = ""
)


