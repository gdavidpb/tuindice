package com.gdavidpb.tuindice.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.model.TopBarBannerBehavior
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.base.ui.style.InternalScreenDefaults
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.base.ui.view.ErrorView
import com.gdavidpb.tuindice.base.ui.view.TopAppBarActionsView
import com.gdavidpb.tuindice.base.ui.view.TopAppBarAnimatedTitleView
import com.gdavidpb.tuindice.base.utils.extension.canNavigateBackFromCurrentDestination
import com.gdavidpb.tuindice.base.utils.extension.isCurrentDestination
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.model.BottomBarConfig
import com.gdavidpb.tuindice.presentation.model.MainShellState
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.ui.view.RecordTopBarViewModeBannerView
import com.gdavidpb.tuindice.record.ui.view.RecordTopBarViewModeSwitchView
import com.gdavidpb.tuindice.ui.MaincoreUiTags
import com.gdavidpb.tuindice.ui.view.TopBarBannerHost
import org.jetbrains.compose.resources.stringResource
import tuindice.maincore.generated.resources.Res
import tuindice.maincore.generated.resources.main_start_failed_message
import tuindice.maincore.generated.resources.main_start_failed_retry
import tuindice.maincore.generated.resources.main_start_failed_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuIndiceScreen(
	state: Main.State,
	shellState: MainShellState,
	onRetryStartUp: () -> Unit,
	navController: NavHostController,
	isSwipeBackNavigationEnabled: Boolean = false,
	snackbarHostState: SnackbarHostState,
	onAction: (action: TopBarAction) -> Unit,
	onRecordViewModeChange: ((RecordViewMode) -> Unit)?,
	onRecordViewModeChangeAvailable: (((RecordViewMode) -> Unit)?) -> Unit,
	onRecordTermSelectionAvailable: ((() -> Unit)?) -> Unit = {},
	onNavigateTo: (destination: Destination) -> Unit,
	onNavigateBack: () -> Unit,
	onConfirmExitClick: () -> Unit,
	isCameraAvailable: Boolean,
	onNavigateToExternalResource: (url: String) -> Unit,
	onWizardFinished: () -> Unit = {},
	onViewStateChanged: (ViewState) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	dismissSnackBar: () -> Unit = {}
) {
	when (state) {
		is Main.State.Starting -> {
			Box(modifier = Modifier.fillMaxSize()) {
				CircularProgressIndicator(
					modifier = Modifier
						.testTag(MaincoreUiTags.TuIndiceStartingIndicator)
						.align(Alignment.Center)
				)
			}
			return
		}

		is Main.State.Failed -> {
			ErrorView(
				title = stringResource(Res.string.main_start_failed_title),
				message = stringResource(Res.string.main_start_failed_message),
				retryText = stringResource(Res.string.main_start_failed_retry),
				onRetryClick = onRetryStartUp,
				headerContent = { ErrorStateAnimationView() }
			)
			return
		}

		is Main.State.Content -> Unit
	}

	val contentState = state
	val canNavigateBack = navController.canNavigateBackFromCurrentDestination()
	val shouldShowTopBarBackButton = canNavigateBack && shellState.showsTopBarBackButton
	val topBarBannerBehavior = remember {
		mutableStateOf<TopBarBannerBehavior?>(null)
	}
	val topBarBannerRequestKey = remember {
		mutableIntStateOf(0)
	}
	val showTopBarBanner: (TopBarBannerBehavior) -> Unit = { behavior ->
		topBarBannerBehavior.value = behavior
		topBarBannerRequestKey.intValue += 1
	}

	val bottomBarConfigs = remember {
		listOf(
			BottomBarConfig.Summary,
			BottomBarConfig.Record,
			BottomBarConfig.Pensum,
			BottomBarConfig.Evaluations,
			BottomBarConfig.About
		)
	}

		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(MaterialTheme.colorScheme.background)
		) {
			Scaffold(
				containerColor = MaterialTheme.colorScheme.background,
				snackbarHost = {
					SnackbarHost(snackbarHostState) { snackbarData ->
						Snackbar(
							modifier = Modifier.testTag(BaseUiTags.SnackbarContainer),
							action = snackbarData.visuals.actionLabel?.let { actionLabel ->
								{
									TextButton(
										modifier = Modifier.testTag(BaseUiTags.SnackbarActionButton),
										onClick = snackbarData::performAction
									) {
										Text(text = actionLabel)
									}
								}
							}
						) {
							Text(
								modifier = Modifier.testTag(BaseUiTags.SnackbarMessage),
								text = snackbarData.visuals.message
							)
						}
					}
				},
				topBar = {
				if (shellState.isTopBarVisible) {
					val recordTopBarViewModeState = shellState.recordTopBarViewModeState
					val topBarContainerColor = MaterialTheme.colorScheme.surface
					val topBarContentColor = MaterialTheme.colorScheme.onSurface
					val onRecordTopBarViewModeSelected =
						if (onRecordViewModeChange == null) null
						else { mode: RecordViewMode -> onRecordViewModeChange(mode) }

					Column(
						modifier = Modifier.fillMaxWidth()
					) {
						Box(
							modifier = Modifier
								.fillMaxWidth()
								.background(topBarContainerColor)
								.windowInsetsPadding(
									WindowInsets.statusBars.only(WindowInsetsSides.Top)
								)
						) {
							TopAppBar(
								expandedHeight = InternalScreenDefaults.TopBarHeight,
								windowInsets = WindowInsets(left = 0, top = 0, right = 0, bottom = 0),
								title = {
									TopAppBarAnimatedTitleView(
										title = shellState.topBarTitle.asString(),
										modifier = Modifier.offset(
											y = InternalScreenDefaults.TopBarContentVerticalOffset
										)
									)
								},
								actions = {
									Row(
										modifier = Modifier.offset(
											y = InternalScreenDefaults.TopBarContentVerticalOffset
										),
										verticalAlignment = Alignment.CenterVertically
									) {
										if (
											recordTopBarViewModeState != null &&
											onRecordTopBarViewModeSelected != null
										) {
											RecordTopBarViewModeSwitchView(
												selectedMode = recordTopBarViewModeState.selectedMode,
												onModeSelected = onRecordTopBarViewModeSelected
											)
										}

										TopAppBarActionsView(
											topBarConfig = shellState.topBarConfig,
											onAction = onAction,
											actionIconContent = { action ->
												Icon(
													imageVector = action.getIcon(),
													contentDescription = null
												)
											}
										)
									}
								},
								navigationIcon = {
									if (shouldShowTopBarBackButton) {
										IconButton(
											modifier = Modifier
												.offset(y = InternalScreenDefaults.TopBarContentVerticalOffset)
												.testTag(MaincoreUiTags.TuIndiceTopBarBackButton),
											onClick = onNavigateBack
										) {
											Icon(
												imageVector = Icons.AutoMirrored.Filled.ArrowBack,
												contentDescription = null
											)
										}
									}
								},
								colors = TopAppBarDefaults.topAppBarColors(
									containerColor = topBarContainerColor,
									titleContentColor = topBarContentColor,
									navigationIconContentColor = topBarContentColor,
									actionIconContentColor = topBarContentColor
								)
							)
						}

						TopBarBannerHost(
							modifier = Modifier.fillMaxWidth(),
							isContentAvailable = recordTopBarViewModeState != null,
							requestKey = topBarBannerRequestKey.intValue,
							behavior = topBarBannerBehavior.value
						) {
							if (recordTopBarViewModeState != null) {
								RecordTopBarViewModeBannerView(
									selectedMode = recordTopBarViewModeState.selectedMode
								)
							}
						}
					}
				}
			},
			bottomBar = {
				if (shellState.isBottomBarVisible) {
					val bottomBarContainerColor = MaterialTheme.colorScheme.onSecondary

					Box(
						modifier = Modifier
							.fillMaxWidth()
							.background(bottomBarContainerColor)
							.windowInsetsPadding(
								WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
							)
							.testTag(MaincoreUiTags.TuIndiceBottomBar)
					) {
						NavigationBar(
							modifier = Modifier
								.fillMaxWidth()
								.height(InternalScreenDefaults.BottomBarHeight),
							containerColor = bottomBarContainerColor,
							tonalElevation = 0.dp,
							windowInsets = WindowInsets(left = 0, top = 0, right = 0, bottom = 0)
						) {
							bottomBarConfigs.forEach { bottomBarConfig ->
								val isNavigationBarItemSelected = navController
									.isCurrentDestination(destination = bottomBarConfig.destination)

								val navigationBarItemIcon =
									bottomBarIcon(
										config = bottomBarConfig,
										selected = isNavigationBarItemSelected
									)

								NavigationBarItem(
									modifier = Modifier.testTag(bottomBarItemTag(bottomBarConfig)),
									icon = {
										Icon(
											modifier = if (isNavigationBarItemSelected)
												Modifier.testTag(bottomBarSelectedItemTag(bottomBarConfig))
											else Modifier,
											imageVector = navigationBarItemIcon,
											contentDescription = null
										)
									},
									colors = NavigationBarItemDefaults.colors(
										indicatorColor = MaterialTheme.colorScheme.secondaryContainer
									),
									selected = isNavigationBarItemSelected,
									onClick = { onNavigateTo(bottomBarConfig.destination) }
								)
							}
						}
					}
				}
			}
		) { innerPadding ->
			TuIndiceNavHost(
				navController = navController,
				startDestination = contentState.startDestination,
				isSwipeBackNavigationEnabled = isSwipeBackNavigationEnabled,
				modifier = Modifier.padding(innerPadding),
				onConfirmExitClick = onConfirmExitClick,
				isCameraAvailable = isCameraAvailable,
				onNavigateToExternalResource = onNavigateToExternalResource,
				onRecordViewModeChangeAvailable = onRecordViewModeChangeAvailable,
				onRecordTermSelectionAvailable = onRecordTermSelectionAvailable,
				onWizardFinished = onWizardFinished,
				showTopBarBanner = showTopBarBanner,
				onViewStateChanged = onViewStateChanged,
				showSnackBar = showSnackBar,
				dismissSnackBar = dismissSnackBar
			)
		}

	}
}

private fun TopBarAction.getIcon(): ImageVector {
	return when (this) {
		is TopBarAction.SignOutAction ->
			Icons.AutoMirrored.Outlined.Logout
		is TopBarAction.FetchEnrollmentProofAction ->
			Icons.Outlined.FindInPage
		is TopBarAction.RecordTermSelectionAction ->
			Icons.Outlined.DateRange
		is TopBarAction.SearchPensumAction ->
			Icons.Outlined.Search
		is TopBarAction.ChangePensumAction ->
			Icons.Outlined.Tune
	}
}

private fun bottomBarIcon(
	config: BottomBarConfig,
	selected: Boolean
): ImageVector = when (config) {
	BottomBarConfig.Summary ->
		if (selected) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder
	BottomBarConfig.Record ->
		if (selected) Icons.Filled.Book else Icons.Outlined.Book
	BottomBarConfig.Pensum ->
		if (selected) Icons.Filled.AccountTree else Icons.Outlined.AccountTree
	BottomBarConfig.Evaluations ->
		if (selected) Icons.AutoMirrored.Filled.Assignment else Icons.AutoMirrored.Outlined.Assignment
	BottomBarConfig.About ->
		if (selected) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder
}

private fun bottomBarItemTag(config: BottomBarConfig): String = when (config) {
	BottomBarConfig.Summary -> MaincoreUiTags.TuIndiceBottomBarSummaryItem
	BottomBarConfig.Record -> MaincoreUiTags.TuIndiceBottomBarRecordItem
	BottomBarConfig.Pensum -> MaincoreUiTags.TuIndiceBottomBarPensumItem
	BottomBarConfig.Evaluations -> MaincoreUiTags.TuIndiceBottomBarEvaluationsItem
	BottomBarConfig.About -> MaincoreUiTags.TuIndiceBottomBarAboutItem
}

private fun bottomBarSelectedItemTag(config: BottomBarConfig): String = when (config) {
	BottomBarConfig.Summary -> MaincoreUiTags.TuIndiceBottomBarSummaryItemSelected
	BottomBarConfig.Record -> MaincoreUiTags.TuIndiceBottomBarRecordItemSelected
	BottomBarConfig.Pensum -> MaincoreUiTags.TuIndiceBottomBarPensumItemSelected
	BottomBarConfig.Evaluations -> MaincoreUiTags.TuIndiceBottomBarEvaluationsItemSelected
	BottomBarConfig.About -> MaincoreUiTags.TuIndiceBottomBarAboutItemSelected
}
