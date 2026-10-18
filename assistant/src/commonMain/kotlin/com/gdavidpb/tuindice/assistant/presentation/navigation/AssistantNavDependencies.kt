package com.gdavidpb.tuindice.assistant.presentation.navigation

import androidx.compose.runtime.Stable
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityRef

// What the assistant's screens ask of the host: the screens it can open, the enrollment proof, and the clipboard. The
// host resolves them in maincore.
@Stable
class AssistantNavDependencies(
	val onNavigateTo: (NavTarget, List<EntityRef>) -> Unit,
	val onOpenEnrollmentProof: () -> Unit,
	val onCopyText: (String) -> Unit
)
