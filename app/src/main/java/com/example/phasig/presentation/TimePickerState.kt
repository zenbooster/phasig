package com.example.phasig.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.wear.compose.material.PickerState

class TimePickerState(
    initiallySelectedOptionH: Int = 0,
    initiallySelectedOptionM: Int = 0
) {
    val hourState: PickerState
    val minuteState: PickerState
    var selectedColumn by mutableStateOf(0)

    init
    {
        hourState = PickerState(
            initialNumberOfOptions = 24,
            initiallySelectedOption = initiallySelectedOptionH
        )
        minuteState = PickerState(
            initialNumberOfOptions = 60,
            initiallySelectedOption = initiallySelectedOptionM
        )
    }
}

@Composable
fun rememberTimePickerState(
    initiallySelectedOptionH: Int,
    initiallySelectedOptionM: Int
) = remember { TimePickerState(initiallySelectedOptionH, initiallySelectedOptionM) }