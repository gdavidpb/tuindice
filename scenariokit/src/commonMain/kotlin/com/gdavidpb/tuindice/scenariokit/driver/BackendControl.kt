package com.gdavidpb.tuindice.scenariokit.driver

/** Plain HTTP against the WireMock the app talks to. */
interface BackendControl {
	fun http(method: String, path: String, body: String?, authorization: String?): HttpReply
}
