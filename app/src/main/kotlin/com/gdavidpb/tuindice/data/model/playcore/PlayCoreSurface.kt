package com.gdavidpb.tuindice.data.model.playcore

enum class PlayCoreSurface(
	val logName: String,
	val serviceAction: String
) {
	Review(
		logName = "review",
		serviceAction = "com.google.android.finsky.BIND_IN_APP_REVIEW_SERVICE"
	),
	Update(
		logName = "update",
		serviceAction = "com.google.android.play.core.install.BIND_UPDATE_SERVICE"
	)
}
