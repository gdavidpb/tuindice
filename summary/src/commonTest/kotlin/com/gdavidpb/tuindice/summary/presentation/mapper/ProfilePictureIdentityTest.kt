package com.gdavidpb.tuindice.summary.presentation.mapper

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfilePictureIdentityTest {
	@Test
	fun when_urlHasSignedParameters_then_identityStripsThem() {
		val url = "https://storage.googleapis.com/bucket/profile_pictures/uid.jpg" +
			"?X-Goog-Algorithm=GOOG4-RSA-SHA256&X-Goog-Signature=abc123&X-Goog-Expires=86400"

		assertEquals(
			"https://storage.googleapis.com/bucket/profile_pictures/uid.jpg",
			profilePictureIdentity(url)
		)
	}

	@Test
	fun when_urlHasNoQuery_then_identityIsTheUrlItself() {
		val url = "https://cdn.tuindice.app/profile/original.jpg"

		assertEquals(url, profilePictureIdentity(url))
	}

	@Test
	fun when_urlMixesSignedAndStableParameters_then_identityKeepsStableOnes() {
		val url = "https://storage.googleapis.com/bucket/uid.jpg" +
			"?generation=1721&x-goog-signature=abc"

		assertEquals(
			"https://storage.googleapis.com/bucket/uid.jpg?generation=1721",
			profilePictureIdentity(url)
		)
	}

	@Test
	fun when_signedUrlsOnlyDifferBySignature_then_identitiesMatch() {
		val first = "https://storage.googleapis.com/bucket/uid.jpg?X-Goog-Signature=aaa"
		val second = "https://storage.googleapis.com/bucket/uid.jpg?X-Goog-Signature=bbb"

		assertEquals(profilePictureIdentity(first), profilePictureIdentity(second))
	}
}
