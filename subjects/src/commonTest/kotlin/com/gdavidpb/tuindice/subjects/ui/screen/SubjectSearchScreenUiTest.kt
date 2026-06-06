package com.gdavidpb.tuindice.subjects.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectSearch
import com.gdavidpb.tuindice.subjects.presentation.model.SubjectSearchResultItem
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SubjectSearchScreenUiTest {
	@Test
	fun when_queryIsEmpty_then_displaysGuidanceAndSearchExamples() = runTuIndiceUiTest {
		var selectedExample: String? = null

		setTuIndiceTestContent {
			SubjectSearchScreen(
				state = SubjectSearch.State(),
				onQueryChange = { query -> selectedExample = query },
				onClearClick = {},
				onRetryClick = {},
				onSubjectClick = {}
			)
		}

			assertNodeVisible(SubjectsUiTags.SearchGuidance)
			onAllNodesWithText("Busca por código o nombre").assertCountEquals(0)
			onAllNodesWithText("Busca materias del catálogo por código, nombre o palabras clave.").assertCountEquals(0)
			onNodeWithText("Búsquedas sugeridas").assertExists()
			onNodeWithTag(SubjectsUiTags.searchExample(0))
				.assertTextEquals("MA1111")
				.performClick()

		assertEquals("MA1111", selectedExample)
	}

	@Test
	fun when_queryHasOneCharacter_then_displaysMinimumLengthHint() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectSearchScreen(
				state = SubjectSearch.State(query = "c"),
				onQueryChange = {},
				onClearClick = {},
				onRetryClick = {},
				onSubjectClick = {}
			)
		}

		assertNodeVisible(SubjectsUiTags.SearchGuidance)
		onNodeWithText("Agrega un carácter más").assertExists()
		onNodeWithText("La búsqueda empieza con al menos 2 caracteres.").assertExists()
	}

	@Test
	fun when_queryHasResults_then_hidesGuidanceAndDisplaysResults() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectSearchScreen(
				state = SubjectSearch.State(
					query = "ci",
					results = listOf(
						SubjectSearchResultItem(
							subjectCode = "CI2511",
							name = "Lógica Simbólica",
							creditsText = "4 UC"
						)
					)
				),
				onQueryChange = {},
				onClearClick = {},
				onRetryClick = {},
				onSubjectClick = {}
			)
		}

		assertNodeHidden(SubjectsUiTags.SearchGuidance)
		assertNodeVisible(SubjectsUiTags.SearchResults)
		assertNodeVisible(SubjectsUiTags.searchResult("CI2511"))
	}

	@Test
	fun when_queryHasNoResults_then_displaysConsistentEmptyMessage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectSearchScreen(
				state = SubjectSearch.State(query = "zz"),
				onQueryChange = {},
				onClearClick = {},
				onRetryClick = {},
				onSubjectClick = {}
			)
		}

		onNodeWithText("No encontramos materias").assertExists()
		onNodeWithText("No hay resultados para \"zz\". Prueba con otro código o nombre.").assertExists()
	}

	@Test
	fun when_searchFails_then_displaysConsistentErrorMessage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectSearchScreen(
				state = SubjectSearch.State(
					query = "ci",
					hasRemoteError = true
				),
				onQueryChange = {},
				onClearClick = {},
				onRetryClick = {},
				onSubjectClick = {}
			)
		}

		onNodeWithText("No pudimos buscar materias").assertExists()
		onNodeWithText("Intenta de nuevo.").assertExists()
	}
}
