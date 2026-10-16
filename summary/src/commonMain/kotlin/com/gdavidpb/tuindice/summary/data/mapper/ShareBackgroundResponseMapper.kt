package com.gdavidpb.tuindice.summary.data.mapper

import com.gdavidpb.tuindice.summary.data.model.ShareBackgroundResponse
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.domain.model.ShareBackgroundArt

// A blank author is no author: a credit with nothing after the title would read as a broken line.
fun ShareBackgroundResponse.toShareBackground() = ShareBackground(
	id = id,
	title = title,
	author = author?.takeIf { name -> name.isNotBlank() },
	shortName = shortName,
	art = ShareBackgroundArt.Remote(
		cardUrl = card,
		thumbUrl = thumb
	)
)
