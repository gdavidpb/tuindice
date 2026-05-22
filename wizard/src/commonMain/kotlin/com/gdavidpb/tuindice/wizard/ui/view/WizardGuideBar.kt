package com.gdavidpb.tuindice.wizard.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.wizard.presentation.contract.Wizard
import com.gdavidpb.tuindice.wizard.ui.WizardUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.wizard.generated.resources.Res
import tuindice.wizard.generated.resources.wizard_back
import tuindice.wizard.generated.resources.wizard_done
import tuindice.wizard.generated.resources.wizard_next
import tuindice.wizard.generated.resources.wizard_progress
import tuindice.wizard.generated.resources.wizard_skip

@Composable
fun WizardGuideBar(
	state: Wizard.State.Content,
	onBack: () -> Unit,
	onSkip: () -> Unit,
	onNext: () -> Unit,
	onFinish: () -> Unit,
	modifier: Modifier = Modifier
) {
	val step = state.currentStep
	val progressText = stringResource(
		Res.string.wizard_progress,
		state.currentProgress,
		state.totalProgress
	)
	val primaryText = if (state.isLastStep)
		stringResource(Res.string.wizard_done)
	else
		stringResource(Res.string.wizard_next)

	Surface(
		modifier = modifier
			.fillMaxWidth()
			.testTag(WizardUiTags.GuideBar),
		color = MaterialTheme.colorScheme.surface,
		tonalElevation = 3.dp,
		shadowElevation = 8.dp
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
				.padding(
					start = 20.dp,
					top = 10.dp,
					end = 20.dp,
					bottom = 8.dp
				),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(12.dp)
			) {
				Text(
					modifier = Modifier
						.weight(1f)
						.testTag(WizardUiTags.currentStep(step.id)),
					text = stringResource(step.title),
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.SemiBold,
					color = MaterialTheme.colorScheme.onSurface,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
				Text(
					modifier = Modifier.testTag(WizardUiTags.ProgressText),
					text = progressText,
					style = MaterialTheme.typography.labelMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 1
				)
			}
			Text(
				text = stringResource(step.message).toWizardAnnotatedText(),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				maxLines = 5,
				overflow = TextOverflow.Ellipsis
			)

			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				TextButton(
					modifier = Modifier
						.widthIn(min = 64.dp, max = 96.dp)
						.testTag(WizardUiTags.SkipButton),
					colors = ButtonDefaults.textButtonColors(
						contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.68f)
					),
					contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
					onClick = onSkip
				) {
					Text(
						text = stringResource(Res.string.wizard_skip),
						maxLines = 1
					)
				}

				Spacer(modifier = Modifier.weight(1f))

				TextButton(
					modifier = Modifier
						.widthIn(min = 64.dp, max = 96.dp)
						.testTag(WizardUiTags.BackButton),
					enabled = !state.isFirstStep,
					contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
					onClick = onBack
				) {
					Text(
						text = stringResource(Res.string.wizard_back),
						maxLines = 1
					)
				}

				Button(
					modifier = Modifier
						.widthIn(min = 132.dp)
						.testTag(WizardUiTags.PrimaryButton),
					onClick = if (state.isLastStep) onFinish else onNext
				) {
					Text(
						text = primaryText,
						maxLines = 1
					)
				}
			}
		}
	}
}
