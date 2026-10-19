package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.ui.graphics.vector.ImageVector
import com.gdavidpb.tuindice.base.ui.icon.ShareIcon
import com.gdavidpb.tuindice.summary.presentation.model.TermReportActionIconKind

// The icon of each mark an action of "Qué sigue" carries.
internal fun TermReportActionIconKind.imageVector(): ImageVector {
	return when (this) {
		TermReportActionIconKind.TREND -> Icons.AutoMirrored.Outlined.TrendingUp
		TermReportActionIconKind.TREE -> Icons.Outlined.AccountTree
		TermReportActionIconKind.SHARE -> ShareIcon
		TermReportActionIconKind.BAR_CHART -> Icons.Outlined.BarChart
		TermReportActionIconKind.SCHOOL -> Icons.Outlined.School
		TermReportActionIconKind.TABLE -> Icons.Outlined.TableChart
		TermReportActionIconKind.ASSIGNMENT -> Icons.AutoMirrored.Outlined.Assignment
		TermReportActionIconKind.FIND_IN_PAGE -> Icons.Outlined.FindInPage
		TermReportActionIconKind.DASHBOARD -> Icons.Outlined.Dashboard
		TermReportActionIconKind.INFO -> Icons.Outlined.Info
		TermReportActionIconKind.LOCK -> Icons.Outlined.Lock
	}
}
