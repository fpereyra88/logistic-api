package com.nauta.challenge.logistic.api.infrastructure.repository

import com.nauta.challenge.logistic.api.core.domain.Container
import com.nauta.challenge.logistic.api.core.domain.Invoice
import com.nauta.challenge.logistic.api.core.domain.Logistic
import com.nauta.challenge.logistic.api.core.domain.Order
import com.nauta.challenge.logistic.api.core.exception.LogisticConflictException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.*
import com.nauta.challenge.logistic.api.configuration.database.BookingTable
import com.nauta.challenge.logistic.api.configuration.database.ContainerTable
import com.nauta.challenge.logistic.api.configuration.database.OrderTable
import com.nauta.challenge.logistic.api.configuration.database.InvoiceTable
import com.nauta.challenge.logistic.api.configuration.database.BookingContainerTable
import io.kotest.assertions.throwables.shouldNotThrowAny
import org.jetbrains.exposed.sql.selectAll

@SpringBootTest
@ActiveProfiles("test")
class ExposedLogisticRepositoryTest @Autowired constructor(
    val repository: ExposedLogisticRepository,
    @Autowired val logisticDatabase: Database
) {
    private val clientId = UUID.randomUUID()
    private val booking = "booking-test"
    private val containerCode = "container-test"
    private val purchase = "purchase-test"
    private val invoiceCode = "invoice-test"

    @BeforeEach
    fun setup() {
        transaction(logisticDatabase) {
            SchemaUtils.drop(
                BookingTable, ContainerTable, OrderTable, InvoiceTable, BookingContainerTable)
            SchemaUtils.createMissingTablesAndColumns(
                BookingTable, ContainerTable, OrderTable, InvoiceTable, BookingContainerTable
            )
        }
    }

    @AfterEach
    fun cleanup() {
        transaction(logisticDatabase) {
            SchemaUtils.drop(
                BookingTable, ContainerTable, OrderTable, InvoiceTable, BookingContainerTable)
        }
    }

    @Test
    fun `save should persist logistic and return bookingId`() {
        val logistic = givenALogistic()

        shouldNotThrowAny {
            repository.save(logistic)
        }
    }

    @Test
    fun `save should throw on duplicated containers`() {
        val logistic = givenALogistic().copy(
            containers = listOf(
                Container(containerId = UUID.randomUUID(), container = containerCode),
                Container(containerId = UUID.randomUUID(), container = containerCode)
            )
        )
        shouldThrow<IllegalArgumentException> { repository.save(logistic) }
    }

    @Test
    fun `save should throw on duplicated orders`() {
        val logistic = givenALogistic().copy(
            orders = listOf(
                Order(purchaseId = UUID.randomUUID(), purchase = purchase, invoices = emptyList()),
                Order(purchaseId = UUID.randomUUID(), purchase = purchase, invoices = emptyList())
            )
        )
        shouldThrow<IllegalArgumentException> { repository.save(logistic) }
    }

    @Test
    fun `save should throw on duplicated invoices`() {
        val order = Order(
            purchaseId = UUID.randomUUID(),
            purchase = purchase,
            invoices = listOf(
                Invoice(invoiceId = UUID.randomUUID(), invoice = invoiceCode),
                Invoice(invoiceId = UUID.randomUUID(), invoice = invoiceCode)
            )
        )
        val logistic = givenALogistic().copy(orders = listOf(order))
        shouldThrow<IllegalArgumentException> { repository.save(logistic) }
    }

    @Test
    fun `save should throw BookingClientConflict if booking exists with different client`() {
        val logistic1 = givenALogistic()
        repository.save(logistic1)
        val logistic2 = logistic1.copy(clientId = UUID.randomUUID())
        shouldThrow<LogisticConflictException.BookingClientConflict> { repository.save(logistic2) }
    }

    @Test
    fun `findOrdersByClientId should return orders for client`() {
        repository.save(givenALogistic())
        val orders = repository.findOrdersByClientId(clientId)
        orders shouldHaveSize 1
        orders[0].purchase shouldBe purchase
    }

    @Test
    fun `findOrdersByClientId should return empty if none`() {
        repository.findOrdersByClientId(UUID.randomUUID()).shouldBeEmpty()
    }

    @Test
    fun `findContainersByClientId should return containers for client`() {
        repository.save(givenALogistic())
        val containers = repository.findContainersByClientId(clientId)
        containers shouldHaveSize 1
        containers[0].container shouldBe containerCode
    }

    @Test
    fun `findContainersByClientId should return empty if none`() {
        repository.findContainersByClientId(UUID.randomUUID()).shouldBeEmpty()
    }

    @Test
    fun `findContainersByPurchaseId should return containers for purchase`() {
        repository.save(givenALogistic())
        val containers = repository.findContainersByPurchaseId(UUID.randomUUID())
        containers.shouldBeEmpty()
    }

    @Test
    fun `findOrdersByContainerId should return orders for container`() {
        repository.save(givenALogistic())
        val orders = repository.findOrdersByContainerId(UUID.randomUUID())
        orders.shouldBeEmpty()
    }

    @Test
    fun `save should be idempotent for the same logistic`() {
        val logistic = givenALogistic()
        val bookingId1 = repository.save(logistic)
        val bookingId2 = repository.save(logistic)
        bookingId1 shouldBe bookingId2

        transaction(logisticDatabase) {
            val bookings = BookingTable.selectAll().toList()
            bookings shouldHaveSize 1
            val containers = ContainerTable.selectAll().toList()
            containers shouldHaveSize 1
            val orders = OrderTable.selectAll().toList()
            orders shouldHaveSize 1
            val invoices = InvoiceTable.selectAll().toList()
            invoices shouldHaveSize 1
        }
    }

    @Test
    fun `save should persist logistic with empty containers`() {
        val logistic = givenALogistic().copy(containers = emptyList())
        val bookingId = repository.save(logistic)
        transaction(logisticDatabase) {
            val bookings = BookingTable.selectAll().toList()
            bookings shouldHaveSize 1
            val containers = ContainerTable.selectAll().toList()
            containers.shouldBeEmpty()
        }
    }

    @Test
    fun `save should persist logistic with empty orders`() {
        val logistic = givenALogistic().copy(orders = emptyList())
        val bookingId = repository.save(logistic)
        transaction(logisticDatabase) {
            val bookings = BookingTable.selectAll().toList()
            bookings shouldHaveSize 1
            val orders = OrderTable.selectAll().toList()
            orders.shouldBeEmpty()
        }
    }

    @Test
    fun `save should persist logistic with order with empty invoices`() {
        val orderWithoutInvoices = Order(purchaseId = UUID.randomUUID(), purchase = purchase, invoices = emptyList())
        val logistic = givenALogistic().copy(orders = listOf(orderWithoutInvoices))
        val bookingId = repository.save(logistic)
        transaction(logisticDatabase) {
            val bookings = BookingTable.selectAll().toList()
            bookings shouldHaveSize 1
            val orders = OrderTable.selectAll().toList()
            orders shouldHaveSize 1
            val invoices = InvoiceTable.selectAll().toList()
            invoices.shouldBeEmpty()
        }
    }

    private fun givenALogistic() = Logistic(
        booking = booking,
        clientId = clientId,
        containers = listOf(Container(containerId = UUID.randomUUID(), container = containerCode)),
        orders = listOf(
            Order(
                purchaseId = UUID.randomUUID(),
                purchase = purchase,
                invoices = listOf(Invoice(invoiceId = UUID.randomUUID(), invoice = invoiceCode))
            )
        )
    )
}

