package com.gdavidpb.tuindice.summary.domain.usecase

/**
 * What Summary does with the term reports: the line of the newest one it has not opened, which follows the reports the
 * service has, and the asking of the service for those reports, which Summary does as it opens.
 */
class SummaryTermReports(
	val entry: ObserveTermReportEntryUseCase,
	val refresh: UpdateTermReportsUseCase
)
