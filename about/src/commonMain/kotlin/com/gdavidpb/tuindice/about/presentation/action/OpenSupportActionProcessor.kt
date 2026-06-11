package com.gdavidpb.tuindice.about.presentation.action

import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.getString
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.label_support

class OpenSupportActionProcessor(
	private val appEnvironmentRepository: AppEnvironmentRepository
) : ActionProcessor<About.State, About.Action.OpenSupport, About.Effect> {
	override suspend fun process(
		action: About.Action.OpenSupport,
		sideEffect: (About.Effect) -> Unit
	): Flow<Mutation<About.State>> {
		sideEffect(
			About.Effect.NavigateToBrowser(
				title = getString(Res.string.label_support),
				url = appEnvironmentRepository.getEnvironment().supportUrl
			)
		)

		return emptyFlow()
	}
}
