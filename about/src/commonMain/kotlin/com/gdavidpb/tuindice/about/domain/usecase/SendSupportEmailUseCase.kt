package com.gdavidpb.tuindice.about.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SendSupportEmailUseCase(
	private val configRepository: ConfigRepository,
	private val deviceInfoRepository: DeviceInfoRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, String, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<String> {
		val email = configRepository.getContactEmail()
		val subject = configRepository.getContactSubject().encodeURLParameter()
		// Pre-filled context so support reports arrive with the app and OS build.
		val body = buildString {
			appendLine()
			appendLine()
			appendLine("--")
			appendLine(
				"Versión: ${deviceInfoRepository.appVersionName()} " +
					"(${deviceInfoRepository.appVersionCode()})"
			)
			append("Sistema: ${deviceInfoRepository.osDescription()}")
		}.encodeURLParameter()

		return flowOf("mailto:$email?subject=$subject&body=$body")
	}
}
