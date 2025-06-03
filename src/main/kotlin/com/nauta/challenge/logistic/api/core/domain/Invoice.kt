package com.nauta.challenge.logistic.api.core.domain

import java.util.UUID

data class Invoice(
    val invoiceId: UUID? = null,
    val invoice: String
)
