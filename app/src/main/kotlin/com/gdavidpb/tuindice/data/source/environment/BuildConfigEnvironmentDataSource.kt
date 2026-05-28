package com.gdavidpb.tuindice.data.source.environment

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository

class BuildConfigEnvironmentDataSource : AppEnvironmentRepository {
	override fun getEnvironment(): AppEnvironment {
		return AppEnvironment(
			apiBaseUrl = BuildConfig.URL_API,
			privacyPolicyUrl = BuildConfig.URL_PRIVACY_POLICY,
			termsAndConditionsUrl = BuildConfig.URL_TERMS_AND_CONDITIONS,
			supportUrl = BuildConfig.URL_SUPPORT,
			debug = BuildConfig.DEBUG
		)
	}
}
