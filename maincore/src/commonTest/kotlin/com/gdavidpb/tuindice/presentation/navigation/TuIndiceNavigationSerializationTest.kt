package com.gdavidpb.tuindice.presentation.navigation

import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.about.presentation.navigation.AboutDestination
import com.gdavidpb.tuindice.auth.presentation.navigation.AuthDestination
import com.gdavidpb.tuindice.enrollmentproof.presentation.navigation.EnrollmentProofDestination
import com.gdavidpb.tuindice.evaluations.presentation.navigation.EvaluationsDestination
import com.gdavidpb.tuindice.pensum.presentation.navigation.PensumDestination
import com.gdavidpb.tuindice.record.presentation.navigation.RecordDestination
import com.gdavidpb.tuindice.subjects.presentation.navigation.SubjectsDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * R1 canary: every destination that can enter a Nav3 back stack must be
 * registered in [TuIndiceSavedStateConfiguration]'s serializers module —
 * rememberNavBackStack resolves them through PolymorphicSerializer(NavKey),
 * and a missing registration crashes state restoration on iOS. The round
 * trip uses Json because the Android host test JVM stubs SavedState
 * (Bundle) writes; serializer resolution is format-agnostic, which is the
 * part this list guards. This list must grow with every new destination.
 */
class TuIndiceNavigationSerializationTest {

	private val json = Json {
		serializersModule = TuIndiceSavedStateConfiguration.serializersModule
	}

	private val backStackSerializer = ListSerializer(PolymorphicSerializer(NavKey::class))

	private val everyNavigableDestination: List<NavKey> = listOf(
		MainDestination.GooglePlayServicesUnavailableDialog,
		BrowserDestination.Browser(
			title = "Términos de servicio",
			url = "https://tuindice.app/terms"
		),
		BrowserDestination.ExternalResourceDialog(
			url = "https://external.example"
		),
		AuthDestination.SignIn,
		AuthDestination.SignOutDialog(
			totalCount = 3,
			recordCount = 2,
			evaluationsCount = 1,
			hasFailedMutations = true
		),
		AuthDestination.UpdatePasswordDialog,
		SummaryDestination.Summary,
		SummaryDestination.ProfilePictureSettingsDialog(showRemove = true),
		SummaryDestination.RemoveProfilePictureConfirmationDialog,
		RecordDestination.Record,
		RecordDestination.CreateSyntheticTerm(termId = "term-1"),
		RecordDestination.CreateSyntheticTerm(termId = null),
		RecordDestination.DeleteSyntheticTermConfirmationDialog(termId = "term-1"),
		EvaluationsDestination.Evaluations,
		EvaluationsDestination.Evaluation(evaluationId = "evaluation-1"),
		EvaluationsDestination.Evaluation(evaluationId = null),
		EvaluationsDestination.GradePickerDialog(
			evaluationName = "Parcial 1",
			subjectCode = "MA1111",
			grade = 15.0,
			maxGrade = 20.0
		),
		EvaluationsDestination.MaxGradePickerDialog(
			evaluationName = "Parcial 1",
			subjectCode = "MA1111",
			grade = null
		),
		EvaluationsDestination.EvaluationGradePickerDialog(
			evaluationId = "evaluation-1",
			evaluationName = "Parcial 1",
			subjectCode = "MA1111",
			grade = 15.0,
			maxGrade = 20.0
		),
		EvaluationsDestination.DeleteEvaluationConfirmationDialog(evaluationId = "evaluation-1"),
		EnrollmentProofDestination.EnrollmentProofDialog,
		SubjectsDestination.SubjectSearch,
		SubjectsDestination.SubjectDetail(subjectCode = "MA1111"),
		PensumDestination.Pensum,
		AboutDestination.About
	)

	@Test
	fun when_everyDestinationIsEncoded_then_backStackRoundTripsPolymorphically() {
		val encoded = json.encodeToString(backStackSerializer, everyNavigableDestination)
		val restored = json.decodeFromString(backStackSerializer, encoded)

		assertEquals(everyNavigableDestination, restored)
	}

	@Test
	fun when_singleDestinationIsEncoded_then_eachRoundTripsIndividually() {
		everyNavigableDestination.forEach { destination ->
			val encoded = json.encodeToString(backStackSerializer, listOf(destination))
			val restored = json.decodeFromString(backStackSerializer, encoded)

			assertEquals(listOf(destination), restored)
		}
	}
}
