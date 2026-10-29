package com.gdavidpb.tuindice.chatcontract.intent

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

/**
 * The app's report of how it ended the intent [intentId], with the mutations it ran. [itemOutcomes] tells how
 * each of those mutations ended; a report that names none (an older app, or an intent that ran nothing) is read
 * by its [outcome] alone, and is written without the field, so it reads on the wire as before the field existed.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
public data class IntentOutcomeReport(
	public val intentId: String,
	public val outcome: OutcomeKind,
	public val mutationIds: List<String> = emptyList(),
	public val detailCode: String? = null,
	public val newRevision: Long? = null,
	@EncodeDefault(EncodeDefault.Mode.NEVER)
	public val itemOutcomes: List<MutationOutcome> = emptyList()
)
