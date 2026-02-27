package com.gdavidpb.tuindice.about.presentation.action

import com.gdavidpb.tuindice.about.domain.usecase.ShareTextUseCase
import com.gdavidpb.tuindice.about.domain.usecase.param.ShareTextParams
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.about_share_message
import tuindice.about.generated.resources.about_share_subject

class ShareAppActionProcessor(
	private val shareTextUseCase: ShareTextUseCase
) : ActionProcessor<About.State, About.Action.ShareApp, About.Effect>() {
	override suspend fun process(
		action: About.Action.ShareApp,
		sideEffect: (About.Effect) -> Unit
	): Flow<Mutation<About.State>> {
		return shareTextUseCase.execute(
			ShareTextParams(
				subject = getString(Res.string.about_share_subject),
				text = getString(Res.string.about_share_message)
			)
		).map { _ -> { state -> state } }
	}
}
