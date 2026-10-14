package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.mapper.toShareableTermCardItem
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermTermItem
import com.gdavidpb.tuindice.summary.testing.statisticsTerm
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ShareTermTermChipsUiTest {
	@Test
	fun when_aTermIsTapped_then_itIsReportedToTheSheet() = runTuIndiceUiTest {
		val tapped = mutableListOf<String>()

		setTuIndiceTestContent {
			ShareTermTermChips(
				terms = terms(),
				onTermClick = { termKey -> tapped += termKey }
			)
		}

		onNodeWithTag(ShareTermUiTags.term(OLDER_KEY)).performClick()
		waitForIdle()

		assertEquals(listOf(OLDER_KEY), tapped)
	}

	@Test
	fun when_theTermsAreShown_then_eachOfThemHasItsChip() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareTermTermChips(
				terms = terms(),
				onTermClick = {}
			)
		}

		onAllNodesWithTag(ShareTermUiTags.term(NEWER_KEY)).assertCountEquals(1)
		onAllNodesWithTag(ShareTermUiTags.term(OLDER_KEY)).assertCountEquals(1)
	}

	private fun terms() = listOf(
		ShareTermTermItem(
			key = NEWER_KEY,
			label = UiText.Raw("Ene - Mar 2025"),
			card = cardOf(year = 2025),
			selected = true
		),
		ShareTermTermItem(
			key = OLDER_KEY,
			label = UiText.Raw("Ene - Mar 2024"),
			card = cardOf(year = 2024),
			selected = false
		)
	)

	private fun cardOf(year: Int) = checkNotNull(
		statisticsTerm(year = year, periodCode = AcademicTermPeriod.JAN_MAR, index = 4.0)
			.toShareableTermCardItem(careerName = "Ingeniería de Computación")
	)

	private companion object {
		const val NEWER_KEY = "closed-2025-JAN_MAR"
		const val OLDER_KEY = "closed-2024-JAN_MAR"
	}
}
