package com.gdavidpb.tuindice.base.ui.style

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AcademicStatusColors {
	val ApprovedDark = Color(0xFF8FE38C)
	val ApprovedLight = Color(0xFF2E7D32)
	val Current = Color(0xFFFABD00)
	val AvailableDark = Color(0xFF8A8F94)
	val AvailableLight = Color(0xFF8A8A8A)
	val BlockedDark = AvailableDark.copy(alpha = 0.72f)
	val BlockedLight = AvailableLight.copy(alpha = 0.72f)
	val SuccessDark = Color(0xFF91EE9A)
	val SuccessLight = Color(0xFF00875A)
	val WarningDark = Color(0xFFFFC400)
	val WarningLight = Color(0xFF9A4F04)

	@Composable
	fun current(): Color = MaterialTheme.colorScheme.primary

	@Composable
	fun approved(): Color = if (isSystemInDarkTheme()) ApprovedDark else ApprovedLight

	@Composable
	fun available(): Color = if (isSystemInDarkTheme()) AvailableDark else AvailableLight

	@Composable
	fun blocked(): Color = if (isSystemInDarkTheme()) BlockedDark else BlockedLight

	@Composable
	fun success(): Color = if (isSystemInDarkTheme()) SuccessDark else SuccessLight

	@Composable
	fun warning(): Color = if (isSystemInDarkTheme()) WarningDark else WarningLight
}
