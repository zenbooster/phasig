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

open class DualPickerState(
	initiallySelectedOptionNL: Int = 1,
    initiallySelectedOptionL: Int = 0,
    leftUnits: String = "x",
    leftFmt: String = "%d",
	initiallySelectedOptionNR: Int = 1,
    initiallySelectedOptionR: Int = 0,
    rightUnits: String = "y",
    rightFmt: String = "%d",
    delimiter: String = ", "
) {
    val leftState: PickerState
	val leftUnits: String
    val leftFmt: String
    val rightState: PickerState
	val rightUnits: String
    val rightFmt: String
    val delimiter: String
    var selectedColumn by mutableStateOf(0)

    init
    {
        leftState = PickerState(
            initialNumberOfOptions = initiallySelectedOptionNL,
            initiallySelectedOption = initiallySelectedOptionL
        )
		this.leftUnits = leftUnits
        this.leftFmt = leftFmt
		
        rightState = PickerState(
            initialNumberOfOptions = initiallySelectedOptionNR,
            initiallySelectedOption = initiallySelectedOptionR
        )
        this.rightUnits = rightUnits
        this.rightFmt = rightFmt

        this.delimiter = delimiter
    }
}

@Composable
fun rememberDualPickerState(
	initiallySelectedOptionNL: Int,
    initiallySelectedOptionL: Int,
    leftUnitsName: String,
    leftFmt: String,
	initiallySelectedOptionNR: Int,
    initiallySelectedOptionR: Int,
    rightUnitsName: String,
    rightFmt: String,
    delimiter: String
) = remember { DualPickerState(
	initiallySelectedOptionNL,
	initiallySelectedOptionL,
    leftUnitsName,
    leftFmt,
	initiallySelectedOptionNR,
	initiallySelectedOptionR,
    rightUnitsName,
    rightFmt,
    delimiter) }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DualPicker(dualPickerState: DualPickerState) {
    val textStyle = MaterialTheme.typography.display1

    @Composable
    fun DP_Option(column: Int, text: String) =
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = text, style = textStyle,
                color = if (dualPickerState.selectedColumn == column) MaterialTheme.colors.secondary
                else MaterialTheme.colors.onBackground,
                modifier = Modifier
                    .align(Alignment.Center)
                    .wrapContentSize()
                    .pointerInteropFilter {
                        if (it.action == MotionEvent.ACTION_DOWN) dualPickerState.selectedColumn =
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
        val leftContentDescription by remember {
            derivedStateOf { "${dualPickerState.leftState.selectedOption + 1} ${dualPickerState.leftUnits}" }
        }
        Picker(
            readOnly = dualPickerState.selectedColumn != 0,
            state = dualPickerState.leftState,
            modifier = Modifier.size(64.dp, 100.dp),
            contentDescription = leftContentDescription,
            option = { left: Int ->
                DP_Option(
                    0,
                    dualPickerState.leftFmt.format(left)
                )
            }
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = dualPickerState.delimiter,
            style = textStyle,
            color = MaterialTheme.colors.onBackground
        )
        Spacer(Modifier.width(8.dp))

        val rightContentDescription by remember {
            derivedStateOf { "${dualPickerState.rightState.selectedOption} ${dualPickerState.rightUnits}" }
        }
        Picker(
            readOnly = dualPickerState.selectedColumn != 1,
            state = dualPickerState.rightState,
            modifier = Modifier.size(64.dp, 100.dp),
            contentDescription = rightContentDescription,
            option = { right: Int ->
                DP_Option(
                    1,
                    dualPickerState.rightFmt.format(right)
                )
            }
        )
    }
}