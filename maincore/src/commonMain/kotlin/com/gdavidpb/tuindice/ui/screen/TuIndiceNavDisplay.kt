package com.gdavidpb.tuindice.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.gdavidpb.tuindice.about.presentation.navigation.aboutEntries
import com.gdavidpb.tuindice.auth.presentation.navigation.AuthDestination
import com.gdavidpb.tuindice.auth.presentation.navigation.AuthNavDependencies
import com.gdavidpb.tuindice.auth.presentation.navigation.authEntries
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.model.TopBarBannerBehavior
import com.gdavidpb.tuindice.base.presentation.navigation.NavShellBindings
import com.gdavidpb.tuindice.enrollmentproof.presentation.navigation.EnrollmentProofDestination
import com.gdavidpb.tuindice.enrollmentproof.presentation.navigation.enrollmentProofEntries
import com.gdavidpb.tuindice.evaluations.presentation.navigation.evaluationsEntries
import com.gdavidpb.tuindice.pensum.presentation.navigation.pensumEntries
import com.gdavidpb.tuindice.presentation.navigation.BrowserDestination
import com.gdavidpb.tuindice.presentation.navigation.TuIndiceNavigator
import com.gdavidpb.tuindice.presentation.navigation.browserEntries
import com.gdavidpb.tuindice.presentation.navigation.mainEntries
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.navigation.RecordNavDependencies
import com.gdavidpb.tuindice.record.presentation.navigation.recordEntries
import com.gdavidpb.tuindice.subjects.presentation.navigation.SubjectsDestination
import com.gdavidpb.tuindice.subjects.presentation.navigation.subjectsEntries
import com.gdavidpb.tuindice.summary.presentation.navigation.summaryEntries
import com.gdavidpb.tuindice.ui.MaincoreUiTags
import com.gdavidpb.tuindice.ui.navigation.rememberTuIndiceEntryScopeDecorator
import com.gdavidpb.tuindice.ui.navigation.rememberTuIndiceRetainedSavedStateDecorator
import com.gdavidpb.tuindice.ui.navigation.rememberTuIndiceRetainedViewModelStoreDecorator
import com.gdavidpb.tuindice.ui.navigation.tuIndiceNavForwardTransitionSpec
import com.gdavidpb.tuindice.ui.navigation.tuIndiceNavPopTransitionSpec
import com.gdavidpb.tuindice.ui.navigation.tuIndiceNavPredictivePopTransitionSpec

@Composable
fun TuIndiceNavDisplay(
	navigator: TuIndiceNavigator,
	modifier: Modifier = Modifier.fillMaxSize(),
	onConfirmExitClick: () -> Unit,
	isCameraAvailable: Boolean,
	onNavigateToExternalResource: (url: String) -> Unit,
	onOutdatedAppDetected: () -> Unit = {},
	onUpdatePasswordDismissRequest: () -> Unit = {},
	onRecordViewModeChangeAvailable: (((RecordViewMode) -> Unit)?) -> Unit,
	onRecordTermSelectionAvailable: ((() -> Unit)?) -> Unit,
	onBackInterceptorAvailable: ((() -> Boolean)?) -> Unit = {},
	showTopBarBanner: (behavior: TopBarBannerBehavior) -> Unit,
	onViewStateChanged: (ViewState) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	dismissSnackBar: () -> Unit = {}
) {
	val shellBindings = NavShellBindings(
		onViewStateChanged = onViewStateChanged,
		showSnackBar = showSnackBar,
		dismissSnackBar = dismissSnackBar,
		showTopBarBanner = showTopBarBanner,
		onBackInterceptorAvailable = onBackInterceptorAvailable
	)

	val navEntryProvider = entryProvider<NavKey> {
		mainEntries(
			navActions = navigator,
			onConfirmExitClick = onConfirmExitClick
		)

		authEntries(
			navActions = navigator,
			shellBindings = shellBindings,
			dependencies = AuthNavDependencies(
				onNavigateToSignIn = navigator::replaceAllForSignIn,
				onNavigateToSummary = { navigator.replaceAllForSignedIn(MainSection.SUMMARY) },
				onNavigateToBrowser = { title, url ->
					navigator.push(BrowserDestination.Browser(title = title, url = url))
				},
				onOutdatedAppDetected = onOutdatedAppDetected,
				onUpdatePasswordDismissRequest = onUpdatePasswordDismissRequest
			)
		)

		summaryEntries(
			navActions = navigator,
			shellBindings = shellBindings,
			isCameraAvailable = isCameraAvailable,
			onNavigateToUpdatePassword = {
				navigator.push(AuthDestination.UpdatePasswordDialog)
			}
		)

		recordEntries(
			navActions = navigator,
			shellBindings = shellBindings,
			dependencies = RecordNavDependencies(
				onNavigateToUpdatePassword = {
					navigator.push(AuthDestination.UpdatePasswordDialog)
				},
				onNavigateToSubjectDetail = { subjectCode ->
					navigator.push(SubjectsDestination.SubjectDetail(subjectCode = subjectCode))
				},
				onTopBarViewModeChangeAvailable = onRecordViewModeChangeAvailable,
				onTopBarTermSelectionAvailable = onRecordTermSelectionAvailable,
				onNavigateToEnrollmentProof = {
					navigator.push(EnrollmentProofDestination.EnrollmentProofDialog)
				}
			)
		)

		evaluationsEntries(
			navActions = navigator,
			shellBindings = shellBindings
		)

		aboutEntries(
			shellBindings = shellBindings,
			onNavigateToBrowser = { title, url ->
				navigator.push(BrowserDestination.Browser(title = title, url = url))
			}
		)

		enrollmentProofEntries(
			navActions = navigator,
			shellBindings = shellBindings,
			onNavigateToUpdatePassword = {
				navigator.push(AuthDestination.UpdatePasswordDialog)
			},
			onRetryRequest = {
				/* Nav2 stacked a second identical dialog; a fresh fetch needs a
				   fresh entry, so retry is pop + push of the same key. */
				navigator.pop()
				navigator.push(EnrollmentProofDestination.EnrollmentProofDialog)
			}
		)

		subjectsEntries(
			navActions = navigator,
			shellBindings = shellBindings
		)

		pensumEntries(
			shellBindings = shellBindings,
			onNavigateToSubjectDetail = { subjectCode ->
				navigator.push(SubjectsDestination.SubjectDetail(subjectCode = subjectCode))
			}
		)

		browserEntries(
			navActions = navigator,
			shellBindings = shellBindings,
			onNavigateToExternalResource = onNavigateToExternalResource
		)
	}

	NavDisplay(
		backStack = navigator.backStack,
		modifier = modifier
			.background(MaterialTheme.colorScheme.background)
			.testTag(MaincoreUiTags.TuIndiceNavHost),
		onBack = { navigator.pop() },
		entryDecorators = listOf(
			rememberTuIndiceRetainedSavedStateDecorator(navigator = navigator),
			rememberTuIndiceRetainedViewModelStoreDecorator(navigator = navigator),
			rememberTuIndiceEntryScopeDecorator(navigator = navigator)
		),
		sceneStrategies = remember {
			listOf(DialogSceneStrategy(), SinglePaneSceneStrategy())
		},
		transitionSpec = tuIndiceNavForwardTransitionSpec(),
		popTransitionSpec = tuIndiceNavPopTransitionSpec(),
		predictivePopTransitionSpec = tuIndiceNavPredictivePopTransitionSpec(),
		entryProvider = { key ->
			val entry = navEntryProvider(key)

			/* Tab-scoped contentKey: the same destination parked in two tabs must
			   keep two independent retained states (stores, saveables, scenes). */
			NavEntry(
				key = key,
				contentKey = navigator.storeKeyOf(key),
				metadata = entry.metadata,
				content = { entry.Content() }
			)
		}
	)
}
