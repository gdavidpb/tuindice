package com.gdavidpb.tuindice.testing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.auth.presentation.navigation.AuthDestination
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.presentation.navigation.NavResultStore
import com.gdavidpb.tuindice.presentation.mapper.toTabRootDestination
import com.gdavidpb.tuindice.presentation.mapper.toTabSectionOrNull
import com.gdavidpb.tuindice.presentation.navigation.NavEntryStores
import com.gdavidpb.tuindice.presentation.navigation.TuIndiceNavStacks
import com.gdavidpb.tuindice.presentation.navigation.TuIndiceNavigator
import com.gdavidpb.tuindice.presentation.navigation.TuIndiceRootMode

fun createTestNavigator(startKey: Destination): TuIndiceNavigator {
	val startSection = startKey.toTabSectionOrNull()
	val isAuthStartKey = startKey is AuthDestination.SignIn

	return TuIndiceNavigator(
		startTab = startSection ?: MainSection.SUMMARY,
		rootModeState = mutableStateOf(
			if (isAuthStartKey) TuIndiceRootMode.AUTH else TuIndiceRootMode.SIGNED_IN
		),
		currentTabState = mutableStateOf(startSection ?: MainSection.SUMMARY),
		stacks = TuIndiceNavStacks(
			authStack = NavBackStack(AuthDestination.SignIn),
			tabStacks = MainSection.entries.associateWith { section ->
				NavBackStack<NavKey>(section.toTabRootDestination())
			}
		),
		entryStores = NavEntryStores(),
		resultStore = NavResultStore()
	).also { navigator ->
		if (!isAuthStartKey && startSection == null) navigator.push(startKey)
	}
}

@Composable
fun rememberTestNavigator(startKey: Destination): TuIndiceNavigator = remember {
	createTestNavigator(startKey = startKey)
}
