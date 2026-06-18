package com.gdavidpb.tuindice.base.data.source

import com.gdavidpb.tuindice.base.data.repository.config.RemoteConfigDataRepository
import com.gdavidpb.tuindice.base.domain.model.AppAvailabilityNotice
import com.gdavidpb.tuindice.base.utils.DefaultRemoteConfig
import com.gdavidpb.tuindice.base.utils.RemoteConfigDefaultsProfile
import com.gdavidpb.tuindice.base.utils.RemoteConfigKeys
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigDataSourceTest {
	@Test
	fun getAppAvailabilityNotice_readsRemoteConfigValues() {
		val dataSource = ConfigDataSource(
			remoteConfigDataSource = MapRemoteConfigDataSource(
				values = mapOf(
					RemoteConfigKeys.APP_AVAILABILITY_NOTICE_ENABLED to "true",
					RemoteConfigKeys.APP_AVAILABILITY_NOTICE_TITLE to "Mantenimiento",
					RemoteConfigKeys.APP_AVAILABILITY_NOTICE_MESSAGE to "Volvemos pronto."
				)
			),
			defaults = DefaultRemoteConfig.values(RemoteConfigDefaultsProfile.PRODUCTION)
		)

		assertEquals(
			expected = AppAvailabilityNotice(
				enabled = true,
				title = "Mantenimiento",
				message = "Volvemos pronto."
			),
			actual = dataSource.getAppAvailabilityNotice()
		)
	}

	@Test
	fun getAppAvailabilityNotice_fallsBackToDefaultsWhenRemoteValuesAreMissing() {
		val dataSource = ConfigDataSource(
			remoteConfigDataSource = MapRemoteConfigDataSource(),
			defaults = DefaultRemoteConfig.values(RemoteConfigDefaultsProfile.PRODUCTION)
		)

		assertEquals(
			expected = AppAvailabilityNotice(
				enabled = false,
				title = "",
				message = ""
			),
			actual = dataSource.getAppAvailabilityNotice()
		)
	}

	private class MapRemoteConfigDataSource(
		private val values: Map<String, String> = emptyMap()
	) : RemoteConfigDataRepository {
		override suspend fun fetch() = Unit

		override fun getString(key: String): String? = values[key]
	}
}
