package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.abuse.StandingLevel
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_chat_restricted_banned
import tuindice.assistant.generated.resources.assistant_chat_restricted_cooldown
import tuindice.assistant.generated.resources.assistant_chat_restricted_cooldown_until
import tuindice.assistant.generated.resources.assistant_chat_restricted_default
import tuindice.assistant.generated.resources.assistant_chat_restricted_suspended
import tuindice.assistant.generated.resources.assistant_chat_restricted_suspended_until
import kotlin.time.Clock

// What the student reads of a restriction: the level in their words and, when the server gave its end, when it ends, in
// the time zone of the device. A pause and a suspension say the moment they end, with the day too when it is not today;
// a ban has no end. A level that is not known yet says only that the assistant cannot answer.
internal fun restrictionText(level: StandingLevel?, untilMillis: Long?, clock: Clock): UiText {
	val nowMillis = clock.now().toEpochMilliseconds()

	return when (level) {
		StandingLevel.COOLDOWN -> if (untilMillis == null) {
			UiText.Resource(Res.string.assistant_chat_restricted_cooldown)
		} else {
			UiText.Resource(
				Res.string.assistant_chat_restricted_cooldown_until,
				listOf(untilMillis.toChatDateText(nowMillis))
			)
		}

		StandingLevel.SUSPENDED -> if (untilMillis == null) {
			UiText.Resource(Res.string.assistant_chat_restricted_suspended)
		} else {
			UiText.Resource(
				Res.string.assistant_chat_restricted_suspended_until,
				listOf(untilMillis.toChatDateText(nowMillis))
			)
		}

		StandingLevel.BANNED -> UiText.Resource(Res.string.assistant_chat_restricted_banned)

		else -> UiText.Resource(Res.string.assistant_chat_restricted_default)
	}
}
