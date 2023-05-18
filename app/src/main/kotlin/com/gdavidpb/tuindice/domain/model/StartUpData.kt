package com.gdavidpb.tuindice.domain.model

import com.gdavidpb.tuindice.base.presentation.navigation.Destination

data class StartUpData(
	val startDestination: Destination,
	val currentDestination: Destination,
	val destinations: List<Destination>
)
