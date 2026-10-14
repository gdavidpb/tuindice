package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationPoints
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import com.gdavidpb.tuindice.base.presentation.mapper.classPlaceItem
import com.gdavidpb.tuindice.base.presentation.mapper.toShortNameText
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress
import com.gdavidpb.tuindice.summary.domain.model.SummaryAttention
import com.gdavidpb.tuindice.summary.domain.model.SummaryClassMoment
import com.gdavidpb.tuindice.summary.domain.model.SummaryFinishedTerm
import com.gdavidpb.tuindice.summary.domain.model.SummaryNextEvaluation
import com.gdavidpb.tuindice.summary.domain.model.SummarySubjectPoints
import com.gdavidpb.tuindice.summary.domain.model.SummaryTerm
import com.gdavidpb.tuindice.summary.domain.model.SummaryWeek
import com.gdavidpb.tuindice.summary.domain.model.SummaryWeekEvaluation
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCardIcon
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCardItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCareerItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.a11y_summary_open_evaluations_week
import tuindice.summary.generated.resources.summary_attention_enrollment_error
import tuindice.summary.generated.resources.summary_attention_ungraded_many
import tuindice.summary.generated.resources.summary_attention_ungraded_one
import tuindice.summary.generated.resources.summary_career_credits_rest
import tuindice.summary.generated.resources.summary_career_percent
import tuindice.summary.generated.resources.summary_class_in_progress
import tuindice.summary.generated.resources.summary_day_date
import tuindice.summary.generated.resources.summary_day_today
import tuindice.summary.generated.resources.summary_evaluation_name
import tuindice.summary.generated.resources.summary_evaluation_type_test
import tuindice.summary.generated.resources.summary_evaluations_invite_action
import tuindice.summary.generated.resources.summary_evaluations_invite_message
import tuindice.summary.generated.resources.summary_evaluations_invite_title
import tuindice.summary.generated.resources.summary_evaluations_pending_message
import tuindice.summary.generated.resources.summary_evaluations_pending_title
import tuindice.summary.generated.resources.summary_link_record
import tuindice.summary.generated.resources.summary_points_evaluated
import tuindice.summary.generated.resources.summary_points_no_evaluations
import tuindice.summary.generated.resources.summary_points_none
import tuindice.summary.generated.resources.summary_points_rest
import tuindice.summary.generated.resources.summary_term_credits_label
import tuindice.summary.generated.resources.summary_term_credits_value
import tuindice.summary.generated.resources.summary_term_finished_state
import tuindice.summary.generated.resources.summary_term_first_message
import tuindice.summary.generated.resources.summary_term_first_title
import tuindice.summary.generated.resources.summary_term_index_label
import tuindice.summary.generated.resources.summary_term_next_evaluation
import tuindice.summary.generated.resources.summary_term_next_today
import tuindice.summary.generated.resources.summary_term_plan_action
import tuindice.summary.generated.resources.summary_term_plan_message
import tuindice.summary.generated.resources.summary_term_plan_title
import tuindice.summary.generated.resources.summary_term_subjects_many
import tuindice.summary.generated.resources.summary_term_subjects_one
import tuindice.summary.generated.resources.summary_term_week_description
import tuindice.summary.generated.resources.summary_term_week_last
import tuindice.summary.generated.resources.summary_term_week_remaining_many
import tuindice.summary.generated.resources.summary_term_week_remaining_one
import tuindice.summary.generated.resources.summary_term_week_rest
import tuindice.summary.generated.resources.summary_term_week_value
import tuindice.summary.generated.resources.summary_week_idle_title
import tuindice.summary.generated.resources.summary_week_next_evaluation
import tuindice.summary.generated.resources.summary_week_no_class_message
import tuindice.summary.generated.resources.summary_week_no_class_title
import tuindice.summary.generated.resources.summary_week_no_term_message
import tuindice.summary.generated.resources.summary_week_no_term_title
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SummaryHubItemTest {
	private val meeting = ScheduleMeeting(
		attemptId = "attempt-CI5437",
		subjectCode = "CI5437",
		section = 1,
		dayOfWeek = 5,
		startBlock = 3,
		endBlock = 4,
		classroom = "MYS-210"
	)

	private fun mondayLead() = UiText.Resource(
		Res.string.summary_day_date,
		listOf(UiText.Capitalized(DayOfWeek.MONDAY.toShortNameText()), 19)
	)

	private fun term(week: Int?, subjects: List<SummarySubjectPoints> = emptyList()) = SummaryTerm.InProgress(
		periodCode = AcademicTermPeriod.SEP_DEC,
		periodYear = 2026,
		academicWeek = week,
		subjects = subjects,
		hasSyncedEvaluations = true
	)

	// A week of a term in progress whose evaluations are downloaded: the common case. The cases that tell
	// otherwise build the week with its constructor.
	private fun inProgress(
		classMoment: SummaryClassMoment? = null,
		evaluations: List<SummaryWeekEvaluation> = emptyList(),
		hasSchedule: Boolean = true
	) = SummaryWeek.InProgress(
		classMoment = classMoment,
		evaluations = evaluations,
		hasSchedule = hasSchedule,
		hasSyncedEvaluations = true,
		hasEvaluations = evaluations.isNotEmpty(),
		nextEvaluation = null
	)

	// A week of a term whose evaluations are registered, none of them in the week, and with no class in it.
	private fun noneThisWeek(nextEvaluation: SummaryNextEvaluation? = null) = SummaryWeek.InProgress(
		classMoment = null,
		evaluations = emptyList(),
		hasSchedule = true,
		hasSyncedEvaluations = true,
		hasEvaluations = true,
		nextEvaluation = nextEvaluation
	)

	private fun subject(
		code: String,
		points: EvaluationPoints?,
		credits: Int = 3,
		nextEvaluation: SummaryNextEvaluation? = null
	) = SummarySubjectPoints(
		subjectCode = code,
		points = points,
		attemptId = "attempt-$code",
		credits = credits,
		nextEvaluation = nextEvaluation
	)

	private fun week(number: Int) = term(week = number).toSummaryTermItem().week!!

	/* Attention */

	@Test
	fun oneUngradedEvaluation_isSaidInTheSingular_andManyInThePlural() {
		val one = SummaryAttention.UngradedEvaluations(
			count = 1,
			oldestWeekStart = LocalDate(2026, 10, 5),
			oldestEvaluationId = "oldest"
		)
		val many = one.copy(count = 2)

		assertEquals(UiText.Resource(Res.string.summary_attention_ungraded_one), one.toSummaryAttentionItem().text)
		assertEquals(
			UiText.Resource(Res.string.summary_attention_ungraded_many, listOf(2)),
			many.toSummaryAttentionItem().text
		)
	}

	@Test
	fun theUngradedPoint_leadsToTheWeekOfTheOldest_andTheEnrollmentErrorToTheRecord() {
		val ungraded = SummaryAttention.UngradedEvaluations(
			count = 3,
			oldestWeekStart = LocalDate(2026, 10, 5),
			oldestEvaluationId = "oldest"
		)
		val clash = SummaryAttention.EnrollmentError(
			subjectCode = "CI5311",
			error = "CHOQUE DE HORARIO",
			attemptId = "attempt-CI5311"
		)

		assertEquals(
			SummaryHubTarget.EvaluationsWeek(weekStart = LocalDate(2026, 10, 5), evaluationId = "oldest"),
			ungraded.toSummaryAttentionItem().target
		)
		// The enrollment error leads to its own attempt in Record, not to the tab as a whole.
		assertEquals(
			SummaryHubTarget.RecordAttempt(attemptId = "attempt-CI5311"),
			clash.toSummaryAttentionItem().target
		)
		assertEquals("enrollment-CI5311", clash.toSummaryAttentionItem().key)
	}

	@Test
	fun anEnrollmentError_losesThePunctuationTheUniversityEndsItWith_andReadsAsOneSentence() {
		fun textOf(error: String) = SummaryAttention.EnrollmentError(
			subjectCode = "CI5311",
			error = error,
			attemptId = "attempt-CI5311"
		).toSummaryAttentionItem().text

		fun expected(error: String) = UiText.Resource(
			Res.string.summary_attention_enrollment_error,
			listOf(error, "CI5311")
		)

		assertEquals(expected("Choque de horario"), textOf("CHOQUE DE HORARIO."))
		assertEquals(expected("Choque de horario"), textOf("  CHOQUE DE HORARIO . "))
		assertEquals(
			expected("Excede el límite de créditos del trimestre según DACE"),
			textOf("EXCEDE EL LÍMITE DE CRÉDITOS DEL TRIMESTRE SEGÚN DACE.")
		)
	}

	@Test
	fun theEnrollmentError_isSaidInSentenceCase_withTheSubjectCode() {
		val clash = SummaryAttention.EnrollmentError(
			subjectCode = "CI5311",
			error = "CHOQUE DE HORARIO",
			attemptId = "attempt-CI5311"
		)

		assertEquals(
			UiText.Resource(Res.string.summary_attention_enrollment_error, listOf("Choque de horario", "CI5311")),
			clash.toSummaryAttentionItem().text
		)
	}

	/* The week */

	@Test
	fun theClassInProgress_isLedByEnCurso_andCarriesTheHour() {
		val week = inProgress(
			classMoment = SummaryClassMoment(
				meeting = meeting,
				isInProgress = true,
				date = LocalDate(2026, 10, 15),
				isToday = true
			),
			evaluations = emptyList(),
			hasSchedule = true
		)

		val row = week.toSummaryWeekItem().classRow!!

		assertEquals(UiText.Resource(Res.string.summary_class_in_progress), row.leadText)
		assertTrue(row.isInProgress)
		assertEquals("CI5437", row.subjectCode)
		assertEquals(classPlaceItem(3, 4, "MYS-210", clockRange = 570..690), row.place)
		assertTrue(row.place.timeText != null)
	}

	@Test
	fun theNextClass_isLedByHoy_whenItIsLaterToday_andByTheDayAndNumberOtherwise() {
		val today = SummaryClassMoment(
			meeting = meeting,
			isInProgress = false,
			date = LocalDate(2026, 10, 15),
			isToday = true
		)
		val monday = today.copy(date = LocalDate(2026, 10, 19), isToday = false)

		val todayRow = inProgress(today, emptyList(), hasSchedule = true).toSummaryWeekItem().classRow!!
		val mondayRow = inProgress(monday, emptyList(), hasSchedule = true).toSummaryWeekItem().classRow!!

		assertEquals(UiText.Resource(Res.string.summary_day_today), todayRow.leadText)
		assertEquals(false, todayRow.isInProgress)
		assertEquals(mondayLead(), mondayRow.leadText)
	}

	@Test
	fun aClassWithBlocksOutsideTheDay_hasNoHour() {
		val late = meeting.copy(startBlock = 17, endBlock = 17)
		val moment = SummaryClassMoment(late, isInProgress = false, date = LocalDate(2026, 10, 15), isToday = true)

		assertNull(inProgress(moment, emptyList(), hasSchedule = true).toSummaryWeekItem().classRow!!.place.timeText)
	}

	@Test
	fun anEvaluation_isNamedByTypeAndOrdinal_ledByItsDay_andPlacedWithoutTheHour() {
		val evaluation = SummaryWeekEvaluation(
			evaluationId = "e1",
			subjectCode = "CI5437",
			type = EvaluationType.TEST,
			ordinal = 1,
			date = LocalDate(2026, 10, 19),
			isToday = false,
			meeting = meeting
		)

		val row = inProgress(null, listOf(evaluation), hasSchedule = true).toSummaryWeekItem().evaluations.single()

		assertEquals(
			UiText.Resource(
				Res.string.summary_evaluation_name,
				listOf(UiText.Resource(Res.string.summary_evaluation_type_test), 1)
			),
			row.titleText
		)
		assertEquals(mondayLead(), row.leadText)
		assertEquals(classPlaceItem(3, 4, "MYS-210", clockRange = null), row.place)
		assertNull(row.place?.timeText)
		assertEquals(LocalDate(2026, 10, 19), row.weekStart)
	}

	@Test
	fun anEvaluationOfASubjectWithNoClassThatDay_hasNoPlace_andOneOfSundayLeadsToItsOwnWeek() {
		val sunday = SummaryWeekEvaluation(
			evaluationId = "e2",
			subjectCode = "EG1114",
			type = EvaluationType.ESSAY,
			ordinal = 2,
			date = LocalDate(2026, 10, 18),
			isToday = false,
			meeting = null
		)

		val row = inProgress(null, listOf(sunday), hasSchedule = false).toSummaryWeekItem().evaluations.single()

		assertNull(row.place)
		assertEquals(LocalDate(2026, 10, 12), row.weekStart)
	}

	@Test
	fun theWeek_offersTheScheduleOnlyWhenTheTermHasOne() {
		val withSchedule = inProgress(null, emptyList(), hasSchedule = true).toSummaryWeekItem()
		val withoutSchedule = inProgress(null, emptyList(), hasSchedule = false).toSummaryWeekItem()

		assertTrue(withSchedule.hasSchedule)
		assertEquals(false, withoutSchedule.hasSchedule)
	}

	/* The term */

	@Test
	fun theTerm_isNamedByItsPeriodAndYear_andSaysItsWeekOutOfTwelve() {
		val item = term(week = 3).toSummaryTermItem()

		assertEquals(UiText.Raw("Sep - Dic 2026"), item.linkText)
		assertEquals(3, item.week?.week)
		assertEquals(12, item.week?.totalWeeks)
		assertEquals(UiText.Resource(Res.string.summary_term_week_value, listOf(3)), item.week?.valueText)
		assertEquals(UiText.Resource(Res.string.summary_term_week_rest, listOf(12)), item.week?.restText)
		assertEquals(UiText.Resource(Res.string.summary_term_week_description, listOf(3, 12)), item.week?.description)
	}

	@Test
	fun outsideTheTwelveWeeks_onlyThePointsRemain() {
		val subjects = listOf(subject("CI5311", EvaluationPoints(41.0, 60.0, 3, 4), credits = 4))

		val item = term(week = null, subjects = subjects).toSummaryTermItem()

		assertNull(item.week)
		assertEquals(listOf("CI5311"), item.subjects.map { it.subjectCode })
	}

	@Test
	fun withNeitherAWeekNorPoints_theTermSaysWhatIsStillToCome_andClaimsNoPoints() {
		val item = term(week = null).toSummaryTermItem()

		assertNull(item.week)
		assertTrue(item.subjects.isEmpty())
		assertEquals(
			SummaryCardItem(
				icon = SummaryCardIcon.Evaluations,
				titleText = UiText.Resource(Res.string.summary_evaluations_pending_title),
				messageText = UiText.Resource(Res.string.summary_evaluations_pending_message)
			),
			item.card
		)
	}

	@Test
	fun thePoints_areEarnedOverEvaluated_withoutDecimalsWhenThereAreNone() {
		val subjects = listOf(
			subject("CI5311", EvaluationPoints(41.0, 60.0, 3, 4)),
			subject("CI5437", EvaluationPoints(18.5, 25.0, 2, 2)),
			subject("EG1114", points = null)
		)

		val items = term(week = 3, subjects = subjects).toSummaryTermItem().subjects

		assertEquals("41", items[0].valueText)
		assertEquals(UiText.Resource(Res.string.summary_points_rest, listOf("60")), items[0].restText)
		assertEquals("18.50", items[1].valueText)
		assertEquals(UiText.Resource(Res.string.summary_points_rest, listOf("25")), items[1].restText)
		assertNull(items[2].valueText)
		assertEquals(UiText.Resource(Res.string.summary_points_no_evaluations), items[2].restText)
	}

	@Test
	fun theWeekLeft_isSaidInThePluralOrTheSingular_andAsTheLastWeekInTheTwelfth() {
		assertEquals(UiText.Resource(Res.string.summary_term_week_remaining_many, listOf(9)), week(3).remainingText)
		assertEquals(UiText.Resource(Res.string.summary_term_week_remaining_one), week(11).remainingText)
		assertEquals(UiText.Resource(Res.string.summary_term_week_last), week(12).remainingText)
	}

	@Test
	fun theSubjectsLine_countsTheRows_addsTheirCredits_andIsAbsentWithNone() {
		val two = term(
			week = 3,
			subjects = listOf(
				subject("CI5311", points = null, credits = 4),
				subject("EG1114", points = null, credits = 3)
			)
		)
		val one = term(week = 3, subjects = listOf(subject("CI5311", points = null, credits = 4)))

		assertEquals(
			UiText.Resource(Res.string.summary_term_subjects_many, listOf(2, 7)),
			two.toSummaryTermItem().summaryText
		)
		assertEquals(
			UiText.Resource(Res.string.summary_term_subjects_one, listOf(1, 4)),
			one.toSummaryTermItem().summaryText
		)
		assertNull(term(week = 3).toSummaryTermItem().summaryText)
	}

	@Test
	fun aSubjectWithEvaluationsButNoGrade_saysSinNotasAun_andOneWithNoEvaluationSaysSinEvaluaciones() {
		val items = term(
			week = 3,
			subjects = listOf(subject("CI5311", EvaluationPoints(0.0, 0.0, 0, 2)), subject("EG1114", points = null))
		).toSummaryTermItem().subjects

		assertNull(items[0].valueText)
		assertEquals(UiText.Resource(Res.string.summary_points_none), items[0].restText)
		assertEquals(UiText.Resource(Res.string.summary_points_no_evaluations), items[1].restText)
	}

	@Test
	fun theEvaluatedLine_isWhatIsEvaluatedOfOneHundred_andTheBarFillsWithIt() {
		val items = term(
			week = 3,
			subjects = listOf(
				subject("CI5311", EvaluationPoints(41.0, 60.0, 3, 4)),
				// Six graded evaluations of 20 make 120 evaluated: the line and the bar stop at the total.
				subject("CI5437", EvaluationPoints(100.0, 120.0, 6, 6)),
				subject("EG1114", points = null)
			)
		).toSummaryTermItem().subjects

		assertEquals(UiText.Resource(Res.string.summary_points_evaluated, listOf("60", "100")), items[0].evaluatedText)
		assertEquals(0.6f, items[0].evaluatedFraction)
		assertEquals(UiText.Resource(Res.string.summary_points_evaluated, listOf("100", "100")), items[1].evaluatedText)
		assertEquals(1f, items[1].evaluatedFraction)
		// Nothing is registered for EG1114: no evaluated line at all, not "0 de 100".
		assertEquals(null, items[2].evaluatedText)
		assertEquals(0f, items[2].evaluatedFraction)
	}

	@Test
	fun theNextEvaluation_isNamedAsTheEvaluationsTabNamesIt_andSaysTodayOrItsDay_andIsOmittedWhenNone() {
		val next = SummaryNextEvaluation(
			evaluationId = "e1",
			subjectCode = "CI5437",
			type = EvaluationType.TEST,
			ordinal = 2,
			date = LocalDate(2026, 10, 26),
			isToday = false
		)
		val todays = next.copy(date = LocalDate(2026, 10, 15), isToday = true)
		val name = UiText.Resource(
			Res.string.summary_evaluation_name,
			listOf(UiText.Resource(Res.string.summary_evaluation_type_test), 2)
		)

		val items = term(
			week = 3,
			subjects = listOf(
				subject("CI5311", points = null, nextEvaluation = next),
				subject("CI5437", points = null, nextEvaluation = todays),
				subject("EG1114", points = null)
			)
		).toSummaryTermItem().subjects

		assertEquals(
			UiText.Resource(
				Res.string.summary_term_next_evaluation,
				listOf(name, UiText.Resource(Res.string.summary_day_date, listOf(DayOfWeek.MONDAY.toShortNameText(), 26)))
			),
			items[0].nextText
		)
		assertEquals(
			UiText.Resource(
				Res.string.summary_term_next_evaluation,
				listOf(name, UiText.Resource(Res.string.summary_term_next_today))
			),
			items[1].nextText
		)
		assertNull(items[2].nextText)
	}

	/* The career */

	@Test
	fun theCareer_isTheCreditsApprovedOutOfTheTotal_withItsShare() {
		val item = assertIs<SummaryCareerItem.Progress>(
			PensumCreditsProgress(approvedCredits = 156, totalCredits = 240).toSummaryCareerItem()
		)

		assertEquals("156", item.valueText)
		assertEquals(UiText.Resource(Res.string.summary_career_credits_rest, listOf(240)), item.restText)
		assertEquals(UiText.Resource(Res.string.summary_career_percent, listOf(65)), item.percentText)
		assertEquals(0.65f, item.fraction)
	}

	@Test
	fun theShare_isRounded_andHeldBetweenNothingAndAll() {
		val third = PensumCreditsProgress(approvedCredits = 80, totalCredits = 240).toSummaryCareerItem()
		val none = PensumCreditsProgress(approvedCredits = 0, totalCredits = 240).toSummaryCareerItem()
		val over = PensumCreditsProgress(approvedCredits = 250, totalCredits = 240).toSummaryCareerItem()

		assertEquals(
			UiText.Resource(Res.string.summary_career_percent, listOf(33)),
			(third as SummaryCareerItem.Progress).percentText
		)
		assertEquals(0f, (none as SummaryCareerItem.Progress).fraction)
		assertEquals(1f, (over as SummaryCareerItem.Progress).fraction)
		assertEquals("240", over.valueText)
	}

	@Test
	fun withNoPensumInTheCache_orOneWithNoCredits_theCareerInvitesToOpenIt() {
		assertEquals(SummaryCareerItem.Invite, (null as PensumCreditsProgress?).toSummaryCareerItem())
		assertEquals(
			SummaryCareerItem.Invite,
			PensumCreditsProgress(approvedCredits = 0, totalCredits = 0).toSummaryCareerItem()
		)
	}

	/* The cards that say what a section lacks */

	@Test
	fun withNoEvaluationRegistered_theWeekInvitesToAddOne_underItsClass() {
		val week = inProgress(
			classMoment = SummaryClassMoment(
				meeting = meeting,
				isInProgress = false,
				date = LocalDate(2026, 10, 16),
				isToday = false
			)
		).toSummaryWeekItem()

		assertNotNull(week.classRow)
		assertEquals(
			SummaryCardItem(
				icon = SummaryCardIcon.Evaluations,
				titleText = UiText.Resource(Res.string.summary_evaluations_invite_title),
				messageText = UiText.Resource(Res.string.summary_evaluations_invite_message),
				action = SummaryCardItem.Action(
					text = UiText.Resource(Res.string.summary_evaluations_invite_action),
					target = SummaryHubTarget.AddEvaluation
				)
			),
			week.card
		)
	}

	@Test
	fun withNothingThisWeek_andAnEvaluationNext_theCardNamesItAndLeadsToItsWeek() {
		val next = SummaryNextEvaluation(
			evaluationId = "e1",
			subjectCode = "CI5437",
			type = EvaluationType.TEST,
			ordinal = 2,
			date = LocalDate(2026, 10, 26),
			isToday = false
		)

		val card = assertNotNull(noneThisWeek(nextEvaluation = next).toSummaryWeekItem().card)

		assertEquals(SummaryCardIcon.Done, card.icon)
		assertEquals(UiText.Resource(Res.string.summary_week_idle_title), card.titleText)
		assertEquals(
			UiText.Resource(
				Res.string.summary_week_next_evaluation,
				listOf(
					UiText.Resource(
						Res.string.summary_evaluation_name,
						listOf(UiText.Resource(Res.string.summary_evaluation_type_test), 2)
					),
					"CI5437",
					UiText.Resource(Res.string.summary_day_date, listOf(DayOfWeek.MONDAY.toShortNameText(), 26))
				)
			),
			card.messageText
		)
		assertEquals(
			SummaryHubTarget.EvaluationsWeek(weekStart = LocalDate(2026, 10, 26), evaluationId = "e1"),
			card.target
		)
		assertEquals(UiText.Resource(Res.string.a11y_summary_open_evaluations_week), card.targetLabel)
	}

	@Test
	fun withNothingThisWeek_andNoEvaluationNext_theCardIsInformative() {
		val card = assertNotNull(noneThisWeek().toSummaryWeekItem().card)

		assertEquals(UiText.Resource(Res.string.summary_week_idle_title), card.titleText)
		assertNull(card.messageText)
		assertNull(card.target)
		assertNull(card.targetLabel)
	}

	@Test
	fun withTheEvaluationsNotDownloaded_theWeekClaimsNothing_andSaysWhereToFindThem() {
		val week = SummaryWeek.InProgress(
			classMoment = null,
			evaluations = emptyList(),
			hasSchedule = true,
			hasSyncedEvaluations = false,
			hasEvaluations = false,
			nextEvaluation = null
		)
		val card = assertNotNull(week.toSummaryWeekItem().card)

		assertEquals(
			SummaryCardItem(
				icon = SummaryCardIcon.Evaluations,
				titleText = UiText.Resource(Res.string.summary_evaluations_pending_title),
				messageText = UiText.Resource(Res.string.summary_evaluations_pending_message)
			),
			card
		)
	}

	@Test
	fun withAClassOfItsOwn_theWeekNeedsNoCardWhileTheEvaluationsAreNotDownloaded() {
		val moment = SummaryClassMoment(
			meeting = meeting,
			isInProgress = false,
			date = LocalDate(2026, 10, 16),
			isToday = false
		)
		val week = SummaryWeek.InProgress(
			classMoment = moment,
			evaluations = emptyList(),
			hasSchedule = true,
			hasSyncedEvaluations = false,
			hasEvaluations = false,
			nextEvaluation = null
		)

		assertNull(week.toSummaryWeekItem().card)
	}

	@Test
	fun withNoTermInProgress_theWeekSaysNoClassWithTerms_andWhenTheFirstWillShowAsANewStudent() {
		val withTerms = SummaryWeek.Idle(hasTerms = true).toSummaryWeekItem()
		val newStudent = SummaryWeek.Idle(hasTerms = false).toSummaryWeekItem()

		assertNull(withTerms.classRow)
		assertTrue(withTerms.evaluations.isEmpty())
		assertFalse(withTerms.hasSchedule)
		assertEquals(
			SummaryCardItem(
				icon = SummaryCardIcon.Calendar,
				titleText = UiText.Resource(Res.string.summary_week_no_class_title),
				messageText = UiText.Resource(Res.string.summary_week_no_class_message)
			),
			withTerms.card
		)
		assertEquals(
			SummaryCardItem(
				icon = SummaryCardIcon.Calendar,
				titleText = UiText.Resource(Res.string.summary_week_no_term_title),
				messageText = UiText.Resource(Res.string.summary_week_no_term_message)
			),
			newStudent.card
		)
	}

	@Test
	fun withNoTermInProgress_theTermLinksToRecord_andInvitesToPlanTheNextOne() {
		val item = SummaryTerm.Idle(hasTerms = true, finished = null).toSummaryTermItem()

		assertEquals(UiText.Resource(Res.string.summary_link_record), item.linkText)
		assertNull(item.week)
		assertNull(item.finished)
		assertEquals(
			SummaryCardItem(
				icon = SummaryCardIcon.NewTerm,
				titleText = UiText.Resource(Res.string.summary_term_plan_title),
				messageText = UiText.Resource(Res.string.summary_term_plan_message),
				action = SummaryCardItem.Action(
					text = UiText.Resource(Res.string.summary_term_plan_action),
					target = SummaryHubTarget.CreateTerm
				)
			),
			item.card
		)
	}

	@Test
	fun withNoTerm_aNewStudent_theTermHasNoLink_andTheFirstOneToCome() {
		val item = SummaryTerm.Idle(hasTerms = false, finished = null).toSummaryTermItem()

		assertNull(item.linkText)
		assertEquals(
			SummaryCardItem(
				icon = SummaryCardIcon.NewTerm,
				titleText = UiText.Resource(Res.string.summary_term_first_title),
				messageText = UiText.Resource(Res.string.summary_term_first_message)
			),
			item.card
		)
	}

	@Test
	fun theFinishedTerm_isItsNameAsAFigure_itsIndexWithFourDecimals_itsApprovedCredits_andItsKey() {
		val finished = SummaryTerm.Idle(
			hasTerms = true,
			finished = SummaryFinishedTerm(
				termKey = "2026-SEP_DEC",
				periodCode = AcademicTermPeriod.SEP_DEC,
				periodYear = 2026,
				index = 14.5,
				approvedCredits = 16,
				isShareable = true
			)
		).toSummaryTermItem().finished

		assertEquals(UiText.Raw("Sep - Dic 2026"), finished?.nameText)
		assertEquals(UiText.Resource(Res.string.summary_term_finished_state), finished?.stateText)
		assertEquals(UiText.Resource(Res.string.summary_term_index_label), finished?.indexLabel)
		assertEquals("14.5000", finished?.indexText)
		assertEquals(UiText.Resource(Res.string.summary_term_credits_label), finished?.creditsLabel)
		assertEquals(UiText.Resource(Res.string.summary_term_credits_value, listOf(16)), finished?.creditsText)
		assertEquals("2026-SEP_DEC", finished?.termKey)
		assertEquals(true, finished?.isShareable)
	}

	@Test
	fun theTermInProgress_linksToItsName_andTheFinishedTermIsNotThere() {
		val item = term(week = 3).toSummaryTermItem()

		assertEquals(UiText.Raw("Sep - Dic 2026"), item.linkText)
		assertNull(item.finished)
		assertNull(item.card)
	}
}
