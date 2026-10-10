package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenarios.fixture.E2eFixture
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** The ids and codes the scenarios look up must be served by the mocks. */
class EntityFixturesTest {
	@Test
	fun fixtureValuesAreUnique() {
		assertEquals(E2eFixtures.all.size, E2eFixtures.all.map { it.value }.toSet().size)
	}

	@Test
	fun everyFixtureAppearsInEveryFileThatDeclaresIt() {
		E2eFixtures.all.forEach { fixture ->
			fixture.declaredIn.forEach { path ->
				val file = RepoFiles.file(path)

				assertTrue(file.isFile, "$path does not exist (declared by '${fixture.value}')")
				assertTrue(fixture.value in file.readText(), "'${fixture.value}' is not in $path")
			}
		}
	}

	@Test
	fun derivedFixturesSayHowTheyAreDerivedAndAreInNoMock() {
		E2eFixtures.all.filter { it.declaredIn.isEmpty() }.forEach { fixture ->
			assertTrue(!fixture.derivedFrom.isNullOrBlank(), fixture.value)
			assertTrue(
				RepoFiles.file("mocks/__files").walkTopDown().none { it.isFile && fixture.value in it.readText() },
				"'${fixture.value}' is in a mock, so it is not derived"
			)
		}
	}

	@Test
	fun aFixtureNeedsAFileOrADerivation() {
		assertFailsWith<IllegalArgumentException> { E2eFixture("x", emptyList()) }
	}
}
