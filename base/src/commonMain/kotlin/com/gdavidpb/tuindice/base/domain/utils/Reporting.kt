package com.gdavidpb.tuindice.base.domain.utils

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError
import kotlin.reflect.KClass

fun Throwable.rootCause(): Throwable {
	var current = this

	while (current.cause != null && current.cause !== current) {
		current = current.cause ?: break
	}

	return current
}

fun Any.reportingName(): String {
	return this::class.simpleName ?: toString()
}

fun KClass<*>.reportingName(): String {
	return simpleName ?: "Unknown"
}

fun Throwable.reportingMessage(): String {
	return message ?: "<no-message>"
}

fun UseCaseError?.reportingName(): String {
	return this?.let { it::class.simpleName ?: it.toString() } ?: "none"
}
