package com.gdavidpb.tuindice.about.presentation.action

import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.getString
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.label_terms_and_conditions

class OpenTermsAndConditionsActionProcessor(
	private val appEnvironmentRepository: AppEnvironmentRepository
) : ActionProcessor<About.State, About.Action.OpenTermsAndConditions, About.Effect>() {
	override suspend fun process(
		action: About.Action.OpenTermsAndConditions,
		sideEffect: (About.Effect) -> Unit
	): Flow<Mutation<About.State>> {
		sideEffect(
			About.Effect.NavigateToBrowser(
				title = getString(Res.string.label_terms_and_conditions),
				url = appEnvironmentRepository.getEnvironment().termsAndConditionsUrl
			)
		)

		return super.process(action, sideEffect)
	}
}
