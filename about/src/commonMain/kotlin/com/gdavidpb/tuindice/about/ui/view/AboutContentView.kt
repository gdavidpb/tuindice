package com.gdavidpb.tuindice.about.ui.view

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.about.generated.AboutDependencyTexts
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.about.ui.custom.AboutHeader
import com.gdavidpb.tuindice.about.ui.custom.AboutItem
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.about_dev_contact
import tuindice.about.generated.resources.about_dev_info
import tuindice.about.generated.resources.about_dev_report
import tuindice.about.generated.resources.about_dst
import tuindice.about.generated.resources.about_header_developer
import tuindice.about.generated.resources.about_header_libs
import tuindice.about.generated.resources.about_header_special_thanks
import tuindice.about.generated.resources.about_license
import tuindice.about.generated.resources.about_privacy_policy
import tuindice.about.generated.resources.about_rate
import tuindice.about.generated.resources.about_share
import tuindice.about.generated.resources.about_source_code
import tuindice.about.generated.resources.about_terms_and_conditions
import tuindice.about.generated.resources.about_version
import tuindice.about.generated.resources.about_x
import tuindice.about.generated.resources.app_name
import tuindice.about.generated.resources.ic_cc
import tuindice.about.generated.resources.ic_compose
import tuindice.about.generated.resources.ic_firebase
import tuindice.about.generated.resources.ic_github
import tuindice.about.generated.resources.ic_koin
import tuindice.about.generated.resources.ic_kotlin
import tuindice.about.generated.resources.ic_ktor
import tuindice.about.generated.resources.ic_usb
import tuindice.about.generated.resources.ic_x

@Composable
fun AboutContentView(
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
			.testTag(AboutUiTags.ContentContainer)
			.verticalScroll(rememberScrollState())
	) {
		AboutHeader(text = stringResource(Res.string.app_name)) {
			AboutItem(
				icon = rememberVectorPainter(Icons.Outlined.Info),
				text = stringResource(Res.string.about_version, state.versionText),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			)

			AboutItem(
				icon = painterResource(Res.drawable.ic_cc),
				text = stringResource(Res.string.about_license),
				testTag = AboutUiTags.OpenCreativeCommons,
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			) { onCreativeCommonsClick() }

			AboutItem(
				icon = rememberVectorPainter(Icons.AutoMirrored.Outlined.Subject),
				text = stringResource(Res.string.about_terms_and_conditions),
				testTag = AboutUiTags.OpenTerms,
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			) { onTermsAndConditionsClick() }

			AboutItem(
				icon = rememberVectorPainter(Icons.Outlined.Lock),
				text = stringResource(Res.string.about_privacy_policy),
				testTag = AboutUiTags.OpenPrivacy,
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			) { onPrivacyPolicyClick() }

			AboutItem(
				icon = painterResource(Res.drawable.ic_x),
				text = stringResource(Res.string.about_x),
				testTag = AboutUiTags.OpenX,
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			) { onXClick() }

			AboutItem(
				icon = rememberVectorPainter(Icons.Outlined.Share),
				text = stringResource(Res.string.about_share),
				testTag = AboutUiTags.ShareApp,
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			) { onShareAppClick() }

			AboutItem(
				icon = rememberVectorPainter(Icons.Outlined.StarBorder),
				text = stringResource(Res.string.about_rate),
				testTag = AboutUiTags.RateOnStore,
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			) { onRateOnPlayStoreClick() }
		}

		AboutHeader(text = stringResource(Res.string.about_header_developer)) {
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
			) { onGithubClick() }

			AboutItem(
				icon = rememberVectorPainter(Icons.Outlined.Email),
				text = stringResource(Res.string.about_dev_contact),
				testTag = AboutUiTags.ContactDeveloper,
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			) { onContactDeveloperClick() }

			AboutItem(
				icon = rememberVectorPainter(Icons.Outlined.BugReport),
				text = stringResource(Res.string.about_dev_report),
				testTag = AboutUiTags.ReportBug,
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			) { onReportBugClick() }
		}

		AboutHeader(text = stringResource(Res.string.about_header_libs)) {
			AboutItem(
				icon = painterResource(Res.drawable.ic_kotlin),
				text = AboutDependencyTexts.kotlinDescription,
				testTag = AboutUiTags.OpenKotlin,
				size = 48.dp,
				scale = 0.85f
			) { onKotlinClick() }

			AboutItem(
				icon = painterResource(Res.drawable.ic_compose),
				text = AboutDependencyTexts.composeDescription,
				testTag = AboutUiTags.OpenCompose,
				size = 48.dp
			) { onComposeClick() }

			AboutItem(
				icon = painterResource(Res.drawable.ic_firebase),
				text = AboutDependencyTexts.firebaseDescription,
				testTag = AboutUiTags.OpenFirebase,
				size = 48.dp
			) { onFirebaseClick() }

			AboutItem(
				icon = painterResource(Res.drawable.ic_koin),
				text = AboutDependencyTexts.koinDescription,
				testTag = AboutUiTags.OpenKoin,
				size = 48.dp
			) { onKoinClick() }

			AboutItem(
				icon = painterResource(Res.drawable.ic_ktor),
				text = AboutDependencyTexts.ktorDescription,
				testTag = AboutUiTags.OpenKtor,
				size = 48.dp
			) { onKtorClick() }
		}

		AboutHeader(text = stringResource(Res.string.about_header_special_thanks)) {
			AboutItem(
				icon = painterResource(Res.drawable.ic_usb),
				text = stringResource(Res.string.about_dst),
				testTag = AboutUiTags.OpenDst,
				size = 48.dp,
				tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onBackground else null
			) { onDstClick() }
		}
	}
}
