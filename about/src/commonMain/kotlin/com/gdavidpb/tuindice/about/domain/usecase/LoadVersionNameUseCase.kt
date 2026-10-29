package com.gdavidpb.tuindice.about.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// The version number alone («6.8.0»), for the subtitle of the About row of Settings. The About screen shows the longer
// description of the build (environment, number and code) that LoadVersionUseCase gives.
class LoadVersionNameUseCase(
	private val deviceInfoRepository: DeviceInfoRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, String, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<String> {
		return flowOf(deviceInfoRepository.appVersionName())
	}
}
