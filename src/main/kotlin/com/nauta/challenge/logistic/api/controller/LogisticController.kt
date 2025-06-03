package com.nauta.challenge.logistic.api.controller

import com.nauta.challenge.logistic.api.controller.common.ApiResponse
import com.nauta.challenge.logistic.api.controller.request.LogisticRequest
import com.nauta.challenge.logistic.api.controller.response.BookingIdResponse
import com.nauta.challenge.logistic.api.core.service.LogisticService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/email")
@Validated
class LogisticController(
    private val logisticService: LogisticService
) {

    @PostMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun ingestLogistic(
        @RequestHeader("X-Client-Id") clientId: UUID,
        @Valid @RequestBody request: LogisticRequest
    ): ResponseEntity<ApiResponse<BookingIdResponse>> {

        val bookingId = logisticService(request.toDomain(clientId))
        val response = ApiResponse(
            data = BookingIdResponse(bookingId),
            message = "Logistic data ingested successfully"
        )
        return ResponseEntity.status(CREATED).body(response)
    }
}
