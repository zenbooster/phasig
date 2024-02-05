package com.example.phasig.presentation

import android.view.MotionEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Picker
import androidx.wear.compose.material.PickerState
import androidx.wear.compose.material.Text
import kotlin.math.round

class FracPickerState(
    initiallySelectedOptionH: Int = 0,
    initiallySelectedOptionM: Int = 0
): DualPickerState(
    100,
    initiallySelectedOptionH,
    "units",
    "%02d",
    10,
    initiallySelectedOptionM,
    "tenths",
    "%02d",
    ".") {
    val unitState: PickerState
        get() = leftState
    val units: Int
        get() = unitState.selectedOption
    val tenthState: PickerState
        get() = rightState
    val tenths: Int
        get() = tenthState.selectedOption

    constructor(v: Float): this(v.toInt(), round((v - v.toInt().toFloat()) * 10.0f).toInt()) {}

    fun toFloat(): Float {
        return units.toFloat() + tenths.toFloat() * 0.1f
    }
}

@Composable
fun rememberFracPickerState(
    initiallySelectedOptionU: Int,
    initiallySelectedOptionT: Int
) = remember { FracPickerState(initiallySelectedOptionU, initiallySelectedOptionT) }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FracPicker(fracPickerState: FracPickerState) {
    DualPicker(dualPickerState = fracPickerState)
}