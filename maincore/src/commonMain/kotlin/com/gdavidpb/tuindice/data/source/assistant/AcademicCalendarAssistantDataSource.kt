package com.gdavidpb.tuindice.data.source.assistant

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicWeekEngine
import com.gdavidpb.tuindice.assistant.domain.model.AssistantAcademicCalendar
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantAcademicCalendarRepository
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import kotlinx.datetime.LocalDate

// Reads the current term the app already knows and counts its weeks with the engine the Evaluations tab counts them
// with, so the assistant never counts a week by itself.
class AcademicCalendarAssistantDataSource(
	private val evaluationRepository: EvaluationRepository
) : AssistantAcademicCalendarRepository {
	override suspend fun currentCalendar(): AssistantAcademicCalendar? {
		return evaluationRepository.getCurrentTerm()?.let { term -> calendarOf(term) }
	}
}

// The calendar of [term]: it opens on the same Monday the Evaluations tab opens its week 1 on.
internal fun calendarOf(term: EvaluationTermDescriptor): AssistantAcademicCalendar {
	return TermCalendar(
		weekOneStart = AcademicWeekEngine.weekOneStart(
			periodYear = term.periodYear,
			periodCode = term.periodCode,
			classesStartDate = term.classesStartDate
		)
	)
}

private class TermCalendar(
	override val weekOneStart: LocalDate
) : AssistantAcademicCalendar {
	override val weeks: IntRange = AcademicWeekEngine.MIN_ACADEMIC_WEEK..AcademicWeekEngine.MAX_ACADEMIC_WEEK

	override fun weekOf(date: LocalDate): Int = AcademicWeekEngine.weekOf(weekOneStart, date)
}
