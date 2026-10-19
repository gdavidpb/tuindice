package com.gdavidpb.tuindice.summary.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.TermStatistic
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantInsightAction
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantInsightSection

/**
 * The term report screen, for the report of one term. [sections] and [nextActions] are what the assistant wrote and
 * chose. [term] is the figures of the term as the academic engines read them, and [previousTerm] the term before it in
 * the record, when there is one. [indexDelta] and [cumulativeDelta] compare the two, and are null when either side has
 * no index. [subjects] are the subjects of the term; [creditsOfTerm] counts the credits of those that were not
 * withdrawn. [paceTerms] are the regular terms the pace draws, the term itself among them when it is regular, and
 * [averageCredits] is their average of approved credits. [isShareable] says whether the term is a closed one with an
 * index, the only kind the share sheet can show. [feedback] is the thumb the student's last vote was confirmed with by
 * the service, or null when there is none.
 */
data class SummaryTermReport(
	val insightId: String,
	val termId: String?,
	val termKey: String,
	val title: String,
	val sections: List<AssistantInsightSection>,
	val nextActions: List<AssistantInsightAction>,
	val term: TermStatistic,
	val previousTerm: TermStatistic?,
	val indexDelta: Double?,
	val cumulativeDelta: Double?,
	val subjects: List<SummaryTermReportSubject>,
	val creditsOfTerm: Int,
	val paceTerms: List<TermStatistic>,
	val averageCredits: Double?,
	val isShareable: Boolean = false,
	val feedback: Boolean? = null
)

/**
 * A subject of the term: its [subjectCode] and [subjectName], its [credits], what the term says of it ([outcome]) and
 * the grade it carries, numeric or symbolic, when it has one.
 */
data class SummaryTermReportSubject(
	val subjectCode: String,
	val subjectName: String,
	val credits: Int,
	val outcome: SummaryTermReportOutcome,
	val numericGrade: Int?,
	val symbolicGrade: String?
)

enum class SummaryTermReportOutcome {
	APPROVED,
	FAILED,
	WITHDRAWN,
	PENDING
}
