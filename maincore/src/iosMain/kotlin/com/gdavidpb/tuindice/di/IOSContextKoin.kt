package com.gdavidpb.tuindice.di

import org.koin.core.KoinApplication
import org.koin.core.scope.Scope

private const val IOS_CONTEXT_PROPERTY = "ios_context"

fun KoinApplication.iOSContext(context: IOSContext) {
	properties(
		mapOf(IOS_CONTEXT_PROPERTY to context)
	)
}

fun Scope.iOSContext(): IOSContext {
	return getKoin().getProperty(IOS_CONTEXT_PROPERTY)
		?: error("iOSContext is not registered. Configure it before loading iosPlatformModule.")
}
