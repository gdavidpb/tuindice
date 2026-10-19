package com.gdavidpb.tuindice.testkit.base.repository

import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository

class FakeAppEnvironmentRepository(
	private val appEnvironment: AppEnvironment = AppEnvironment(
		apiBaseUrl = "https://api.tuindice.app/",
		privacyPolicyUrl = "https://tuindice.app/privacy",
		termsAndConditionsUrl = "https://tuindice.app/terms",
		supportUrl = "https://tuindice.app/support",
		debug = false
	)
) : AppEnvironmentRepository {
	override fun getEnvironment(): AppEnvironment = appEnvironment
}
