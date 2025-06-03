package com.nauta.challenge.logistic.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.nauta.challenge.logistic.api.core.domain.Container
import com.nauta.challenge.logistic.api.core.domain.Order
import com.nauta.challenge.logistic.api.core.service.LogisticOperationService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.Called
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.util.*

@WebMvcTest(LogisticOperationController::class)
class LogisticOperationControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper
) {
    @MockkBean
    lateinit var logisticOperationService: LogisticOperationService

    @Test
    fun `given valid clientId header when GET orders then returns 200 and orders`() {
        val clientId = UUID.randomUUID()
        val orders = listOf(Order(UUID.randomUUID(), "purchase1", emptyList()))
        every { logisticOperationService.findOrdersByClientId(clientId) } returns orders

        mockMvc.get("/api/orders") {
            header(HEADER_CLIENT_ID, clientId.toString())
            accept = MediaType.APPLICATION_JSON
        }
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.data") { isArray() }
            }
        verify { logisticOperationService.findOrdersByClientId(clientId) }
    }

    @Test
    fun `given missing clientId header when GET orders then returns 400`() {
        mockMvc.get("/api/orders") {
            accept = MediaType.APPLICATION_JSON
        }
            .andExpect {
                status { isBadRequest() }
            }
        verify { logisticOperationService wasNot Called }
    }

    @Test
    fun `given valid clientId header when GET containers then returns 200 and containers`() {
        val clientId = UUID.randomUUID()
        val containers = listOf(Container(UUID.randomUUID(), "container1"))
        every { logisticOperationService.findContainersByClientId(clientId) } returns containers

        mockMvc.get("/api/containers") {
            header(HEADER_CLIENT_ID, clientId.toString())
            accept = MediaType.APPLICATION_JSON
        }
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.data") { isArray() }
            }
        verify { logisticOperationService.findContainersByClientId(clientId) }
    }

    @Test
    fun `given missing clientId header when GET containers then returns 400`() {
        mockMvc.get("/api/containers") {
            accept = MediaType.APPLICATION_JSON
        }
            .andExpect {
                status { isBadRequest() }
            }
        verify { logisticOperationService wasNot Called }
    }

    @Test
    fun `given valid purchaseId when GET containers by purchase then returns 200 and containers`() {
        val purchaseId = UUID.randomUUID()
        val containers = listOf(Container(UUID.randomUUID(), "container1"))
        every { logisticOperationService.findContainersByPurchaseId(purchaseId) } returns containers

        mockMvc.get("/api/orders/$purchaseId/containers") {
            accept = MediaType.APPLICATION_JSON
        }
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.data") { isArray() }
            }
        verify { logisticOperationService.findContainersByPurchaseId(purchaseId) }
    }

    @Test
    fun `given valid containerId when GET orders by container then returns 200 and orders`() {
        val containerId = UUID.randomUUID()
        val orders = listOf(Order(UUID.randomUUID(), "purchase1", emptyList()))
        every { logisticOperationService.findOrdersByContainerId(containerId) } returns orders

        mockMvc.get("/api/containers/$containerId/orders") {
            accept = MediaType.APPLICATION_JSON
        }
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.data") { isArray() }
            }
        verify { logisticOperationService.findOrdersByContainerId(containerId) }
    }

    private companion object {
        const val HEADER_CLIENT_ID = "X-Client-Id"
    }
}

