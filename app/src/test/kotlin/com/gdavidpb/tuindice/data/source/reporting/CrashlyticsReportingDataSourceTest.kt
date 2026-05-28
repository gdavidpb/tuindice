package com.gdavidpb.tuindice.data.source.reporting

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CrashlyticsReportingDataSourceTest {
	@Test
	fun reportingOperations_delegateToCrashReporter() {
		var recordedUserId: String? = null
		val messages = mutableListOf<String>()
		val exceptions = mutableListOf<Throwable>()
		val reporter = CrashReporterDataSource(
			setUserIdAction = { recordedUserId = it },
			recordExceptionAction = { exceptions += it },
			logAction = { messages += it },
			setIntKeyAction = { _, _ -> },
			setLongKeyAction = { _, _ -> },
			setFloatKeyAction = { _, _ -> },
			setDoubleKeyAction = { _, _ -> },
			setStringKeyAction = { _, _ -> },
			setBooleanKeyAction = { _, _ -> }
		)
		val dataSource = CrashlyticsReportingDataSource(crashReporter = reporter)
		val throwable = IllegalStateException("boom")

		dataSource.setIdentifier("uid-1")
		dataSource.logMessage("message-1")
		dataSource.logException(throwable)

		assertEquals("uid-1", recordedUserId)
		assertEquals(listOf("message-1"), messages)
		assertEquals(listOf(throwable), exceptions)
	}

	@Test
	fun setCustomKey_supportsPrimitiveAndStringValueTypes() {
		val intKeys = mutableMapOf<String, Int>()
		val longKeys = mutableMapOf<String, Long>()
		val floatKeys = mutableMapOf<String, Float>()
		val doubleKeys = mutableMapOf<String, Double>()
		val stringKeys = mutableMapOf<String, String>()
		val booleanKeys = mutableMapOf<String, Boolean>()
		val reporter = CrashReporterDataSource(
			setUserIdAction = {},
			recordExceptionAction = {},
			logAction = {},
			setIntKeyAction = { key, value -> intKeys[key] = value },
			setLongKeyAction = { key, value -> longKeys[key] = value },
			setFloatKeyAction = { key, value -> floatKeys[key] = value },
			setDoubleKeyAction = { key, value -> doubleKeys[key] = value },
			setStringKeyAction = { key, value -> stringKeys[key] = value },
			setBooleanKeyAction = { key, value -> booleanKeys[key] = value }
		)
		val dataSource = CrashlyticsReportingDataSource(crashReporter = reporter)

		dataSource.setCustomKey("int", 1)
		dataSource.setCustomKey("long", 2L)
		dataSource.setCustomKey("float", 3.5f)
		dataSource.setCustomKey("double", 4.5)
		dataSource.setCustomKey("string", "text")
		dataSource.setCustomKey("boolean", true)

		assertEquals(1, intKeys["int"])
		assertEquals(2L, longKeys["long"])
		assertEquals(3.5f, floatKeys["float"])
		assertEquals(4.5, doubleKeys["double"])
		assertEquals("text", stringKeys["string"])
		assertEquals(true, booleanKeys["boolean"])
	}

	@Test
	fun setCustomKey_withUnsupportedType_throws() {
		val reporter = CrashReporterDataSource(
			setUserIdAction = {},
			recordExceptionAction = {},
			logAction = {},
			setIntKeyAction = { _, _ -> },
			setLongKeyAction = { _, _ -> },
			setFloatKeyAction = { _, _ -> },
			setDoubleKeyAction = { _, _ -> },
			setStringKeyAction = { _, _ -> },
			setBooleanKeyAction = { _, _ -> }
		)
		val dataSource = CrashlyticsReportingDataSource(crashReporter = reporter)

		val throwable = runCatching {
			dataSource.setCustomKey("unsupported", listOf("a", "b"))
		}.exceptionOrNull()

		assertTrue(throwable is IllegalArgumentException)
	}
}
