package com.nauta.challenge.logistic.api.configuration.database

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import java.util.*

const val CONTAINER_TABLE = "container"
const val CONTAINER_ID = "container_id"
const val CONTAINER_CODE = "container"

object ContainerTable : IdTable<String>(CONTAINER_TABLE) {
    override val id: Column<EntityID<String>> = varchar(CONTAINER_ID, 36)
        .entityId()
        .clientDefault { EntityID(UUID.randomUUID().toString(), ContainerTable) }

    val code = varchar(CONTAINER_CODE, 50)

    val booking = reference(BOOKING_ID, BookingTable)
}
