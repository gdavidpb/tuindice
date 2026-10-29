package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.text.EditableTextFieldState
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_chat_composer_placeholder

private const val COMPOSER_MAX_LINES = 5

// The text keeps 12 dp from the field's edges: the field pads 4 dp (contentPadding) and the text the other 8 dp.
private val TEXT_PADDING = TuIndiceSpacing.XLarge - TuIndiceSpacing.XSmall

// The field: the text on top, across the whole width, and under it a row with the photo clip at its start
// (ComposerStartIcon) and the send button at its end (ComposerEndIcons), so the text has the field's width to itself
// and the field reads as room to write. The field is off while a restriction holds, and with it the clip; the send
// button stays, off. While a turn can be stopped, the stop button takes the place of the send button.
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
					Column {
						Box(
							modifier = Modifier
								.fillMaxWidth()
								.padding(start = TEXT_PADDING, top = TEXT_PADDING, end = TEXT_PADDING)
						) {
							if (field.value.text.isEmpty()) {
								Text(
									text = stringResource(Res.string.assistant_chat_composer_placeholder),
									style = textStyle,
									color = MaterialTheme.colorScheme.onSurfaceVariant
								)
							}
							innerTextField()
						}
						Row(verticalAlignment = Alignment.CenterVertically) {
							ComposerStartIcon(composer = composer, isCameraAvailable = isCameraAvailable, onAction = onAction)
							Spacer(modifier = Modifier.weight(1f))
							ComposerEndIcons(composer = composer, onAction = onAction)
						}
					}
				},
				enabled = composer.isEnabled,
				singleLine = false,
				visualTransformation = VisualTransformation.None,
				interactionSource = interactionSource,
				container = { ComposerContainer(enabled = composer.isEnabled, interactionSource = interactionSource) },
				contentPadding = PaddingValues(TuIndiceSpacing.XSmall)
			)
		}
	)
}

// The glyphs are centred in their line, so the placeholder and the text sit at the same height.
@Composable
private fun composerTextStyle(): TextStyle =
	MaterialTheme.typography.bodyLarge.copy(
		color = MaterialTheme.colorScheme.onSurface,
		lineHeightStyle = LineHeightStyle(alignment = LineHeightStyle.Alignment.Center, trim = LineHeightStyle.Trim.None)
	)

// The photo clip, at the start of the row under the text. While the field is off there is no clip, but its place
// stays, so the row keeps its height when the clip comes or goes.
@Composable
private fun ComposerStartIcon(
	composer: ChatComposerItem,
	isCameraAvailable: Boolean,
	onAction: (AssistantChat.Action) -> Unit
) {
	if (composer.isEnabled) {
		AssistantChatPhotoButtonView(
			isEnabled = composer.isPhotoEnabled,
			isCameraAvailable = isCameraAvailable,
			onAction = onAction
		)
	} else {
		// Where the clip would be: the 48 dp touch target of an icon button, so the row keeps its height.
		Spacer(modifier = Modifier.size(TuIndiceComponentSizes.IconButton))
	}
}

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
