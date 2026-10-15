package com.gdavidpb.tuindice.summary.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.model.TopBarActionBus

/**
 * Carries the top bar actions of the Statistics screen from the host to the screen, which is the one that opens
 * what they ask for. It keeps no action for a screen that is not listening: the bar only shows them while the screen
 * is in front.
 */
class StatisticsTopBarActionBus : TopBarActionBus<TopBarAction>()
