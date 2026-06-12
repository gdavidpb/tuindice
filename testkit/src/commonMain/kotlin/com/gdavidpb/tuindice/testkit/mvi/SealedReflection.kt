package com.gdavidpb.tuindice.testkit.mvi

import kotlin.reflect.KClass

/**
 * Direct sealed subclasses of [root], or null on platforms without sealed-hierarchy
 * reflection (Kotlin/Native). Assertions that depend on it must no-op when null:
 * the android host test run is the enforcing platform.
 */
expect fun sealedSubclassesOf(root: KClass<*>): List<KClass<*>>?
