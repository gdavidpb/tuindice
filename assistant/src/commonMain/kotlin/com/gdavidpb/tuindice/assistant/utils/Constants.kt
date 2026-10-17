package com.gdavidpb.tuindice.assistant.utils

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

// Version of the consent the user accepts; the service keeps the answer per version.
internal const val ASSISTANT_CONSENT_VERSION = 1

// How long the server holds the assistant off after a 503 that gave no retry time.
internal const val DEFAULT_SERVER_DISABLED_SECONDS = 900

// Waits before each reconnection of the event stream. Its length is the number of reconnections in a row;
// a stream that delivers an event starts the count over.
internal val STREAM_RECONNECT_BACKOFF: List<Duration> = listOf(1.seconds, 2.seconds, 4.seconds, 8.seconds, 16.seconds)
