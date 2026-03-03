package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.logging.appLogger
import io.ktor.client.plugins.logging.Logger as KtorLogger

private const val HTTP_CLIENT_LOG_TAG = "HttpClient"

fun createAppKtorLogger(tag: String = HTTP_CLIENT_LOG_TAG): KtorLogger {
	val logger = appLogger(tag)

	return object : KtorLogger {
		override fun log(message: String) {
			logger.v { message }
		}
	}
}
