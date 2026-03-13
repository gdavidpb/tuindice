package $PACKAGE.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig

object $FEATURE_NAME {
	sealed class State(
		override val topBarTitle: String = $SCREEN_TITLE_LITERAL,
		override val topBarConfig: TopBarConfig = TopBarConfig.$TOP_BAR_CONFIG,
		override val isTopBarVisible: Boolean = true,
		override val isBottomBarVisible: Boolean = $BOTTOM_BAR_VISIBLE_LITERAL
	) : ViewState() {
		data object Loading : State()

		data class Content(
			val message: String
		) : State()

		data object Failed : State()
	}

	sealed class Action : ViewAction() {
		data object $LOAD_ACTION_NAME : Action()
	}

	sealed class Effect : ViewEffect() {
		class ShowSnackBar(val message: String) : Effect()
	}
}
