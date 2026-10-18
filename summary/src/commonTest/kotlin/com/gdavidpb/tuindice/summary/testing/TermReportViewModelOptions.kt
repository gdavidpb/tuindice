package com.gdavidpb.tuindice.summary.testing

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.summary.domain.repository.TermReportFeedbackRepository

// What the doubles of the screen hold beside its reports: the thumbs the device keeps, the ids opened, the readiness of
// the record and the events the screen sends. The defaults hold nothing.
class TermReportViewModelOptions(
	val feedback: TermReportFeedbackRepository = ControllableTermReportFeedbackRepository(),
	val seen: ControllableTermReportSeenRepository = ControllableTermReportSeenRepository(),
	val prerequisite: ControllableRecordDataPrerequisiteRepository = ControllableRecordDataPrerequisiteRepository(),
	val eventPublisher: EventPublisher = NoOpEventPublisher
)
