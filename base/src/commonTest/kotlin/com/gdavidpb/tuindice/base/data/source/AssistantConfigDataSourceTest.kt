package com.gdavidpb.tuindice.base.data.source

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.data.repository.config.RemoteConfigDataRepository
import com.gdavidpb.tuindice.base.data.source.config.DebugRemoteConfigDataSource
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantMode
import com.gdavidpb.tuindice.base.utils.DefaultRemoteConfig
import com.gdavidpb.tuindice.base.utils.RemoteConfigDefaultsProfile
import com.gdavidpb.tuindice.base.utils.RemoteConfigKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AssistantConfigDataSourceTest {
	@Test
	fun observeAssistantMode_emitsTheInitialValue() = runTest {
		val dataSource = AssistantConfigDataSource(
			remoteConfigDataSource = FakeRemoteConfigDataSource(
				initialValues = mapOf(RemoteConfigKeys.ASSISTANT_MODE to "guided")
			),
			defaults = DefaultRemoteConfig.values(RemoteConfigDefaultsProfile.PRODUCTION)
		)

		dataSource.observeAssistantMode().test {
			assertEquals(expected = AssistantMode.GUIDED, actual = awaitItem())
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun observeAssistantMode_emitsTheNewValueWhenAssistantModeIsUpdated() = runTest {
		val remote = FakeRemoteConfigDataSource(
			initialValues = mapOf(RemoteConfigKeys.ASSISTANT_MODE to "off")
		)
		val dataSource = AssistantConfigDataSource(
			remoteConfigDataSource = remote,
			defaults = DefaultRemoteConfig.values(RemoteConfigDefaultsProfile.PRODUCTION)
		)

		dataSource.observeAssistantMode().test {
			assertEquals(expected = AssistantMode.OFF, actual = awaitItem())

			remote.update(key = RemoteConfigKeys.ASSISTANT_MODE, value = "on")

			assertEquals(expected = AssistantMode.ON, actual = awaitItem())
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun observeAssistantMode_ignoresUpdatesOfOtherKeys() = runTest {
		val remote = FakeRemoteConfigDataSource(
			initialValues = mapOf(RemoteConfigKeys.ASSISTANT_MODE to "on")
		)
		val dataSource = AssistantConfigDataSource(
			remoteConfigDataSource = remote,
			defaults = DefaultRemoteConfig.values(RemoteConfigDefaultsProfile.PRODUCTION)
		)

		dataSource.observeAssistantMode().test {
			assertEquals(expected = AssistantMode.ON, actual = awaitItem())

			// The stored mode changes silently, so only the key filter keeps an unrelated update from reading it.
			remote.setSilently(key = RemoteConfigKeys.ASSISTANT_MODE, value = "guided")
			remote.update(key = RemoteConfigKeys.CONTACT_EMAIL, value = "soporte@tuindice.app")

			expectNoEvents()
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun observeAssistantMode_missingValueFallsBackToTheDefault() = runTest {
		val dataSource = AssistantConfigDataSource(
			remoteConfigDataSource = FakeRemoteConfigDataSource(),
			defaults = DefaultRemoteConfig.values(RemoteConfigDefaultsProfile.DEBUG)
		)

		dataSource.observeAssistantMode().test {
			assertEquals(expected = AssistantMode.ON, actual = awaitItem())
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun observeAssistantMode_invalidValueFailsClosedToOff() = runTest {
		val dataSource = AssistantConfigDataSource(
			remoteConfigDataSource = FakeRemoteConfigDataSource(
				initialValues = mapOf(RemoteConfigKeys.ASSISTANT_MODE to "banana")
			),
			defaults = DefaultRemoteConfig.values(RemoteConfigDefaultsProfile.DEBUG)
		)

		dataSource.observeAssistantMode().test {
			assertEquals(expected = AssistantMode.OFF, actual = awaitItem())
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun observeAssistantMode_followsDebugOverridesAndTheirRemoval() = runTest {
		val debugSource = DebugRemoteConfigDataSource(
			defaults = DefaultRemoteConfig.values(RemoteConfigDefaultsProfile.DEBUG),
			sourceName = "test"
		)
		val dataSource = AssistantConfigDataSource(
			remoteConfigDataSource = debugSource,
			defaults = DefaultRemoteConfig.values(RemoteConfigDefaultsProfile.DEBUG)
		)

		dataSource.observeAssistantMode().test {
			assertEquals(expected = AssistantMode.ON, actual = awaitItem())

			debugSource.setStringOverride(key = RemoteConfigKeys.ASSISTANT_MODE, value = "off")
			assertEquals(expected = AssistantMode.OFF, actual = awaitItem())

			debugSource.setStringOverride(key = RemoteConfigKeys.ASSISTANT_MODE, value = null)
			assertEquals(expected = AssistantMode.ON, actual = awaitItem())
			cancelAndIgnoreRemainingEvents()
		}
	}

	private class FakeRemoteConfigDataSource(
		initialValues: Map<String, String> = emptyMap()
	) : RemoteConfigDataRepository {
		private val values = initialValues.toMutableMap()
		private val updatedKeys = MutableSharedFlow<Set<String>>(extraBufferCapacity = 16)

		override suspend fun fetch() = Unit

		override fun getString(key: String): String? = values[key]

		override fun observeUpdatedKeys(): Flow<Set<String>> = updatedKeys

		// Changes the value and notifies the observers, as a real remote update does.
		fun update(key: String, value: String) {
			values[key] = value
			updatedKeys.tryEmit(setOf(key))
		}

		// Changes the value without notifying anyone.
		fun setSilently(key: String, value: String) {
			values[key] = value
		}
	}
}
