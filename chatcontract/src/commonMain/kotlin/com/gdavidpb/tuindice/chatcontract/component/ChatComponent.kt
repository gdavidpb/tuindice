package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.Serializable

/**
 * Structured piece of a message. [fallbackText] is what an app that cannot render the component shows instead.
 * Every subclass lives in this package, as Kotlin requires for the members of a sealed hierarchy.
 */
@Serializable
public sealed interface ChatComponent {
	public val id: String
	public val fallbackText: String
	public val display: ComponentDisplay
	public val entityRefs: List<EntityRef>
}
