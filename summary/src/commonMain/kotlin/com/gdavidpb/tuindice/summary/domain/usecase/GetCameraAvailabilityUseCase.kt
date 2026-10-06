package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Whether the device can take a picture. It decides if the profile picture can be taken on the
 * spot or only picked from what the device already has.
 */
class GetCameraAvailabilityUseCase(
	private val deviceInfoRepository: DeviceInfoRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Boolean, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Boolean> {
		return flowOf(deviceInfoRepository.hasCamera())
	}
}
