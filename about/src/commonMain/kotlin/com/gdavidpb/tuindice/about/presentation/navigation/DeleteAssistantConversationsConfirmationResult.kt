package com.gdavidpb.tuindice.about.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.NavResult

// The student confirmed deleting the assistant's conversations; the About screen starts the deletion then.
sealed class DeleteAssistantConversationsConfirmationResult : NavResult {
	data object Confirmed : DeleteAssistantConversationsConfirmationResult()
}
