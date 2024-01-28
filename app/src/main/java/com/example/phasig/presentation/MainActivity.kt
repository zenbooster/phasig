/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.phasig.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.Picker
import androidx.wear.compose.material.PickerState
import androidx.wear.compose.material.CompactButton
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.wear.compose.material.rememberPickerState
import androidx.compose.runtime.mutableStateOf
import android.content.Intent
import android.content.Context
import android.content.SharedPreferences
import android.view.MotionEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.pointerInteropFilter

import com.example.phasig.MyService

//import com.example.phasig.R
import com.example.phasig.presentation.theme.PhasigTheme
import com.starry.greenstash.ui.common.ExpandableCard
import java.text.DecimalFormat

class MainActivity : ComponentActivity() {
    var sharedPref: SharedPreferences ?= null
    var pkrState : PickerState ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp("Android", this)
            //WearApp("Android", null)
        }
    }

    override fun onPause()
    {
        //super.onPause();
        with(sharedPref!!.edit())
        {
            putInt("pkrIdx", pkrState!!.selectedOption)
            apply()
        }
        super.onPause();
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WearApp(greetingName: String, ctx: Context?) {
    //val ctx = LocalContext.current
    val sharedPref = ctx?.getSharedPreferences("myPref", Context.MODE_PRIVATE) ?: null
    val btcap = listOf("❚❚", "▶")
    val df = DecimalFormat("#.##")
    val pkrItems = List(101) { df.format(it) }
    val pkrIdx = sharedPref?.getInt("pkrIdx", 12) ?: 12
    val pkrState = rememberPickerState(pkrItems.size, pkrIdx)
    var pkrEnabled by remember { mutableStateOf(true) }
    val contentDescription by remember { derivedStateOf { "${pkrState.selectedOption + 1}" } }
    var btnChecked by remember { mutableStateOf(true) }
    val mysvcIntent: Intent by lazy { Intent(ctx, MyService::class.java) }

    val listState = rememberScalingLazyListState()
    var expandedState by remember { mutableStateOf(false) }
    // begin
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

    var timePickerStateBegin = rememberTimePickerState(4, 0)
    var timePickerStateEnd = rememberTimePickerState(7, 0)

    if(ctx != null) {
        val activity = ctx as MainActivity

        activity.sharedPref = sharedPref
        activity.pkrState = pkrState
    }

    PhasigTheme {
        Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background),
                contentAlignment = Alignment.Center
        ) {
            @Composable
            fun TimePicker(timePickerState: TimePickerState)
            {
                val textStyle = MaterialTheme.typography.display1

                @Composable
                fun TP_Option(column: Int, text: String) = Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = text, style = textStyle,
                        color = if (timePickerState.selectedColumn == column) MaterialTheme.colors.secondary
                        else MaterialTheme.colors.onBackground,
                        modifier = Modifier
                            .align(Alignment.Center).wrapContentSize()
                            .pointerInteropFilter {
                                if (it.action == MotionEvent.ACTION_DOWN) timePickerState.selectedColumn = column
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
                        derivedStateOf { "${timePickerState.hourState.selectedOption + 1 } hours" }
                    }
                    Picker(
                        readOnly = timePickerState.selectedColumn != 0,
                        state = timePickerState.hourState,
                        modifier = Modifier.size(64.dp, 100.dp),
                        contentDescription = hourContentDescription,
                        option = { hour: Int -> TP_Option(0, "%2d".format(hour)) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = ":", style = textStyle, color = MaterialTheme.colors.onBackground)
                    Spacer(Modifier.width(8.dp))

                    val minuteContentDescription by remember {
                        derivedStateOf { "${timePickerState.minuteState.selectedOption} minutes" }
                    }
                    Picker(
                        readOnly = timePickerState.selectedColumn != 1,
                        state = timePickerState.minuteState,
                        modifier = Modifier.size(64.dp, 100.dp),
                        contentDescription = minuteContentDescription,
                        option = { minute: Int -> TP_Option(1, "%02d".format(minute)) }
                    )
                }
            }

            ScalingLazyColumn(
                //contentPadding = PaddingValues(top = 1.dp),
                state = listState,
                modifier = Modifier
                    .padding(top = 1.dp)
                    .fillMaxWidth()
            ) {
                item {
                    ExpandableCard(title = "Threshold: ${pkrItems[pkrState.selectedOption]}") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Picker(
                                modifier = Modifier.size(64.dp, 100.dp),
                                state = pkrState,
                                contentDescription = contentDescription,
                                userScrollEnabled = pkrEnabled,
                            ) {
                                Text(
                                    //text = "%02d".format(pkrItems[it]),
                                    text = pkrItems[it],
                                    fontSize = 32.sp
                                )
                            }
                        }
                    }
                }

                item {
                    ExpandableCard(title = "begin at " +
                        "%02d:".format(timePickerStateBegin.hourState.selectedOption) +
                        "%02d".format(timePickerStateBegin.minuteState.selectedOption)) {
                        TimePicker(timePickerStateBegin)
                    }
                }

                item {
                    ExpandableCard(title = "end at " +
                        "%02d:".format(timePickerStateEnd.hourState.selectedOption) +
                        "%02d".format(timePickerStateEnd.minuteState.selectedOption)) {
                        TimePicker(timePickerStateEnd)
                    }
                }

                item {
                    ExpandableCard(title = "vibration") {
                        Text(
                            modifier = Modifier
                                //.align(Alignment.Center)
                                .padding(top = 1.dp),
                            text = "HIT.3"
                        )
                    }
                }
            }
            CompactButton(
                enabled = true,
                onClick = {
                    btnChecked = !btnChecked

                    if (btnChecked) { // pause
                        pkrEnabled = true
                        ctx?.stopService(mysvcIntent)
                    } else { // play
                        pkrEnabled = false;
                        val threshold = pkrItems[pkrState.selectedOption].toDouble()
                        mysvcIntent.putExtra("threshold", threshold)
                        mysvcIntent.setAction("apply")

                        ctx?.startForegroundService(mysvcIntent)
                        //ctx?.startService(mysvcIntent)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(top = 1.dp)
            ) {
                Text("${btcap[if (btnChecked) 1 else 0]}")
            }
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android", null)
}