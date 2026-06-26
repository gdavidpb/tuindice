package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubjectAvailability
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubjectAvailabilityDetail
import com.gdavidpb.tuindice.record.presentation.mapper.toCreateTermSubjectItem
import com.gdavidpb.tuindice.record.presentation.model.CreateTermSubjectItem
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class CreateTermSubjectStatusRowViewUiTest {
	@Test
	fun when_approvedStatusClicked_then_showsTermTooltip() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateTermSubjectStatusRow(
				subject = subject(
					availability = SyntheticTermSubjectAvailability.APPROVED,
					detail = SyntheticTermSubjectAvailabilityDetail(
						termLabel = "Ene - Mar 2025"
					)
				),
				availableText = "Disponible",
				availableIcon = CreateTermSubjectStatusIcon.Dot
			)
		}

		onNodeWithText("Aprobada").performClick()

		onNodeWithText("Cursada en Ene - Mar 2025").assertIsDisplayed()
	}

	@Test
	fun when_currentStatusClicked_then_showsTermTooltip() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateTermSubjectStatusRow(
				subject = subject(
					availability = SyntheticTermSubjectAvailability.CURRENT,
					detail = SyntheticTermSubjectAvailabilityDetail(
						termLabel = "Abr - Jul 2026"
					)
				),
				availableText = "Disponible",
				availableIcon = CreateTermSubjectStatusIcon.Dot
			)
		}

		onNodeWithText("En curso").performClick()

		onNodeWithText("En curso en Abr - Jul 2026").assertIsDisplayed()
	}

	@Test
	fun when_alreadyPlannedStatusClicked_then_showsTermTooltip() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateTermSubjectStatusRow(
				subject = subject(
					availability = SyntheticTermSubjectAvailability.ALREADY_PLANNED,
					detail = SyntheticTermSubjectAvailabilityDetail(
						termLabel = "Sep - Dic 2026"
					)
				),
				availableText = "Disponible",
				availableIcon = CreateTermSubjectStatusIcon.Dot
			)
		}

		onNodeWithText("Ya planificada").performClick()

		onNodeWithText("Planificada en Sep - Dic 2026").assertIsDisplayed()
	}

	@Test
	fun when_blockedStatusClicked_then_showsMissingRequirementCodesTooltip() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateTermSubjectStatusRow(
				subject = subject(
					availability = SyntheticTermSubjectAvailability.BLOCKED,
					detail = SyntheticTermSubjectAvailabilityDetail(
						missingSubjectCodes = listOf("EP1308", "EP5855")
					)
				),
				availableText = "Disponible",
				availableIcon = CreateTermSubjectStatusIcon.Dot
			)
		}

		onNodeWithText("Bloqueada").performClick()

		onNodeWithText("Faltan requisitos: EP1308, EP5855").assertIsDisplayed()
	}

	@Test
	fun when_availableStatusRendered_then_usesPensumLanguage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateTermSubjectStatusRow(
				subject = subject(
					availability = SyntheticTermSubjectAvailability.AVAILABLE,
					detail = SyntheticTermSubjectAvailabilityDetail()
				),
				availableText = "Disponible",
				availableIcon = CreateTermSubjectStatusIcon.Available
			)
		}

		onNodeWithText("Disponible").assertIsDisplayed()
	}

	private fun subject(
		availability: SyntheticTermSubjectAvailability,
		detail: SyntheticTermSubjectAvailabilityDetail
	): CreateTermSubjectItem {
		return SyntheticTermSubject(
			subjectCode = "EP2308",
			name = "Proyecto de Grado II",
			credits = 3,
			availability = availability,
			availabilityDetail = detail
		).toCreateTermSubjectItem()
	}
}
