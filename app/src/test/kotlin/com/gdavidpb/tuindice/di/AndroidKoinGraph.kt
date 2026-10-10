package com.gdavidpb.tuindice.di

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.manualFileKitCoreInitialization
import org.koin.core.Koin
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.context.stopKoin
import org.koin.core.definition.BeanDefinition
import org.koin.core.error.InstanceCreationException
import org.koin.core.module.Module
import org.koin.core.module.flatten
import org.robolectric.RuntimeEnvironment
import java.util.Collections
import java.util.IdentityHashMap
import kotlin.reflect.KClass

/**
 * The Koin graph of the Android app on the host, put together by the code that puts it together on
 * a device: [AndroidKoinBootstrap] says the platform modules and [startAppKoin] adds the shared
 * ones. A module list written out here would keep passing after the real one changed.
 *
 * Two things a device does are left out. `afterStart` is not run, so no `AppStartupTask` starts
 * and no activity callback is registered. And the manifest is not loaded, so no provider runs:
 * FileKit, which a provider initialises on a device, is initialised by hand.
 */
@OptIn(KoinInternalApi::class)
internal class AndroidKoinGraph(
	private val platformVariantModules: List<Module> = emptyList()
) {
	/**
	 * Every definition the modules declare, one entry per declaration. Compared by identity: two
	 * definitions of the same type and qualifier are equal for Koin, which is how one replaces the
	 * other in the registry, and here both have to be seen.
	 */
	val declared: List<BeanDefinition<*>> by lazy {
		flatten(appModules(platformBootstrap = bootstrap()))
			.flatMap { module -> module.mappings.values }
			.map { factory -> factory.beanDefinition }
			.distinctByIdentity()
	}

	// Stops Koin on both sides, as `withStartedKoin` of testkit does. That module is not used
	// from here: verifyModuleGraph reads every project(...) of this build file as a main-scope
	// dependency, and testkit is not one.
	fun start(block: Koin.() -> Unit) {
		FileKit.manualFileKitCoreInitialization(RuntimeEnvironment.getApplication())
		stopKoin()

		val koin = startAppKoin(
			AppKoinBootstrapRequest(
				platformBootstrap = WithoutAfterStart(bootstrap())
			)
		)

		try {
			koin.block()
		} finally {
			stopKoin()
		}
	}

	fun declaring(type: KClass<*>): List<BeanDefinition<*>> {
		return declared.filter { definition -> definition.hasType(type) }
	}

	// The variant modules and the override flag travel together, as in the debug application.
	private fun bootstrap(): AndroidKoinBootstrap {
		return AndroidKoinBootstrap(
			application = RuntimeEnvironment.getApplication(),
			platformVariantModules = platformVariantModules,
			isOverrideEnabled = platformVariantModules.isNotEmpty()
		)
	}
}

private class WithoutAfterStart(
	bootstrap: PlatformKoinBootstrap
) : PlatformKoinBootstrap by bootstrap {
	override fun afterStart(koin: Koin) = Unit
}

/**
 * A definition that is not built on the host. It is checked by what it declares instead: it has to
 * be in the graph, and so does everything it asks the graph for.
 *
 * @param asksFor what the definition takes from the graph. Left out when the definition is built
 * from its constructor, which is then read. Written by hand when it is a lambda around a factory
 * of an SDK, where there is nothing to read.
 */
internal class HostExclusion(
	val type: KClass<*>,
	val reason: String,
	val asksFor: List<KClass<*>>? = null
)

/** What is wrong with the excluded definitions, one line each. Empty when they all hold. */
internal fun AndroidKoinGraph.exclusionProblems(exclusions: List<HostExclusion>): List<String> {
	return exclusions.flatMap { exclusion ->
		val name = exclusion.type.qualifiedName

		if (declared.none { definition -> definition.primaryType == exclusion.type }) {
			return@flatMap listOf(
				"$name is not built on the host (${exclusion.reason}) and no module declares it any more."
			)
		}

		val asksFor = exclusion.asksFor
			?: exclusion.type.java.constructors.single().parameterTypes.map { parameter -> parameter.kotlin }

		asksFor
			.filter { dependency -> declaring(dependency).isEmpty() }
			.map { dependency ->
				"$name is not built on the host (${exclusion.reason}) and asks for " +
					"${dependency.qualifiedName}, which no module provides."
			}
	}
}

/**
 * Builds every definition in the registry but the excluded ones and says which could not be built.
 * One entry per thing that is missing, with the definitions that ask for it: a missing definition
 * fails everything that reaches it, and a line per failure would bury the cause.
 */
@OptIn(KoinInternalApi::class)
internal fun Koin.resolutionProblems(exclusions: List<HostExclusion>): List<String> {
	val excluded = exclusions.map { exclusion -> exclusion.type }.toSet()
	val definitions = instanceRegistry.instances.values
		.map { factory -> factory.beanDefinition }
		.distinctByIdentity()
		.filterNot { definition -> definition.primaryType in excluded }

	return definitions
		.mapNotNull { definition ->
			runCatching { get<Any>(definition.primaryType, definition.qualifier) }
				.exceptionOrNull()
				?.let(::ResolutionFailure)
		}
		.groupBy { failure -> failure.missing }
		.map { (missing, failures) ->
			val askedBy = failures.map { failure -> failure.askedBy }.distinct().sorted()

			"$missing\n\tasked for by:" +
				askedBy.joinToString(separator = "") { definition -> "\n\t\t$definition" } +
				"\n\t${failures.size} of ${definitions.size} definitions cannot be built because of it."
		}
}

private class ResolutionFailure(error: Throwable) {
	private val causes = generateSequence(error) { throwable -> throwable.cause }.toList()

	val missing: String = causes.last().let { root -> "${root::class.simpleName}: ${root.message}" }

	// The definition that asked for what is missing, not the ones that only reach it.
	val askedBy: String = causes.filterIsInstance<InstanceCreationException>().lastOrNull()
		?.message
		?.substringAfter(delimiter = "'")
		?.substringBeforeLast(delimiter = "'")
		?: "(not a definition)"
}

private fun <T> List<T>.distinctByIdentity(): List<T> {
	val seen = Collections.newSetFromMap(IdentityHashMap<T, Boolean>())

	return filter { element -> seen.add(element) }
}
