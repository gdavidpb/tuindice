package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAcademicCalendar
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantAcademicCalendarRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// The calendar of the current term, read when it is asked for: null when the app has no current term.
class GetAssistantAcademicCalendarUseCase(
	private val calendarRepository: AssistantAcademicCalendarRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, AssistantAcademicCalendar?, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<AssistantAcademicCalendar?> {
		return flow { emit(calendarRepository.currentCalendar()) }
	}
}
