package com.nauta.challenge.logistic.api.core.exception

sealed class LogisticConflictException(message: String) : RuntimeException(message) {

    class BookingClientConflict(booking: String) :
        LogisticConflictException("Booking '$booking' already exists with different client_id.")
}
