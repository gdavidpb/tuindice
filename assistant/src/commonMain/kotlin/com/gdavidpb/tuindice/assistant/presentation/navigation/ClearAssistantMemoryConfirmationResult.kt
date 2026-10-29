package com.gdavidpb.tuindice.assistant.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.NavResult

// The student confirmed clearing everything the assistant remembers. The memory screen clears it when this comes back.
sealed class ClearAssistantMemoryConfirmationResult : NavResult {
	data object Confirmed : ClearAssistantMemoryConfirmationResult()
}
