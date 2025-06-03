package com.nauta.challenge.logistic.api.core.domain

import java.util.UUID

data class Logistic(
    val id: UUID? = null,
    val clientId: UUID,
    val booking: String,
    val containers: List<Container> = emptyList(),
    val orders: List<Order> = emptyList()
)
