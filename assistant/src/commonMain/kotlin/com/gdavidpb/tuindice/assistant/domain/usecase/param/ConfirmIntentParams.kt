package com.gdavidpb.tuindice.assistant.domain.usecase.param

import com.gdavidpb.tuindice.chatcontract.intent.IntentArgs

/** The intent to confirm and, when the user edited it, the args to run in place of the proposed ones. */
data class ConfirmIntentParams(
	val intentId: String,
	val editedArgs: IntentArgs? = null
)
