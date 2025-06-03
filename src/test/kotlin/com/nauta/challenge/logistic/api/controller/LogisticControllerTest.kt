package com.nauta.challenge.logistic.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.nauta.challenge.logistic.api.controller.request.ContainerRequest
import com.nauta.challenge.logistic.api.controller.request.InvoiceRequest
import com.nauta.challenge.logistic.api.controller.request.LogisticRequest
import com.nauta.challenge.logistic.api.controller.request.OrderRequest
import com.nauta.challenge.logistic.api.core.service.LogisticService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.Called
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.util.*

@WebMvcTest(LogisticController::class)
class LogisticControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper
) {
    @MockkBean
    lateinit var logisticService: LogisticService

    @Test
    fun `given valid request and header when POST then returns 201 and booking id`() {
        val clientId = UUID.randomUUID()
        val request = givenALogistic()
        val bookingId = UUID.randomUUID().toString()

        every { logisticService.invoke(any()) } returns bookingId

        mockMvc.post(ENDPOINT) {
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            header(HEADER_CLIENT_ID, clientId.toString())
            content = objectMapper.writeValueAsString(request)
        }
            .andExpect {
                status { isCreated() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.data.bookingId") { value(bookingId) }
                jsonPath("$.message") { value("Logistic data ingested successfully") }
            }

        io.mockk.verify { logisticService.invoke(any()) }
    }

    @Test
    fun `given missing X-Client-Id header when POST then returns 400`() {
        val request = givenALogistic()

        mockMvc.post(ENDPOINT) {
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }
            .andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
        verify { logisticService wasNot Called }
    }

    @Test
    fun `given invalid request body when POST then returns 400`() {
        val clientId = UUID.randomUUID()
        val invalidRequest = "{}"
        mockMvc.post(ENDPOINT) {
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            header(HEADER_CLIENT_ID, clientId.toString())
            content = invalidRequest
        }
            .andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
        verify { logisticService wasNot Called }
    }

    @Test
    fun `given service throws unexpected exception when POST then returns 500`() {
        val clientId = UUID.randomUUID()
        val request = givenALogistic()
        every { logisticService.invoke(any()) } throws RuntimeException("Unexpected error")

        mockMvc.post(ENDPOINT) {
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            header(HEADER_CLIENT_ID, clientId.toString())
            content = objectMapper.writeValueAsString(request)
        }
            .andExpect {
                status { isInternalServerError() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
    }

    @Test
    fun `given request with null booking when POST then returns 400`() {
        val clientId = UUID.randomUUID()
        val request = givenALogistic().copy(booking = "")
        mockMvc.post(ENDPOINT) {
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            header(HEADER_CLIENT_ID, clientId.toString())
            content = objectMapper.writeValueAsString(request)
        }
            .andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
        verify { logisticService wasNot Called }
    }

    private fun givenALogistic() = LogisticRequest(
        booking = "booking123",
        containers = listOf(ContainerRequest(container = "container1")),
        orders = listOf(
            OrderRequest(
                purchase = "purchase1",
                invoices = listOf(InvoiceRequest(invoice = "invoice1"))
            )
        )
    )

    private companion object {
        const val ENDPOINT = "/api/email"
        const val HEADER_CLIENT_ID = "X-Client-Id"
    }
}
