package com.gdavidpb.tuindice.assistant.presentation.navigation

import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlin.test.Test
import kotlin.test.assertEquals

// Every destination of the assistant survives a round trip through the serializers its contribution registers. The
// app's back stack is restored through them, so a destination missing from the registration would crash the
// restoration.
class AssistantDestinationSerializationTest {
	private val json = Json {
		serializersModule = SerializersModule {
			polymorphic(NavKey::class) {
				AssistantNavContribution.registerNavKeys(this)
			}
		}
	}

	private val serializer = PolymorphicSerializer(NavKey::class)

	@Test
	fun everyDestination_roundTripsThroughItsRegisteredSerializers() {
		val destinations: List<NavKey> = listOf(
			AssistantDestination.Chat(context = null),
			AssistantDestination.Chat(
				context = AssistantScreenContext(
					screen = AssistantScreen.EVALUATIONS,
					visibleFields = mapOf("subject" to "CI4325")
				)
			),
			AssistantDestination.ComponentSheet(messageId = "message-1", componentId = "component-1"),
			AssistantDestination.Memory,
			AssistantDestination.ClearMemoryConfirmationDialog
		)

		for (destination in destinations) {
			val encoded = json.encodeToString(serializer, destination)

			assertEquals(destination, json.decodeFromString(serializer, encoded))
		}
	}
}
