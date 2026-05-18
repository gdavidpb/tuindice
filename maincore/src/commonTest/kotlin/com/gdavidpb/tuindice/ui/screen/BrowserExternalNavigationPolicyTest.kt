package com.gdavidpb.tuindice.ui.screen

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BrowserExternalNavigationPolicyTest {
	@Test
	fun when_requestedUrlMatchesInitialUrl_then_keepsNavigationEmbedded() {
		assertFalse(
			shouldOpenExternalResource(
				initialUrl = "https://tuindice.app/privacy",
				currentUrl = null,
				requestedUrl = "https://tuindice.app/privacy"
			)
		)
	}

	@Test
	fun when_requestedUrlMatchesCurrentUrl_then_keepsNavigationEmbedded() {
		assertFalse(
			shouldOpenExternalResource(
				initialUrl = "https://tuindice.app/privacy",
				currentUrl = "https://tuindice.app/help",
				requestedUrl = "https://tuindice.app/help"
			)
		)
	}

	@Test
	fun when_requestedUrlOnlyChangesFragment_then_keepsNavigationEmbedded() {
		assertFalse(
			shouldOpenExternalResource(
				initialUrl = "https://tuindice.app/privacy_policy_v6_0.html",
				currentUrl = "https://tuindice.app/privacy_policy_v6_0.html",
				requestedUrl = "https://tuindice.app/privacy_policy_v6_0.html#datos"
			)
		)
	}

	@Test
	fun when_requestedUrlChangesFragmentFromAnotherFragment_then_keepsNavigationEmbedded() {
		assertFalse(
			shouldOpenExternalResource(
				initialUrl = "https://tuindice.app/privacy_policy_v6_0.html",
				currentUrl = "https://tuindice.app/privacy_policy_v6_0.html#responsable",
				requestedUrl = "https://tuindice.app/privacy_policy_v6_0.html#datos"
			)
		)
	}

	@Test
	fun when_requestedUrlIsRelativeFragment_then_keepsNavigationEmbedded() {
		assertFalse(
			shouldOpenExternalResource(
				initialUrl = "https://tuindice.app/privacy_policy_v6_0.html",
				currentUrl = "https://tuindice.app/privacy_policy_v6_0.html",
				requestedUrl = "#datos"
			)
		)
	}

	@Test
	fun when_requestedUrlDiffersFromCurrentUrl_then_opensExternalResource() {
		assertTrue(
			shouldOpenExternalResource(
				initialUrl = "https://tuindice.app/privacy",
				currentUrl = "https://tuindice.app/privacy",
				requestedUrl = "https://portal.tuindice.app/help"
			)
		)
	}

	@Test
	fun when_requestedUrlChangesDocument_then_opensExternalResource() {
		assertTrue(
			shouldOpenExternalResource(
				initialUrl = "https://tuindice.app/privacy_policy_v6_0.html",
				currentUrl = "https://tuindice.app/privacy_policy_v6_0.html",
				requestedUrl = "https://tuindice.app/terms_and_conditions_v6_0.html"
			)
		)
	}

	@Test
	fun when_currentUrlIsUnknown_then_keepsNavigationEmbedded() {
		assertFalse(
			shouldOpenExternalResource(
				initialUrl = "https://tuindice.app/privacy",
				currentUrl = null,
				requestedUrl = "mailto:soporte@tuindice.app"
			)
		)
	}

	@Test
	fun when_currentUrlOnlyDiffersByFragment_then_doesNotReloadBrowserUrl() {
		assertFalse(
			shouldLoadBrowserUrl(
				currentUrl = "https://tuindice.app/privacy_policy_v6_0.html#datos",
				targetUrl = "https://tuindice.app/privacy_policy_v6_0.html"
			)
		)
	}

	@Test
	fun when_currentUrlDiffersByDocument_then_reloadsBrowserUrl() {
		assertTrue(
			shouldLoadBrowserUrl(
				currentUrl = "https://tuindice.app/terms_and_conditions_v6_0.html",
				targetUrl = "https://tuindice.app/privacy_policy_v6_0.html"
			)
		)
	}
}
