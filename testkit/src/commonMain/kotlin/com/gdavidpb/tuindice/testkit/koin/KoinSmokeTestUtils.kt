package com.gdavidpb.tuindice.testkit.koin

import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import kotlin.reflect.KClass

inline fun withStartedKoin(
	start: () -> Koin,
	block: Koin.() -> Unit
) {
	stopKoin()

	val koin = start()

	try {
		koin.block()
	} finally {
		stopKoin()
	}
}

inline fun withKoinSmokeTest(
	vararg modules: Module,
	block: Koin.() -> Unit
) {
	withStartedKoin(
		start = {
			startKoin {
				allowOverride(true)
				modules(modules.toList())
			}.koin
		},
		block = block
	)
}

fun Koin.assertResolves(vararg definitions: KClass<*>) {
	definitions.forEach { definition ->
		get(definition)
	}
}
