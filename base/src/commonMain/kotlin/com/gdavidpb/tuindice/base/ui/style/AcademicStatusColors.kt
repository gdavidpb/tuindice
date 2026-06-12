package com.gdavidpb.tuindice.base.ui.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AcademicStatusColors {
	val ApprovedDark = Color(0xFF8FE38C)
	val ApprovedLight = Color(0xFF2E7D32)
	val AvailableDark = Color(0xFF8A8F94)
	val AvailableLight = Color(0xFF8A8A8A)
	val BlockedDark = AvailableDark.copy(alpha = 0.72f)
	val BlockedLight = AvailableLight.copy(alpha = 0.72f)
	val SuccessDark = Color(0xFF91EE9A)
	val SuccessLight = Color(0xFF00875A)
	val WarningDark = Color(0xFFFFC400)
	val WarningLight = Color(0xFF9A4F04)
	val LoadBandLightDark = Color(0xFF8CCBFF)
	val LoadBandLightLight = Color(0xFF1565C0)
	val LoadBandManageableDark = Color(0xFF7DE7C6)
	val LoadBandManageableLight = Color(0xFF00796B)

	@Composable
	fun current(): Color = MaterialTheme.colorScheme.primary

	@Composable
	fun approved(): Color = if (TuIndiceDarkTheme.isDark()) ApprovedDark else ApprovedLight

	@Composable
	fun available(): Color = if (TuIndiceDarkTheme.isDark()) AvailableDark else AvailableLight

	@Composable
	fun blocked(): Color = if (TuIndiceDarkTheme.isDark()) BlockedDark else BlockedLight

	@Composable
	fun success(): Color = if (TuIndiceDarkTheme.isDark()) SuccessDark else SuccessLight

	@Composable
	fun warning(): Color = if (TuIndiceDarkTheme.isDark()) WarningDark else WarningLight

	@Composable
	fun loadBandLight(): Color = if (TuIndiceDarkTheme.isDark()) LoadBandLightDark else LoadBandLightLight

	@Composable
	fun loadBandManageable(): Color = if (TuIndiceDarkTheme.isDark()) LoadBandManageableDark else LoadBandManageableLight
}
