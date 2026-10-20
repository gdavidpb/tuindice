package com.gdavidpb.tuindice.assistant.ui.component.perfil

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.presentation.mapper.toSyncStatusItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.SyncSourceRow
import com.gdavidpb.tuindice.chatcontract.component.SyncStatusComponent
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SyncStatusComponentViewUiTest {
	@Test
	fun when_aSourceIsUnavailable_then_theHeadlineSaysThereAreProblems_andEachSourceReadsItsState() =
		runTuIndiceUiTest {
			setTuIndiceTestContent {
				SyncStatusComponentView(item = sync(SOURCES_WITH_A_PROBLEM).toSyncStatusItem())
			}

			assertNodeVisible(AssistantUiTags.Profile.sync(COMPONENT_ID))
			onNodeWithText("SINCRONIZACIÓN").assertIsDisplayed()
			onNodeWithText("Con problemas").assertIsDisplayed()
			onNodeWithText("Notas").assertIsDisplayed()
			onNodeWithText("Pensum · Hace 3 días").assertIsDisplayed()
			onNodeWithText("Al día").assertIsDisplayed()
			onNodeWithText("La universidad no respondió").assertIsDisplayed()
		}

	@Test
	fun when_everySourceIsFine_then_theHeadlineSaysAllIsWell() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SyncStatusComponentView(item = sync(FINE_SOURCES).toSyncStatusItem())
		}

		onNodeWithText("Con problemas").assertDoesNotExist()
		onAllNodesWithText("Al día").assertCountEquals(2)
		onNodeWithText("No se intentó").assertIsDisplayed()
	}

	@Test
	fun when_noSourceIsSynced_then_aLineSaysSo() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SyncStatusComponentView(item = sync(emptyList()).toSyncStatusItem())
		}

		onNodeWithText("Sin fuentes sincronizadas todavía").assertIsDisplayed()
	}

	@Test
	fun when_theAnnulmentHasACause_then_itIsARow_andTheEnrollmentCodeIsNotShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SyncStatusComponentView(item = sync(FINE_SOURCES, annulmentCause = CAUSE).toSyncStatusItem())
		}

		onNodeWithText(CAUSE).assertIsDisplayed()
		onNodeWithText("enrolled").assertDoesNotExist()
	}

	@Test
	fun when_theLastSyncIsKnown_then_TheLabelSaysWhen() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SyncStatusComponentView(item = sync(FINE_SOURCES, lastSyncAtMillis = LAST_SYNC_MILLIS).toSyncStatusItem())
		}

		onNodeWithText("ÚLTIMA SINCRONIZACIÓN", substring = true).assertIsDisplayed()
	}

	private fun sync(
		sources: List<SyncSourceRow>,
		annulmentCause: String? = null,
		lastSyncAtMillis: Long? = null
	): SyncStatusComponent =
		SyncStatusComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			lastSyncAtMillis = lastSyncAtMillis,
			sources = sources,
			enrollmentStatus = "enrolled",
			annulmentCause = annulmentCause
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Sincronización de prueba"
		const val CAUSE = "La universidad anuló tu inscripción de este trimestre."
		const val LAST_SYNC_MILLIS = 1_760_000_000_000L

		val SOURCES_WITH_A_PROBLEM = listOf(
			SyncSourceRow("Notas", "success"),
			SyncSourceRow("Pensum", "unavailable", detail = "Hace 3 días")
		)

		val FINE_SOURCES = listOf(
			SyncSourceRow("Notas", "success"),
			SyncSourceRow("Pensum", "not_attempted")
		)
	}
}
