package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.gdavidpb.tuindice.about.ui.SettingsUiTags
import com.gdavidpb.tuindice.about.ui.model.SettingsActions
import com.gdavidpb.tuindice.base.ui.icon.ShareIcon
import com.gdavidpb.tuindice.base.ui.view.SettingsActionRow
import com.gdavidpb.tuindice.base.ui.view.SettingsLinkRow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.ic_x
import tuindice.about.generated.resources.settings_about
import tuindice.about.generated.resources.settings_rate
import tuindice.about.generated.resources.settings_share
import tuindice.about.generated.resources.settings_x

@Composable
fun SettingsAppItems(
	versionText: String,
	actions: SettingsActions
) {
	SettingsActionRow(
		icon = rememberVectorPainter(ShareIcon),
		text = stringResource(Res.string.settings_share),
		testTag = SettingsUiTags.ShareApp,
		isDestructive = false,
		onClick = actions.onShareAppClick
	)

	SettingsActionRow(
		icon = rememberVectorPainter(Icons.Outlined.StarBorder),
		text = stringResource(Res.string.settings_rate),
		testTag = SettingsUiTags.RateOnStore,
		isDestructive = false,
		onClick = actions.onRateOnPlayStoreClick
	)

	SettingsActionRow(
		icon = painterResource(Res.drawable.ic_x),
		text = stringResource(Res.string.settings_x),
		testTag = SettingsUiTags.OpenX,
		isDestructive = false,
		onClick = actions.onXClick
	)

	SettingsLinkRow(
		icon = rememberVectorPainter(Icons.Outlined.Info),
		text = stringResource(Res.string.settings_about, versionText),
		testTag = SettingsUiTags.OpenAbout,
		onClick = actions.onAboutClick
	)
}
