package com.gdavidpb.tuindice.data.source.reporting


import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CrashlyticsReportingDataSourceTest {
	@Test
	fun reportingOperations_delegateToCrashReporter() {
		val reporter = FakeCrashReporter()
		val dataSource = CrashlyticsReportingDataSource(crashReporter = reporter)
		val throwable = IllegalStateException("boom")

		dataSource.setIdentifier("uid-1")
		dataSource.logMessage("message-1")
		dataSource.logException(throwable)

		assertEquals("uid-1", reporter.recordedUserId)
		assertEquals(listOf("message-1"), reporter.messages)
		assertEquals(listOf(throwable), reporter.exceptions)
	}

	@Test
	fun setCustomKey_supportsPrimitiveAndStringValueTypes() {
		val reporter = FakeCrashReporter()
		val dataSource = CrashlyticsReportingDataSource(crashReporter = reporter)

		dataSource.setCustomKey("int", 1)
		dataSource.setCustomKey("long", 2L)
		dataSource.setCustomKey("float", 3.5f)
		dataSource.setCustomKey("double", 4.5)
		dataSource.setCustomKey("string", "text")
		dataSource.setCustomKey("boolean", true)

		assertEquals(1, reporter.intKeys["int"])
		assertEquals(2L, reporter.longKeys["long"])
		assertEquals(3.5f, reporter.floatKeys["float"])
		assertEquals(4.5, reporter.doubleKeys["double"])
		assertEquals("text", reporter.stringKeys["string"])
		assertEquals(true, reporter.booleanKeys["boolean"])
	}

	@Test
	fun setCustomKey_withUnsupportedType_throws() {
		val reporter = FakeCrashReporter()
		val dataSource = CrashlyticsReportingDataSource(crashReporter = reporter)

		val throwable = runCatching {
			dataSource.setCustomKey("unsupported", listOf("a", "b"))
		}.exceptionOrNull()

		assertTrue(throwable is IllegalArgumentException)
	}
}

private class FakeCrashReporter : CrashReporterDataSource {
	var recordedUserId: String? = null
	val messages = mutableListOf<String>()
	val exceptions = mutableListOf<Throwable>()
	val intKeys = mutableMapOf<String, Int>()
	val longKeys = mutableMapOf<String, Long>()
	val floatKeys = mutableMapOf<String, Float>()
	val doubleKeys = mutableMapOf<String, Double>()
	val stringKeys = mutableMapOf<String, String>()
	val booleanKeys = mutableMapOf<String, Boolean>()

	override fun setUserId(identifier: String) {
		recordedUserId = identifier
	}

	override fun recordException(throwable: Throwable) {
		exceptions += throwable
	}

	override fun log(message: String) {
		messages += message
	}

	override fun setCustomKey(key: String, value: Int) {
		intKeys[key] = value
	}

	override fun setCustomKey(key: String, value: Long) {
		longKeys[key] = value
	}

	override fun setCustomKey(key: String, value: Float) {
		floatKeys[key] = value
	}

	override fun setCustomKey(key: String, value: Double) {
		doubleKeys[key] = value
	}

	override fun setCustomKey(key: String, value: String) {
		stringKeys[key] = value
	}

	override fun setCustomKey(key: String, value: Boolean) {
		booleanKeys[key] = value
	}
}
