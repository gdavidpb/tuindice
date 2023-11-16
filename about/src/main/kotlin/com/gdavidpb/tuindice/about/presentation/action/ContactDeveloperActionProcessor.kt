package com.gdavidpb.tuindice.about.presentation.action

import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow

class ContactDeveloperActionProcessor
	: ActionProcessor<About.State, About.Action.ContactDeveloper, About.Effect>() {

	override fun process(
		action: About.Action.ContactDeveloper,
		sideEffect: (About.Effect) -> Unit
	): Flow<Mutation<About.State>> {
		sideEffect(
			About.Effect.StartEmail
		)

		return super.process(action, sideEffect)
	}
}