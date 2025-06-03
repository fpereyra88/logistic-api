package com.nauta.challenge.logistic.api.configuration.database

import org.jetbrains.exposed.sql.Table

object BookingContainerTable : Table("booking_container") {
    val booking = reference("booking_id", BookingTable)
    val container = reference("container_id", ContainerTable)

    override val primaryKey = PrimaryKey(booking, container, name = "PK_BookingContainer")
}