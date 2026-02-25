package com.gdavidpb.tuindice.base.data.source.config

import com.gdavidpb.tuindice.base.utils.DefaultRemoteConfig
import com.gdavidpb.tuindice.base.utils.RemoteConfigDefaultsProfile
import com.gdavidpb.tuindice.base.utils.RemoteConfigKeys
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigDataSourceTest {
	@Test
	fun usesDefaultsWhenRemoteValuesAreMissing() {
		val defaults = DefaultRemoteConfig.values(RemoteConfigDefaultsProfile.PRODUCTION)
		val remote = FakeRemoteConfigDataSource()
		val dataSource = ConfigDataSource(
			remoteConfigDataSource = remote,
			defaults = defaults
		)

		assertEquals(defaults.timeoutMillis, dataSource.getTimeout())
		assertEquals(defaults.contactEmail, dataSource.getContactEmail())
		assertEquals(defaults.contactSubject, dataSource.getContactSubject())
		assertEquals(defaults.loadingMessages, dataSource.getLoadingMessages())
		assertEquals(defaults.updateStalenessDays, dataSource.getTimeUpdateStalenessDays())
		assertEquals(defaults.syncsToSuggestReview, dataSource.getSyncsToSuggestReview())
	}

	@Test
	fun appliesRemoteValuesForAllConfigKeys() {
		val defaults = DefaultRemoteConfig.values(RemoteConfigDefaultsProfile.PRODUCTION)
		val remote = FakeRemoteConfigDataSource(
			strings = mapOf(
				RemoteConfigKeys.TIME_OUT_CONNECTION to "120000",
				RemoteConfigKeys.CONTACT_EMAIL to "remote@tuindice.app",
				RemoteConfigKeys.CONTACT_SUBJECT to "Remote Subject",
				RemoteConfigKeys.LOADING_MESSAGES to "[\"Remote 1\", \"Remote 2\"]",
				RemoteConfigKeys.TIME_UPDATE_STALENESS_DAYS to "3",
				RemoteConfigKeys.SYNCS_TO_SUGGEST_REVIEW to "8"
			)
		)
		val dataSource = ConfigDataSource(
			remoteConfigDataSource = remote,
			defaults = defaults
		)

		assertEquals(120000L, dataSource.getTimeout())
		assertEquals("remote@tuindice.app", dataSource.getContactEmail())
		assertEquals("Remote Subject", dataSource.getContactSubject())
		assertEquals(listOf("Remote 1", "Remote 2"), dataSource.getLoadingMessages())
		assertEquals(3, dataSource.getTimeUpdateStalenessDays())
		assertEquals(8, dataSource.getSyncsToSuggestReview())
	}

	@Test
	fun fallsBackToDefaultsOnInvalidRemoteNumericsAndJson() {
		val defaults = DefaultRemoteConfig.values(RemoteConfigDefaultsProfile.PRODUCTION)
		val remote = FakeRemoteConfigDataSource(
			strings = mapOf(
				RemoteConfigKeys.TIME_OUT_CONNECTION to "not-a-number",
				RemoteConfigKeys.CONTACT_EMAIL to "remote@tuindice.app",
				RemoteConfigKeys.CONTACT_SUBJECT to "Remote Subject",
				RemoteConfigKeys.LOADING_MESSAGES to "{invalid-json}",
				RemoteConfigKeys.TIME_UPDATE_STALENESS_DAYS to "invalid-days",
				RemoteConfigKeys.SYNCS_TO_SUGGEST_REVIEW to "invalid-syncs"
			)
		)
		val dataSource = ConfigDataSource(
			remoteConfigDataSource = remote,
			defaults = defaults
		)

		assertEquals(defaults.timeoutMillis, dataSource.getTimeout())
		assertEquals("remote@tuindice.app", dataSource.getContactEmail())
		assertEquals("Remote Subject", dataSource.getContactSubject())
		assertEquals(defaults.loadingMessages, dataSource.getLoadingMessages())
		assertEquals(defaults.updateStalenessDays, dataSource.getTimeUpdateStalenessDays())
		assertEquals(defaults.syncsToSuggestReview, dataSource.getSyncsToSuggestReview())
	}

	@Test
	fun tryFetchNeverThrows() = runBlocking {
		val defaults = DefaultRemoteConfig.values(RemoteConfigDefaultsProfile.PRODUCTION)
		val remote = FakeRemoteConfigDataSource(
			fetchError = IllegalStateException("boom")
		)
		val dataSource = ConfigDataSource(
			remoteConfigDataSource = remote,
			defaults = defaults
		)

		dataSource.tryFetch()

		assertEquals(1, remote.fetchCalls)
	}
}

private class FakeRemoteConfigDataSource(
	private val strings: Map<String, String> = emptyMap(),
	private val fetchError: Throwable? = null
) : RemoteConfigDataSource {
	var fetchCalls: Int = 0
		private set

	override suspend fun fetch() {
		fetchCalls++
		fetchError?.let { throw it }
	}

	override fun getString(key: String): String? = strings[key]
}
