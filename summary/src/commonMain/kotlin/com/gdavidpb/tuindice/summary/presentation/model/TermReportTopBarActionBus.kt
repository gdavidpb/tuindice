package com.gdavidpb.tuindice.summary.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.model.TopBarActionBus

/**
 * Carries the top bar actions of the term report from the host to the screen, which is the one that opens what they ask
 * for. Like the Statistics bus, it keeps nothing for a screen that is not listening.
 */
class TermReportTopBarActionBus : TopBarActionBus<TopBarAction>()
