package com.gdavidpb.tuindice.domain.model

import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.presentation.navigation.Destination

data class StartUpData(
	val title: String,
	val startDestination: Destination,
	val currentDestination: Destination,
	val destinations: Map<String, Destination>,
	val topBarConfig: TopBarConfig?
)
