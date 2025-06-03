package com.nauta.challenge.logistic.api.core.repository

import com.nauta.challenge.logistic.api.core.domain.Container
import com.nauta.challenge.logistic.api.core.domain.Logistic
import com.nauta.challenge.logistic.api.core.domain.Order
import java.util.UUID

interface LogisticRepository {

    fun save(logistic: Logistic): String

    fun findOrdersByClientId(clientId: UUID): List<Order>

    fun findContainersByClientId(clientId: UUID): List<Container>

    fun findContainersByPurchaseId(purchaseId: UUID): List<Container>

    fun findOrdersByContainerId(containerId: UUID): List<Order>
}

