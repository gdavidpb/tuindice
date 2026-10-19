package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.about.ui.model.AboutActions
import com.gdavidpb.tuindice.base.ui.style.TuIndiceDarkTheme
import com.gdavidpb.tuindice.base.ui.view.universityLogoPainter
import org.jetbrains.compose.resources.stringResource
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.about_dst

@Composable
fun AboutSpecialThanksItems(actions: AboutActions) {
	AboutItem(
		icon = universityLogoPainter(),
		text = stringResource(Res.string.about_dst),
		testTag = AboutUiTags.OpenDst,
		size = 48.dp,
		tint = if (TuIndiceDarkTheme.isDark()) MaterialTheme.colorScheme.onBackground else null
	) { actions.onDstClick() }
}
