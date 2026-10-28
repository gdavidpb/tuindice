package com.gdavidpb.tuindice.presentation.navigation

import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.about.presentation.navigation.AboutDestination
import com.gdavidpb.tuindice.assistant.presentation.navigation.AssistantDestination
import com.gdavidpb.tuindice.auth.presentation.navigation.AuthDestination
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.enrollmentproof.presentation.navigation.EnrollmentProofDestination
import com.gdavidpb.tuindice.evaluations.presentation.navigation.EvaluationsDestination
import com.gdavidpb.tuindice.pensum.presentation.navigation.PensumDestination
import com.gdavidpb.tuindice.record.presentation.navigation.RecordDestination
import com.gdavidpb.tuindice.subjects.presentation.navigation.SubjectsDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.modules.SerializersModuleCollector
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * R1 canary: every destination that can enter a Nav3 back stack must be
 * registered in [TuIndiceSavedStateConfiguration]'s serializers module —
 * rememberNavBackStack resolves them through PolymorphicSerializer(NavKey),
 * and a missing registration crashes state restoration on iOS. The round
 * trip uses Json because the Android host test JVM stubs SavedState
 * (Bundle) writes; serializer resolution is format-agnostic, which is the
 * part this list guards.
 *
 * The destinations are not listed here by hand: they are the subtypes of the
 * sealed roots, read from their serializable descriptors. A destination added
 * under a root is therefore covered by the two checks below, which fail until it
 * is registered and until it has a sample in [everyNavigableDestination]. A new
 * feature root must be added to [sealedNavigationRoots]; the registration check
 * fails if a registered destination lies under no root.
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
		SummaryDestination.ProfilePictureSettingsDialog(
			showRemove = true,
			isCameraAvailable = true
		),
		SummaryDestination.ProfilePictureSettingsDialog(
			showRemove = false,
			isCameraAvailable = false
		),
		SummaryDestination.RemoveProfilePictureConfirmationDialog,
		SummaryDestination.Statistics,
		SummaryDestination.ShareTermDialog(termKey = null),
		SummaryDestination.ShareTermDialog(termKey = "2026-JAN_MAR"),
		SummaryDestination.TermReport(insightId = "term-1:2"),
		RecordDestination.Record,
		RecordDestination.CreateSyntheticTerm(termId = "term-1"),
		RecordDestination.CreateSyntheticTerm(termId = null),
		RecordDestination.DeleteSyntheticTermConfirmationDialog(termId = "term-1"),
		RecordDestination.ScheduleDialog,
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
		AssistantDestination.Chat(
			context = AssistantScreenContext(screen = AssistantScreen.SUMMARY)
		),
		AssistantDestination.Chat(context = null),
		AssistantDestination.ComponentSheet(
			messageId = "message-1",
			componentId = "component-1"
		),
		AssistantDestination.Memory,
		AssistantDestination.ClearMemoryConfirmationDialog,
		AboutDestination.Settings,
		AboutDestination.About,
		AboutDestination.DeleteAssistantConversationsDialog
	)

	@Test
	fun when_everyDestinationIsEncoded_then_backStackRoundTripsPolymorphically() {
		val encoded = json.encodeToString(backStackSerializer, everyNavigableDestination)
		val restored = json.decodeFromString(backStackSerializer, encoded)

		assertEquals(everyNavigableDestination, restored)
	}

	// The sealed roots of the navigation keys, one per feature. A root's descriptor lists its subtypes.
	private val sealedNavigationRoots: List<KSerializer<*>> = listOf(
		AboutDestination.serializer(),
		AssistantDestination.serializer(),
		AuthDestination.serializer(),
		BrowserDestination.serializer(),
		EnrollmentProofDestination.serializer(),
		EvaluationsDestination.serializer(),
		MainDestination.serializer(),
		PensumDestination.serializer(),
		RecordDestination.serializer(),
		SubjectsDestination.serializer(),
		SummaryDestination.serializer()
	)

	@Test
	fun when_everySealedDestinationIsRegistered_then_theSavedStateModuleKnowsIt() {
		assertEquals(
			sealedDestinationNames(),
			registeredDestinationNames(),
			"a destination under a sealed navigation root is not registered, or a registered one lies under no root"
		)
	}

	@Test
	fun when_everySealedDestinationHasASample_then_itIsCoveredByTheRoundTrip() {
		assertEquals(
			sealedDestinationNames(),
			everyNavigableDestination.map { destination -> wireNameOf(destination) }.toSet(),
			"a destination under a sealed navigation root has no sample in everyNavigableDestination"
		)
	}

	@Test
	fun when_singleDestinationIsEncoded_then_eachRoundTripsIndividually() {
		everyNavigableDestination.forEach { destination ->
			val encoded = json.encodeToString(backStackSerializer, listOf(destination))
			val restored = json.decodeFromString(backStackSerializer, encoded)

			assertEquals(listOf(destination), restored)
		}
	}

	// The serial names of every destination under the roots: the subtypes are the elements of the sealed
	// descriptor's value (the same reading ChatComponentItemExhaustivenessTest uses).
	private fun sealedDestinationNames(): Set<String> = sealedNavigationRoots.flatMap { root ->
		val subtypes = root.descriptor.getElementDescriptor(1)

		(0 until subtypes.elementsCount).map { index -> subtypes.getElementName(index) }
	}.toSet()

	// The serial names of every NavKey the saved-state module registers.
	@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
	private fun registeredDestinationNames(): Set<String> {
		val names = mutableSetOf<String>()

		TuIndiceSavedStateConfiguration.serializersModule.dumpTo(
			object : SerializersModuleCollector {
				override fun <T : Any> contextual(
					kClass: KClass<T>,
					provider: (typeArgumentsSerializers: List<KSerializer<*>>) -> KSerializer<*>
				) = Unit

				override fun <Base : Any, Sub : Base> polymorphic(
					baseClass: KClass<Base>,
					actualClass: KClass<Sub>,
					actualSerializer: KSerializer<Sub>
				) {
					if (baseClass == NavKey::class) names += actualSerializer.descriptor.serialName
				}

				override fun <Base : Any> polymorphicDefaultSerializer(
					baseClass: KClass<Base>,
					defaultSerializerProvider: (value: Base) -> SerializationStrategy<Base>?
				) = Unit

				override fun <Base : Any> polymorphicDefaultDeserializer(
					baseClass: KClass<Base>,
					defaultDeserializerProvider: (className: String?) -> DeserializationStrategy<Base>?
				) = Unit
			}
		)

		return names
	}

	private fun wireNameOf(destination: NavKey): String {
		return json
			.encodeToJsonElement(PolymorphicSerializer(NavKey::class), destination)
			.jsonObject
			.getValue("type")
			.jsonPrimitive
			.content
	}
}
