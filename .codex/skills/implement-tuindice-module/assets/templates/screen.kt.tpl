package $PACKAGE.ui.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import $PACKAGE.presentation.contract.$FEATURE_NAME
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.base.ui.view.ErrorView
import com.gdavidpb.tuindice.base.ui.view.SealedCrossfade
import org.jetbrains.compose.resources.stringResource
import $GENERATED_RESOURCES_PACKAGE.Res

@Composable
fun $SCREEN_NAME(
	state: $FEATURE_NAME.State,
	onRetryClick: () -> Unit
) {
	SealedCrossfade(targetState = state) { targetState ->
		when (targetState) {
			is $FEATURE_NAME.State.Loading ->
				Text(text = stringResource(Res.string.${MODULE_NAME}_loading_message))

			is $FEATURE_NAME.State.Content ->
				Text(text = targetState.message)

			is $FEATURE_NAME.State.Failed ->
				ErrorView(
					title = stringResource(Res.string.${MODULE_NAME}_failed_title),
					message = stringResource(Res.string.${MODULE_NAME}_failed_message),
					retryText = stringResource(Res.string.${MODULE_NAME}_failed_retry),
					onRetryClick = onRetryClick,
					headerContent = {
						ErrorStateAnimationView()
					}
				)
		}
	}
}
