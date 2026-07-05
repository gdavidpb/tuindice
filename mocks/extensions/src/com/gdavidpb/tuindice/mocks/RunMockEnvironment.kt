package com.gdavidpb.tuindice.mocks

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration

// Drop-in replacement for wiremock.Run with the same CLI surface the mock
// scripts use (--root-dir, --port, --verbose): verbose console logging stays,
// but the notifier is wrapped so raw binary bodies never land in the log.
object RunMockEnvironment {
	@JvmStatic
	fun main(args: Array<String>) {
		val rootDir = requireNotNull(argumentValue(args, "--root-dir")) {
			"Missing required argument: --root-dir"
		}
		val port = requireNotNull(argumentValue(args, "--port")) {
			"Missing required argument: --port"
		}

		WireMockServer(
			WireMockConfiguration.wireMockConfig()
				.port(port.toInt())
				.usingFilesUnderDirectory(rootDir)
				.extensionScanningEnabled(true)
				.notifier(BinaryBodyOmittingNotifier(delegate = ConsoleNotifier(true)))
		).start()
	}

	private fun argumentValue(args: Array<String>, name: String): String? {
		val index = args.indexOf(name)

		return if (index >= 0 && index + 1 < args.size) args[index + 1] else null
	}
}
