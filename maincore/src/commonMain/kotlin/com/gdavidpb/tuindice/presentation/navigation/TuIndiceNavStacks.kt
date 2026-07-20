package com.gdavidpb.tuindice.presentation.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.base.domain.model.MainSection

class TuIndiceNavStacks(
	val authStack: NavBackStack<NavKey>,
	val tabStacks: Map<MainSection, NavBackStack<NavKey>>
)
