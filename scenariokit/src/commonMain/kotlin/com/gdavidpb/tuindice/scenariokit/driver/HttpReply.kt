package com.gdavidpb.tuindice.scenariokit.driver

/** Backend reply; [status] is -1 when the transport failed and [body] then holds the error. */
data class HttpReply(val status: Int, val body: String) {
	val isSuccess: Boolean get() = status in HTTP_SUCCESS_RANGE

	private companion object {
		val HTTP_SUCCESS_RANGE = 200..299
	}
}
