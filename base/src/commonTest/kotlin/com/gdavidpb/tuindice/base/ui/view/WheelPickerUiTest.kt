package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class WheelPickerUiTest {
	@Test
	fun when_itemTapped_then_dispatchesPickedIndex() = runTuIndiceUiTest {
		var pickedIndex = -1

		setTuIndiceTestContent {
			WheelPicker(
				count = 5,
				itemHeight = 32.dp,
				additionalItemCount = 1,
				onItemPicked = { index -> pickedIndex = index }
			) { index ->
				Text("Item $index")
			}
		}

		assertNodeVisible(BaseUiTags.WheelPickerList)
		assertNodeVisible(BaseUiTags.wheelPickerItem(2))

		onNodeWithTag(BaseUiTags.wheelPickerItem(2)).performClick()
		waitForIdle()

		assertEquals(2, pickedIndex)
	}

	@Test
	fun when_pickerIsDisabled_then_itemTapDoesNotDispatchPickedIndex() = runTuIndiceUiTest {
		var pickedIndex = -1

		setTuIndiceTestContent {
			WheelPicker(
				count = 5,
				enabled = false,
				itemHeight = 32.dp,
				additionalItemCount = 1,
				onItemPicked = { index -> pickedIndex = index }
			) { index ->
				Text("Item $index")
			}
		}

		assertNodeVisible(BaseUiTags.WheelPickerList)
		onNodeWithText("Item 2").assertIsDisplayed()

		onNodeWithTag(BaseUiTags.wheelPickerItem(2)).performClick()
		waitForIdle()

		assertEquals(-1, pickedIndex)
	}
}
