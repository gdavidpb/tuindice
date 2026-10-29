package com.gdavidpb.tuindice.summary.ui.view

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfilePictureRequestUrlTest {
	@Test
	fun when_aNewSignatureArrives_then_theRequestKeepsItsUrl() {
		val requestUrl = ProfilePictureRequestUrl(initialUrl = FIRST_URL)

		requestUrl.onUrlReceived(SECOND_URL)

		assertEquals(FIRST_URL, requestUrl.url)
	}

	@Test
	fun when_theLoadFailsAfterANewSignature_then_theRequestMovesToIt() {
		val requestUrl = ProfilePictureRequestUrl(initialUrl = FIRST_URL)

		requestUrl.onUrlReceived(SECOND_URL)
		requestUrl.onLoadFailed()

		assertEquals(SECOND_URL, requestUrl.url)
	}

	@Test
	fun when_theLoadFailsBeforeANewSignature_then_theNextOneIsTakenAsItArrives() {
		val requestUrl = ProfilePictureRequestUrl(initialUrl = FIRST_URL)

		requestUrl.onLoadFailed()
		assertEquals(FIRST_URL, requestUrl.url)

		requestUrl.onUrlReceived(SECOND_URL)
		assertEquals(SECOND_URL, requestUrl.url)

		// Once it moved, the next signature waits for a failure again.
		requestUrl.onUrlReceived(THIRD_URL)
		assertEquals(SECOND_URL, requestUrl.url)
	}

	private companion object {
		const val PATH = "https://storage.googleapis.com/tu-indice-usb/profile_pictures/uid.jpg"
		const val FIRST_URL = "$PATH?GoogleAccessId=svc&Expires=1000&Signature=a"
		const val SECOND_URL = "$PATH?GoogleAccessId=svc&Expires=2000&Signature=b"
		const val THIRD_URL = "$PATH?GoogleAccessId=svc&Expires=3000&Signature=c"
	}
}
