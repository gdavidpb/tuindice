package com.gdavidpb.tuindice.base.data.source

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.data.repository.config.RemoteConfigDataRepository
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantMode
import com.gdavidpb.tuindice.base.utils.DefaultRemoteConfig
import com.gdavidpb.tuindice.base.utils.RemoteConfigDefaultsProfile
import com.gdavidpb.tuindice.base.utils.RemoteConfigKeys
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigDataSourceAssistantModeSubscriptionTest {
	@Test
	fun observeAssistantMode_updateEmittedWhileSubscribingIsNotLost() = runTest {
		val dataSource = ConfigDataSource(
			remoteConfigDataSource = SubscribingRemoteConfigDataSource(initialValue = "off"),
			defaults = DefaultRemoteConfig.values(RemoteConfigDefaultsProfile.PRODUCTION)
		)

		dataSource.observeAssistantMode().test {
			// The stored mode changes and the update is emitted as the observer subscribes, so the first value is the new one.
			assertEquals(expected = AssistantMode.ON, actual = awaitItem())
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun observeAssistantMode_updateLandingRightAfterTheInitialReadIsNotLost() = runTest {
		val dataSource = ConfigDataSource(
			remoteConfigDataSource = HotRemoteConfigDataSource(initialValue = "off"),
			defaults = DefaultRemoteConfig.values(RemoteConfigDefaultsProfile.PRODUCTION)
		)

		dataSource.observeAssistantMode().test {
			assertEquals(expected = AssistantMode.OFF, actual = awaitItem())
			assertEquals(expected = AssistantMode.ON, actual = awaitItem())
			cancelAndIgnoreRemainingEvents()
		}
	}

	private class SubscribingRemoteConfigDataSource(
		initialValue: String
	) : RemoteConfigDataRepository {
		private val values = mutableMapOf(RemoteConfigKeys.ASSISTANT_MODE to initialValue)

		override suspend fun fetch() = Unit

		override fun getString(key: String): String? = values[key]

		// Changes the value and emits the key only when the observer subscribes.
		override fun observeUpdatedKeys(): Flow<Set<String>> = flow {
			values[RemoteConfigKeys.ASSISTANT_MODE] = "on"
			emit(setOf(RemoteConfigKeys.ASSISTANT_MODE))
			awaitCancellation()
		}
	}

	private class HotRemoteConfigDataSource(
		initialValue: String
	) : RemoteConfigDataRepository {
		private val values = mutableMapOf(RemoteConfigKeys.ASSISTANT_MODE to initialValue)
		private val updatedKeys = MutableSharedFlow<Set<String>>(extraBufferCapacity = 16)
		private var updated = false

		override suspend fun fetch() = Unit

		// The update lands right after the first read returns: the window a late subscription used to miss.
		override fun getString(key: String): String? {
			val value = values[key]

			if (key == RemoteConfigKeys.ASSISTANT_MODE && !updated) {
				updated = true
				values[key] = "on"
				updatedKeys.tryEmit(setOf(key))
			}

			return value
		}

		override fun observeUpdatedKeys(): Flow<Set<String>> = updatedKeys
	}
}
