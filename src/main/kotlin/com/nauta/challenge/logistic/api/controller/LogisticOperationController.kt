package com.nauta.challenge.logistic.api.controller

import com.nauta.challenge.logistic.api.controller.common.ApiResponse
import com.nauta.challenge.logistic.api.controller.response.ContainerResponse
import com.nauta.challenge.logistic.api.controller.response.OrderResponse
import com.nauta.challenge.logistic.api.core.service.LogisticOperationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class LogisticOperationController(
    private val logisticOperationService: LogisticOperationService
) {

    @GetMapping("/api/orders")
    fun searchOrders(
        @RequestHeader("X-Client-Id") clientId: String
    ): ResponseEntity<ApiResponse<List<OrderResponse>>> {
        val orders = logisticOperationService.findOrdersByClientId(UUID.fromString(clientId))
        return ResponseEntity.ok(
            ApiResponse(
                data = orders.map { OrderResponse.fromDomain(it) }
            )
        )
    }

    @GetMapping("/api/containers")
    fun searchContainers(
        @RequestHeader("X-Client-Id") clientId: String
    ): ResponseEntity<ApiResponse<List<ContainerResponse>>> {
        val containers = logisticOperationService.findContainersByClientId(UUID.fromString(clientId))
        return ResponseEntity.ok(
            ApiResponse(
                data = containers.map { ContainerResponse.fromDomain(it) }
            )
        )
    }

    @GetMapping("/api/orders/{purchaseId}/containers")
    fun searchOrderByPurchaseId(
        @PathVariable purchaseId: UUID
    ): ResponseEntity<ApiResponse<List<ContainerResponse>>> {
        val containers = logisticOperationService.findContainersByPurchaseId(purchaseId)
        return ResponseEntity.ok(
            ApiResponse(
                data = containers.map { ContainerResponse.fromDomain(it) }
            )
        )
    }

    @GetMapping("/api/containers/{containerId}/orders")
    fun searchOrderByContainerId(
        @PathVariable containerId: UUID
    ): ResponseEntity<ApiResponse<List<OrderResponse>>> {
        val orders = logisticOperationService.findOrdersByContainerId(containerId)
        return ResponseEntity.ok(
            ApiResponse(
                data = orders.map { OrderResponse.fromDomain(it) }
            )
        )
    }
}
