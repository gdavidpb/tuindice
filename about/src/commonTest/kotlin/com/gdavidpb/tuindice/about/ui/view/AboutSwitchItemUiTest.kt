package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class AboutSwitchItemUiTest {
	@Test
	fun when_uncheckedItemTapped_then_requestsCheckedValue() = runTuIndiceUiTest {
		val changes = mutableListOf<Boolean>()

		setTuIndiceTestContent {
			AboutSwitchItem(
				icon = ColorPainter(Color.Red),
				text = "Datos de uso\nComparte datos anonimos",
				checked = false,
				onCheckedChange = { checked -> changes += checked },
				testTag = AboutUiTags.UsageDataConsentToggle
			)
		}

		assertNodeVisible(AboutUiTags.UsageDataConsentToggle)
		onNodeWithTag(AboutUiTags.UsageDataConsentToggle)
			.assertIsOff()
			.performClick()

		assertEquals(listOf(true), changes)
	}

	@Test
	fun when_checkedItemTapped_then_requestsUncheckedValue() = runTuIndiceUiTest {
		val changes = mutableListOf<Boolean>()

		setTuIndiceTestContent {
			AboutSwitchItem(
				icon = ColorPainter(Color.Red),
				text = "Datos de uso\nComparte datos anonimos",
				checked = true,
				onCheckedChange = { checked -> changes += checked },
				testTag = AboutUiTags.UsageDataConsentToggle
			)
		}

		onNodeWithTag(AboutUiTags.UsageDataConsentToggle)
			.assertIsOn()
			.performClick()

		assertEquals(listOf(false), changes)
	}

	@Test
	fun when_checkedStateChanges_then_rowReflectsNewToggleState() = runTuIndiceUiTest {
		val checked = mutableStateOf(false)

		setTuIndiceTestContent {
			AboutSwitchItem(
				icon = ColorPainter(Color.Red),
				text = "Datos de uso",
				checked = checked.value,
				onCheckedChange = { value -> checked.value = value },
				testTag = AboutUiTags.UsageDataConsentToggle
			)
		}

		onNodeWithTag(AboutUiTags.UsageDataConsentToggle).assertIsOff()
		onNodeWithTag(AboutUiTags.UsageDataConsentToggle).performClick()
		onNodeWithTag(AboutUiTags.UsageDataConsentToggle).assertIsOn()
	}

	@Test
	fun when_itemRendered_then_exposesSingleSwitchRowWithItsText() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AboutSwitchItem(
				icon = ColorPainter(Color.Red),
				text = "Datos de uso\nComparte datos anonimos",
				checked = true,
				onCheckedChange = {}
			)
		}

		onNodeWithTag(AboutUiTags.ItemContainer)
			.assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))
			.assertTextEquals("Datos de uso\nComparte datos anonimos")
	}
}
