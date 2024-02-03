package com.example.phasig.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.wear.compose.material.Checkbox
import androidx.wear.compose.material.Text

class OptionalTimePickerState(
    initiallySelectedOptionH: Int = 0,
    initiallySelectedOptionM: Int = 0,
    initialEnabled: Boolean = false
)
{
    val timePickerState: TimePickerState
    var tpkrEnabled by mutableStateOf(initialEnabled)

    init {
        timePickerState = TimePickerState(initiallySelectedOptionH, initiallySelectedOptionM)
    }
}

@Composable
fun rememberOptionalTimePickerState(
    initiallySelectedOptionH: Int,
    initiallySelectedOptionM: Int,
    initialEnabled: Boolean
) = remember { OptionalTimePickerState(initiallySelectedOptionH, initiallySelectedOptionM, initialEnabled) }

@Composable
fun OptionalTimePicker(
    label: String,
    optionalTimePickerState: OptionalTimePickerState
) {
    Column()
    {
        Row()
        {
            Text(label)
            Checkbox(
                checked = optionalTimePickerState.tpkrEnabled,
                enabled = true,
                onCheckedChange = {
                    optionalTimePickerState.tpkrEnabled =
                        it
                }
            )
        }

        if (optionalTimePickerState.tpkrEnabled) {
            TimePicker(optionalTimePickerState.timePickerState)
        }
    }
}