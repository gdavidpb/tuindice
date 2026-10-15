package com.gdavidpb.tuindice.summary.ui.share

import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArt
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArtwork
import com.gdavidpb.tuindice.summary.presentation.model.ShareableTermCardItem

/**
 * The card on screen: the term it shows and the artwork it wears, which change together. [settledFor] is the artwork
 * the student chose that [artwork] was made for, so that a term which comes while that artwork is still loading waits
 * for it.
 */
internal data class ShareableTermCardShown(
	val item: ShareableTermCardItem,
	val artwork: ShareTermArtwork?,
	val settledFor: ShareTermArtwork?
) {
	// A new term is drawn at once, unless the artwork the student chose with it is still loading: then the card keeps the
	// term and the artwork it shows, and both change once the artwork is ready. A card with no artwork yet keeps nothing.
	fun showingTerm(term: ShareableTermCardItem, chosen: ShareTermArtwork): ShareableTermCardShown {
		val waitsForArtwork = artwork != null && settledFor != chosen

		return if (waitsForArtwork) this else copy(item = term)
	}

	// The artwork the student chose is drawn once its picture is known, which is the artwork itself or the one it falls
	// back to, together with the term that is current by then.
	fun showingArtwork(
		chosen: ShareTermArtwork,
		resolved: ShareTermArtwork,
		term: ShareableTermCardItem
	): ShareableTermCardShown {
		return copy(item = term, artwork = resolved, settledFor = chosen)
	}

	// The card turns to the artwork it falls back to when the picture on screen fails to load.
	fun fallingBackFrom(failed: ShareTermArtwork): ShareableTermCardShown {
		if (artwork?.id != failed.id) return this

		return copy(artwork = failed.shownAfter(loaded = false))
	}

	companion object {
		// The artwork the app ships with is drawn at once; a remote one is not drawn until its picture has loaded.
		fun initial(term: ShareableTermCardItem, chosen: ShareTermArtwork): ShareableTermCardShown {
			val isBundled = chosen.art == ShareTermArt.Bundled

			return ShareableTermCardShown(
				item = term,
				artwork = chosen.takeIf { isBundled },
				settledFor = chosen.takeIf { isBundled }
			)
		}
	}
}
