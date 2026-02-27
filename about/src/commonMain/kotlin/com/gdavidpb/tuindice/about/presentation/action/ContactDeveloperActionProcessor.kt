package com.gdavidpb.tuindice.about.presentation.action

import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ContactDeveloperActionProcessor(
	private val sendSupportEmailUseCase: SendSupportEmailUseCase
) : ActionProcessor<About.State, About.Action.ContactDeveloper, About.Effect>() {
	override suspend fun process(
		action: About.Action.ContactDeveloper,
		sideEffect: (About.Effect) -> Unit
	): Flow<Mutation<About.State>> {
		return sendSupportEmailUseCase.execute(Unit)
			.map { _ -> { state -> state } }
	}
}
