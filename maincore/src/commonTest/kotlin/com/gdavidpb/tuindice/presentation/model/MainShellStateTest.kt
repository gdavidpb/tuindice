package com.gdavidpb.tuindice.presentation.model

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import kotlin.test.Test
import kotlin.test.assertEquals

class MainShellStateTest {

	@Test
	fun when_theScreenDeclaresAContextAndTheAssistantIsOn_then_theShellLeadsWithTheAssistant() {
		val shell = pensumScreen().toMainShellState(isAssistantVisible = true)

		assertEquals(pensumContext, shell.assistantContext)
		assertEquals(
			listOf(TopBarSlot.Assistant, TopBarSlot.Action(TopBarAction.SearchPensumAction)),
			shell.inlineActions
		)
		assertEquals(emptyList(), shell.overflowActions)
	}

	@Test
	fun when_theAssistantIsOff_then_theShellOffersOnlyTheActionsOfTheScreen() {
		val shell = pensumScreen().toMainShellState(isAssistantVisible = false)

		assertEquals(listOf(TopBarSlot.Action(TopBarAction.SearchPensumAction)), shell.inlineActions)
		assertEquals(emptyList(), shell.overflowActions)
	}

	@Test
	fun when_theTabRootDeclaresTheAvatar_then_theBarEndsWithIt() {
		val shell = tabRoot(declaresAvatar = true).toMainShellState()

		assertEquals(true, shell.showsProfileAvatar)
	}

	@Test
	fun when_aTabRootDoesNotDeclareTheAvatar_then_itsBarHasNone() {
		val shell = tabRoot(declaresAvatar = false).toMainShellState()

		assertEquals(false, shell.showsProfileAvatar)
	}

	@Test
	fun when_theScreenIsPushedOverATab_then_thereIsNoAvatar() {
		val pushed = object : ViewState {
			override val isTopBarVisible: Boolean = true
			override val isBottomBarVisible: Boolean = false
			override val isProfileAvatarVisible: Boolean = true
		}

		assertEquals(false, pushed.toMainShellState().showsProfileAvatar)
	}

	@Test
	fun when_theTopBarIsHidden_then_thereIsNoAvatar() {
		val hidden = object : ViewState {
			override val isTopBarVisible: Boolean = false
			override val isBottomBarVisible: Boolean = true
			override val isProfileAvatarVisible: Boolean = true
		}

		assertEquals(false, hidden.toMainShellState().showsProfileAvatar)
	}

	@Test
	fun when_theAvatarIsShown_then_itDoesNotTakeAPlaceFromTheActionsOfTheScreen() {
		val shell = pensumScreen().toMainShellState(isAssistantVisible = true)

		assertEquals(
			listOf(TopBarSlot.Assistant, TopBarSlot.Action(TopBarAction.SearchPensumAction)),
			shell.inlineActions
		)
		assertEquals(emptyList(), shell.overflowActions)
	}

	private fun tabRoot(declaresAvatar: Boolean): ViewState = object : ViewState {
		override val isTopBarVisible: Boolean = true
		override val isBottomBarVisible: Boolean = true
		override val isProfileAvatarVisible: Boolean = declaresAvatar
	}

	private fun pensumScreen(): ViewState = object : ViewState {
		override val topBarConfig: TopBarConfig = TopBarConfig.Pensum
		override val assistantContext: AssistantScreenContext = pensumContext
	}

	private val pensumContext = AssistantScreenContext(screen = AssistantScreen.PENSUM)
}
