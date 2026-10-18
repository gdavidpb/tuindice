package com.gdavidpb.tuindice.assistant.presentation.navigation

import androidx.compose.runtime.Stable
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityRef

// What the assistant's screens ask of the host: the screens it can open, the enrollment proof, a page of the in-app
// browser, and the clipboard. The host resolves them in maincore.
@Stable
class AssistantNavDependencies(
	val onNavigateTo: (NavTarget, List<EntityRef>) -> Unit,
	val onOpenEnrollmentProof: () -> Unit,
	val onNavigateToBrowser: (title: String, url: String) -> Unit,
	val onCopyText: (String) -> Unit
)
