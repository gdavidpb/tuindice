package com.gdavidpb.tuindice.summary.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.model.ShareCardPicture
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArtwork
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermBackgroundItem
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermTermItem

/**
 * The sheet that shares one closed term as an image, opened over the Statistics screen: the terms to choose from, the
 * backgrounds the card can wear and the share itself. The card says nothing of the student but the figures and the
 * career.
 */
object ShareTerm {
	sealed class State : ViewState {
		data object Idle : State()

		// The term the sheet was opened on, or null for the newest one.
		data class Loading(
			val requestedTermKey: String?
		) : State()

		// [terms] are the closed terms that have an index, newest first, with one of them selected; empty when none has
		// closed yet. [isSharing] is true while the image is encoded and handed to the platform's share sheet.
		data class Content(
			val terms: List<ShareTermTermItem>,
			val backgrounds: List<ShareTermBackgroundItem>,
			val isSharing: Boolean = false
		) : State() {
			// The term and the background the card shows, resolved from the selected items each time the content is built, so a
			// copy with other selections resolves them again. The previous and next backgrounds are the ones beside the selected
			// one in the strip: a swipe across the card moves to them.
			val selectedTerm: ShareTermTermItem? = terms.firstOrNull { item -> item.selected }

			val selectedBackground: ShareTermArtwork? = backgrounds.firstOrNull { item -> item.selected }?.artwork

			private val selectedIndex: Int = backgrounds.indexOfFirst { item -> item.selected }

			val previousBackgroundId: String? = backgrounds.getOrNull(selectedIndex - 1)?.artwork?.id

			val nextBackgroundId: String? = if (selectedIndex < 0) {
				null
			} else {
				backgrounds.getOrNull(selectedIndex + 1)?.artwork?.id
			}
		}

		// The statistics cannot be read, so there is no term to share: the sheet says so and does not retry.
		data class Unavailable(
			val title: UiText,
			val message: UiText
		) : State()
	}

	sealed class Action : ViewAction {
		// The sheet is opened: it reads the terms and the backgrounds, and selects [termKey] when it is one of them.
		class ObserveShareTerm(
			val termKey: String?
		) : Action()

		class SelectTerm(
			val termKey: String
		) : Action()

		// The background is chosen, and it is kept as the one the next card opens with.
		class SelectBackground(
			val backgroundId: String
		) : Action()

		// The card that the screen captured: it is encoded as a PNG and handed to the platform's share sheet.
		class ShareImage(
			val picture: ShareCardPicture
		) : Action()
	}

	sealed class Effect : ViewEffect {
		// The image could not be prepared for the share sheet: the sheet says so and stays open to try again.
		data object ImageShareFailed : Effect()
	}
}
