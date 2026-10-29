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

	// The shape the backend sends: blob.signUrl without a version option signs with V2.
	@Test
	fun when_urlIsSignedWithV2_then_identityStripsItsSignature() {
		val url = "$PICTURE_PATH?GoogleAccessId=svc%40tu-indice.iam.gserviceaccount.com" +
			"&Expires=1761700000&Signature=AAAA%2Bsig%3D%3D"

		assertEquals(PICTURE_PATH, profilePictureIdentity(url))
	}

	@Test
	fun when_v2SignaturesOnlyDifferByExpiryAndSignature_then_identitiesAndCacheKeysMatch() {
		val first = "$PICTURE_PATH?GoogleAccessId=svc&Expires=1761700000&Signature=aaa"
		val second = "$PICTURE_PATH?googleaccessid=svc&expires=1761700042&signature=bbb"

		assertEquals(profilePictureIdentity(first), profilePictureIdentity(second))
		assertEquals(profilePictureCacheKey(first, version = 3), profilePictureCacheKey(second, version = 3))
	}

	@Test
	fun when_urlIsSignedWithV4_then_identityStripsItsSignature() {
		val url = "$PICTURE_PATH?X-Goog-Algorithm=GOOG4-RSA-SHA256&X-Goog-Credential=svc%2F20261029" +
			"&X-Goog-Date=20261029T120000Z&X-Goog-Expires=86400&X-Goog-SignedHeaders=host&X-Goog-Signature=abc"

		assertEquals(PICTURE_PATH, profilePictureIdentity(url))
	}

	@Test
	fun when_v2SignatureMixesWithAStableParameter_then_identityKeepsTheStableOne() {
		val url = "$PICTURE_PATH?generation=1721&GoogleAccessId=svc&Expires=1761700000&Signature=aaa"

		assertEquals("$PICTURE_PATH?generation=1721", profilePictureIdentity(url))
	}

	private companion object {
		const val PICTURE_PATH = "https://storage.googleapis.com/tu-indice-usb/profile_pictures/uid.jpg"
	}
}
