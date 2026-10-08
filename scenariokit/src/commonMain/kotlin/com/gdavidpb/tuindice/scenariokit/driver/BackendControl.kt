package com.gdavidpb.tuindice.scenariokit.driver

/**
 * Plain HTTP against the WireMock the app talks to, as the device reaches it (the harness hands the base URL to the
 * runner). The interpreter uses it for the resets and mock states before a scenario, for the admin journal behind
 * `expectRequest`, and for the driver contract's check of the backend.
 */
interface BackendControl {
	/**
	 * Sends the request and returns the reply. [body], when present, is sent as JSON; [authorization] is the full
	 * value of the `Authorization` header. A transport failure or a timeout (a few seconds) is not an exception: the
	 * reply has status -1 and the error in its body.
	 */
	fun http(method: String, path: String, body: String?, authorization: String?): HttpReply
}
