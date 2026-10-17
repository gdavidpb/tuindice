package com.gdavidpb.tuindice.chatcontract.intent

import kotlinx.serialization.Serializable

/** Arguments of an [Intent]. An args type this build does not know decodes to [UnknownIntentArgs]. */
@Serializable
public sealed interface IntentArgs
