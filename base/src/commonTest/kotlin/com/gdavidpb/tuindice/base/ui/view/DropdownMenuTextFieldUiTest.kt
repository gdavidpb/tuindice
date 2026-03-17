package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.mutableStateOf
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class DropdownMenuTextFieldUiTest {
	@Test
	fun when_itemSelected_then_updatesSelectionAndInvokesCallback() = runTuIndiceUiTest {
		val itemA = TestDropdownItem("Opcion A")
		val itemB = TestDropdownItem("Opcion B")
		var selectedItem: TestDropdownItem? = null

		setTuIndiceTestContent {
			DropdownMenuTextField(
				items = listOf(itemA, itemB),
				selectedItem = itemA,
				onItemSelected = { item -> selectedItem = item }
			)
		}

		assertNodeVisible(BaseUiTags.DropdownMenuTextField)
		onNodeWithTag(BaseUiTags.DropdownMenuTextField).performClick()
		onNodeWithText("Opcion B").performClick()

		assertEquals(itemB, selectedItem)
		onNodeWithText("Opcion B").assertIsDisplayed()
	}

	@Test
	fun when_errorProvided_then_displaysSupportingErrorText() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			DropdownMenuTextField(
				items = listOf(TestDropdownItem("Opcion A")),
				onItemSelected = {},
				error = "Campo requerido"
			)
		}

		assertNodeVisible(BaseUiTags.DropdownMenuTextField)
		assertNodeVisible(
			tag = BaseUiTags.DropdownMenuError,
			useUnmergedTree = true
		)
	}

	@Test
	fun when_selectedItemChangesExternally_then_updatesDisplayedValue() = runTuIndiceUiTest {
		val itemA = TestDropdownItem("Opcion A")
		val itemB = TestDropdownItem("Opcion B")
		val selectedItem = mutableStateOf<DropdownMenuItem?>(itemA)

		setTuIndiceTestContent {
			DropdownMenuTextField(
				items = listOf(itemA, itemB),
				selectedItem = selectedItem.value as TestDropdownItem?,
				onItemSelected = { item -> selectedItem.value = item }
			)
		}

		onNodeWithTag(BaseUiTags.DropdownMenuTextField).assertTextContains("Opcion A")

		runOnIdle {
			selectedItem.value = itemB
		}

		onNodeWithTag(BaseUiTags.DropdownMenuTextField).assertTextContains("Opcion B")
	}

	private data class TestDropdownItem(
		override val text: String
	) : DropdownMenuItem
}
