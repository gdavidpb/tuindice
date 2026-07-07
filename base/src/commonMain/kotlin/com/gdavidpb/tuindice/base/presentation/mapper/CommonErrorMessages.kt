package com.gdavidpb.tuindice.base.presentation.mapper

import org.jetbrains.compose.resources.getString
import tuindice.base.generated.resources.Res
import tuindice.base.generated.resources.snack_network_unavailable
import tuindice.base.generated.resources.snack_service_unavailable
import tuindice.base.generated.resources.snack_timeout
import tuindice.base.generated.resources.snack_unexpected_error

// Canonical failure wording shared by every feature snackbar: the same cause
// must read the same on every tab.
suspend fun commonNetworkUnavailableMessage(): String =
	getString(Res.string.snack_network_unavailable)

suspend fun commonServiceUnavailableMessage(): String =
	getString(Res.string.snack_service_unavailable)

suspend fun commonTimeoutMessage(): String =
	getString(Res.string.snack_timeout)

suspend fun commonUnexpectedErrorMessage(): String =
	getString(Res.string.snack_unexpected_error)
