package com.gdavidpb.tuindice.summary.presentation.action

import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SummaryActionProcessorsTest {
	@Test
	fun pickProfilePictureActionProcessor_emitsOpenPicker() = runBlocking {
		val processor = PickProfilePictureActionProcessor()
		val effects = mutableListOf<Summary.Effect>()

		processor.process(
			action = Summary.Action.PickProfilePicture,
			sideEffect = effects::add
		).toList()

		assertEquals(Summary.Effect.OpenPicker, effects.single())
	}

	@Test
	fun removeProfilePictureActionProcessor_emitsConfirmationNavigation() = runBlocking {
		val processor = RemoveProfilePictureActionProcessor()
		val effects = mutableListOf<Summary.Effect>()

		processor.process(
			action = Summary.Action.RemoveProfilePicture,
			sideEffect = effects::add
		).toList()

		assertEquals(Summary.Effect.NavigateToRemoveProfilePictureConfirmationDialog, effects.single())
	}

	@Test
	fun openProfilePictureSettingsActionProcessor_whenHasPicture_emitsShowRemoveTrue() = runBlocking {
		val processor = OpenProfilePictureSettingsActionProcessor()
		val effects = mutableListOf<Summary.Effect>()
		val initialState = summaryContentState(profilePictureUrl = "https://tuindice.app/profile.jpg")

		val mutations = processor.process(
			action = Summary.Action.OpenProfilePictureSettings,
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(initialState, mutations)

		val effect = assertIs<Summary.Effect.NavigateToProfilePictureSettingsDialog>(effects.single())
		assertEquals(true, effect.showRemove)
		assertEquals(initialState, finalState)
	}

	@Test
	fun openProfilePictureSettingsActionProcessor_whenNoPicture_emitsShowRemoveFalse() = runBlocking {
		val processor = OpenProfilePictureSettingsActionProcessor()
		val effects = mutableListOf<Summary.Effect>()
		val initialState = summaryContentState(profilePictureUrl = "")

		val mutations = processor.process(
			action = Summary.Action.OpenProfilePictureSettings,
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(initialState, mutations)

		val effect = assertIs<Summary.Effect.NavigateToProfilePictureSettingsDialog>(effects.single())
		assertEquals(false, effect.showRemove)
		assertEquals(initialState, finalState)
	}

	private fun summaryContentState(profilePictureUrl: String): Summary.State.Content {
		return Summary.State.Content(
			name = "TuIndice",
			lastUpdate = "now",
			careerName = "Software",
			grade = 4.5f,
			enrolledSubjects = 5,
			enrolledCredits = 20,
			approvedSubjects = 10,
			approvedCredits = 40,
			retiredSubjects = 0,
			retiredCredits = 0,
			failedSubjects = 1,
			failedCredits = 4,
			profilePictureUrl = profilePictureUrl,
			isGradeVisible = true,
			isProfilePictureLoading = false,
			isLoading = false,
			isUpdated = true,
			isUpdating = false
		)
	}

	private fun applyMutations(
		initialState: Summary.State,
		mutations: List<(Summary.State) -> Summary.State>
	): Summary.State {
		return mutations.fold(initialState) { state, mutation -> mutation(state) }
	}
}
