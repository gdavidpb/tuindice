package com.gdavidpb.tuindice.base.presentation.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

interface Dialog

typealias DialogState<T> = MutableState<T?>

@Composable
fun <T : Dialog> rememberDialogState(): DialogState<T> = remember {
	mutableStateOf(null)
}