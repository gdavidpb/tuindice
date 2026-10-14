package com.gdavidpb.tuindice.summary.presentation.route

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.summary.presentation.contract.ShareTerm
import com.gdavidpb.tuindice.summary.presentation.viewmodel.ShareTermViewModel
import com.gdavidpb.tuindice.summary.ui.dialog.ShareTermContentDialog
import com.gdavidpb.tuindice.summary.ui.dialog.ShareTermSheetActions
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.share_term_failed_message

/**
 * The sheet that shares a closed term. It opens on [termKey] when it is one of the terms, else on the newest. A share
 * that fails says so in the sheet, which stays open to try again.
 */
@Composable
fun ShareTermRoute(
	termKey: String?,
	viewModel: ShareTermViewModel,
	onDismissRequest: () -> Unit
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()
	val snackbarHostState = remember { SnackbarHostState() }
	val coroutineScope = rememberCoroutineScope()
	val failedMessage = stringResource(Res.string.share_term_failed_message)
	val actions = remember(viewModel) {
		ShareTermSheetActions(
			onTermClick = viewModel::selectTermAction,
			onBackgroundClick = viewModel::selectBackgroundAction,
			onShareClick = viewModel::shareImageAction
		)
	}

	LaunchedEffect(termKey) {
		viewModel.observeShareTermAction(termKey = termKey)
	}

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			ShareTerm.Effect.ImageShareFailed ->
				coroutineScope.launch {
					snackbarHostState.showSnackbar(message = failedMessage)
				}
		}
	}

	ShareTermContentDialog(
		state = viewState,
		snackbarHostState = snackbarHostState,
		actions = actions,
		onDismissRequest = onDismissRequest
	)
}
