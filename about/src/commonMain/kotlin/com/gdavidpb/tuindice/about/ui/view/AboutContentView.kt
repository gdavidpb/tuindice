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
import com.gdavidpb.tuindice.base.ui.view.TuIndiceSectionHeader
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
		TuIndiceSectionHeader(
			text = stringResource(Res.string.app_name),
			modifier = Modifier.testTag(AboutUiTags.HeaderTitle),
			topPadding = InternalScreenDefaults.TopBarSpacing
		)
		AboutAppItems(versionText = state.versionText, actions = actions)

		TuIndiceSectionHeader(
			text = stringResource(Res.string.about_header_developer),
			modifier = Modifier.testTag(AboutUiTags.HeaderTitle)
		)
		AboutDeveloperItems(actions = actions)

		TuIndiceSectionHeader(
			text = stringResource(Res.string.about_header_libs),
			modifier = Modifier.testTag(AboutUiTags.HeaderTitle)
		)
		AboutLibrariesItems(actions = actions)

		TuIndiceSectionHeader(
			text = stringResource(Res.string.about_header_special_thanks),
			modifier = Modifier.testTag(AboutUiTags.HeaderTitle)
		)
		AboutSpecialThanksItems(actions = actions)
	}
}
