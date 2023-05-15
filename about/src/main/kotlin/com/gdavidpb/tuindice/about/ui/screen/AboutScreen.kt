package com.gdavidpb.tuindice.about.ui.screen

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Subject
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.gdavidpb.tuindice.about.R
import com.gdavidpb.tuindice.about.ui.view.AboutHeaderView
import com.gdavidpb.tuindice.about.ui.view.AboutView
import com.gdavidpb.tuindice.about.ui.view.getVersionName

@Composable
fun AboutScreen(
	onCreativeCommonsClick: () -> Unit,
	onTwitterClick: () -> Unit,
	onGithubClick: () -> Unit,
	onKotlinClick: () -> Unit,
	onComposeClick: () -> Unit,
	onFirebaseClick: () -> Unit,
	onKoinClick: () -> Unit,
	onRetrofitClick: () -> Unit,
	onDstClick: () -> Unit,
	onTermsAndConditionsClick: () -> Unit,
	onPrivacyPolicyClick: () -> Unit,
	onShareAppClick: () -> Unit,
	onRateOnPlayStoreClick: () -> Unit,
	onContactDeveloperClick: () -> Unit,
	onReportBugClick: () -> Unit
) {
	Column(
		modifier = Modifier
			.verticalScroll(rememberScrollState())
	) {
		AboutHeaderView(
			text = stringResource(R.string.app_name)
		) {
			AboutView(
				icon = Icons.Outlined.Info,
				text = stringResource(id = R.string.about_version, getVersionName()),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			)

			AboutView(
				icon = ImageVector.vectorResource(id = R.drawable.ic_cc),
				text = stringResource(id = R.string.about_license),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			) { onCreativeCommonsClick() }

			AboutView(
				icon = Icons.Outlined.Subject,
				text = stringResource(id = R.string.about_terms_and_conditions),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			) { onTermsAndConditionsClick() }

			AboutView(
				icon = Icons.Outlined.Lock,
				text = stringResource(id = R.string.about_privacy_policy),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			) { onPrivacyPolicyClick() }

			AboutView(
				icon = ImageVector.vectorResource(id = R.drawable.ic_twitter),
				text = stringResource(id = R.string.about_twitter),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			) { onTwitterClick() }

			AboutView(
				icon = Icons.Outlined.Share,
				text = stringResource(id = R.string.about_share),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			) { onShareAppClick() }

			AboutView(
				icon = Icons.Outlined.StarBorder,
				text = stringResource(id = R.string.about_rate),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			) { onRateOnPlayStoreClick() }
		}

		AboutHeaderView(
			text = stringResource(R.string.about_header_developer)
		) {
			AboutView(
				icon = Icons.Outlined.Person,
				text = stringResource(id = R.string.about_dev_info),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			)

			AboutView(
				icon = ImageVector.vectorResource(id = R.drawable.ic_github),
				text = stringResource(id = R.string.about_source_code),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			) { onGithubClick() }

			AboutView(
				icon = Icons.Outlined.Email,
				text = stringResource(id = R.string.about_dev_contact),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			) { onContactDeveloperClick() }

			AboutView(
				icon = Icons.Outlined.BugReport,
				text = stringResource(id = R.string.about_dev_report),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			) { onReportBugClick() }
		}

		AboutHeaderView(
			text = stringResource(R.string.about_header_libs)
		) {
			AboutView(
				icon = ImageVector.vectorResource(id = R.drawable.ic_kotlin),
				text = stringResource(id = R.string.about_kotlin),
				size = dimensionResource(id = R.dimen.dp_48)
			) { onKotlinClick() }

			AboutView(
				icon = ImageVector.vectorResource(id = R.drawable.ic_compose),
				text = stringResource(id = R.string.about_compose),
				size = dimensionResource(id = R.dimen.dp_48)
			) { onComposeClick() }

			AboutView(
				icon = ImageVector.vectorResource(id = R.drawable.ic_firebase),
				text = stringResource(id = R.string.about_firebase),
				size = dimensionResource(id = R.dimen.dp_48)
			) { onFirebaseClick() }

			AboutView(
				icon = ImageVector.vectorResource(id = R.drawable.ic_koin),
				text = stringResource(id = R.string.about_koin),
				size = dimensionResource(id = R.dimen.dp_48)
			) { onKoinClick() }

			AboutView(
				icon = ImageVector.vectorResource(id = R.drawable.ic_square),
				text = stringResource(id = R.string.about_retrofit),
				size = dimensionResource(id = R.dimen.dp_48),
				tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onBackground else null
			) { onRetrofitClick() }
		}

		AboutHeaderView(
			text = stringResource(R.string.about_header_special_thanks)
		) {
			AboutView(
				icon = ImageVector.vectorResource(id = R.drawable.ic_usb),
				text = stringResource(id = R.string.about_dst),
				size = dimensionResource(id = R.dimen.dp_48),
				tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onBackground else null
			) { onDstClick() }
		}
	}
}