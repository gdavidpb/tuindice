package com.gdavidpb.tuindice.base.utils.extension

import kotlin.test.Test
import kotlin.test.assertEquals

class StringTest {
	@Test
	fun toSnakeCase_convertsCamelAndAcronymBoundaries() {
		assertEquals("http_request_state", "HTTPRequestState".toSnakeCase())
		assertEquals("set_usage_data_collection_enabled", "SetUsageDataCollectionEnabled".toSnakeCase())
	}
}
