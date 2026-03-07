package com.gdavidpb.tuindice.base.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

private val canonicalRiskJson = Json {
    explicitNulls = true
    prettyPrint = false
}

fun <T> canonicalRiskPayloadJson(
    serializer: KSerializer<T>,
    value: T
): String {
    return canonicalRiskJson.encodeToString(serializer, value)
}
