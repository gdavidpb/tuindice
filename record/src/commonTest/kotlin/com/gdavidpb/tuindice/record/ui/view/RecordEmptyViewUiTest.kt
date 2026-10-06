package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class RecordEmptyViewUiTest {
	@Test
	fun when_thereAreNoTermsYet_then_theIllustrationHeadsTheTitleAndItsMessage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecordEmptyView(
				title = "Informe académico",
				message = "Cuando tengas periodos académicos disponibles, los verás aquí."
			)
		}

		assertNodeVisible(RecordUiTags.EmptyContainer)
		assertNodeVisible(RecordUiTags.EmptyIllustration)
		onNodeWithTag(RecordUiTags.EmptyTitle).assertTextEquals("Informe académico")
		onNodeWithTag(RecordUiTags.EmptyMessage)
			.assertTextEquals("Cuando tengas periodos académicos disponibles, los verás aquí.")

		val illustration = onNodeWithTag(RecordUiTags.EmptyIllustration).getUnclippedBoundsInRoot()
		val title = onNodeWithTag(RecordUiTags.EmptyTitle).getUnclippedBoundsInRoot()
		val message = onNodeWithTag(RecordUiTags.EmptyMessage).getUnclippedBoundsInRoot()

		assertTrue(illustration.bottom <= title.top, "the illustration comes first")
		assertTrue(title.bottom <= message.top, "the message explains the title under it")
	}

	@Test
	fun when_theReasonIsAFinalAnnulment_then_theViewSaysWhatItIsHanded_andOffersNothingToTap() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecordEmptyView(
				title = "Tu inscripción aparece anulada",
				message = "La universidad la tiene anulada."
			)
		}

		// The copy arrives resolved: the view has no words of its own to fall back on.
		onNodeWithTag(RecordUiTags.EmptyTitle)
			.assertTextEquals("Tu inscripción aparece anulada")
			.assertHasNoClickAction()
		onNodeWithTag(RecordUiTags.EmptyMessage).assertTextEquals("La universidad la tiene anulada.")
		// Nothing here can be retried or opened: an empty record has no action.
		onAllNodes(hasClickAction()).assertCountEquals(0)
	}
}
