package com.nauta.challenge.logistic.api.controller.response

import com.nauta.challenge.logistic.api.core.domain.Container
import java.util.UUID

data class ContainerResponse(
    val containerId: UUID? = null,
    val container: String
) {
    companion object {
        fun fromDomain(container: Container): ContainerResponse {
            return ContainerResponse(
                containerId = container.containerId,
                container = container.container
            )
        }
    }
}
