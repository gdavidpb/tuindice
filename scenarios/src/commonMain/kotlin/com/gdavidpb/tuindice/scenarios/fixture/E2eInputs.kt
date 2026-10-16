package com.gdavidpb.tuindice.scenarios.fixture

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSearchPensumStatus

/**
 * Values several scenarios hand to the app and the tags they build from them, named once so two copies
 * cannot drift apart. The ones that depend on what the mocks serve say which.
 */
object E2eInputs {
	/** Where a tap on an attempt's grade slider raises the grade: near its end. */
	const val SliderHighX = 0.95

	/** Where a tap on an attempt's grade slider lowers the grade again: further in. */
	const val SliderLowX = 0.75

	/** The vertical middle of the slider, where its track is. */
	const val SliderMiddleY = 0.5

	/** The grade the primary attempt of the canonical record shows before the high tap. */
	const val GradeBefore = 4

	/** The grade the high tap leaves the primary attempt with. */
	const val GradeAfter = 5

	/** The year of the older pensum the version dialog lists after the current one (served by the 2018 mocks). */
	const val OlderPensumYear = 2018

	/** The modality id of the pensum the scenarios pick in the dialog (served by the 2018 mocks). */
	const val LongInternship = "long_internship"

	/** A day of the month the evaluation date picker always offers. */
	const val CalendarDay = 15

	/**
	 * The week the evaluations strip opens on with the frozen clock: the clock is past the term's last week, and the
	 * strip clamps to it (`E2eClockFixtureTest` checks both).
	 */
	const val LastAcademicWeek = 12

	/** A query shorter than the search takes: the guidance stays (`SubjectSearchFixturesTest` checks the length). */
	const val SubjectQueryTooShort = "c"

	/** A query no subject of the catalog or of the debug source matches (`SubjectSearchFixturesTest` checks it). */
	const val SubjectQueryNoMatch = "zzzznomatch"

	/** A query the pensum search answers with the subject the scenarios look for (`SubjectCi2511`). */
	const val SubjectQueryMatch = "ci"

	/**
	 * A search text of the create-term screen that has no results; the other texts the scenarios type are fixtures
	 * the mocks answer.
	 */
	const val RecordQueryNoResults = "zz"

	/**
	 * The key of the hub's point of attention for the evaluations whose day went by with no grade: the summary
	 * mapper (`SummaryAttention.UngradedEvaluations.toSummaryAttentionItem`) builds the tag from it.
	 */
	const val SummaryAttentionUngradedKey = "ungraded"

	/** The key of the hub's point of attention for the enrollment error of [subjectCode], built by the same mapper. */
	fun summaryAttentionEnrollmentKey(subjectCode: String): String = "enrollment-$subjectCode"

	/** The week of the hub term (classes start on 2026-09-28) where its oldest ungraded test falls. */
	const val HubWeek2 = 2

	/** The week of the hub term the frozen clock is in. */
	const val HubWeek3 = 3

	/** Class days of CI5437 in the hub term: a Thursday with class (22) and the Wednesday before it, without (21). */
	const val HubClassDay = 22
	const val HubNoClassDay = 21

	/**
	 * The day codes the backend uses for the schedule (1 = Sunday) and the first block of the meetings the hub scenarios
	 * tap in the sheet: CI5311 on Monday at block 1, CI5437 on Thursday at block 3 (`E2eFixtures.HubAttemptCi5311`,
	 * `HubAttemptCi5437`). An evaluation of the week falls on each of those two meetings.
	 */
	const val ScheduleMonday = 2
	const val ScheduleThursday = 5
	const val HubMondayStartBlock = 1

	/** The keys the Statistics screen gives its charts (`SummaryStatisticsChartKey`); their tags carry them. */
	const val StatisticsIndexChartKey = "index"
	const val StatisticsSubjectsChartKey = "subjects"
	const val StatisticsGradesChartKey = "grades"

	/**
	 * The point of the index line that is the newest closed term of the hub record: the line runs oldest first over its
	 * eleven closed terms (`StatisticsHubFixturesTest` checks the count). [StatisticsEarlierIndexPoint] is the leftmost
	 * target fully on screen on both platforms: the tooltip of the newest point covers the points beside it, and a tap
	 * there would hit the tooltip.
	 */
	const val StatisticsNewestIndexPoint = 10
	const val StatisticsEarlierIndexPoint = 4

	/** The approved segment of the subjects bar, the first of its segments. */
	const val StatisticsApprovedSegment = 0

	/** The bar of the grade 4 of the grades histogram, whose bars run from grade 1. */
	const val StatisticsGradeFourBar = 3

	/** `EvaluationType.TEST`, as the type chip tag spells it (the tag lowercases the name). */
	val EvaluationTypeTest: String = EvaluationType.TEST.name.lowercase()

	/** `EvaluationType.WRITTEN_WORK`, as the type chip tag spells it. */
	val EvaluationTypeWrittenWork: String = EvaluationType.WRITTEN_WORK.name.lowercase()

	/** `AttemptOutcome.APPROVED`, as the status tags of an attempt spell it. */
	val AttemptApproved: String = AttemptOutcome.APPROVED.name.lowercase()

	/** `SubjectSearchPensumStatus.APPROVED`, as the status tag of a search result spells it. */
	val SearchStatusApproved: String = SubjectSearchPensumStatus.APPROVED.name.lowercase()
}
