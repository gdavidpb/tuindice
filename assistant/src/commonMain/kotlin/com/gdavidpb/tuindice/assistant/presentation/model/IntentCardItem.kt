package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.chatcontract.intent.IntentEffectLine

// An intent the agent proposed. At most two actions: [confirmLabel] (primary, error colors when [isDestructive])
// and [cancelLabel]. Once resolved it turns into a receipt.
data class IntentCardItem(
	val intentId: String,
	val title: String,
	val effect: List<IntentEffectLine>,
	val isDestructive: Boolean,
	// Confirming asks for the password saved on the device (comprobante.pedir).
	val usesCredential: Boolean,
	val confirmLabel: String,
	val cancelLabel: String,
	// The rows of a batch the student can unmark before confirming; empty for any other intent.
	val drafts: List<IntentDraftItem>,
	val expiresAtMillis: Long,
	val status: IntentCardStatus
)
