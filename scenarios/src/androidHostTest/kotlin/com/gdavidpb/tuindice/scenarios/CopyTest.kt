package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenarios.fixture.Copy
import com.gdavidpb.tuindice.scenarios.fixture.CopyBinding
import java.io.File
import java.lang.reflect.Modifier
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The texts the scenarios assert must be the ones the app shows, so a reworded string fails here. */
class CopyTest {
	private val placeholder = Regex("""%(\d)\$[sd]""")

	@Test
	fun everyConstantHasExactlyOneBindingAndEveryBindingIsAConstant() {
		val constants = Copy::class.java.declaredFields
			.filter { Modifier.isStatic(it.modifiers) && it.type == String::class.java && it.name != "INSTANCE" }
			.map { it.get(null) as String }
		val bound = Copy.bindings.map { it.text }

		assertTrue(constants.isNotEmpty())
		assertEquals(constants.sorted(), bound.sorted(), "constants and bindings differ")
	}

	@Test
	fun everyResourceBindingResolvesToItsText() {
		val resources = Copy.bindings.filterIsInstance<CopyBinding.Resource>()

		assertTrue(resources.isNotEmpty())

		resources.forEach { binding ->
			val raw = stringsOf(binding.module)[binding.key]

			assertTrue(raw != null, "no string '${binding.key}' in module '${binding.module}'")
			assertEquals(
				binding.text,
				fill(raw, binding.arguments),
				"'${binding.key}' of ${binding.module} no longer reads like that"
			)
		}
	}

	@Test
	fun everyMockDataTextIsInItsFile() {
		Copy.bindings.filterIsInstance<CopyBinding.MockData>().forEach { binding ->
			assertTrue(binding.text in RepoFiles.file(binding.file).readText(), "'${binding.text}' is not in ${binding.file}")
		}
	}

	@Test
	fun everySuppliedAndDerivedBindingSaysWhy() {
		Copy.bindings.forEach { binding ->
			when (binding) {
				is CopyBinding.Supplied -> assertTrue(binding.by.isNotBlank())
				is CopyBinding.Derived -> assertTrue(binding.reason.isNotBlank())
				else -> Unit
			}
		}
	}

	/** Resource name to value of a module's `strings.xml`, with `[quantity]` appended to plural items. */
	private fun stringsOf(module: String): Map<String, String> {
		val file = File(RepoFiles.file(module), "src/commonMain/composeResources/values/strings.xml")
		val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
		val values = mutableMapOf<String, String>()
		val strings = document.getElementsByTagName("string")
		val plurals = document.getElementsByTagName("plurals")

		for (index in 0 until strings.length) {
			val node = strings.item(index)
			values[node.attributes.getNamedItem("name").nodeValue] = unescape(node.textContent)
		}

		for (index in 0 until plurals.length) {
			val node = plurals.item(index)
			val name = node.attributes.getNamedItem("name").nodeValue
			val items = node.childNodes

			for (child in 0 until items.length) {
				val item = items.item(child)
				val quantity = item.attributes?.getNamedItem("quantity")?.nodeValue ?: continue

				values["$name[$quantity]"] = unescape(item.textContent)
			}
		}

		return values
	}

	private fun unescape(raw: String): String = raw.replace("\\'", "'").replace("\\\"", "\"").replace("\\n", "\n")

	private fun fill(raw: String, arguments: List<String>): String =
		placeholder.replace(raw) { match -> arguments[match.groupValues[1].toInt() - 1] }
}
