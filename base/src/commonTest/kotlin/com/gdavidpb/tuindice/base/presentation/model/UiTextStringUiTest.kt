package com.gdavidpb.tuindice.base.presentation.model

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import org.jetbrains.compose.resources.getString
import tuindice.base.generated.resources.Res
import tuindice.base.generated.resources.enrollment_annulled_cause_credit_limit
import tuindice.base.generated.resources.enrollment_annulled_final_message
import tuindice.base.generated.resources.outdated_app_title
import kotlin.test.Test
import kotlin.test.assertTrue

// Resolves resources through getString, so it runs on the iOS host only (androidHostTestExcludedPatterns).
@OptIn(ExperimentalTestApi::class)
class UiTextStringUiTest {
	@Test
	fun when_uiTextIsRaw_then_resolvesItsValueVerbatim() = runTuIndiceUiTest {
		val uiText = mutableStateOf<UiText>(UiText.Raw("Índice 4,25 · %1\$s"))

		setTuIndiceTestContent {
			Text(
				modifier = Modifier.testTag(ResolvedTextTag),
				text = uiText.value.asString()
			)
		}

		// A raw value is data, not a template: the placeholder survives untouched.
		onNodeWithTag(ResolvedTextTag).assertTextEquals("Índice 4,25 · %1\$s")

		runOnIdle {
			uiText.value = UiText.Raw("Sin promedio")
		}

		onNodeWithTag(ResolvedTextTag).assertTextEquals("Sin promedio")
	}

	@Test
	fun when_uiTextIsEmpty_then_resolvesBlankString() = runTuIndiceUiTest {
		val uiText = mutableStateOf<UiText>(UiText.Raw("Cargando"))

		setTuIndiceTestContent {
			Text(
				modifier = Modifier.testTag(ResolvedTextTag),
				text = uiText.value.asString()
			)
		}

		onNodeWithTag(ResolvedTextTag).assertTextEquals("Cargando")

		runOnIdle {
			uiText.value = UiText.Empty
		}

		onNodeWithTag(ResolvedTextTag).assertTextEquals("")
	}

	@Test
	fun when_uiTextIsResource_then_resolvesTheStringResource() = runTuIndiceUiTest {
		val expected = getString(Res.string.outdated_app_title)

		setTuIndiceTestContent {
			Text(
				modifier = Modifier.testTag(ResolvedTextTag),
				text = UiText.Resource(Res.string.outdated_app_title).asString()
			)
		}

		assertTrue(expected.isNotBlank())
		assertResolvedText(expected)
	}

	@Test
	fun when_resourceArgumentIsUiText_then_resolvesTheNestedTextBeforeFormatting() = runTuIndiceUiTest {
		val cause = getString(Res.string.enrollment_annulled_cause_credit_limit)
		val expectedWithResourceCause = getString(Res.string.enrollment_annulled_final_message, cause)
		val expectedWithRawCause = getString(Res.string.enrollment_annulled_final_message, "por decisión de DACE")
		val causeText = mutableStateOf<UiText>(
			UiText.Resource(Res.string.enrollment_annulled_cause_credit_limit)
		)

		setTuIndiceTestContent {
			Text(
				modifier = Modifier.testTag(ResolvedTextTag),
				text = UiText.Resource(
					resource = Res.string.enrollment_annulled_final_message,
					args = listOf(causeText.value)
				).asString()
			)
		}

		assertTrue(expectedWithResourceCause.contains(cause))
		assertResolvedText(expectedWithResourceCause)

		runOnIdle {
			causeText.value = UiText.Raw("por decisión de DACE")
		}

		assertResolvedText(expectedWithRawCause)
	}

	// stringResource resolves asynchronously off Android: wait for the text instead of reading the first frame.
	private fun ComposeUiTest.assertResolvedText(expected: String) {
		waitUntil(timeoutMillis = RESOURCE_TIMEOUT_MILLIS) {
			onAllNodesWithText(expected).fetchSemanticsNodes().isNotEmpty()
		}

		onNodeWithTag(ResolvedTextTag).assertTextEquals(expected)
	}

	private companion object {
		const val ResolvedTextTag = "ui_text_resolved"
		const val RESOURCE_TIMEOUT_MILLIS = 5_000L
	}
}
