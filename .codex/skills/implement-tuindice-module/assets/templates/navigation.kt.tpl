package $PACKAGE.presentation.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import $PACKAGE.presentation.route.$ROUTE_NAME
import $PACKAGE.presentation.viewmodel.$VIEWMODEL_NAME
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.$NAVIGATION_FUNCTION_NAME(
	onViewStateChanged: (ViewState) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	navigation<$DESTINATION_NAME.NavGraph>(startDestination = $DESTINATION_NAME.$FEATURE_NAME) {
		composable<$DESTINATION_NAME.$FEATURE_NAME> { backStackEntry ->
			val viewModel = koinViewModel<$VIEWMODEL_NAME>(viewModelStoreOwner = backStackEntry)
			val viewState by viewModel.state.collectAsStateWithLifecycle()

			LaunchedEffect(viewState) {
				onViewStateChanged(viewState)
			}

			$ROUTE_NAME(
				showSnackBar = showSnackBar,
				viewModel = viewModel
			)
		}
	}
}
