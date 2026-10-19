package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Email
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.gdavidpb.tuindice.about.ui.SettingsUiTags
import com.gdavidpb.tuindice.about.ui.model.SettingsActions
import org.jetbrains.compose.resources.stringResource
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.settings_report_bug
import tuindice.about.generated.resources.settings_support

@Composable
fun SettingsHelpItems(actions: SettingsActions) {
	SettingsLinkRow(
		icon = rememberVectorPainter(Icons.Outlined.Email),
		text = stringResource(Res.string.settings_support),
		testTag = SettingsUiTags.OpenSupport,
		onClick = actions.onSupportClick
	)

	SettingsLinkRow(
		icon = rememberVectorPainter(Icons.Outlined.BugReport),
		text = stringResource(Res.string.settings_report_bug),
		testTag = SettingsUiTags.ReportBug,
		onClick = actions.onReportBugClick
	)
}
