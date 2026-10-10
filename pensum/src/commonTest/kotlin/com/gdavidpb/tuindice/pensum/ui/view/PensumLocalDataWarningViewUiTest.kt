package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PensumLocalDataWarningViewUiTest {
	@Test
	fun when_messageIsProvided_then_warningShowsItInsideItsContainer() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumLocalDataWarningView(message = OFFLINE_MESSAGE)
		}

		assertNodeVisible(PensumUiTags.LocalDataWarning)
		onNode(
			hasText(OFFLINE_MESSAGE) and hasParent(hasTestTag(PensumUiTags.LocalDataWarning))
		).assertExists()
	}

	@Test
	fun when_messageChanges_then_warningShowsOnlyTheLatestMessage() = runTuIndiceUiTest {
		val messageState = mutableStateOf(OFFLINE_MESSAGE)

		setTuIndiceTestContent {
			PensumLocalDataWarningView(message = messageState.value)
		}

		runOnIdle { messageState.value = TIMEOUT_MESSAGE }
		waitForIdle()

		onNode(
			hasText(TIMEOUT_MESSAGE) and hasParent(hasTestTag(PensumUiTags.LocalDataWarning))
		).assertExists()
		onAllNodesWithText(OFFLINE_MESSAGE).assertCountEquals(0)
	}
}

private const val OFFLINE_MESSAGE = "Sin conexión. Mostramos la información guardada."
private const val TIMEOUT_MESSAGE = "La actualización tardó demasiado."
