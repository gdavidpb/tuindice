package com.gdavidpb.tuindice.summary.presentation.contract

import com.gdavidpb.tuindice.base.presentation.reducer.ViewAction
import com.gdavidpb.tuindice.base.presentation.reducer.ViewEvent
import com.gdavidpb.tuindice.base.presentation.reducer.ViewState
import com.gdavidpb.tuindice.summary.presentation.model.SummaryViewState

object Summary {
	sealed class State : ViewState {
		object Loading : State()
		class Loaded(val value: SummaryViewState) : State()
		object Failed : State()
	}

	sealed class Action : ViewAction {
		object LoadSummary : Action()
		object TakeProfilePicture : Action()
		object PickProfilePicture : Action()
		class UploadProfilePicture(val path: String) : Action()
		object RemoveProfilePicture : Action()
		object ShowTryLater : Action()
	}

	sealed class Event : ViewEvent {
		object NavigateToAccountDisabled : Event()
		object NavigateToOutdatedPassword : Event()
		class OpenCamera(val output: String) : Event()
		object OpenPicker : Event()
		object ShowProfilePictureUpdatedSnackBar : Event()
		object ShowProfilePictureRemovedSnackBar : Event()
		object ShowTryLaterSnackBar : Event()
		object ShowTimeoutSnackBar : Event()
		class ShowNoConnectionSnackBar(val isNetworkAvailable: Boolean) : Event()
		object ShowDefaultErrorSnackBar : Event()
	}
}