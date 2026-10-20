package com.gdavidpb.tuindice.base.presentation.mapper

// The moments of a cell of a card, where a value stands on its own and no article leads the day. They are the moments
// of the chat's sentences without «el» and without «a las»: "hoy, 3:05 p. m.", "lunes 19 de octubre, 11:22 p. m.".
enum class DateCellStyle {
	TODAY_CLOCK,
	WEEKDAY_DAY_MONTH_CLOCK,
	WEEKDAY_DAY_MONTH_YEAR_CLOCK
}
