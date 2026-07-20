package com.gdavidpb.tuindice.base.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

/**
 * Per-module registration of every serializable [Destination] subclass; the app
 * host aggregates all contributions into the back-stack saved-state configuration
 * required for open polymorphism on non-JVM targets.
 */
interface TuIndiceNavContribution {

	fun registerNavKeys(builder: PolymorphicModuleBuilder<NavKey>)
}
