package com.gdavidpb.tuindice.presentation.transition

import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.contract.PendingSessionEnd

// The same state with another pending end of the session: the way every transition between states
// carries it over.
internal fun Main.State.withPendingSessionEnd(pending: PendingSessionEnd?): Main.State {
	return when (this) {
		is Main.State.Starting -> copy(pendingSessionEnd = pending)
		is Main.State.AppUnavailable -> copy(pendingSessionEnd = pending)
		is Main.State.OutdatedApp -> copy(pendingSessionEnd = pending)
		is Main.State.Content -> copy(pendingSessionEnd = pending)
		is Main.State.Failed -> copy(pendingSessionEnd = pending)
	}
}
