package com.gdavidpb.tuindice.presentation.action

import com.gdavidpb.tuindice.login.presentation.navigation.LoginDestination
import com.gdavidpb.tuindice.presentation.action.main.UpdateStateActionProcessor
import com.gdavidpb.tuindice.presentation.contract.Main
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MainActionProcessorsTest {
	@Test
	fun updateStateActionProcessor_replacesCurrentState() = runBlocking {
		val processor = UpdateStateActionProcessor()
		val targetState = Main.State.Content(
			startDestination = LoginDestination.SignIn
		)
		val effects = mutableListOf<Main.Effect>()

		val mutations = processor.process(
			action = Main.Action.UpdateState(targetState),
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(
			initialState = Main.State.Starting,
			mutations = mutations
		)

		assertEquals(targetState, finalState)
		assertTrue(effects.isEmpty())
	}

	private fun applyMutations(
		initialState: Main.State,
		mutations: List<(Main.State) -> Main.State>
	): Main.State {
		return mutations.fold(initialState) { state, mutation -> mutation(state) }
	}
}
