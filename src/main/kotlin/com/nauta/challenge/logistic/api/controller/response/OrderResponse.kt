package com.nauta.challenge.logistic.api.controller.response

import com.nauta.challenge.logistic.api.core.domain.Invoice
import com.nauta.challenge.logistic.api.core.domain.Order
import java.util.UUID

data class OrderResponse(
    val purchaseId: UUID? = null,
    val purchase: String,
    val invoices: List<InvoiceResponse>
) {
    companion object {
        fun fromDomain(order: Order): OrderResponse {
            return OrderResponse(
                purchaseId = order.purchaseId,
                purchase = order.purchase,
                invoices = order.invoices.map { InvoiceResponse.fromDomain(it) }
            )
        }
    }
}

data class InvoiceResponse(
    val invoiceId: UUID? = null,
    val invoice: String
) {
    companion object {
        fun fromDomain(invoice: Invoice): InvoiceResponse {
            return InvoiceResponse(
                invoiceId = invoice.invoiceId,
                invoice = invoice.invoice
            )
        }
    }
}
