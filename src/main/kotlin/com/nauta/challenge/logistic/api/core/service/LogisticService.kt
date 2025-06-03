package com.nauta.challenge.logistic.api.core.service

import com.nauta.challenge.logistic.api.core.domain.Logistic
import com.nauta.challenge.logistic.api.core.repository.LogisticRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class LogisticService(
    private val logisticRepository: LogisticRepository
) {

    operator fun invoke(logistic: Logistic): String {
        logger.info { "Trying to create logistic for booking: ${logistic.booking}" }

        val bookingId = logisticRepository.save(logistic)

        logger.info { "Logistic created for booking: ${logistic.booking}" }

        return bookingId
    }

    private companion object {
        val logger = KotlinLogging.logger {}
    }
}