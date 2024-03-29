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

class TimePickerState(
    initiallySelectedOptionH: Int = 0,
    initiallySelectedOptionM: Int = 0
): DualPickerState(
    24,
    initiallySelectedOptionH,
    "hours",
    "%2d",
    60,
    initiallySelectedOptionM,
    "minutes",
    "%02d",
    ":") {
    val hourState: PickerState
        get() = leftState
    val minuteState: PickerState
        get() = rightState
}

@Composable
fun rememberTimePickerState(
    initiallySelectedOptionH: Int,
    initiallySelectedOptionM: Int
) = remember { TimePickerState(initiallySelectedOptionH, initiallySelectedOptionM) }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TimePicker(timePickerState: TimePickerState) {
    DualPicker(dualPickerState = timePickerState)
}