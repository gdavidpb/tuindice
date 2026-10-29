package com.gdavidpb.tuindice.summary.testing

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.summary.domain.repository.TermReportFeedbackRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository

// What the doubles of the screen hold beside its reports: the thumbs the device keeps, the ids opened, the readiness of
// the record, the events the screen sends and the reports its use cases make. The defaults hold nothing.
class TermReportViewModelOptions(
	val feedback: TermReportFeedbackRepository = ControllableTermReportFeedbackRepository(),
	val seen: ControllableTermReportSeenRepository = ControllableTermReportSeenRepository(),
	val prerequisite: ControllableRecordDataPrerequisiteRepository = ControllableRecordDataPrerequisiteRepository(),
	val eventPublisher: EventPublisher = NoOpEventPublisher,
	val reporting: RecordingReportingRepository = RecordingReportingRepository()
)
