package com.gdavidpb.tuindice.base.presentation.navigation

import androidx.compose.runtime.Stable
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.model.TopBarBannerBehavior

/**
 * Shell wiring shared by every entry provider: ViewState hoisting for the
 * top/bottom bars, snackbar access, banner access and the hoisted back
 * interceptor publisher.
 */
@Stable
class NavShellBindings(
	val onViewStateChanged: (ViewState) -> Unit,
	val showSnackBar: (message: SnackBarMessage) -> Unit,
	val dismissSnackBar: () -> Unit = {},
	val showTopBarBanner: (behavior: TopBarBannerBehavior) -> Unit = {},
	val onBackInterceptorAvailable: ((() -> Boolean)?) -> Unit = {}
)
