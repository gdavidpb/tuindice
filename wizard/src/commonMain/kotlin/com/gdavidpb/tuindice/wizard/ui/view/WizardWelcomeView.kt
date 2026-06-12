package com.gdavidpb.tuindice.wizard.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.view.AppLogoView
import com.gdavidpb.tuindice.wizard.ui.WizardUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.wizard.generated.resources.Res
import tuindice.wizard.generated.resources.wizard_skip
import tuindice.wizard.generated.resources.wizard_start
import tuindice.wizard.generated.resources.wizard_welcome_message
import tuindice.wizard.generated.resources.wizard_welcome_privacy
import tuindice.wizard.generated.resources.wizard_welcome_summary
import tuindice.wizard.generated.resources.wizard_welcome_title

@Composable
fun WizardWelcomeView(
	onStart: () -> Unit,
	onSkip: () -> Unit,
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier
			.fillMaxSize()
			.testTag(WizardUiTags.WelcomeScreen),
		color = MaterialTheme.colorScheme.background
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
				.windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
				.padding(horizontal = 28.dp, vertical = 24.dp),
			verticalArrangement = Arrangement.SpaceBetween
		) {
			Spacer(modifier = Modifier.weight(0.36f))

			Column(
				modifier = Modifier.fillMaxWidth(),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.spacedBy(22.dp)
			) {
				AppLogoView(
					modifier = Modifier.size(112.dp),
					contentDescription = null
				)

				Column(
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.spacedBy(12.dp)
				) {
					Text(
						modifier = Modifier.fillMaxWidth(),
						text = stringResource(Res.string.wizard_welcome_title),
						style = MaterialTheme.typography.headlineMedium,
						fontWeight = FontWeight.Bold,
						color = MaterialTheme.colorScheme.onBackground,
						textAlign = TextAlign.Center
					)
					Text(
						modifier = Modifier.fillMaxWidth(),
						text = stringResource(Res.string.wizard_welcome_message).toWizardAnnotatedText(),
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						textAlign = TextAlign.Center
					)
				}

				Surface(
					modifier = Modifier.fillMaxWidth(),
					shape = MaterialTheme.shapes.medium,
					color = MaterialTheme.colorScheme.surfaceVariant,
					contentColor = MaterialTheme.colorScheme.onSurfaceVariant
				) {
					Column(
						modifier = Modifier.padding(18.dp),
						verticalArrangement = Arrangement.spacedBy(10.dp)
					) {
						Text(
							text = stringResource(Res.string.wizard_welcome_summary).toWizardAnnotatedText(),
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
						Text(
							text = stringResource(Res.string.wizard_welcome_privacy).toWizardAnnotatedText(),
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				}
			}

			Spacer(modifier = Modifier.weight(0.64f))

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				TextButton(
					modifier = Modifier.testTag(WizardUiTags.SkipButton),
					colors = ButtonDefaults.textButtonColors(
						contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = TuIndiceAlpha.Deemphasis)
					),
					onClick = onSkip
				) {
					Text(text = stringResource(Res.string.wizard_skip))
				}

				Button(
					modifier = Modifier.testTag(WizardUiTags.PrimaryButton),
					contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
					onClick = onStart
				) {
					Text(text = stringResource(Res.string.wizard_start))
				}
			}
		}
	}
}
