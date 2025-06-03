package com.nauta.challenge.logistic.api.configuration

import com.nauta.challenge.logistic.api.configuration.database.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ExposedConfig(
    @Value("\${spring.datasource.url}") private val url: String,
    @Value("\${spring.datasource.driver-class-name}") private val driver: String,
    @Value("\${spring.datasource.username:}") private val user: String,
    @Value("\${spring.datasource.password:}") private val password: String
) {

    @Bean
    fun logisticDatabase(): Database {
        val db = Database.connect(
            url = url,
            driver = driver,
            user = user,
            password = password
        )

        transaction(db) {
            SchemaUtils.create(
                ClientTable,
                BookingTable,
                ContainerTable,
                OrderTable,
                InvoiceTable,
                BookingContainerTable,
                ClientContainerTable)
        }

        return db
    }
}