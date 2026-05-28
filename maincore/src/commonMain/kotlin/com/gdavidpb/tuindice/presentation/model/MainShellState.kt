package com.gdavidpb.tuindice.presentation.model

import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.presentation.model.RecordRouteViewState
import com.gdavidpb.tuindice.record.presentation.model.RecordTopBarViewModeState
import com.gdavidpb.tuindice.wizard.presentation.model.WizardRouteViewState

data class MainShellState(
	val topBarTitle: UiText = UiText.Empty,
	val topBarConfig: TopBarConfig? = null,
	val isTopBarVisible: Boolean = false,
	val isBottomBarVisible: Boolean = false,
	val recordTopBarViewModeState: RecordTopBarViewModeState? = null,
	val showsTopBarBackButton: Boolean = true
)

fun ViewState.toMainShellState(): MainShellState = MainShellState(
	topBarTitle = topBarTitle,
	topBarConfig = topBarConfig,
	isTopBarVisible = isTopBarVisible,
	isBottomBarVisible = isBottomBarVisible,
	showsTopBarBackButton = this !is WizardRouteViewState,
	recordTopBarViewModeState = when (this) {
		is RecordRouteViewState -> topBarViewModeState
		is WizardRouteViewState -> topBarViewModeState
		else -> null
	}
)
