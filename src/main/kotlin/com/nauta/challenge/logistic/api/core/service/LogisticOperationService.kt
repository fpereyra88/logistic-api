package com.nauta.challenge.logistic.api.core.service

import com.nauta.challenge.logistic.api.core.domain.Container
import com.nauta.challenge.logistic.api.core.domain.Order
import com.nauta.challenge.logistic.api.core.repository.LogisticRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class LogisticOperationService(
    private val logisticRepository: LogisticRepository
) {

    fun findOrdersByClientId(clientId: UUID): List<Order> {
        logger.info { "Finding orders for client ID: $clientId" }

        val result = logisticRepository.findOrdersByClientId(clientId)

        logger.debug { "Found ${result.size} orders for client ID: $clientId" }

        return result
    }

    fun findContainersByClientId(clientId:UUID): List<Container> {
        logger.info { "Finding containers for client ID: $clientId" }

        val result = logisticRepository.findContainersByClientId(clientId)

        logger.debug { "Found ${result.size} containers for client ID: $clientId" }

        return result
    }

    fun findContainersByPurchaseId(purchaseId: UUID): List<Container> {
        logger.info { "Finding containers for purchase ID: $purchaseId" }

        val result = logisticRepository.findContainersByPurchaseId(purchaseId)

        logger.debug { "Found ${result.size} containers for purchase ID: $purchaseId" }

        return result
    }

    fun findOrdersByContainerId(containerId: UUID): List<Order> {
        logger.info { "Finding orders for container ID: $containerId" }

        val result = logisticRepository.findOrdersByContainerId(containerId)

        logger.debug { "Found ${result.size} orders for container ID: $containerId" }

        return result
    }

    private companion object {
        val logger = KotlinLogging.logger {}
    }

}

