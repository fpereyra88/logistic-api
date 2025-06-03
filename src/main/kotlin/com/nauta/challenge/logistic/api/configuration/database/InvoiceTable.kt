package com.nauta.challenge.logistic.api.configuration.database

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import java.util.UUID

const val INVOICE_TABLE = "invoice"
const val INVOICE_ID = "invoice_id"
const val INVOICE_CODE = "invoice"

object InvoiceTable : IdTable<String>(INVOICE_TABLE) {
    override val id: Column<EntityID<String>> = varchar(INVOICE_ID, 36)
        .entityId()
        .clientDefault { EntityID(UUID.randomUUID().toString(), InvoiceTable) }

    val code = varchar(INVOICE_CODE, 50)
        .uniqueIndex()

    val order = reference(ORDER_ID, OrderTable)

    val booking = reference(BOOKING_ID, BookingTable)

    init {
        index(true, order, code)
    }
}
