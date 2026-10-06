package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PensumSubjectDetailMetaUiTest {
	@Test
	fun when_labelAndValueAreProvided_then_showsBothWithoutTaggingTheValue() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumSubjectDetailMeta(label = "Unidades crédito", value = "4 UC")
		}

		onNodeWithText("Unidades crédito").assertExists()
		onNodeWithText("4 UC").assertExists()
		onAllNodesWithTag(VALUE_TAG).assertCountEquals(0)
	}

	@Test
	fun when_valueTagIsProvided_then_tagPointsAtTheValueNotTheLabel() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumSubjectDetailMeta(
				modifier = Modifier.width(160.dp),
				label = "Trimestre",
				value = "1° trimestre",
				valueTag = VALUE_TAG
			)
		}

		onNodeWithTag(VALUE_TAG).assertTextEquals("1° trimestre")
		onNodeWithText("Trimestre").assertExists()
	}
}

private const val VALUE_TAG = "pensum_subject_detail_meta_value"
