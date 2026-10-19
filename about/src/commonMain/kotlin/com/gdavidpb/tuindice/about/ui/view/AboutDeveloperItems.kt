package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.about.ui.model.AboutActions
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.about_dev_contact
import tuindice.about.generated.resources.about_dev_info
import tuindice.about.generated.resources.about_dev_report
import tuindice.about.generated.resources.about_source_code
import tuindice.about.generated.resources.ic_github

@Composable
fun AboutDeveloperItems(actions: AboutActions) {
	AboutItem(
		icon = rememberVectorPainter(Icons.Outlined.Person),
		text = stringResource(Res.string.about_dev_info),
		tint = MaterialTheme.colorScheme.onSurfaceVariant
	)

	AboutItem(
		icon = painterResource(Res.drawable.ic_github),
		text = stringResource(Res.string.about_source_code),
		testTag = AboutUiTags.OpenGithub,
		tint = MaterialTheme.colorScheme.onSurfaceVariant
	) { actions.onGithubClick() }

	AboutItem(
		icon = rememberVectorPainter(Icons.Outlined.Email),
		text = stringResource(Res.string.about_dev_contact),
		testTag = AboutUiTags.ContactDeveloper,
		tint = MaterialTheme.colorScheme.onSurfaceVariant
	) { actions.onContactDeveloperClick() }

	AboutItem(
		icon = rememberVectorPainter(Icons.Outlined.BugReport),
		text = stringResource(Res.string.about_dev_report),
		testTag = AboutUiTags.ReportBug,
		tint = MaterialTheme.colorScheme.onSurfaceVariant
	) { actions.onReportBugClick() }
}
