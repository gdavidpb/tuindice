package com.gdavidpb.tuindice.data.source.application

import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Modifier

// The wipe leaves the error reporter's own files where they are (they hold the report of the
// invalidation that runs it), by the names it keeps them under. The reporter names them itself and can
// change them with a version: this reads the directories it roots in the files directory from the
// reporter, so that a version that adds or renames one fails here instead of having its reports wiped.
class ReporterFileNamesTest {
	@Test
	fun everyDirectoryTheReporterRootsInTheFilesDirectory_isKeptByTheWipe() {
		val fileStore = Class.forName("com.google.firebase.crashlytics.internal.persistence.FileStore")

		// The constants that name a directory of the files directory begin with a dot; the rest
		// (open-sessions, reports...) are subdirectories inside those.
		val rootedInTheFilesDirectory = fileStore.declaredFields
			.filter { field -> Modifier.isStatic(field.modifiers) && field.type == String::class.java }
			.onEach { field -> field.isAccessible = true }
			.map { field -> field.get(null) as String }
			.filter { name -> name.startsWith(".") }

		assertTrue("the reporter's constants were found: $rootedInTheFilesDirectory", rootedInTheFilesDirectory.isNotEmpty())
		assertTrue(
			"the wipe must keep ${rootedInTheFilesDirectory - REPORTER_FILE_NAMES}",
			REPORTER_FILE_NAMES.containsAll(rootedInTheFilesDirectory)
		)
	}
}
