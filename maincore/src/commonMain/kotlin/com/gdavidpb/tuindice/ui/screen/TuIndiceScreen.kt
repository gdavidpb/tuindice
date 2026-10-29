package com.gdavidpb.tuindice.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.TableChart
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.ProfileIdentity
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.model.TopBarBannerBehavior
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.presentation.navigation.AssistantLauncher
import com.gdavidpb.tuindice.base.presentation.navigation.LocalAssistantLauncher
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.base.ui.icon.ShareIcon
import com.gdavidpb.tuindice.base.ui.icon.rememberAssistantSparkleIcon
import com.gdavidpb.tuindice.base.ui.style.InternalScreenDefaults
import com.gdavidpb.tuindice.base.ui.style.TuIndiceShellColors
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.base.ui.view.ErrorView
import com.gdavidpb.tuindice.base.ui.view.OutdatedAppScreen
import com.gdavidpb.tuindice.base.ui.view.TopAppBarActionsView
import com.gdavidpb.tuindice.base.ui.view.TopAppBarAnimatedTitleView
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.model.BottomBarConfig
import com.gdavidpb.tuindice.presentation.model.MainShellState
import com.gdavidpb.tuindice.presentation.model.TopBarSlot
import com.gdavidpb.tuindice.presentation.navigation.TuIndiceNavigator
import com.gdavidpb.tuindice.presentation.navigation.openAssistant
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.ui.view.RecordTopBarViewModeBannerView
import com.gdavidpb.tuindice.record.ui.view.RecordTopBarViewModeSwitchView
import com.gdavidpb.tuindice.ui.MaincoreUiTags
import com.gdavidpb.tuindice.ui.view.TopBarAssistantButtonView
import com.gdavidpb.tuindice.ui.view.TopBarBannerHost
import com.gdavidpb.tuindice.ui.view.TopBarProfileButtonView
import com.gdavidpb.tuindice.wizard.presentation.contract.CoachmarkOverlay
import com.gdavidpb.tuindice.wizard.presentation.model.CoachmarkId
import com.gdavidpb.tuindice.wizard.ui.anchor.CoachmarkAnchorRegistry
import com.gdavidpb.tuindice.wizard.ui.anchor.coachmarkAnchor
import com.gdavidpb.tuindice.wizard.ui.view.CoachmarkOverlayHost
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import tuindice.maincore.generated.resources.Res
import tuindice.maincore.generated.resources.a11y_navigate_back
import tuindice.maincore.generated.resources.a11y_top_bar_assistant
import tuindice.maincore.generated.resources.a11y_top_bar_change_pensum
import tuindice.maincore.generated.resources.a11y_top_bar_enrollment_proof
import tuindice.maincore.generated.resources.a11y_top_bar_record_schedule
import tuindice.maincore.generated.resources.a11y_top_bar_record_term_selection
import tuindice.maincore.generated.resources.a11y_top_bar_search_pensum
import tuindice.maincore.generated.resources.a11y_top_bar_settings
import tuindice.maincore.generated.resources.a11y_top_bar_statistics_share
import tuindice.maincore.generated.resources.a11y_top_bar_term_report_share
import tuindice.maincore.generated.resources.app_availability_notice_default_message
import tuindice.maincore.generated.resources.app_availability_notice_default_title
import tuindice.maincore.generated.resources.bottom_bar_evaluations
import tuindice.maincore.generated.resources.bottom_bar_pensum
import tuindice.maincore.generated.resources.bottom_bar_record
import tuindice.maincore.generated.resources.bottom_bar_summary
import tuindice.maincore.generated.resources.main_start_failed_message
import tuindice.maincore.generated.resources.main_start_failed_retry
import tuindice.maincore.generated.resources.main_start_failed_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuIndiceScreen(
	state: Main.State,
	shellState: MainShellState,
	onRetryStartUp: () -> Unit,
	onUpdateAppClick: () -> Unit,
	navigator: TuIndiceNavigator?,
	snackbarHostState: SnackbarHostState,
	profileIdentity: ProfileIdentity? = null,
	onAction: (action: TopBarAction) -> Unit,
	onSignOutRequest: () -> Unit,
	onRecordViewModeChange: ((RecordViewMode) -> Unit)?,
	onRecordViewModeChangeAvailable: (((RecordViewMode) -> Unit)?) -> Unit,
	onRecordTermSelectionAvailable: ((() -> Unit)?) -> Unit = {},
	onBackInterceptorAvailable: ((() -> Boolean)?) -> Unit = {},
	onNavigateToEvaluationsWeek: (weekStart: LocalDate, evaluationId: String) -> Unit = { _, _ -> },
	onNavigateToRecordAttempt: (attemptId: String) -> Unit = {},
	onNavigateTo: (section: MainSection) -> Unit,
	onNavigateBack: () -> Unit,
	onConfirmExitClick: () -> Unit,
	onNavigateToExternalResource: (url: String) -> Unit,
	onOutdatedAppDetected: () -> Unit = {},
	onUpdatePasswordDismissRequest: () -> Unit = {},
	coachmarkOverlayState: CoachmarkOverlay.State = CoachmarkOverlay.State(),
	onCoachmarkPreviousActionClick: () -> Unit = {},
	onCoachmarkPrimaryActionClick: () -> Unit = {},
	onViewStateChanged: (ViewState) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	dismissSnackBar: () -> Unit = {}
) {
	when (state) {
		is Main.State.Starting -> {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(MaterialTheme.colorScheme.background)
					.windowInsetsPadding(WindowInsets.systemBars)
			) {
				CircularProgressIndicator(
					modifier = Modifier
						.testTag(MaincoreUiTags.TuIndiceStartingIndicator)
						.align(Alignment.Center)
				)
			}
			return
		}

		is Main.State.Failed -> {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(MaterialTheme.colorScheme.background)
					.windowInsetsPadding(WindowInsets.systemBars)
			) {
				ErrorView(
					title = stringResource(Res.string.main_start_failed_title),
					message = stringResource(Res.string.main_start_failed_message),
					retryText = stringResource(Res.string.main_start_failed_retry),
					onRetryClick = onRetryStartUp,
					headerContent = { ErrorStateAnimationView() }
				)
			}
			return
		}

		is Main.State.AppUnavailable -> {
			AppAvailabilityNoticeScreen(
				title = state.notice.title.takeIf { it.isNotBlank() }
					?: stringResource(Res.string.app_availability_notice_default_title),
				message = state.notice.message.takeIf { it.isNotBlank() }
					?: stringResource(Res.string.app_availability_notice_default_message),
				retryText = stringResource(Res.string.main_start_failed_retry),
				onRetryClick = onRetryStartUp
			)
			return
		}

		is Main.State.OutdatedApp -> {
			OutdatedAppScreen(
				onUpdateClick = onUpdateAppClick
			)
			return
		}

		is Main.State.Content -> Unit
	}

	val contentNavigator = checkNotNull(navigator) {
		"TuIndiceScreen requires a navigator once Main.State.Content is reached"
	}
	val assistantLauncher = remember(contentNavigator, shellState.isAssistantVisible) {
		if (shellState.isAssistantVisible) {
			AssistantLauncher { context -> contentNavigator.openAssistant(context) }
		} else {
			null
		}
	}

	val shouldShowTopBarBackButton =
		contentNavigator.showsBackButton && shellState.showsTopBarBackButton
	val topBarBannerBehavior = remember {
		mutableStateOf<TopBarBannerBehavior?>(null)
	}
	val topBarBannerRequestKey = remember {
		mutableIntStateOf(0)
	}
	val coachmarkAnchorRegistry = remember {
		CoachmarkAnchorRegistry()
	}
	// The Settings coachmark points at the avatar in the bar; every other one points at the content under it.
	val activeCoachmarkId = coachmarkOverlayState.activeCoachmark?.id
	val settingsCoachmarkActive = activeCoachmarkId == CoachmarkId.Settings
	val showTopBarBanner: (TopBarBannerBehavior) -> Unit = { behavior ->
		topBarBannerBehavior.value = behavior
		topBarBannerRequestKey.intValue += 1
	}

	val bottomBarConfigs = remember {
		listOf(
			BottomBarConfig.Summary,
			BottomBarConfig.Record,
			BottomBarConfig.Pensum,
			BottomBarConfig.Evaluations
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
					val topBarContainerColor = TuIndiceShellColors.topBarContainer()
					val topBarContentColor = TuIndiceShellColors.topBarContent()
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
										// The places that lead the bar come first: the assistant, then Record's view switch.
										// The actions follow them, and the overflow menu closes the row.
										shellState.inlineActions.forEach { slot ->
											when (slot) {
												TopBarSlot.Assistant -> TopBarAssistantButtonView(
													contentDescription = stringResource(Res.string.a11y_top_bar_assistant),
													onClick = { onAction(TopBarAction.AssistantAction) }
												)
												TopBarSlot.ViewModeSelector -> {
													if (
														recordTopBarViewModeState != null &&
														onRecordTopBarViewModeSelected != null
													) {
														RecordTopBarViewModeSwitchView(
															selectedMode = recordTopBarViewModeState.selectedMode,
															onModeSelected = onRecordTopBarViewModeSelected
														)
													}
												}
												is TopBarSlot.Action -> Unit
											}
										}

										val inlineTopBarActions = shellState.inlineActions
											.filterIsInstance<TopBarSlot.Action>()
											.map { slot -> slot.action }

										if (inlineTopBarActions.isNotEmpty() || shellState.overflowActions.isNotEmpty()) {
											TopAppBarActionsView(
												actions = inlineTopBarActions,
												overflowActions = shellState.overflowActions,
												onAction = onAction,
												actionContentDescription = { action -> action.getContentDescription() },
												actionIconContent = { action ->
													Icon(
														imageVector = action.getIcon(),
														contentDescription = null
													)
												}
											)
										}

										// The avatar closes the row of a main section, after the sparkle, the switch, the
										// actions and the menu. When Settings is the coachmark on screen, the bubble points at it.
										if (shellState.showsProfileAvatar) {
											TopBarProfileButtonView(
												modifier = Modifier.coachmarkAnchor(
													id = CoachmarkId.Settings.takeIf { settingsCoachmarkActive },
													registry = coachmarkAnchorRegistry
												),
												identity = profileIdentity,
												contentDescription = stringResource(Res.string.a11y_top_bar_settings),
												onClick = { onAction(TopBarAction.SettingsAction) }
											)
										}
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
												contentDescription = stringResource(Res.string.a11y_navigate_back)
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
					val bottomBarContainerColor = TuIndiceShellColors.bottomBarContainer()

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
								val isNavigationBarItemSelected =
									contentNavigator.currentTab == bottomBarConfig.section

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
											contentDescription = bottomBarLabel(config = bottomBarConfig)
										)
									},
									colors = NavigationBarItemDefaults.colors(
										indicatorColor = TuIndiceShellColors.bottomBarIndicator()
									),
									selected = isNavigationBarItemSelected,
									onClick = { onNavigateTo(bottomBarConfig.section) }
								)
							}
						}
					}
				}
			}
		) { innerPadding ->
			CompositionLocalProvider(LocalAssistantLauncher provides assistantLauncher) {
				TuIndiceNavDisplay(
					navigator = contentNavigator,
					modifier = Modifier
						.padding(innerPadding)
						// What the shell already pads (the bars, the home indicator) is consumed, so a screen's imePadding()
						// adds only the part of the keyboard that rises above it, not the keyboard's whole height again.
						.consumeWindowInsets(innerPadding)
						.coachmarkAnchor(
							id = activeCoachmarkId.takeUnless { settingsCoachmarkActive },
							registry = coachmarkAnchorRegistry
						),
					onConfirmExitClick = onConfirmExitClick,
					onSignOutRequest = onSignOutRequest,
					onNavigateToExternalResource = onNavigateToExternalResource,
					onOutdatedAppDetected = onOutdatedAppDetected,
					onUpdatePasswordDismissRequest = onUpdatePasswordDismissRequest,
					onRecordViewModeChangeAvailable = onRecordViewModeChangeAvailable,
					onRecordTermSelectionAvailable = onRecordTermSelectionAvailable,
					onBackInterceptorAvailable = onBackInterceptorAvailable,
					onNavigateToEvaluationsWeek = onNavigateToEvaluationsWeek,
					onNavigateToRecordAttempt = onNavigateToRecordAttempt,
					showTopBarBanner = showTopBarBanner,
					onViewStateChanged = onViewStateChanged,
					showSnackBar = showSnackBar,
					dismissSnackBar = dismissSnackBar
				)
			}
		}

		CoachmarkOverlayHost(
			state = coachmarkOverlayState,
			anchorRegistry = coachmarkAnchorRegistry,
			onPreviousActionClick = onCoachmarkPreviousActionClick,
			onPrimaryActionClick = onCoachmarkPrimaryActionClick,
			modifier = Modifier
				.fillMaxSize()
				.padding(
					bottom = if (shellState.isBottomBarVisible) {
						InternalScreenDefaults.BottomBarHeight
					} else {
						0.dp
					}
				)
		)

	}
}

@Composable
private fun TopBarAction.getContentDescription(): String {
	return when (this) {
		is TopBarAction.FetchEnrollmentProofAction ->
			stringResource(Res.string.a11y_top_bar_enrollment_proof)
		is TopBarAction.RecordTermSelectionAction ->
			stringResource(Res.string.a11y_top_bar_record_term_selection)
		is TopBarAction.RecordScheduleAction ->
			stringResource(Res.string.a11y_top_bar_record_schedule)
		// The same schedule, the same words as Record's action: one icon says one thing across the app.
		is TopBarAction.EvaluationsScheduleAction ->
			stringResource(Res.string.a11y_top_bar_record_schedule)
		is TopBarAction.SearchPensumAction ->
			stringResource(Res.string.a11y_top_bar_search_pensum)
		is TopBarAction.ChangePensumAction ->
			stringResource(Res.string.a11y_top_bar_change_pensum)
		is TopBarAction.StatisticsShareAction ->
			stringResource(Res.string.a11y_top_bar_statistics_share)
		is TopBarAction.TermReportShareAction ->
			stringResource(Res.string.a11y_top_bar_term_report_share)
		is TopBarAction.AssistantAction ->
			stringResource(Res.string.a11y_top_bar_assistant)
		is TopBarAction.SettingsAction ->
			stringResource(Res.string.a11y_top_bar_settings)
	}
}

@Composable
private fun TopBarAction.getIcon(): ImageVector {
	return when (this) {
		is TopBarAction.FetchEnrollmentProofAction ->
			Icons.Outlined.FindInPage
		// A term is a stretch of the year and the schedule is a table of blocks: a calendar and a
		// table, so the two actions cannot be taken for each other (both used to be calendars).
		is TopBarAction.RecordTermSelectionAction ->
			Icons.Outlined.CalendarMonth
		is TopBarAction.RecordScheduleAction ->
			Icons.Outlined.TableChart
		is TopBarAction.EvaluationsScheduleAction ->
			Icons.Outlined.TableChart
		is TopBarAction.SearchPensumAction ->
			Icons.Outlined.Search
		is TopBarAction.ChangePensumAction ->
			Icons.Outlined.Tune
		is TopBarAction.StatisticsShareAction ->
			ShareIcon
		is TopBarAction.TermReportShareAction ->
			ShareIcon
		is TopBarAction.AssistantAction ->
			rememberAssistantSparkleIcon()
		// The bar draws the avatar for this one; the icon is what a menu would show if it were ever listed in one.
		is TopBarAction.SettingsAction ->
			Icons.Outlined.Person
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
}

@Composable
private fun bottomBarLabel(config: BottomBarConfig): String = when (config) {
	BottomBarConfig.Summary -> stringResource(Res.string.bottom_bar_summary)
	BottomBarConfig.Record -> stringResource(Res.string.bottom_bar_record)
	BottomBarConfig.Pensum -> stringResource(Res.string.bottom_bar_pensum)
	BottomBarConfig.Evaluations -> stringResource(Res.string.bottom_bar_evaluations)
}

private fun bottomBarItemTag(config: BottomBarConfig): String = when (config) {
	BottomBarConfig.Summary -> MaincoreUiTags.TuIndiceBottomBarSummaryItem
	BottomBarConfig.Record -> MaincoreUiTags.TuIndiceBottomBarRecordItem
	BottomBarConfig.Pensum -> MaincoreUiTags.TuIndiceBottomBarPensumItem
	BottomBarConfig.Evaluations -> MaincoreUiTags.TuIndiceBottomBarEvaluationsItem
}

private fun bottomBarSelectedItemTag(config: BottomBarConfig): String = when (config) {
	BottomBarConfig.Summary -> MaincoreUiTags.TuIndiceBottomBarSummaryItemSelected
	BottomBarConfig.Record -> MaincoreUiTags.TuIndiceBottomBarRecordItemSelected
	BottomBarConfig.Pensum -> MaincoreUiTags.TuIndiceBottomBarPensumItemSelected
	BottomBarConfig.Evaluations -> MaincoreUiTags.TuIndiceBottomBarEvaluationsItemSelected
}
