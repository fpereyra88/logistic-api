package com.nauta.challenge.logistic.api.controller.common

data class ApiResponse<T>(
    val data: T? = null,
    val error: String? = null,
    val message: String? = null
)
