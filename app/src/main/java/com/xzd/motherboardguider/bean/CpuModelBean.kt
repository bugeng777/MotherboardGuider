package com.xzd.motherboardguider.bean

data class CpuModelBean(
    val id: String,
    val code: String?,
    val name: String,
    val powerConsumption: String?,
    val releaseYear: String?,
    val supportedMotherboards: List<String>,
    val recommendedMotherboards: List<String>
)

