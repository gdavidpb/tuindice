package com.gdavidpb.tuindice.about.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import org.jetbrains.compose.resources.stringResource
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.snack_mail_app_missing
import tuindice.about.generated.resources.snack_open_uri_failed

/**
 * Hands a uri over to the system (mail, store, browser) and tells the student when nothing could take it. A store link
 * that the device cannot open falls back to its web page. Settings and the About screen both open uris this way.
 */
@Composable
internal fun rememberExternalUriOpener(
	showSnackBar: (message: SnackBarMessage) -> Unit
): (uri: String) -> Unit {
	val uriHandler = LocalUriHandler.current
	val mailAppMissingMessage = stringResource(Res.string.snack_mail_app_missing)
	val openUriFailedMessage = stringResource(Res.string.snack_open_uri_failed)

	return remember(uriHandler, showSnackBar, mailAppMissingMessage, openUriFailedMessage) {
		val opener: (String) -> Unit = { uri ->
			runCatching { uriHandler.openUri(uri) }
				.recoverCatching { throwable ->
					val fallbackUri = uri.toStoreWebUri() ?: throw throwable

					uriHandler.openUri(fallbackUri)
				}
				.onFailure {
					showSnackBar(
						SnackBarMessage(
							message = if (uri.startsWith(MAILTO_SCHEME)) mailAppMissingMessage else openUriFailedMessage
						)
					)
				}
		}

		opener
	}
}

private const val MAILTO_SCHEME = "mailto:"
private const val MARKET_SCHEME = "market://"
private const val STORE_WEB_BASE = "https://play.google.com/store/apps/"

private fun String.toStoreWebUri(): String? =
	takeIf { startsWith(MARKET_SCHEME) }?.replace(MARKET_SCHEME, STORE_WEB_BASE)
