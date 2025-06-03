package com.nauta.challenge.logistic.api.configuration.database

import org.jetbrains.exposed.sql.Table

object ClientContainerTable : Table("client_container") {
    val client = reference(CLIENT_ID, ClientTable)
    val container = reference(CONTAINER_ID, ContainerTable)
    override val primaryKey = PrimaryKey(client, container, name = "PK_ClientContainerTable")
}
