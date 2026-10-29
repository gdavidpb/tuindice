package com.gdavidpb.tuindice.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.platform.LocalClipboard
import com.gdavidpb.tuindice.about.presentation.navigation.AboutDestination
import com.gdavidpb.tuindice.assistant.presentation.navigation.AssistantDestination
import com.gdavidpb.tuindice.assistant.presentation.navigation.AssistantNavDependencies
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.enrollmentproof.presentation.navigation.EnrollmentProofDestination
import com.gdavidpb.tuindice.evaluations.presentation.navigation.EvaluationsDestination
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.usecase.GetTermKeyUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetRecordViewModeUseCase
import com.gdavidpb.tuindice.record.presentation.navigation.RecordDestination
import com.gdavidpb.tuindice.subjects.presentation.navigation.SubjectsDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.getKoin

/**
 * What the assistant's screens ask of the host, connected to this navigator. Every screen the assistant can open
 * goes to its jump in the app, and the chat sheet is closed first, so it is not left parked on the tab it came
 * from. A reference the target needs and the assistant did not send falls back to the tab of that screen.
 * Sign-in and the password dialog are not opened from the assistant.
 */
internal fun TuIndiceNavigator.toAssistantNavDependencies(
	scope: CoroutineScope,
	writeRecordViewMode: suspend (RecordViewMode) -> Unit,
	termKeyOf: suspend (termId: String) -> String?,
	copyText: (String) -> Unit
) = AssistantNavDependencies(
	onNavigateTo = { target, refs ->
		navigateFromAssistant(
			target = target,
			refs = refs,
			scope = scope,
			writeRecordViewMode = writeRecordViewMode,
			termKeyOf = termKeyOf
		)
	},
	// The proof opens over the chat, as the dialogs the chat offers do, so its back returns to the chat.
	onOpenEnrollmentProof = { push(EnrollmentProofDestination.EnrollmentProofDialog) },
	onCopyText = copyText
)

/**
 * The assistant's jumps as the host gives them. The Projection jump saves the mode through the same use case as
 * Record's switch, and the share sheet finds the key of the term the assistant names in the local record. Those use
 * cases are looked up only when the assistant asks for them, so no screen needs the Record bindings just to be drawn.
 * Copied text goes to the clipboard of the platform.
 */
@Composable
internal fun rememberAssistantNavDependencies(navigator: TuIndiceNavigator): AssistantNavDependencies {
	val koin = getKoin()
	val scope = rememberCoroutineScope()
	val clipboard = LocalClipboard.current

	return navigator.toAssistantNavDependencies(
		scope = scope,
		writeRecordViewMode = { mode ->
			koin.get<SetRecordViewModeUseCase>().execute(mode).collect { }
		},
		termKeyOf = { termId ->
			koin.get<GetTermKeyUseCase>().termKeyOrNull(termId)
		},
		copyText = { text ->
			scope.launch { clipboard.setClipEntry(plainTextClipEntry(text)) }
		}
	)
}

private fun TuIndiceNavigator.navigateFromAssistant(
	target: NavTarget,
	refs: List<EntityRef>,
	scope: CoroutineScope,
	writeRecordViewMode: suspend (RecordViewMode) -> Unit,
	termKeyOf: suspend (termId: String) -> String?
) {
	when (target) {
		// This is what tapping Record's view switch does: the Projection mode is saved, then the tab opens. If the save
		// fails, the tab still opens, in the mode it has.
		NavTarget.RECORD_PROJECTION -> scope.launch {
			writeRecordViewMode(RecordViewMode.Projection)
			goFromAssistant(section = MainSection.RECORD)
		}

		// The term the assistant names is the record's id of it; the sheet opens on its key. Without a term, or when the
		// record does not know it, the sheet opens on the newest term.
		NavTarget.SHARE_TERM -> scope.launch {
			val termKey = refs.idOf(EntityKind.TERM)?.let { termId -> termKeyOf(termId) }
			goFromAssistant(
				section = MainSection.SUMMARY,
				keys = listOf(SummaryDestination.Statistics, SummaryDestination.ShareTermDialog(termKey = termKey))
			)
		}

		NavTarget.SIGN_IN, NavTarget.UPDATE_PASSWORD -> Unit

		else -> goFromAssistant(section = target.tabSection(), keys = target.destinationKeys(refs))
	}
}

// The tab a screen of the assistant lives on. Null for the screens that open over the tab on screen, which includes
// Settings: the wire still says ABOUT, as it did when that was the fifth tab, and the host maps it to Settings.
private fun NavTarget.tabSection(): MainSection? = when (this) {
	NavTarget.SUMMARY, NavTarget.STATISTICS, NavTarget.SHARE_TERM -> MainSection.SUMMARY
	NavTarget.RECORD, NavTarget.RECORD_PROJECTION, NavTarget.RECORD_SCHEDULE, NavTarget.RECORD_CREATE_TERM ->
		MainSection.RECORD
	NavTarget.EVALUATIONS, NavTarget.EVALUATIONS_ADD, NavTarget.EVALUATIONS_DETAIL -> MainSection.EVALUATIONS
	NavTarget.PENSUM, NavTarget.SUBJECTS_SEARCH, NavTarget.SUBJECTS_DETAIL -> MainSection.PENSUM
	NavTarget.ENROLLMENT_PROOF, NavTarget.ABOUT, NavTarget.SIGN_IN, NavTarget.UPDATE_PASSWORD -> null
}

// The screens a target opens over its tab, in order. A target that needs a record the assistant did not name
// opens none, and the user sees the tab of the screen.
private fun NavTarget.destinationKeys(refs: List<EntityRef>): List<Destination> = when (this) {
	NavTarget.STATISTICS -> listOf(SummaryDestination.Statistics)
	NavTarget.RECORD_SCHEDULE -> listOf(RecordDestination.ScheduleDialog)
	NavTarget.RECORD_CREATE_TERM -> listOf(RecordDestination.CreateSyntheticTerm())
	NavTarget.EVALUATIONS_ADD -> listOf(EvaluationsDestination.Evaluation(evaluationId = null))
	NavTarget.EVALUATIONS_DETAIL -> listOfNotNull(
		refs.idOf(EntityKind.EVALUATION)?.let { evaluationId ->
			EvaluationsDestination.Evaluation(evaluationId = evaluationId)
		}
	)
	NavTarget.SUBJECTS_SEARCH -> listOf(SubjectsDestination.SubjectSearch)
	NavTarget.SUBJECTS_DETAIL -> listOfNotNull(
		refs.idOf(EntityKind.SUBJECT)?.let { subjectCode ->
			SubjectsDestination.SubjectDetail(subjectCode = subjectCode)
		}
	)
	NavTarget.ENROLLMENT_PROOF -> listOf(EnrollmentProofDestination.EnrollmentProofDialog)
	NavTarget.ABOUT -> listOf(AboutDestination.Settings)
	else -> emptyList()
}

// The id of the first reference of [kind] the assistant sent, if it sent one.
private fun List<EntityRef>.idOf(kind: EntityKind): String? = firstOrNull { ref -> ref.kind == kind }?.id

// Closes the assistant's sheets on top of the tab (the chat, and a component sheet opened from it), goes to [section]
// when there is one, and opens [keys] over it. It is one change of the observable stack, so no frame shows the sheet
// parked on the tab it came from. The tab is switched without writing the section the app opens on, as the hub does.
private fun TuIndiceNavigator.goFromAssistant(section: MainSection?, keys: List<Destination> = emptyList()) {
	if (rootMode != TuIndiceRootMode.SIGNED_IN) return

	Snapshot.withMutableSnapshot {
		while (isAssistantSheetOnTop()) {
			if (!pop()) break
		}

		if (section != null) switchTab(section)

		keys.forEach { key -> push(key) }
	}
}

private fun TuIndiceNavigator.isAssistantSheetOnTop(): Boolean {
	val key = currentKey

	return key is AssistantDestination.Chat || key is AssistantDestination.ComponentSheet
}

// The key of the term as the local record answers it, or null when the read fails or the record does not know the term.
private suspend fun GetTermKeyUseCase.termKeyOrNull(termId: String): String? {
	val state = execute(termId).first { state -> state !is UseCaseState.Loading }

	return if (state is UseCaseState.Data) state.value else null
}
