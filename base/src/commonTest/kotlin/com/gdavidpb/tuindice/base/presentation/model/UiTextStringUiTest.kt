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
import tuindice.base.generated.resources.date_day_month_year
import tuindice.base.generated.resources.date_month_names
import tuindice.base.generated.resources.date_weekday_names
import tuindice.base.generated.resources.date_weekday_short_names
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

	@Test
	fun when_uiTextIsArrayItem_then_resolvesTheElementAtItsIndex() = runTuIndiceUiTest {
		val uiText = mutableStateOf<UiText>(UiText.ArrayItem(Res.array.date_month_names, 0))

		setTuIndiceTestContent {
			Text(
				modifier = Modifier.testTag(ResolvedTextTag),
				text = uiText.value.asString()
			)
		}

		assertResolvedText("enero")

		runOnIdle {
			uiText.value = UiText.ArrayItem(Res.array.date_month_names, 11)
		}

		assertResolvedText("diciembre")

		runOnIdle {
			uiText.value = UiText.ArrayItem(Res.array.date_weekday_short_names, 2)
		}

		assertResolvedText("mié")
	}

	@Test
	fun when_arrayItemIndexIsOutOfTheArray_then_resolvesBlankString() = runTuIndiceUiTest {
		val uiText = mutableStateOf<UiText>(UiText.ArrayItem(Res.array.date_weekday_names, 6))

		setTuIndiceTestContent {
			Text(
				modifier = Modifier.testTag(ResolvedTextTag),
				text = uiText.value.asString()
			)
		}

		assertResolvedText("domingo")

		runOnIdle {
			uiText.value = UiText.ArrayItem(Res.array.date_weekday_names, 7)
		}

		// A week has seven days: an eighth is nothing to read, and it does not throw.
		assertResolvedText("")
	}

	@Test
	fun when_resourceArgumentIsArrayItem_then_resolvesTheElementBeforeFormatting() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Text(
				modifier = Modifier.testTag(ResolvedTextTag),
				text = UiText.Resource(
					resource = Res.string.date_day_month_year,
					args = listOf("05", UiText.ArrayItem(Res.array.date_month_names, 2), "2026")
				).asString()
			)
		}

		assertResolvedText("05 de marzo 2026")
	}

	@Test
	fun when_uiTextIsCapitalized_then_raisesOnlyTheFirstLetter() = runTuIndiceUiTest {
		val uiText = mutableStateOf<UiText>(
			UiText.Capitalized(UiText.ArrayItem(Res.array.date_weekday_names, 2))
		)

		setTuIndiceTestContent {
			Text(
				modifier = Modifier.testTag(ResolvedTextTag),
				text = uiText.value.asString()
			)
		}

		assertResolvedText("Miércoles")

		runOnIdle {
			uiText.value = UiText.Capitalized(UiText.Raw("sábado — 17/01/26"))
		}

		assertResolvedText("Sábado — 17/01/26")

		runOnIdle {
			// Already upper case, a digit, or nothing at all: left as it is.
			uiText.value = UiText.Capitalized(UiText.Raw("Hoy, 03:05 p. m."))
		}

		assertResolvedText("Hoy, 03:05 p. m.")

		runOnIdle {
			uiText.value = UiText.Capitalized(UiText.Raw("15 de enero 2026"))
		}

		assertResolvedText("15 de enero 2026")

		runOnIdle {
			uiText.value = UiText.Capitalized(UiText.Empty)
		}

		assertResolvedText("")
	}

	@Test
	fun when_uiTextIsUppercase_then_raisesEveryLetter() = runTuIndiceUiTest {
		val uiText = mutableStateOf<UiText>(
			UiText.Uppercase(UiText.ArrayItem(Res.array.date_weekday_short_names, 0))
		)

		setTuIndiceTestContent {
			Text(
				modifier = Modifier.testTag(ResolvedTextTag),
				text = uiText.value.asString()
			)
		}

		assertResolvedText("LUN")

		runOnIdle {
			uiText.value = UiText.Uppercase(UiText.ArrayItem(Res.array.date_weekday_short_names, 5))
		}

		assertResolvedText("SÁB")
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
