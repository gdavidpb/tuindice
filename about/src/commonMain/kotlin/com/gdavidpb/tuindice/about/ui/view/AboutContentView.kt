package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.about.ui.model.AboutActions
import com.gdavidpb.tuindice.base.ui.style.InternalScreenDefaults
import org.jetbrains.compose.resources.stringResource
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.about_header_developer
import tuindice.about.generated.resources.about_header_libs
import tuindice.about.generated.resources.about_header_special_thanks
import tuindice.about.generated.resources.app_name

@Composable
fun AboutContentView(
	state: About.State.Content,
	actions: AboutActions
) {
	Column(
		modifier = Modifier
			.testTag(AboutUiTags.ContentContainer)
			.verticalScroll(rememberScrollState())
	) {
		AboutHeader(
			text = stringResource(Res.string.app_name),
			topPadding = InternalScreenDefaults.TopBarSpacing
		) {
			AboutAppItems(versionText = state.versionText, actions = actions)
		}

		AboutHeader(text = stringResource(Res.string.about_header_developer)) {
			AboutDeveloperItems(actions = actions)
		}

		AboutHeader(text = stringResource(Res.string.about_header_libs)) {
			AboutLibrariesItems(actions = actions)
		}

		AboutHeader(text = stringResource(Res.string.about_header_special_thanks)) {
			AboutSpecialThanksItems(actions = actions)
		}
	}
}
