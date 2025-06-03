package com.nauta.challenge.logistic.api.core.domain

import java.util.UUID

data class Container(
    val containerId: UUID? = null,
    val container: String
)
