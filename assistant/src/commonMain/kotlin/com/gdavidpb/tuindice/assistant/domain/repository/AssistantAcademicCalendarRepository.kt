package com.gdavidpb.tuindice.assistant.domain.repository

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAcademicCalendar

// Implemented by maincore, which holds the current term and the engine that counts its weeks. The answer is null when
// the app has no current term; the evaluations are then shown without weeks.
interface AssistantAcademicCalendarRepository {
	suspend fun currentCalendar(): AssistantAcademicCalendar?
}
