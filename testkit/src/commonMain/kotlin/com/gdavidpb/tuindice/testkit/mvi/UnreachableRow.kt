package com.gdavidpb.tuindice.testkit.mvi

import kotlin.reflect.KClass

/**
 * A row of the table that the walk cannot reach by design, named with the reason. It leaves the denominator of the
 * coverage floor, which is then measured over the rows the walk can visit. [from] null names a machine-level row.
 */
class UnreachableRow(
	val from: KClass<*>?,
	val on: KClass<*>,
	val reason: String
)
