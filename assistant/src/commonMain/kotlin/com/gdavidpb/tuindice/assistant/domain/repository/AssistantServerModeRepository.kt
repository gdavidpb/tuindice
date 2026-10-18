package com.gdavidpb.tuindice.assistant.domain.repository

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantMode

/**
 * The mode the server gives the signed-in student with the status. While the session lasts, it decides over the mode the
 * configuration holds.
 */
interface AssistantServerModeRepository {
	fun reportServerMode(mode: AssistantMode)
}
