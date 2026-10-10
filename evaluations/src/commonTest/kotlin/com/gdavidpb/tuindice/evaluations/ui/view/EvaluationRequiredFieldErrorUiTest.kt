package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

// The error colour and the small type are only drawn: nothing here reads them.
@OptIn(ExperimentalTestApi::class)
class EvaluationRequiredFieldErrorUiTest {
	@Test
	fun when_severalFieldsAreMissing_then_eachErrorReadsItsOwnMessageUnderItsOwnTag() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Column {
				EvaluationRequiredFieldError(
					modifier = Modifier.testTag(EvaluationsUiTags.EvaluationSubjectRequiredError),
					text = "Debes seleccionar una materia"
				)
				EvaluationRequiredFieldError(
					modifier = Modifier.testTag(EvaluationsUiTags.EvaluationTypeRequiredError),
					text = "Debes seleccionar un tipo"
				)
			}
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationSubjectRequiredError)
		assertNodeVisible(EvaluationsUiTags.EvaluationTypeRequiredError)
		onNodeWithTag(EvaluationsUiTags.EvaluationSubjectRequiredError)
			.assertTextEquals("Debes seleccionar una materia")
		onNodeWithTag(EvaluationsUiTags.EvaluationTypeRequiredError)
			.assertTextEquals("Debes seleccionar un tipo")
		assertNodeHidden(EvaluationsUiTags.EvaluationMaxGradeRequiredError)
	}

	@Test
	fun when_theMessageChanges_then_theErrorReadsTheNewMessage() = runTuIndiceUiTest {
		val messageState = mutableStateOf("Debes asignar una nota máxima")

		setTuIndiceTestContent {
			EvaluationRequiredFieldError(
				modifier = Modifier.testTag(EvaluationsUiTags.EvaluationMaxGradeRequiredError),
				text = messageState.value
			)
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationMaxGradeRequiredError)
			.assertTextEquals("Debes asignar una nota máxima")

		runOnIdle {
			messageState.value = "Debes seleccionar un tipo"
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationMaxGradeRequiredError)
			.assertTextEquals("Debes seleccionar un tipo")
	}
}
