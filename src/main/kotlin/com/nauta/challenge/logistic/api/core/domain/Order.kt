package com.nauta.challenge.logistic.api.core.domain

import java.util.UUID

data class Order(
    val purchaseId: UUID? = null,
    val purchase: String,
    val invoices: List<Invoice>
)
