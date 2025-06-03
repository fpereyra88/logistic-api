package com.nauta.challenge.logistic.api.configuration.database

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import java.util.*

const val BOOKING_TABLE = "booking"
const val BOOKING_ID = "booking_id"
const val BOOKING_CODE = "booking"

object BookingTable : IdTable<String>(BOOKING_TABLE) {
    override val id: Column<EntityID<String>> = varchar(BOOKING_ID, 36)
        .entityId()
        .clientDefault { EntityID(UUID.randomUUID().toString(), BookingTable) }
    val code = varchar(BOOKING_CODE, 50).uniqueIndex()
    
    val client = reference(CLIENT_ID, ClientTable)
}
