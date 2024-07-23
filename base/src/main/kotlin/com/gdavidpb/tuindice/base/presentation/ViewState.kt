package com.gdavidpb.tuindice.base.presentation

abstract class ViewState(
	open val topBarTitle: String = "",
	open val isTopBarVisible: Boolean = false,
	open val isBottomBarVisible: Boolean = false
)