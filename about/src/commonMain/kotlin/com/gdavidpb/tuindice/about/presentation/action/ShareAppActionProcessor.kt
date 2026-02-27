package com.gdavidpb.tuindice.about.presentation.action

import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.getString
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.about_share_message
import tuindice.about.generated.resources.about_share_subject

class ShareAppActionProcessor
	: ActionProcessor<About.State, About.Action.ShareApp, About.Effect>() {
	override suspend fun process(
		action: About.Action.ShareApp,
		sideEffect: (About.Effect) -> Unit
	): Flow<Mutation<About.State>> {
		sideEffect(
			About.Effect.StartShare(
				text = getString(Res.string.about_share_message),
				subject = getString(Res.string.about_share_subject)
			)
		)

		return super.process(action, sideEffect)
	}
}
