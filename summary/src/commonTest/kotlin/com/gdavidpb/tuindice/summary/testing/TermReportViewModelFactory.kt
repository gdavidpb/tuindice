package com.gdavidpb.tuindice.summary.testing

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.AssistantInsightsRepository
import com.gdavidpb.tuindice.summary.domain.usecase.MarkTermReportSeenUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveTermReportUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RateTermReportUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateTermReportsUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.ObserveTermReportExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RateTermReportExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateTermReportsExceptionHandler
import com.gdavidpb.tuindice.summary.presentation.machine.TermReportMachine
import com.gdavidpb.tuindice.summary.presentation.viewmodel.TermReportViewModel
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository

// The term report screen over the given reports. The record holds a closed term with an index, and the term in progress
// beside it, so a report of the closed term can be shared and a report of the other cannot.
fun createTermReportViewModel(
	dispatchers: TuIndiceDispatchers,
	insights: AssistantInsightsRepository,
	options: TermReportViewModelOptions = TermReportViewModelOptions()
): TermReportViewModel {
	val reporting = RecordingReportingRepository()

	return TermReportViewModel(
		screenMachine = TermReportMachine(
			observeTermReportUseCase = ObserveTermReportUseCase(
				insightsRepository = insights,
				visibleAcademicRecordRepository = ControllableVisibleAcademicRecordRepository(termReportRecord()),
				recordDataPrerequisiteRepository = options.prerequisite,
				feedbackRepository = options.feedback,
				reportingRepository = reporting,
				exceptionHandler = ObserveTermReportExceptionHandler()
			),
			markTermReportSeenUseCase = MarkTermReportSeenUseCase(options.seen, reporting),
			rateTermReportUseCase = RateTermReportUseCase(
				insightsRepository = insights,
				feedbackRepository = options.feedback,
				reportingRepository = reporting,
				exceptionHandler = RateTermReportExceptionHandler()
			),
			updateTermReportsUseCase = UpdateTermReportsUseCase(
				insightsRepository = insights,
				reportingRepository = reporting,
				exceptionHandler = UpdateTermReportsExceptionHandler()
			)
		),
		eventPublisher = options.eventPublisher,
		dispatchers = dispatchers
	)
}

// A closed term with an index (January - March 2026) and the term in progress (April - July 2026).
fun termReportRecord(): AcademicRecord {
	val closed = hubClosedTerm(
		year = 2026,
		attempts = listOf(hubPassedAttempt("CI1")),
		index = 4.0,
		periodCode = AcademicTermPeriod.JAN_MAR
	)
	val current = hubTerm(
		attempts = listOf(hubPassedAttempt("CI3")),
		kind = TermKind.CURRENT,
		periodCode = AcademicTermPeriod.APR_JUL
	)

	return hubRecord(closed, current)
}
