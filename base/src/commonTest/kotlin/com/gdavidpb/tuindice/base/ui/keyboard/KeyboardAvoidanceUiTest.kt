package com.gdavidpb.tuindice.base.ui.keyboard

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.isNotFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class KeyboardAvoidanceUiTest {
	@Test
	fun when_backgroundIsTapped_then_fieldLosesFocus() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			KeyboardTestForm()
		}

		onNodeWithTag(FieldTag).performClick()
		onNodeWithTag(FieldTag).assert(isFocused())

		onNodeWithTag(FormTag).performTouchInput { click(Offset(2f, 2f)) }
		onNodeWithTag(FieldTag).assert(isNotFocused())
	}

	// A tap a field takes is not a tap on the background: the background must not clear the focus the field just took.
	@Test
	fun when_fieldIsTapped_then_theTapDoesNotTakeTheFocusAway() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			KeyboardTestForm()
		}

		onNodeWithTag(FieldTag).performClick()

		onNodeWithTag(FieldTag).assert(isFocused())
	}

	@Test
	fun when_theFormIsDragged_then_fieldLosesFocus() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			KeyboardTestForm()
		}

		onNodeWithTag(FieldTag).performClick()
		onNodeWithTag(FieldTag).assert(isFocused())

		onNodeWithTag(FormTag).performTouchInput { swipeUp() }

		onNodeWithTag(FieldTag).assert(isNotFocused())
	}

	// The focus is on something under the fold: the region that holds it is scrolled into view with it, so the focus
	// does not stay below the keyboard.
	@Test
	fun when_aFocusInTheRegionIsBelowTheViewport_then_theRegionIsBroughtIntoView() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			val region = remember { BringIntoViewRequester() }

			Column(
				modifier = Modifier
					.fillMaxSize()
					.verticalScroll(rememberScrollState())
			) {
				Spacer(modifier = Modifier.height(BelowTheFoldDp.dp))

				Column(
					modifier = Modifier
						.fillMaxWidth()
						.bringRegionIntoViewOnFocus(region)
				) {
					Box(
						modifier = Modifier
							.testTag(FocusableTag)
							.size(48.dp)
							.focusable()
					)
				}
			}
		}

		onNodeWithTag(FocusableTag).requestFocus()

		onNodeWithTag(FocusableTag).assertIsDisplayed()
	}
}

@Composable
private fun KeyboardTestForm() {
	val focusManager = LocalFocusManager.current
	val region = remember { BringIntoViewRequester() }
	var text by remember { mutableStateOf("") }

	BoxWithConstraints(
		modifier = Modifier
			.testTag(FormTag)
			.fillMaxSize()
			.dismissKeyboardOnTap(focusManager)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.dismissKeyboardOnScroll(focusManager)
				.verticalScroll(rememberScrollState())
				.heightIn(min = maxHeight)
		) {
			Spacer(modifier = Modifier.height(120.dp))

			Column(
				modifier = Modifier
					.fillMaxWidth()
					.bringRegionIntoViewOnFocus(region)
			) {
				OutlinedTextField(
					modifier = Modifier
						.testTag(FieldTag)
						.fillMaxWidth(),
					value = text,
					onValueChange = { text = it }
				)
			}

			Spacer(modifier = Modifier.height(1_000.dp))
		}
	}
}

private const val FormTag = "keyboard_test_form"
private const val FieldTag = "keyboard_test_field"
private const val FocusableTag = "keyboard_test_focusable"
private const val BelowTheFoldDp = 1_000
