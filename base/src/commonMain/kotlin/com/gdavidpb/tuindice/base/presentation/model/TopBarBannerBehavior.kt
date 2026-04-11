package com.gdavidpb.tuindice.base.presentation.model

sealed interface TopBarBannerBehavior {
	data object Persistent : TopBarBannerBehavior

	data class AutoDismiss(
		val millis: Long
	) : TopBarBannerBehavior {
		init {
			require(millis > 0) { "AutoDismiss duration must be positive." }
		}
	}
}
