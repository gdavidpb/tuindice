package com.gdavidpb.tuindice.about.ui.screen

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Subject
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.ui.view.AboutHeaderView
import com.gdavidpb.tuindice.about.ui.view.AboutView
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tuindice.about.generated.resources.*

@Composable
fun AboutContentRouteScreen(
	state: About.State,
	onCreativeCommonsClick: () -> Unit,
	onXClick: () -> Unit,
	onGithubClick: () -> Unit,
	onKotlinClick: () -> Unit,
	onComposeClick: () -> Unit,
	onFirebaseClick: () -> Unit,
	onKoinClick: () -> Unit,
	onKtorClick: () -> Unit,
	onDstClick: () -> Unit,
	onTermsAndConditionsClick: () -> Unit,
	onPrivacyPolicyClick: () -> Unit,
	onShareAppClick: () -> Unit,
	onRateOnPlayStoreClick: () -> Unit,
	onContactDeveloperClick: () -> Unit,
	onReportBugClick: () -> Unit
) {
	AboutScreen(
		state = state,
		onCreativeCommonsClick = onCreativeCommonsClick,
		onXClick = onXClick,
		onGithubClick = onGithubClick,
		onKotlinClick = onKotlinClick,
		onComposeClick = onComposeClick,
		onFirebaseClick = onFirebaseClick,
		onKoinClick = onKoinClick,
		onKtorClick = onKtorClick,
		onDstClick = onDstClick,
		onTermsAndConditionsClick = onTermsAndConditionsClick,
		onPrivacyPolicyClick = onPrivacyPolicyClick,
		onShareAppClick = onShareAppClick,
		onRateOnPlayStoreClick = onRateOnPlayStoreClick,
		onContactDeveloperClick = onContactDeveloperClick,
		onReportBugClick = onReportBugClick,
		contentStateContent = { contentState, creativeCommonsClick, xClick, githubClick, kotlinClick, composeClick, firebaseClick, koinClick, ktorClick, dstClick, termsClick, privacyClick, shareClick, rateClick, contactClick, reportClick ->
			AboutContentScreen(
				state = contentState,
				onCreativeCommonsClick = creativeCommonsClick,
				onXClick = xClick,
				onGithubClick = githubClick,
				onKotlinClick = kotlinClick,
				onComposeClick = composeClick,
				onFirebaseClick = firebaseClick,
				onKoinClick = koinClick,
				onKtorClick = ktorClick,
				onDstClick = dstClick,
				onTermsAndConditionsClick = termsClick,
				onPrivacyPolicyClick = privacyClick,
				onShareAppClick = shareClick,
				onRateOnPlayStoreClick = rateClick,
				onContactDeveloperClick = contactClick,
				onReportBugClick = reportClick
			)
		}
	)
}

@Composable
fun AboutContentScreen(
	state: About.State.Content,
	onCreativeCommonsClick: () -> Unit,
	onXClick: () -> Unit,
	onGithubClick: () -> Unit,
	onKotlinClick: () -> Unit,
	onComposeClick: () -> Unit,
	onFirebaseClick: () -> Unit,
	onKoinClick: () -> Unit,
	onKtorClick: () -> Unit,
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
		AboutHeaderView(text = stringResource(Res.string.app_name)) {
			AboutView(
				icon = rememberVectorPainter(Icons.Outlined.Info),
				text = stringResource(Res.string.about_version, state.versionText),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			)

			AboutView(
				icon = painterResource(Res.drawable.ic_cc),
				text = stringResource(Res.string.about_license),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			) { onCreativeCommonsClick() }

			AboutView(
				icon = rememberVectorPainter(Icons.AutoMirrored.Outlined.Subject),
				text = stringResource(Res.string.about_terms_and_conditions),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			) { onTermsAndConditionsClick() }

			AboutView(
				icon = rememberVectorPainter(Icons.Outlined.Lock),
				text = stringResource(Res.string.about_privacy_policy),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			) { onPrivacyPolicyClick() }

			AboutView(
				icon = painterResource(Res.drawable.ic_x),
				text = stringResource(Res.string.about_x),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			) { onXClick() }

			AboutView(
				icon = rememberVectorPainter(Icons.Outlined.Share),
				text = stringResource(Res.string.about_share),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			) { onShareAppClick() }

			AboutView(
				icon = rememberVectorPainter(Icons.Outlined.StarBorder),
				text = stringResource(Res.string.about_rate),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			) { onRateOnPlayStoreClick() }
		}

		AboutHeaderView(text = stringResource(Res.string.about_header_developer)) {
			AboutView(
				icon = rememberVectorPainter(Icons.Outlined.Person),
				text = stringResource(Res.string.about_dev_info),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			)

			AboutView(
				icon = painterResource(Res.drawable.ic_github),
				text = stringResource(Res.string.about_source_code),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			) { onGithubClick() }

			AboutView(
				icon = rememberVectorPainter(Icons.Outlined.Email),
				text = stringResource(Res.string.about_dev_contact),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			) { onContactDeveloperClick() }

			AboutView(
				icon = rememberVectorPainter(Icons.Outlined.BugReport),
				text = stringResource(Res.string.about_dev_report),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			) { onReportBugClick() }
		}

		AboutHeaderView(text = stringResource(Res.string.about_header_libs)) {
			AboutView(
				icon = painterResource(Res.drawable.ic_kotlin),
				text = stringResource(Res.string.about_kotlin),
				size = 48.dp
			) { onKotlinClick() }

			AboutView(
				icon = painterResource(Res.drawable.ic_compose),
				text = stringResource(Res.string.about_compose),
				size = 48.dp
			) { onComposeClick() }

			AboutView(
				icon = painterResource(Res.drawable.ic_firebase),
				text = stringResource(Res.string.about_firebase),
				size = 48.dp
			) { onFirebaseClick() }

			AboutView(
				icon = painterResource(Res.drawable.ic_koin),
				text = stringResource(Res.string.about_koin),
				size = 48.dp
			) { onKoinClick() }

			AboutView(
				icon = painterResource(Res.drawable.ic_ktor),
				text = stringResource(Res.string.about_ktor),
				size = 48.dp,
				tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onBackground else null
			) { onKtorClick() }
		}

		AboutHeaderView(text = stringResource(Res.string.about_header_special_thanks)) {
			AboutView(
				icon = painterResource(Res.drawable.ic_usb),
				text = stringResource(Res.string.about_dst),
				size = 48.dp,
				tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onBackground else null
			) { onDstClick() }
		}
	}
}
