package com.gdavidpb.tuindice.assistant.domain.repository

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantMode

/**
 * The mode the server gives the signed-in student with the status. While the session lasts, it decides over the mode
 * the configuration holds.
 */
interface AssistantServerModeRepository {
	/** The session the caller is in, read before its status is requested. */
	fun sessionMark(): Int

	/** A mode reported for a session that has ended is dropped: it is the mode of the student who left. */
	fun reportServerMode(mode: AssistantMode, sessionMark: Int)
}
