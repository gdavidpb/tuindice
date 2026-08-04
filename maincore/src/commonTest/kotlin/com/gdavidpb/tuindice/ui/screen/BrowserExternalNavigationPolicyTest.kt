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
				initialUrl = "https://tuindice.app/privacy",
				currentUrl = "https://tuindice.app/privacy",
				requestedUrl = "https://tuindice.app/privacy#datos"
			)
		)
	}

	@Test
	fun when_requestedUrlChangesFragmentFromAnotherFragment_then_keepsNavigationEmbedded() {
		assertFalse(
			shouldOpenExternalResource(
				initialUrl = "https://tuindice.app/privacy",
				currentUrl = "https://tuindice.app/privacy#responsable",
				requestedUrl = "https://tuindice.app/privacy#datos"
			)
		)
	}

	@Test
	fun when_requestedUrlIsRelativeFragment_then_keepsNavigationEmbedded() {
		assertFalse(
			shouldOpenExternalResource(
				initialUrl = "https://tuindice.app/privacy",
				currentUrl = "https://tuindice.app/privacy",
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
				initialUrl = "https://tuindice.app/privacy",
				currentUrl = "https://tuindice.app/privacy",
				requestedUrl = "https://tuindice.app/terms"
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
				currentUrl = "https://tuindice.app/privacy#datos",
				targetUrl = "https://tuindice.app/privacy"
			)
		)
	}

	@Test
	fun when_currentUrlDiffersByDocument_then_reloadsBrowserUrl() {
		assertTrue(
			shouldLoadBrowserUrl(
				currentUrl = "https://tuindice.app/terms",
				targetUrl = "https://tuindice.app/privacy"
			)
		)
	}
}
