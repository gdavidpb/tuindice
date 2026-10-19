package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.gdavidpb.tuindice.about.ui.SettingsUiTags
import com.gdavidpb.tuindice.base.domain.model.ProfileIdentity
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import org.jetbrains.compose.resources.stringResource
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.settings_identity_carnet

/**
 * The card on top of Settings: the photo, the name, the career and the carnet of the student. It only shows who is
 * signed in, so it is not tappable. The photo is [profilePhoto], drawn by the host, which keeps one avatar for the app.
 */
@Composable
fun SettingsIdentityCard(
	identity: ProfileIdentity,
	profilePhoto: @Composable (identity: ProfileIdentity) -> Unit
) {
	Row(
		modifier = Modifier
			.testTag(SettingsUiTags.IdentityCard)
			.padding(horizontal = TuIndiceSpacing.Screen)
			.fillMaxWidth()
			.clip(RoundedCornerShape(TuIndiceRadius.Card))
			.background(MaterialTheme.colorScheme.surfaceVariant)
			.padding(TuIndiceSpacing.Section),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Section)
	) {
		profilePhoto(identity)

		Column(verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Two)) {
			Text(
				modifier = Modifier.testTag(SettingsUiTags.IdentityName),
				text = identity.name,
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onSurface,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)

			Text(
				modifier = Modifier.testTag(SettingsUiTags.IdentityCareer),
				text = identity.careerName,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Deemphasis),
				maxLines = 2,
				overflow = TextOverflow.Ellipsis
			)

			Text(
				modifier = Modifier.testTag(SettingsUiTags.IdentityCarnet),
				text = stringResource(Res.string.settings_identity_carnet, identity.carnet),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Deemphasis)
			)
		}
	}
}
