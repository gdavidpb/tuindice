package com.gdavidpb.tuindice.platform

import com.gdavidpb.tuindice.summary.data.source.IMAGE_SHARE_DIRECTORY_NAME
import org.junit.Assert.assertEquals
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

// The unit tests run without the Android resources, so the FileProvider roots are read from the files themselves: a
// shared image is only readable by the receiving app when its directory is a root of both builds.
class FileProviderPathsTest {
	@Test
	fun when_theDebugAndReleasePathsAreRead_then_bothServeTheSharedImageDirectoryOfTheCache() {
		val main = cachePathsOf(File("src/main/res/xml/paths.xml"))
		val debug = cachePathsOf(File("src/debug/res/xml/paths.xml"))

		assertEquals(mapOf(IMAGE_SHARE_DIRECTORY_NAME to "$IMAGE_SHARE_DIRECTORY_NAME/"), main)
		assertEquals(main, debug)
	}

	private fun cachePathsOf(file: File): Map<String, String> {
		val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
		val nodes = document.getElementsByTagName("cache-path")

		return (0 until nodes.length)
			.map { index -> nodes.item(index) as Element }
			.associate { element -> element.getAttribute("name") to element.getAttribute("path") }
	}
}
