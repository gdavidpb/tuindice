package com.gdavidpb.tuindice.about.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.about.domain.model.AboutLinks
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import com.gdavidpb.tuindice.about.ui.model.AboutActions
import com.gdavidpb.tuindice.about.ui.screen.AboutScreen
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle

@Composable
fun AboutRoute(
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: AboutViewModel
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()
	val openUri = rememberExternalUriOpener(showSnackBar = showSnackBar)

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is About.Effect.OpenUri -> openUri(effect.uri)
		}
	}

	AboutScreen(
		state = viewState,
		actions = viewModel.toAboutActions()
	)
}

private fun AboutViewModel.toAboutActions() = AboutActions(
	onCreativeCommonsClick = { dispatchAction(About.Action.OpenUrl(AboutLinks.CREATIVE_COMMONS)) },
	onGithubClick = { dispatchAction(About.Action.OpenUrl(AboutLinks.GITHUB)) },
	onKotlinClick = { dispatchAction(About.Action.OpenUrl(AboutLinks.KOTLIN)) },
	onComposeClick = { dispatchAction(About.Action.OpenUrl(AboutLinks.COMPOSE)) },
	onFirebaseClick = { dispatchAction(About.Action.OpenUrl(AboutLinks.FIREBASE)) },
	onKoinClick = { dispatchAction(About.Action.OpenUrl(AboutLinks.KOIN)) },
	onKtorClick = { dispatchAction(About.Action.OpenUrl(AboutLinks.KTOR)) },
	onDstClick = { dispatchAction(About.Action.OpenUrl(AboutLinks.DST)) },
	onContactDeveloperClick = { dispatchAction(About.Action.ContactDeveloper) }
)
