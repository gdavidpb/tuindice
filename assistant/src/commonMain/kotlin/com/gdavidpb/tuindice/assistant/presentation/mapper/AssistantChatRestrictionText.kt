package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.daysToNow
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.abuse.StandingLevel
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_chat_restricted_banned
import tuindice.assistant.generated.resources.assistant_chat_restricted_cooldown
import tuindice.assistant.generated.resources.assistant_chat_restricted_cooldown_today
import tuindice.assistant.generated.resources.assistant_chat_restricted_cooldown_until
import tuindice.assistant.generated.resources.assistant_chat_restricted_default
import tuindice.assistant.generated.resources.assistant_chat_restricted_suspended
import tuindice.assistant.generated.resources.assistant_chat_restricted_suspended_until
import kotlin.time.Clock

// What the student reads of a restriction: the level in their words and, when the server gave its end, when it ends,
// in the time zone of the device. A pause says the hour, and the day too when it does not end today; a suspension
// says the day; a ban has no end. A level that is not known yet says only that the assistant cannot answer.
internal fun restrictionText(level: StandingLevel?, untilMillis: Long?, clock: Clock): UiText {
	return when (level) {
		StandingLevel.COOLDOWN -> when {
			untilMillis == null -> UiText.Resource(Res.string.assistant_chat_restricted_cooldown)

			untilMillis.daysToNow(clock) == 0 -> UiText.Resource(
				Res.string.assistant_chat_restricted_cooldown_today,
				listOf(untilMillis.hourText())
			)

			else -> UiText.Resource(
				Res.string.assistant_chat_restricted_cooldown_until,
				listOf(untilMillis.dayText(), untilMillis.hourText())
			)
		}

		StandingLevel.SUSPENDED -> if (untilMillis == null) {
			UiText.Resource(Res.string.assistant_chat_restricted_suspended)
		} else {
			UiText.Resource(Res.string.assistant_chat_restricted_suspended_until, listOf(untilMillis.dayText()))
		}

		StandingLevel.BANNED -> UiText.Resource(Res.string.assistant_chat_restricted_banned)

		else -> UiText.Resource(Res.string.assistant_chat_restricted_default)
	}
}

// "18 de octubre 2026", as the app writes a day.
private fun Long.dayText(): UiText = formatDate(DateTextStyle.DAY_MONTH_YEAR)

// "03:05 p. m.", as the app writes an hour.
private fun Long.hourText(): UiText = formatDate(DateTextStyle.TIME)
