package com.gdavidpb.tuindice.chatcontract.abuse

import kotlinx.serialization.Serializable

/** Text the user sends to appeal a restriction. */
@Serializable
public data class AbuseAppeal(public val text: String) {
	init {
		require(text.length <= MAX_LENGTH) { "An appeal has at most $MAX_LENGTH characters" }
	}

	public companion object {
		public const val MAX_LENGTH: Int = 500
	}
}
