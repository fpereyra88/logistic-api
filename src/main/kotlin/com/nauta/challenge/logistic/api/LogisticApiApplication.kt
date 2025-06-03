package com.nauta.challenge.logistic.api

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
	scanBasePackages = ["com.nauta.challenge.logistic.api"]
)
class LogisticApiApplication

private val logger = LoggerFactory.getLogger(LogisticApiApplication::class.java)

fun main(args: Array<String>) {

	logger.info("Starting application Logistic Api..")

	runApplication<LogisticApiApplication>(*args)

	logger.info("Logistic Api is running..")
}
