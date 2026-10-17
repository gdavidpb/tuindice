package com.gdavidpb.tuindice.chatcontract.component

import kotlinx.serialization.descriptors.elementNames
import kotlin.test.Test
import kotlin.test.assertEquals

class ChatCatalogsTest {

	@Test
	fun supportedCatalogsAreExactlyTheCatalogsOfTheComponentWireNames() {
		val names = ChatComponent.serializer().descriptor.getElementDescriptor(1).elementNames
		val components = names.toSet() - UnknownChatComponent.serializer().descriptor.serialName
		val catalogs = components.map { it.substringBefore('.') + "/" + it.substringAfter('/') }.toSet()

		assertEquals(ChatCatalogs.supported.toSet(), catalogs)
		assertEquals(ChatCatalogs.supported.size, ChatCatalogs.supported.toSet().size)
	}
}
