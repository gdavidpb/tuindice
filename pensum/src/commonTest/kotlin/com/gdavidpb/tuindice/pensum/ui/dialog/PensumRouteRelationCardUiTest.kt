package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.pensum.testing.sampleApprovedPensumNode
import com.gdavidpb.tuindice.pensum.testing.toSampleRelationItem
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class PensumRouteRelationCardUiTest {
	@Test
	fun when_relationIsRendered_then_cardExposesCodeNameAndOpenAction() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumRouteRelationCard(
				item = sampleApprovedPensumNode().toSampleRelationItem(),
				testTag = CARD_TAG,
				onClick = {}
			)
		}

		assertNodeVisible(CARD_TAG)
		onNodeWithTag(CARD_TAG)
			.assert(hasText("EE1111"))
			.assert(hasText("Electiva General"))
			.assertContentDescriptionEquals("Abrir EE1111")
			.assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
	}

	@Test
	fun when_cardIsClicked_then_invokesOnClickOncePerTap() = runTuIndiceUiTest {
		var clickCount = 0

		setTuIndiceTestContent {
			PensumRouteRelationCard(
				item = sampleApprovedPensumNode().toSampleRelationItem(),
				testTag = CARD_TAG,
				onClick = { clickCount += 1 }
			)
		}

		onNodeWithTag(CARD_TAG).assertHasClickAction().performClick()
		runOnIdle { assertEquals(1, clickCount) }
		onNodeWithTag(CARD_TAG).performClick()
		runOnIdle { assertEquals(2, clickCount) }
	}
}

private const val CARD_TAG = "pensum_route_relation_card"
