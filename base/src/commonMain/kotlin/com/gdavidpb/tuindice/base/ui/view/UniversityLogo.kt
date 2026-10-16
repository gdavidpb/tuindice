package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.painterResource
import tuindice.base.generated.resources.Res
import tuindice.base.generated.resources.ic_usb

/**
 * The logo of Universidad Simón Bolívar, drawn in the colour it is tinted with. About credits the university with it,
 * and the shareable term card signs its header with it, so both read the one drawable kept here.
 */
@Composable
fun universityLogoPainter(): Painter = painterResource(Res.drawable.ic_usb)
