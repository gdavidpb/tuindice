package com.gdavidpb.tuindice.base.utils.extension

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class RememberLastNonNullUiTest {
	@Test
	fun when_valueIsCleared_then_keepsReturningTheLastNonNullValue() = runTuIndiceUiTest {
		val message = mutableStateOf<String?>("Sin conexión")

		setTuIndiceTestContent {
			Text(
				modifier = Modifier.testTag(RememberedTextTag),
				text = rememberLastNonNull(message.value) ?: NO_VALUE_TEXT
			)
		}

		onNodeWithTag(RememberedTextTag).assertTextEquals("Sin conexión")

		runOnIdle {
			message.value = null
		}

		onNodeWithTag(RememberedTextTag).assertTextEquals("Sin conexión")
	}

	@Test
	fun when_noValueWasEverProvided_then_returnsNull() = runTuIndiceUiTest {
		val message = mutableStateOf<String?>(null)

		setTuIndiceTestContent {
			Text(
				modifier = Modifier.testTag(RememberedTextTag),
				text = rememberLastNonNull(message.value) ?: NO_VALUE_TEXT
			)
		}

		onNodeWithTag(RememberedTextTag).assertTextEquals(NO_VALUE_TEXT)

		runOnIdle {
			message.value = "Servicio no disponible"
		}

		onNodeWithTag(RememberedTextTag).assertTextEquals("Servicio no disponible")
	}

	@Test
	fun when_newNonNullValueArrives_then_replacesTheRememberedOne() = runTuIndiceUiTest {
		val message = mutableStateOf<String?>("Sin conexión")

		setTuIndiceTestContent {
			Text(
				modifier = Modifier.testTag(RememberedTextTag),
				text = rememberLastNonNull(message.value) ?: NO_VALUE_TEXT
			)
		}

		runOnIdle {
			message.value = null
		}
		onNodeWithTag(RememberedTextTag).assertTextEquals("Sin conexión")

		runOnIdle {
			message.value = "Tiempo de espera agotado"
		}
		onNodeWithTag(RememberedTextTag).assertTextEquals("Tiempo de espera agotado")

		runOnIdle {
			message.value = null
		}
		// Clearing again falls back to the newest value, not to the first one.
		onNodeWithTag(RememberedTextTag).assertTextEquals("Tiempo de espera agotado")
	}

	private companion object {
		const val RememberedTextTag = "remembered_text"
		const val NO_VALUE_TEXT = "sin valor"
	}
}
