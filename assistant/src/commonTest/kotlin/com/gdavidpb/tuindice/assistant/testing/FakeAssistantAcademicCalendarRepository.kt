package com.gdavidpb.tuindice.assistant.testing

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAcademicCalendar
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantAcademicCalendarRepository

// The calendar the tests read: none unless a test gives one, which is the app with no current term.
internal class FakeAssistantAcademicCalendarRepository(
	private val calendar: AssistantAcademicCalendar? = null
) : AssistantAcademicCalendarRepository {
	override suspend fun currentCalendar(): AssistantAcademicCalendar? = calendar
}
