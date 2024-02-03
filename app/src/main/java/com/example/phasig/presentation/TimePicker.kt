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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TimePicker(timePickerState: TimePickerState) {
    val textStyle = MaterialTheme.typography.display1

    @Composable
    fun TP_Option(column: Int, text: String) =
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = text, style = textStyle,
                color = if (timePickerState.selectedColumn == column) MaterialTheme.colors.secondary
                else MaterialTheme.colors.onBackground,
                modifier = Modifier
                    .align(Alignment.Center)
                    .wrapContentSize()
                    .pointerInteropFilter {
                        if (it.action == MotionEvent.ACTION_DOWN) timePickerState.selectedColumn =
                            column
                        true
                    }
            )
        }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        val hourContentDescription by remember {
            derivedStateOf { "${timePickerState.hourState.selectedOption + 1} hours" }
        }
        Picker(
            readOnly = timePickerState.selectedColumn != 0,
            state = timePickerState.hourState,
            modifier = Modifier.size(64.dp, 100.dp),
            contentDescription = hourContentDescription,
            option = { hour: Int ->
                TP_Option(
                    0,
                    "%2d".format(hour)
                )
            }
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = ":",
            style = textStyle,
            color = MaterialTheme.colors.onBackground
        )
        Spacer(Modifier.width(8.dp))

        val minuteContentDescription by remember {
            derivedStateOf { "${timePickerState.minuteState.selectedOption} minutes" }
        }
        Picker(
            readOnly = timePickerState.selectedColumn != 1,
            state = timePickerState.minuteState,
            modifier = Modifier.size(64.dp, 100.dp),
            contentDescription = minuteContentDescription,
            option = { minute: Int ->
                TP_Option(
                    1,
                    "%02d".format(minute)
                )
            }
        )
    }
}