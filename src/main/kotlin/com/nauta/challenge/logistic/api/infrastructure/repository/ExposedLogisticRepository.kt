package com.nauta.challenge.logistic.api.infrastructure.repository

import com.nauta.challenge.logistic.api.configuration.database.*
import com.nauta.challenge.logistic.api.core.domain.Container
import com.nauta.challenge.logistic.api.core.domain.Invoice
import com.nauta.challenge.logistic.api.core.domain.Logistic
import com.nauta.challenge.logistic.api.core.domain.Order
import com.nauta.challenge.logistic.api.core.repository.LogisticRepository
import com.nauta.challenge.logistic.api.core.exception.LogisticConflictException
import mu.KotlinLogging
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository
import java.util.*

typealias BookingId = String

@Repository
class ExposedLogisticRepository(
    private val logisticDatabase: Database
) : LogisticRepository {

    override fun save(logistic: Logistic): String {
        logger.info { "Saving logistic for booking: ${logistic.booking} in database" }

        return transaction(logisticDatabase) {

            insertClientIfNotExists(logistic.clientId)

            val bookingId = saveBooking(logistic)

            saveContainers(bookingId, logistic)

            saveOrdersAndInvoices(bookingId, logistic)

            logger.info { "Logistic for booking: ${logistic.booking} saved in database" }

            bookingId
        }
    }

    override fun findOrdersByClientId(clientId: UUID): List<Order> {
        logger.info { "Searching orders for client ID: $clientId in database" }
        return transaction(logisticDatabase) {
            OrderTable
                .select(OrderTable.id, OrderTable.purchase)
                .where { OrderTable.client eq clientId.toString() }
                .map { orderRow ->
                    val orderId = orderRow[OrderTable.id].value
                    val invoices = InvoiceTable
                        .select(InvoiceTable.id, InvoiceTable.code)
                        .where { InvoiceTable.order eq orderId }
                        .map { it.toInvoiceDomain() }
                    orderRow.toOrderDomain(orderId, invoices)
                }
        }
    }

    override fun findContainersByClientId(clientId: UUID): List<Container> {
        logger.info { "Searching containers for client ID: $clientId in database" }
        return transaction(logisticDatabase) {
            ClientContainerTable
                .join(ContainerTable, JoinType.INNER, ClientContainerTable.container, ContainerTable.id)
                .select( ContainerTable.id, ClientContainerTable.client, ContainerTable.code )
                .where { ClientContainerTable.client eq clientId.toString() }
                .map { row ->
                    Container(
                        containerId = UUID.fromString(row[ContainerTable.id].value),
                        container = row[ContainerTable.code]
                    )
                }
        }
    }

    override fun findContainersByPurchaseId(purchaseId: UUID): List<Container> {
        logger.info { "Searching containers by purchase ID: $purchaseId in database" }
        return transaction(logisticDatabase) {
            ContainerTable
                .join(
                    OrderTable,
                    onColumn = ContainerTable.booking,
                    otherColumn = OrderTable.booking,
                    joinType = JoinType.INNER
                )
                .select(ContainerTable.id, ContainerTable.code)
                .where { OrderTable.id eq purchaseId.toString() }
                .map { row ->
                    Container(
                        containerId = UUID.fromString(row[ContainerTable.id].value),
                        container = row[ContainerTable.code]
                    )
                }
        }
    }

    override fun findOrdersByContainerId(containerId: UUID): List<Order> {
        logger.info { "Searching orders by container ID: $containerId in database" }
        return transaction(logisticDatabase) {
            OrderTable
                .join(
                    ContainerTable,
                    onColumn = OrderTable.booking,
                    otherColumn = ContainerTable.booking,
                    joinType = JoinType.INNER
                )
                .select(OrderTable.id, OrderTable.purchase)
                .where { ContainerTable.id eq containerId.toString() }
                .map { orderRow ->
                    val orderId = orderRow[OrderTable.id].value
                    val invoices = InvoiceTable
                        .select(InvoiceTable.id, InvoiceTable.code)
                        .where { InvoiceTable.order eq orderId }
                        .map { it.toInvoiceDomain() }
                    orderRow.toOrderDomain(orderId, invoices)
                }
        }
    }

    private fun saveBooking(logistic: Logistic): BookingId {
        val bookingRow = BookingTable
            .select(BookingTable.id, BookingTable.client)
            .where { BookingTable.code eq logistic.booking }
            .limit(1)
            .firstOrNull()

        return if (bookingRow != null) {
            val existingClientId = bookingRow[BookingTable.client].value
            if (existingClientId != logistic.clientId.toString()) {
                throw LogisticConflictException.BookingClientConflict(logistic.booking)
            }

            bookingRow[BookingTable.id].value
        } else {
            val newId = BookingTable
                .insertAndGetId {
                    it[code] = logistic.booking
                    it[client] = logistic.clientId.toString()
                }.value

            logger.info { "Inserted new booking '${logistic.booking}' with ID: '$newId'" }
            newId
        }
    }

    private fun saveContainers(bookingId: BookingId, logistic: Logistic) {
        require(logistic.containers.map { it.container }.toSet().size == logistic.containers.size) {
            "Duplicated containers ${logistic.containers.joinToString { it.container }} for booking $bookingId"
        }

        val containerCodes = logistic.containers.map { it.container }

        val existingContainers = ContainerTable
            .select(ContainerTable.code, ContainerTable.id)
            .where { ContainerTable.code inList containerCodes }
            .associate { it[ContainerTable.code] to it[ContainerTable.id].value }

        logistic.containers.forEach { container ->
            val containerId = existingContainers[container.container] ?: run {

                val newId = ContainerTable.insertAndGetId {
                    it[this.code] = container.container
                    it[this.booking] = bookingId
                }.value

                logger.info { "Inserted container '${container.container}' with ID: '$newId'" }
                newId
            }

            val existsRelation = !BookingContainerTable
                .select(BookingContainerTable.booking, BookingContainerTable.container)
                .where { BookingContainerTable.booking eq bookingId and (BookingContainerTable.container eq containerId) }
                .limit(1)
                .empty()

            if (!existsRelation) {
                BookingContainerTable.insert {
                    it[this.booking] = EntityID(bookingId, BookingTable)
                    it[this.container] = EntityID(containerId, ContainerTable)
                }
                logger.info { "Added relation from container '${container.container}' (ID: $containerId) to booking '$bookingId'" }
            } else {
                logger.debug { "Container '${container.container}' (ID: $containerId) already linked to booking '$bookingId'" }
            }


            insertClientContainerIfNotExists(logistic.clientId.toString(), containerId)
        }
    }

    private fun insertClientIfNotExists(clientId: UUID) {
        ClientTable.insertIgnore {
            it[id] = clientId.toString()
        }
        logger.debug { "Ensured client with ID: '$clientId' exists" }
    }

    private fun insertClientContainerIfNotExists(clientId: String, containerId: String) {
        ClientContainerTable.insertIgnore {
            it[client] = EntityID(clientId, ClientTable)
            it[container] = EntityID(containerId, ContainerTable)
        }
        logger.debug { "Ensured relation client '$clientId' <-> container '$containerId' exists" }
    }

    private fun saveOrdersAndInvoices(bookingId: BookingId, logistic: Logistic) {
        require(logistic.orders.map { it.purchase }.toSet().size == logistic.orders.size) {
            "Duplicated orders ${logistic.orders.joinToString { it.purchase }} for booking '$bookingId'"
        }

        val purchases = logistic.orders.map { it.purchase }

        val existingOrders = OrderTable
            .select(OrderTable.purchase, OrderTable.id, OrderTable.booking)
            .where { OrderTable.purchase inList purchases }
            .associateBy({ it[OrderTable.purchase] }, { Triple(it[OrderTable.id].value, it[OrderTable.booking].value, it) })

        logistic.orders.forEach { order ->
            val existingOrder = existingOrders[order.purchase]

            val orderId = when {
                existingOrder == null -> {
                    val newId = OrderTable.insertAndGetId {
                        it[purchase] = order.purchase
                        it[booking] = EntityID(bookingId, BookingTable)
                        it[client] = logistic.clientId.toString()
                    }.value
                    logger.info { "Inserted order '${order.purchase}' for booking '$bookingId' with ID: $newId" }
                    newId
                }

                existingOrder.second == bookingId -> {
                    logger.debug { "Order '${order.purchase}' already exists for current booking '$bookingId'" }
                    existingOrder.first
                }

                else -> {
                    logger.warn { "Order '${order.purchase}' already assigned to booking '${existingOrder.second}'" }
                    return@forEach
                }
            }

            saveInvoices(order, orderId, bookingId)
        }
    }

    private fun saveInvoices(order: Order, orderId: String, bookingId: BookingId) {
        require(order.invoices.map { it.invoice }.toSet().size == order.invoices.size) {
            "Duplicated invoices ${order.invoices.joinToString { it.invoice }} for order '$orderId'"
        }

        val invoiceCodes = order.invoices.map { it.invoice }

        val existingInvoices = InvoiceTable
            .select(InvoiceTable.code, InvoiceTable.order)
            .where { InvoiceTable.code inList invoiceCodes }
            .associateBy({ it[InvoiceTable.code] }, { it[InvoiceTable.order].value })

        order.invoices.forEach { invoice ->
            when (val existingOrderId = existingInvoices[invoice.invoice]) {
                null -> {
                    InvoiceTable.insert {
                        it[this.code] = invoice.invoice
                        it[this.order] = EntityID(orderId, OrderTable)
                        it[this.booking] = EntityID(bookingId, BookingTable)
                    }
                    logger.info { "Inserted invoice '${invoice.invoice}' for order '${order.purchase}'" }
                }

                orderId -> {
                    logger.debug { "Invoice '${invoice.invoice}' already exists for order '${order.purchase}'" }
                }

                else -> {
                    logger.warn {
                        "Invoice '${invoice.invoice}' already assigned to different order '$existingOrderId'"
                    }
                }
            }
        }
    }

    private fun ResultRow.toOrderDomain(orderId: String, invoices: List<Invoice>): Order {
        return Order(
            purchaseId = UUID.fromString(orderId),
            purchase = this[OrderTable.purchase],
            invoices = invoices
        )
    }

    private fun ResultRow.toInvoiceDomain(): Invoice {
        return Invoice(
            invoiceId = UUID.fromString(this[InvoiceTable.id].value),
            invoice = this[InvoiceTable.code]
        )
    }

    private companion object {
        val logger = KotlinLogging.logger {}
    }
}
