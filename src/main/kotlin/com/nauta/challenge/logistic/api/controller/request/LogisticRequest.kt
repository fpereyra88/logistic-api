package com.nauta.challenge.logistic.api.controller.request

import com.nauta.challenge.logistic.api.core.domain.Container
import com.nauta.challenge.logistic.api.core.domain.Invoice
import com.nauta.challenge.logistic.api.core.domain.Logistic
import com.nauta.challenge.logistic.api.core.domain.Order
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import java.util.*

data class LogisticRequest(
    @field:NotBlank(message = "Field 'booking' must not be null or empty")
    val booking: String,
    @field:Valid
    val containers: List<ContainerRequest>?,
    @field:Valid
    val orders: List<OrderRequest>?
) {
    fun toDomain(clientId: UUID) = Logistic(
        clientId = clientId,
        booking = booking,
        containers = containers?.let { container -> container.map { it.toDomain() }} ?: emptyList(),
        orders = orders?.let { order -> order.map { it.toDomain() } } ?: emptyList()
    )
}

data class ContainerRequest(
    @field:NotBlank(message = "Field 'container' must not be null or empty")
    val container: String
) {
    fun toDomain() = Container(
        container = container
    )
}

data class OrderRequest(
    @field:NotBlank(message = "Field 'purchase' must not be null or empty")
    val purchase: String,
    @field:Valid
    val invoices: List<InvoiceRequest>
) {
    fun toDomain() = Order(
        purchase = purchase,
        invoices = invoices.map { it.toDomain() }
    )
}

data class InvoiceRequest(
    @field:NotBlank(message = "Field 'invoice' must not be null or empty")
    val invoice: String
) {
    fun toDomain() = Invoice(
        invoice = invoice
    )
}
