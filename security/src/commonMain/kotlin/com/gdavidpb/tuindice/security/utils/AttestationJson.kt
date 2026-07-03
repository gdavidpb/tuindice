package com.gdavidpb.tuindice.security.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

private val canonicalAttestationJson = Json {
    explicitNulls = true
    prettyPrint = false
}

fun <T> canonicalAttestationPayloadJson(
    serializer: KSerializer<T>,
    value: T
): String {
    return canonicalAttestationJson.encodeToString(serializer, value)
}
