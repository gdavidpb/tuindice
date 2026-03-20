package $PACKAGE.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import $PACKAGE.presentation.contract.$FEATURE_NAME
import $PACKAGE.presentation.viewmodel.$VIEWMODEL_NAME
import $PACKAGE.ui.screen.$SCREEN_NAME
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle

@Composable
fun $ROUTE_NAME(
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: $VIEWMODEL_NAME
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is $FEATURE_NAME.Effect.ShowSnackBar ->
				showSnackBar(SnackBarMessage(message = effect.message))
		}
	}

	LaunchedEffect(Unit) {
		viewModel.$REFRESH_METHOD_NAME()
	}

	$SCREEN_NAME(
		state = viewState,
		onRetryClick = viewModel::$REFRESH_METHOD_NAME
	)
}
