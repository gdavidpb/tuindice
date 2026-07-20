package $PACKAGE.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import $PACKAGE.presentation.route.$ROUTE_NAME
import $PACKAGE.presentation.viewmodel.$VIEWMODEL_NAME
import com.gdavidpb.tuindice.base.presentation.navigation.NavShellBindings
import com.gdavidpb.tuindice.base.utils.extension.CollectCurrentEntryValueWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.$NAVIGATION_FUNCTION_NAME(
	shellBindings: NavShellBindings
) {
	entry<$DESTINATION_NAME.$FEATURE_NAME> {
		val viewModel = koinViewModel<$VIEWMODEL_NAME>()
		val viewState by viewModel.state.collectAsStateWithLifecycle()

		CollectCurrentEntryValueWithLifecycle(
			value = viewState,
			onValue = shellBindings.onViewStateChanged
		)

		$ROUTE_NAME(
			showSnackBar = shellBindings.showSnackBar,
			viewModel = viewModel
		)
	}
}
