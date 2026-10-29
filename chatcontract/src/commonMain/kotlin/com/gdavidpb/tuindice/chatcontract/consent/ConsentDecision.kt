package com.gdavidpb.tuindice.chatcontract.consent

import kotlinx.serialization.Serializable

/** The user's answer to consent [version]. */
@Serializable
public data class ConsentDecision(public val version: Int, public val accepted: Boolean)
