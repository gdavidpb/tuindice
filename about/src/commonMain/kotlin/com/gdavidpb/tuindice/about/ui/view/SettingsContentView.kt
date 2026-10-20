package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.about.presentation.contract.Settings
import com.gdavidpb.tuindice.about.ui.SettingsUiTags
import com.gdavidpb.tuindice.about.ui.model.SettingsActions
import com.gdavidpb.tuindice.base.domain.model.ProfileIdentity
import com.gdavidpb.tuindice.base.ui.style.InternalScreenDefaults
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.SettingsActionRow
import com.gdavidpb.tuindice.base.ui.view.TuIndiceSectionHeader
import org.jetbrains.compose.resources.stringResource
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.settings_section_app
import tuindice.about.generated.resources.settings_section_assistant
import tuindice.about.generated.resources.settings_section_help
import tuindice.about.generated.resources.settings_section_privacy
import tuindice.about.generated.resources.settings_sign_out

@Composable
fun SettingsContentView(
	state: Settings.State.Content,
	actions: SettingsActions,
	profilePhoto: @Composable (identity: ProfileIdentity) -> Unit
) {
	Column(
		modifier = Modifier
			.testTag(SettingsUiTags.ContentContainer)
			.verticalScroll(rememberScrollState())
			.padding(top = InternalScreenDefaults.TopBarSpacing, bottom = TuIndiceSpacing.Dialog)
	) {
		state.identity?.let { identity ->
			SettingsIdentityCard(identity = identity, profilePhoto = profilePhoto)
		}

		if (state.assistantAvailable) {
			TuIndiceSectionHeader(
				text = stringResource(Res.string.settings_section_assistant),
				modifier = Modifier.testTag(SettingsUiTags.SectionTitle)
			)
			SettingsAssistantItems(
				consentGranted = state.assistantConsentGranted,
				onConsentChange = actions.onAssistantConsentChange,
				onMemoryClick = actions.onAssistantMemoryClick,
				onConversationsClick = actions.onAssistantConversationsClick
			)
		}

		TuIndiceSectionHeader(
			text = stringResource(Res.string.settings_section_privacy),
			modifier = Modifier.testTag(SettingsUiTags.SectionTitle)
		)
		SettingsPrivacyItems(state = state, actions = actions)

		TuIndiceSectionHeader(
			text = stringResource(Res.string.settings_section_help),
			modifier = Modifier.testTag(SettingsUiTags.SectionTitle)
		)
		SettingsHelpItems(actions = actions)

		TuIndiceSectionHeader(
			text = stringResource(Res.string.settings_section_app),
			modifier = Modifier.testTag(SettingsUiTags.SectionTitle)
		)
		SettingsAppItems(versionText = state.versionText, actions = actions)

		// Leaving is the last thing in the list and the only row in the error color of the theme.
		Column(modifier = Modifier.padding(top = TuIndiceSpacing.Dialog)) {
			SettingsActionRow(
				icon = rememberVectorPainter(Icons.AutoMirrored.Outlined.Logout),
				text = stringResource(Res.string.settings_sign_out),
				testTag = SettingsUiTags.SignOut,
				isDestructive = true,
				onClick = actions.onSignOutClick
			)
		}
	}
}
