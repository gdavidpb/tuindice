package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.LineHeightStyle
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatComposerItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.text.EditableTextFieldState
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_chat_composer_placeholder

private const val COMPOSER_MAX_LINES = 4

// The text keeps 12 dp above and below each line, so one line is as tall as the 48 dp end icons.
private val LINE_PADDING = TuIndiceSpacing.XLarge

// The field, with the photo clip and the send button inside its end (ComposerEndIcons). The field is off while a
// restriction holds, and with it the clip; the send button stays, off. While a turn can be stopped, the stop button
// takes the place of the send button.
//
// The end icons sit at the bottom of the field: with one line they are centred on it, and when the field grows they
// stay by the last line, where the text is being written.
//
// The field keeps its own text (EditableTextFieldState), as every field of the app the machine echoes: on iOS the
// String overload loses and reorders keys (CMP-7312), and an echo between two keys would undo the second. The text the
// chat replaces (cleared by the send, given back by a refused send) comes with a new revision and is adopted.
@Composable
fun AssistantChatComposerView(
	composer: ChatComposerItem,
	isCameraAvailable: Boolean,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	val field = remember { EditableTextFieldState(composer.text, composer.textRevision) }
	val interactionSource = remember { MutableInteractionSource() }
	val textStyle = composerTextStyle()

	field.syncExternal(composer.text, composer.textRevision)

	BasicTextField(
		value = field.value,
		onValueChange = { value ->
			if (field.edit(value)) onAction(AssistantChat.Action.ChangeText(value.text))
		},
		modifier = modifier
			.fillMaxWidth()
			.defaultMinSize(minHeight = OutlinedTextFieldDefaults.MinHeight)
			.testTag(AssistantUiTags.Composer),
		enabled = composer.isEnabled,
		textStyle = textStyle,
		cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
		maxLines = COMPOSER_MAX_LINES,
		interactionSource = interactionSource,
		decorationBox = { innerTextField ->
			OutlinedTextFieldDefaults.DecorationBox(
				value = field.value.text,
				innerTextField = {
					Row(verticalAlignment = Alignment.Bottom) {
						Box(modifier = Modifier.weight(1f).padding(vertical = LINE_PADDING)) {
							if (field.value.text.isEmpty()) {
								Text(
									text = stringResource(Res.string.assistant_chat_composer_placeholder),
									style = textStyle,
									color = MaterialTheme.colorScheme.onSurfaceVariant
								)
							}
							innerTextField()
						}
						ComposerEndIcons(composer = composer, isCameraAvailable = isCameraAvailable, onAction = onAction)
					}
				},
				enabled = composer.isEnabled,
				singleLine = false,
				visualTransformation = VisualTransformation.None,
				interactionSource = interactionSource,
				container = { ComposerContainer(enabled = composer.isEnabled, interactionSource = interactionSource) },
				contentPadding = PaddingValues(
					start = TuIndiceSpacing.Section,
					top = TuIndiceSpacing.XSmall,
					end = TuIndiceSpacing.XSmall,
					bottom = TuIndiceSpacing.XSmall
				)
			)
		}
	)
}

// The glyphs are centred in their line, so a single line sits in the middle of the field, level with its icons.
@Composable
private fun composerTextStyle(): TextStyle =
	MaterialTheme.typography.bodyLarge.copy(
		color = MaterialTheme.colorScheme.onSurface,
		lineHeightStyle = LineHeightStyle(alignment = LineHeightStyle.Alignment.Center, trim = LineHeightStyle.Trim.None)
	)

// The field's outline: a neutral border, focused or not, because the accent belongs to the send button, not the field.
@Composable
private fun ComposerContainer(enabled: Boolean, interactionSource: MutableInteractionSource) {
	OutlinedTextFieldDefaults.Container(
		enabled = enabled,
		isError = false,
		interactionSource = interactionSource,
		colors = OutlinedTextFieldDefaults.colors(
			focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
			unfocusedBorderColor = MaterialTheme.colorScheme.outline
		),
		shape = RoundedCornerShape(TuIndiceRadius.Large)
	)
}
