package com.gdavidpb.tuindice.login.ui.view

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString

@Composable
fun LinkText(
	text: String,
	style: TextStyle,
	linkStyle: SpanStyle,
	links: Map<String, () -> Unit>
) {
	val annotatedString = remember {
		buildAnnotatedString {
			append(text)

			links.forEach { (link, block) ->
				val start = text.indexOf(link)
				val end = start + link.length

				if (start != -1) {
					addStyle(
						style = linkStyle,
						start = start,
						end = end
					)

					addLink(
						LinkAnnotation.Clickable(tag = link) { block() },
						start = start,
						end = end
					)

					addStringAnnotation(
						tag = link,
						annotation = link,
						start = start,
						end = end
					)
				}
			}
		}
	}

	Text(
		text = annotatedString,
		style = style
	)
}
