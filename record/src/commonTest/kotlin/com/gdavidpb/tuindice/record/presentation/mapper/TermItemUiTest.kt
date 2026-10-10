package com.gdavidpb.tuindice.record.presentation.mapper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.academiccore.domain.model.TermProjection
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.model.RecordNotice
import com.gdavidpb.tuindice.record.presentation.model.RecordNoticeKind
import com.gdavidpb.tuindice.record.presentation.model.TermItem
import com.gdavidpb.tuindice.record.presentation.model.TermItemKind
import com.gdavidpb.tuindice.record.testing.recordMapperTexts
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// The header texts, the deltas and the edit flags are covered by TermItemMappingUiTest; these are
// the highlight of a value, which page carries the notice, and what a closed term lets be edited.
@OptIn(ExperimentalTestApi::class)
class TermItemUiTest {
	@Test
	fun when_aValueIsAnnotated_then_onlyItsSymbolIsHighlighted_inBold() = runTuIndiceUiTest {
		var annotated: AnnotatedString? = null

		setTuIndiceTestContent {
			annotated = "∑x 3.5000".annotatedTermValue(highlightColor = Color.Red)
		}

		val value = assertNotNull(annotated)
		val span = value.spanStyles.single()

		assertEquals("∑x 3.5000", value.text)
		// The symbol that says which metric it is stands out; the number keeps the text's own style.
		assertEquals("∑x", value.text.substring(span.start, span.end))
		assertEquals(Color.Red, span.item.color)
		assertEquals(FontWeight.Bold, span.item.fontWeight)
	}

	@Test
	fun when_aTermIsMapped_then_itsThreeValuesCarryTheHighlightTheyWereGiven() = runTuIndiceUiTest {
		var item: TermItem? = null

		setTuIndiceTestContent {
			item = termProjection().toTermItem(
				viewMode = RecordViewMode.Projection,
				texts = recordMapperTexts(),
				highlightColor = Color.Magenta
			)
		}

		val mapped = assertNotNull(item)

		listOf(mapped.gradeText, mapped.gradeSumText, mapped.creditsText).forEach { value ->
			assertEquals(Color.Magenta, value.spanStyles.single().item.color, "the highlight of \"${value.text}\"")
		}
		assertEquals("Sep - Dic 2026", mapped.shortNameText)
		assertNull(mapped.notice)
	}

	@Test
	fun when_theNoticeSpeaksOfTheCurrentTerm_then_onlyTheCurrentTermsPageCarriesIt() = runTuIndiceUiTest {
		var items: List<TermItem>? = null
		val notice = annulment(RecordNoticeKind.AnnulledProvisional)

		setTuIndiceTestContent {
			items = listOf(
				termProjection(id = "current", kind = TermKind.CURRENT),
				termProjection(id = "closed", kind = TermKind.HISTORICAL, periodYear = 2025)
			).toTermItemList(
				viewMode = RecordViewMode.Projection,
				texts = recordMapperTexts(),
				highlightColor = Color.Red,
				notice = notice
			)
		}

		val mapped = assertNotNull(items).associateBy(TermItem::termId)

		assertEquals(notice, mapped.getValue("current").notice)
		assertNull(mapped.getValue("closed").notice, "a closed term has nothing to be told about this enrollment")
	}

	@Test
	fun when_theAnnulmentIsFinal_then_noPageCarriesIt_notEvenTheCurrentOne() = runTuIndiceUiTest {
		var item: TermItem? = null

		setTuIndiceTestContent {
			item = termProjection(kind = TermKind.CURRENT).toTermItem(
				viewMode = RecordViewMode.Projection,
				texts = recordMapperTexts(),
				highlightColor = Color.Red,
				notice = annulment(RecordNoticeKind.AnnulledFinal)
			)
		}

		// A final annulment is said above the pager, for every page at once.
		assertNull(assertNotNull(item).notice)
	}

	@Test
	fun when_aClosedTermIsMappedInProjection_then_itsSubjectsStayReadOnly_andItCannotBeEdited() =
		runTuIndiceUiTest {
			var item: TermItem? = null

			setTuIndiceTestContent {
				item = termProjection(kind = TermKind.HISTORICAL).toTermItem(
					viewMode = RecordViewMode.Projection,
					texts = recordMapperTexts(),
					highlightColor = Color.Red
				)
			}

			val mapped = assertNotNull(item)

			assertEquals(TermItemKind.HISTORICAL, mapped.kind)
			assertFalse(mapped.isCurrent)
			assertFalse(mapped.canEdit)
			assertFalse(mapped.canDelete)
			assertTrue(mapped.attempts.single().isReadOnly, "what is closed is not simulated, whatever the mode")
		}

	@Test
	fun when_aSimulatedTermIsSeenInHistorical_then_itCanStillBeDeleted_butNotEdited() = runTuIndiceUiTest {
		var item: TermItem? = null

		setTuIndiceTestContent {
			item = termProjection(kind = TermKind.SYNTHETIC).toTermItem(
				viewMode = RecordViewMode.Historical,
				texts = recordMapperTexts(),
				highlightColor = Color.Red
			)
		}

		val mapped = assertNotNull(item)

		assertEquals(TermItemKind.SYNTHETIC, mapped.kind)
		assertTrue(mapped.canDelete)
		assertFalse(mapped.canEdit)
		assertTrue(mapped.attempts.single().isReadOnly)
	}

	private fun annulment(kind: RecordNoticeKind): RecordNotice = recordNotice(
		title = UiText.Raw("Tu inscripción aparece anulada"),
		message = UiText.Raw("La universidad la tiene anulada."),
		kind = kind
	)

	private fun termProjection(
		id: String = "term-1",
		kind: TermKind = TermKind.SYNTHETIC,
		periodYear: Int = 2026
	) = TermProjection(
		id = id,
		periodYear = periodYear,
		periodCode = AcademicTermPeriod.SEP_DEC,
		termKey = "$periodYear-SEP_DEC",
		termOrder = periodYear * 10 + AcademicTermPeriod.SEP_DEC.sequence,
		periodLabel = "Septiembre - Diciembre $periodYear",
		kind = kind,
		periodAverage = 4.0,
		cumulativeAverage = 3.5,
		periodCredits = 12,
		cumulativeCredits = 40,
		attempts = listOf(
			AttemptProjection(
				id = "attempt-$id",
				subjectCode = "MA1112",
				subjectName = "materia uno",
				credits = 4,
				gradingMode = AttemptGradingMode.NUMERIC,
				score = AttemptScore.numeric(4),
				outcome = AttemptOutcome.APPROVED,
				badge = AttemptBadge.NONE
			)
		)
	)
}
