package com.gdavidpb.tuindice.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation3.runtime.rememberNavBackStack
import com.gdavidpb.tuindice.about.presentation.navigation.AboutDestination
import com.gdavidpb.tuindice.auth.presentation.navigation.AuthDestination
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.evaluations.presentation.navigation.EvaluationsDestination
import com.gdavidpb.tuindice.pensum.presentation.navigation.PensumDestination
import com.gdavidpb.tuindice.presentation.mapper.toTabSectionOrNull
import com.gdavidpb.tuindice.record.presentation.navigation.RecordDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import org.koin.compose.viewmodel.koinViewModel

private val rootModeSaver = Saver<TuIndiceRootMode, String>(
	save = { rootMode -> rootMode.name },
	restore = { name -> TuIndiceRootMode.valueOf(name) }
)

private val mainSectionSaver = Saver<MainSection, String>(
	save = { section -> section.name },
	restore = { name -> MainSection.valueOf(name) }
)

@Composable
fun rememberTuIndiceNavigator(
	startKey: Destination,
	storesViewModel: NavEntryStoresViewModel = koinViewModel<NavEntryStoresViewModel>()
): TuIndiceNavigator {
	val startSection = startKey.toTabSectionOrNull()
	val isAuthStartKey = startKey is AuthDestination.SignIn

	val startTabState = rememberSaveable(stateSaver = mainSectionSaver) {
		mutableStateOf(startSection ?: MainSection.SUMMARY)
	}
	val rootModeState = rememberSaveable(stateSaver = rootModeSaver) {
		mutableStateOf(if (isAuthStartKey) TuIndiceRootMode.AUTH else TuIndiceRootMode.SIGNED_IN)
	}
	val currentTabState = rememberSaveable(stateSaver = mainSectionSaver) {
		mutableStateOf(startTabState.value)
	}
	val isStartKeyApplied = rememberSaveable {
		mutableStateOf(false)
	}

	val summaryStack = rememberNavBackStack(TuIndiceSavedStateConfiguration, SummaryDestination.Summary)
	val recordStack = rememberNavBackStack(TuIndiceSavedStateConfiguration, RecordDestination.Record)
	val pensumStack = rememberNavBackStack(TuIndiceSavedStateConfiguration, PensumDestination.Pensum)
	val evaluationsStack = rememberNavBackStack(TuIndiceSavedStateConfiguration, EvaluationsDestination.Evaluations)
	val aboutStack = rememberNavBackStack(TuIndiceSavedStateConfiguration, AboutDestination.About)
	val authStack = rememberNavBackStack(TuIndiceSavedStateConfiguration, AuthDestination.SignIn)

	return remember {
		TuIndiceNavigator(
			startTab = startTabState.value,
			rootModeState = rootModeState,
			currentTabState = currentTabState,
			stacks = TuIndiceNavStacks(
				authStack = authStack,
				tabStacks = mapOf(
					MainSection.SUMMARY to summaryStack,
					MainSection.RECORD to recordStack,
					MainSection.PENSUM to pensumStack,
					MainSection.EVALUATIONS to evaluationsStack,
					MainSection.ABOUT to aboutStack
				)
			),
			entryStores = storesViewModel.entryStores,
			resultStore = storesViewModel.resultStore
		).also { navigator ->
			if (!isStartKeyApplied.value) {
				isStartKeyApplied.value = true

				if (!isAuthStartKey && startSection == null) navigator.push(startKey)
			}
		}
	}
}
