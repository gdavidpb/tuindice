package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.base.domain.session.SessionResidue
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.definition.BeanDefinition
import org.koin.core.module.flatten
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The wipe reaches a holder only through its `SessionMemory` binding, and a missing binding fails
 * silently: the holder just keeps what it had. Reads the declared definitions of the shared
 * modules on the JVM, where a class can be asked what it implements.
 */
@OptIn(KoinInternalApi::class)
class SessionMemoryBindingTest {
	private val definitions: List<BeanDefinition<*>> =
		flatten(sharedModules(platformModules = emptyList()))
			.flatMap { module -> module.mappings.values }
			.map { factory -> factory.beanDefinition }
			.distinct()

	@Test
	fun everyDefinitionDeclaredAsASessionMemoryIsBoundAsOne() {
		val unbound = definitions
			.filter { definition -> SessionMemory::class.java.isAssignableFrom(definition.primaryType.java) }
			.filterNot { definition -> SessionMemory::class in definition.secondaryTypes }
			.map { definition -> definition.primaryType.qualifiedName }

		assertEquals(emptyList(), unbound)
	}

	// The inventory of what a sign-out clears from memory. A new holder shows up here on purpose.
	@Test
	fun theSharedModulesBindExactlyTheKnownSessionMemoryHolders() {
		val bound = definitions
			.filter { definition -> SessionMemory::class in definition.secondaryTypes }
			.map { definition -> definition.primaryType.qualifiedName.orEmpty().removePrefix("com.gdavidpb.tuindice.") }
			.sorted()

		assertEquals(
			listOf(
				"base.data.source.InMemorySessionDataSource",
				"base.data.source.usage.UsageDataConsentSettingsDataSource",
				"data.source.credentials.CredentialsDataSource",
				"data.source.session.SessionRecoveryDataSource",
				"data.source.sync.SyncStatusSettingsDataSource",
				"enrollmentproof.data.source.FileKitStorageDataSource",
				"evaluations.data.source.LocalSettingsDataSource",
				"evaluations.data.source.RoomDatabaseDataSource",
				"pensum.data.source.LocalSettingsDataSource",
				"pensum.data.source.PensumDataSource",
				"persistence.domain.mutation.StoreBackedMutationEngine",
				"persistence.domain.mutation.StoreBackedMutationEngine",
				"record.data.source.LocalSettingsDataSource",
				"subjects.data.source.SubjectStatsRoomDataSource",
				"summary.data.source.LocalSettingsDataSource",
				"summary.data.source.ProfilePictureImageLoaderDataSource"
			),
			bound
		)
	}

	// The start without a session asks only these, so a holder that is not bound here is never
	// asked. Narrow on purpose: it is not a second list of every session memory. Only the shared
	// modules are seen here; the iOS-only holder (IosProfilePictureInputDataSource) is bound in the
	// iOS platform module and is covered by IosAppKoinSmokeTest.
	@Test
	fun theSharedModulesBindExactlyTheKnownSessionResidueHolders() {
		val bound = definitions
			.filter { definition -> SessionResidue::class in definition.secondaryTypes }
			.map { definition -> definition.primaryType.qualifiedName.orEmpty().removePrefix("com.gdavidpb.tuindice.") }
			.sorted()

		assertEquals(listOf("enrollmentproof.data.source.FileKitStorageDataSource"), bound)
	}

	@Test
	fun everyDefinitionDeclaredAsASessionResidueIsBoundAsOne() {
		val unbound = definitions
			.filter { definition -> SessionResidue::class.java.isAssignableFrom(definition.primaryType.java) }
			.filterNot { definition -> SessionResidue::class in definition.secondaryTypes }
			.map { definition -> definition.primaryType.qualifiedName }

		assertEquals(emptyList(), unbound)
	}
}
