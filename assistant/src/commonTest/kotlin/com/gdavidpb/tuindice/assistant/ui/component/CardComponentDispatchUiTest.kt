package com.gdavidpb.tuindice.assistant.ui.component

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.presentation.mapper.toChatComponentItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.view.ChatComponentView
import com.gdavidpb.tuindice.chatcontract.component.AttentionComponent
import com.gdavidpb.tuindice.chatcontract.component.AttentionItem
import com.gdavidpb.tuindice.chatcontract.component.ChatComponent
import com.gdavidpb.tuindice.chatcontract.component.ClashesComponent
import com.gdavidpb.tuindice.chatcontract.component.IndexGoalComponent
import com.gdavidpb.tuindice.chatcontract.component.IndexPoint
import com.gdavidpb.tuindice.chatcontract.component.IndexSeriesComponent
import com.gdavidpb.tuindice.chatcontract.component.NextClassComponent
import com.gdavidpb.tuindice.chatcontract.component.PaceComponent
import com.gdavidpb.tuindice.chatcontract.component.ProfileSummaryComponent
import com.gdavidpb.tuindice.chatcontract.component.RecordStatsComponent
import com.gdavidpb.tuindice.chatcontract.component.ScheduleBlock
import com.gdavidpb.tuindice.chatcontract.component.ScheduleClash
import com.gdavidpb.tuindice.chatcontract.component.ScheduleDay
import com.gdavidpb.tuindice.chatcontract.component.SimulatedGrade
import com.gdavidpb.tuindice.chatcontract.component.SimulationComponent
import com.gdavidpb.tuindice.chatcontract.component.SyncSourceRow
import com.gdavidpb.tuindice.chatcontract.component.SyncStatusComponent
import com.gdavidpb.tuindice.chatcontract.component.TermComponent
import com.gdavidpb.tuindice.chatcontract.component.TermCredits
import com.gdavidpb.tuindice.chatcontract.component.TermSubjectRow
import com.gdavidpb.tuindice.chatcontract.component.WeekScheduleComponent
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

// Each card catalog component reaches its own renderer, and no longer the text it falls back to.
@OptIn(ExperimentalTestApi::class)
class CardComponentDispatchUiTest {
	@Test
	fun when_profileSummaryIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(profileSummary(), AssistantUiTags.Profile.summary(COMPONENT_ID))
	}

	@Test
	fun when_syncStatusIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(syncStatus(), AssistantUiTags.Profile.sync(COMPONENT_ID))
	}

	@Test
	fun when_termIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(term(), AssistantUiTags.Report.term(COMPONENT_ID))
	}

	@Test
	fun when_indexSeriesIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(indexSeries(), AssistantUiTags.Report.indexSeries(COMPONENT_ID))
	}

	@Test
	fun when_recordStatsIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(recordStats(), AssistantUiTags.Report.recordStats(COMPONENT_ID))
	}

	@Test
	fun when_paceIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(pace(), AssistantUiTags.Report.pace(COMPONENT_ID))
	}

	@Test
	fun when_simulationIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(simulation(), AssistantUiTags.Report.simulation(COMPONENT_ID))
	}

	@Test
	fun when_indexGoalIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(indexGoal(), AssistantUiTags.Report.indexGoal(COMPONENT_ID))
	}

	@Test
	fun when_attentionIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(attention(), AssistantUiTags.Summary.attention(COMPONENT_ID))
	}

	@Test
	fun when_weekScheduleIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(weekSchedule(), AssistantUiTags.Schedule.week(COMPONENT_ID))
	}

	@Test
	fun when_nextClassIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(nextClass(), AssistantUiTags.Schedule.nextClass(COMPONENT_ID))
	}

	@Test
	fun when_clashesAreShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(clashes(), AssistantUiTags.Schedule.clashes(COMPONENT_ID))
	}

	private fun ComposeUiTest.assertDispatchedTo(component: ChatComponent, tag: String) {
		setTuIndiceTestContent {
			ChatComponentView(messageId = MESSAGE_ID, item = component.toChatComponentItem(nowMillis = 0L), onAction = {})
		}

		assertNodeVisible(tag)
		onNodeWithText(FALLBACK_TEXT).assertDoesNotExist()
	}

	private fun profileSummary() = ProfileSummaryComponent(
		id = COMPONENT_ID,
		fallbackText = FALLBACK_TEXT,
		careerCode = "IS",
		careerName = "Ingeniería de Sistemas",
		pensumLabel = "2016",
		cumulativeIndex = 16.4,
		approvedCredits = 80,
		totalCredits = 180,
		currentTermLabel = "2026-JAN_MAR"
	)

	private fun syncStatus() = SyncStatusComponent(
		id = COMPONENT_ID,
		fallbackText = FALLBACK_TEXT,
		lastSyncAtMillis = null,
		sources = listOf(SyncSourceRow("Notas", "success")),
		enrollmentStatus = null,
		annulmentCause = null
	)

	private fun term() = TermComponent(
		id = COMPONENT_ID,
		fallbackText = FALLBACK_TEXT,
		termId = "2026-JAN_MAR",
		termLabel = "2026 enero-marzo",
		termIndex = 16.2,
		cumulativeIndex = 15.8,
		credits = 14,
		rows = listOf(TermSubjectRow("attempt-1", "IS-101", "Programación I", 6, grade = 18))
	)

	private fun indexSeries() = IndexSeriesComponent(
		id = COMPONENT_ID,
		fallbackText = FALLBACK_TEXT,
		points = listOf(IndexPoint("2026-JAN_MAR", "2026 enero-marzo", 16.2, 15.8))
	)

	private fun recordStats() = RecordStatsComponent(
		id = COMPONENT_ID,
		fallbackText = FALLBACK_TEXT,
		approved = 44,
		failed = 2,
		retired = 1,
		approvedFirstTry = 40,
		repeated = 3,
		histogram = listOf(0, 2, 10, 20, 12)
	)

	private fun pace() = PaceComponent(
		id = COMPONENT_ID,
		fallbackText = FALLBACK_TEXT,
		creditsPerTerm = listOf(TermCredits("2026 enero-marzo", 14)),
		averageCredits = 14.0,
		remainingTermsEstimate = 4
	)

	private fun simulation() = SimulationComponent(
		id = COMPONENT_ID,
		fallbackText = FALLBACK_TEXT,
		termId = "2026-JAN_MAR",
		termIndex = 17.0,
		cumulativeIndex = 16.0,
		changes = listOf(SimulatedGrade("attempt-1", "IS-101", grade = 20, outcome = "approved"))
	)

	private fun indexGoal() = IndexGoalComponent(
		id = COMPONENT_ID,
		fallbackText = FALLBACK_TEXT,
		targetCumulative = 17.0,
		credits = 60,
		requiredTermIndex = 18.2,
		feasible = true
	)

	private fun attention() = AttentionComponent(
		id = COMPONENT_ID,
		fallbackText = FALLBACK_TEXT,
		items = listOf(
			AttentionItem(
				kind = "evaluation_unscored",
				title = "Parcial 2 sin nota",
				detail = "CI5437",
				entityRef = EntityRef(EntityKind.EVALUATION, "eval-1")
			)
		)
	)

	private fun weekSchedule() = WeekScheduleComponent(
		id = COMPONENT_ID,
		fallbackText = FALLBACK_TEXT,
		days = listOf(ScheduleDay(1, listOf(ScheduleBlock("IS-101", "Programación I", 450, 570, 1, 2))))
	)

	private fun nextClass() = NextClassComponent(
		id = COMPONENT_ID,
		fallbackText = FALLBACK_TEXT,
		subjectCode = "IS-101",
		subjectName = "Programación I",
		dayOfWeek = 2,
		startMinute = 450,
			endMinute = 570,
			startBlock = 1,
			endBlock = 2,
		room = null,
		inProgress = false,
		dateMillis = DATE_MILLIS
	)

	private fun clashes() = ClashesComponent(
		id = COMPONENT_ID,
		fallbackText = FALLBACK_TEXT,
		clashes = listOf(ScheduleClash(3, 570, 630, 3, 3, listOf("IS-101", "IS-102")))
	)

	private companion object {
		const val COMPONENT_ID = "card-1"
		const val MESSAGE_ID = "message-1"
		const val FALLBACK_TEXT = "Respaldo de prueba"
		const val DATE_MILLIS = 1_760_000_000_000L
	}
}
