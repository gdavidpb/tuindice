package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Subject
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.about.ui.model.AboutActions
import com.gdavidpb.tuindice.base.ui.icon.ShareIcon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.about_license
import tuindice.about.generated.resources.about_privacy_policy
import tuindice.about.generated.resources.about_rate
import tuindice.about.generated.resources.about_share
import tuindice.about.generated.resources.about_support
import tuindice.about.generated.resources.about_terms_and_conditions
import tuindice.about.generated.resources.about_version
import tuindice.about.generated.resources.about_x
import tuindice.about.generated.resources.ic_cc
import tuindice.about.generated.resources.ic_x

@Composable
fun AboutAppItems(
	versionText: String,
	actions: AboutActions
) {
	AboutItem(
		icon = rememberVectorPainter(Icons.Outlined.Info),
		text = stringResource(Res.string.about_version, versionText),
		tint = MaterialTheme.colorScheme.onSurfaceVariant
	)

	AboutItem(
		icon = painterResource(Res.drawable.ic_cc),
		text = stringResource(Res.string.about_license),
		testTag = AboutUiTags.OpenCreativeCommons,
		tint = MaterialTheme.colorScheme.onSurfaceVariant
	) { actions.onCreativeCommonsClick() }

	AboutItem(
		icon = rememberVectorPainter(Icons.AutoMirrored.Outlined.Subject),
		text = stringResource(Res.string.about_terms_and_conditions),
		testTag = AboutUiTags.OpenTerms,
		tint = MaterialTheme.colorScheme.onSurfaceVariant
	) { actions.onTermsAndConditionsClick() }

	AboutItem(
		icon = rememberVectorPainter(Icons.Outlined.Lock),
		text = stringResource(Res.string.about_privacy_policy),
		testTag = AboutUiTags.OpenPrivacy,
		tint = MaterialTheme.colorScheme.onSurfaceVariant
	) { actions.onPrivacyPolicyClick() }

	AboutItem(
		icon = rememberVectorPainter(Icons.Outlined.Email),
		text = stringResource(Res.string.about_support),
		testTag = AboutUiTags.OpenSupport,
		tint = MaterialTheme.colorScheme.onSurfaceVariant
	) { actions.onSupportClick() }

	AboutItem(
		icon = painterResource(Res.drawable.ic_x),
		text = stringResource(Res.string.about_x),
		testTag = AboutUiTags.OpenX,
		tint = MaterialTheme.colorScheme.onSurfaceVariant
	) { actions.onXClick() }

	AboutItem(
		icon = rememberVectorPainter(ShareIcon),
		text = stringResource(Res.string.about_share),
		testTag = AboutUiTags.ShareApp,
		tint = MaterialTheme.colorScheme.onSurfaceVariant
	) { actions.onShareAppClick() }

	AboutItem(
		icon = rememberVectorPainter(Icons.Outlined.StarBorder),
		text = stringResource(Res.string.about_rate),
		testTag = AboutUiTags.RateOnStore,
		tint = MaterialTheme.colorScheme.onSurfaceVariant
	) { actions.onRateOnPlayStoreClick() }
}
