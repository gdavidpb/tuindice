package com.gdavidpb.tuindice.chatcontract.context

import kotlinx.serialization.Serializable

/** Reference to one record of the app, by its kind and its id. */
@Serializable
public data class EntityRef(public val kind: EntityKind, public val id: String)
