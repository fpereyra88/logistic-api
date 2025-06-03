package com.nauta.challenge.logistic.api.configuration.database

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import java.util.*

const val ORDER_TABLE = "order"
const val ORDER_ID = "order_id"
const val PURCHASE = "purchase"

object OrderTable : IdTable<String>(ORDER_TABLE) {
    override val id: Column<EntityID<String>> = varchar(ORDER_ID, 36)
        .entityId()
        .clientDefault { EntityID(UUID.randomUUID().toString(), OrderTable) }

    val purchase = varchar(PURCHASE, 50).uniqueIndex()

    val booking = reference(BOOKING_ID, BookingTable)

    val client = reference(CLIENT_ID, BookingTable)

    init {
        index(true, booking, purchase)

        index(false, client)
    }
}