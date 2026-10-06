package com.gdavidpb.tuindice.record.presentation.mapper

import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicScheduleEntry
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.record.presentation.model.AttemptItem
import com.gdavidpb.tuindice.record.testing.recordMapperTexts
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// The grade texts and the outcome are covered by AttemptItemMappingUiTest; these are the fields
// the enrollment adds to the card: where the subject meets, what the university flagged, and
// whether it was withdrawn.
@OptIn(ExperimentalTestApi::class)
class AttemptItemUiTest {
	@Test
	fun when_everyMeetingSharesTheRoom_then_theDetailNamesSectionAndClassroom() = runTuIndiceUiTest {
		var item: AttemptItem? = null

		setTuIndiceTestContent {
			item = attemptProjection(
				section = 1,
				schedule = listOf(meeting(day = 2, classroom = "MYS-116"), meeting(day = 4, classroom = " MYS-116 "))
			).toAttemptItem(isReadOnly = false, texts = recordMapperTexts())
		}

		val mapped = assertNotNull(item)

		assertEquals("Sección 1 · MYS-116", mapped.detailText)
		assertNull(mapped.enrollmentErrorText)
		assertFalse(mapped.isWithdrawn)
		assertFalse(mapped.isReadOnly)
	}

	@Test
	fun when_theMeetingsUseSeveralRooms_then_theDetailKeepsOnlyTheSection() = runTuIndiceUiTest {
		var item: AttemptItem? = null

		setTuIndiceTestContent {
			item = attemptProjection(
				section = 3,
				schedule = listOf(meeting(day = 2, classroom = "MYS-116"), meeting(day = 4, classroom = "ENE-201"))
			).toAttemptItem(isReadOnly = false, texts = recordMapperTexts())
		}

		assertEquals("Sección 3", assertNotNull(item).detailText)
	}

	@Test
	fun when_theSubjectHasNeitherSectionNorSchedule_then_thereIsNoDetailToShow() = runTuIndiceUiTest {
		var item: AttemptItem? = null

		setTuIndiceTestContent {
			item = attemptProjection().toAttemptItem(isReadOnly = false, texts = recordMapperTexts())
		}

		val mapped = assertNotNull(item)

		assertNull(mapped.detailText)
		assertNull(mapped.enrollmentErrorText)
	}

	@Test
	fun when_theUniversityFlaggedSeveralErrors_then_theFirstIsShownInSentenceCase_withTheRestCounted() =
		runTuIndiceUiTest {
			var item: AttemptItem? = null

			setTuIndiceTestContent {
				item = attemptProjection(
					errors = listOf("CHOQUE DE HORARIO CON CI5311", " ", "EXCEDE EL LIMITE DE CREDITOS")
				).toAttemptItem(isReadOnly = false, texts = recordMapperTexts())
			}

			// The subject code keeps its capitals; the blank entry is not an error to count.
			assertEquals("Choque de horario con CI5311 · +1", assertNotNull(item).enrollmentErrorText)
		}

	@Test
	fun when_theSubjectWasWithdrawn_then_itIsReadOnly_evenInATermThatCanBeEdited() = runTuIndiceUiTest {
		var item: AttemptItem? = null

		setTuIndiceTestContent {
			item = attemptProjection(withdrawn = true).toAttemptItem(isReadOnly = false, texts = recordMapperTexts())
		}

		val mapped = assertNotNull(item)

		assertTrue(mapped.isWithdrawn)
		assertTrue(mapped.isReadOnly, "a withdrawn subject has no grade left to simulate")
	}

	@Test
	fun when_twoSubjectsAreMapped_then_eachTakesTheColorsOfItsOwnCode_andItsNameInCapitals() = runTuIndiceUiTest {
		var first: AttemptItem? = null
		var second: AttemptItem? = null

		setTuIndiceTestContent {
			first = attemptProjection(code = "CI5311").toAttemptItem(isReadOnly = true, texts = recordMapperTexts())
			second = attemptProjection(code = "FS2211").toAttemptItem(isReadOnly = true, texts = recordMapperTexts())
		}

		val ci = assertNotNull(first)
		val fs = assertNotNull(second)

		assertEquals(CourseCodeColorGenerator.fromCode("CI5311").color, ci.codeColor)
		assertEquals(CourseCodeColorGenerator.fromCode("FS2211").containerColor, fs.codeContainerColor)
		assertNotEquals(ci.codeColor, fs.codeColor)
		assertEquals("INGENIERÍA DE SOFTWARE", ci.nameText)
		assertEquals("3 UC", ci.creditsText)
	}

	private fun meeting(day: Int, classroom: String) = AcademicScheduleEntry(
		dayOfWeek = day,
		startBlock = 1,
		endBlock = 2,
		classroom = classroom
	)

	private fun attemptProjection(
		code: String = "CI5311",
		section: Int? = null,
		schedule: List<AcademicScheduleEntry>? = null,
		errors: List<String>? = null,
		withdrawn: Boolean = false
	) = AttemptProjection(
		id = "attempt-1",
		subjectCode = code,
		subjectName = "Ingeniería de software",
		credits = 3,
		gradingMode = AttemptGradingMode.NUMERIC,
		score = AttemptScore.empty(),
		outcome = AttemptOutcome.PENDING,
		badge = AttemptBadge.NONE,
		section = section,
		schedule = schedule,
		enrollmentErrors = errors,
		withdrawn = withdrawn
	)
}
