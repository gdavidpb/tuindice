package com.gdavidpb.tuindice.mocks

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Extension
import com.github.tomakehurst.wiremock.extension.ExtensionFactory
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformerV2
import com.github.tomakehurst.wiremock.extension.WireMockServices
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import com.github.tomakehurst.wiremock.stubbing.ServeEvent
import java.net.URI
import wiremock.com.fasterxml.jackson.databind.ObjectMapper
import wiremock.com.fasterxml.jackson.databind.node.ObjectNode

// GET users/v1/share-backgrounds: the catalog file, with the origin of its image URLs moved to the host the request was
// sent to. The app asks for the catalog from 10.0.2.2 on Android and from localhost on iOS, and the E2E runner serves
// each platform on its own port, so the file cannot name one origin. Only the origin changes; the path stays the
// bucket's. The 503 that the catalog can answer instead is a mapping in its own scenario state, not part of this.
class ShareBackgroundsResponseTransformerFactory : ExtensionFactory {
	override fun create(services: WireMockServices): List<Extension> =
		listOf(ShareBackgroundsResponseTransformer(services.files))

	private class ShareBackgroundsResponseTransformer(
		private val files: FileSource
	) : ResponseDefinitionTransformerV2 {
		private val objectMapper = ObjectMapper()

		override fun getName(): String = "share-backgrounds-response-transformer"

		override fun applyGlobally(): Boolean = false

		override fun transform(serveEvent: ServeEvent): ResponseDefinition {
			val catalog = objectMapper.readTree(
				files.child(SHARE_DIRECTORY).getTextFileNamed(CATALOG_FILENAME).readContentsAsString()
			)
			val origin = serveEvent.request.getHeader(HOST_HEADER)
				?.takeIf(String::isNotBlank)
				?.let { host -> "http://$host" }

			if (origin != null) {
				catalog.path(BACKGROUNDS_FIELD).forEach { entry ->
					val background = entry as ObjectNode

					IMAGE_FIELDS.forEach { field ->
						background.put(field, origin + URI(background.path(field).asText()).rawPath)
					}
				}
			}

			return ResponseDefinitionBuilder.like(serveEvent.responseDefinition)
				.but()
				.withBody(objectMapper.writeValueAsString(catalog))
				.build()
		}

		private companion object {
			private const val SHARE_DIRECTORY = "share"
			private const val CATALOG_FILENAME = "get-share-backgrounds-catalog.json"
			private const val BACKGROUNDS_FIELD = "backgrounds"
			private const val HOST_HEADER = "Host"
			private val IMAGE_FIELDS = listOf("card", "thumb")
		}
	}
}
