package com.nauta.challenge.logistic.api.configuration.database

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import java.util.*

const val CLIENT_TABLE = "client"
const val CLIENT_ID = "client_id"

object ClientTable : IdTable<String>(CLIENT_TABLE) {
    override val id: Column<EntityID<String>> = varchar(CLIENT_ID, 36)
        .entityId()
        .clientDefault { EntityID(UUID.randomUUID().toString(), ClientTable) }
}