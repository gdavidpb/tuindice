package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.about.ui.model.AboutActions
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.about_license
import tuindice.about.generated.resources.about_version
import tuindice.about.generated.resources.ic_cc

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
}
