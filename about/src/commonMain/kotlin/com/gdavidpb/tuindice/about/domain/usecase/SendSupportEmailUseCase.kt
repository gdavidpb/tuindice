package com.gdavidpb.tuindice.about.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SendSupportEmailUseCase(
	private val configRepository: ConfigRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, String, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<String> {
		val email = configRepository.getContactEmail()
		val subject = configRepository.getContactSubject().encodeURLParameter()
		val body = "".encodeURLParameter()

		return flowOf("mailto:$email?subject=$subject&body=$body")
	}
}
