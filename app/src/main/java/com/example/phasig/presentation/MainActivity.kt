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
import androidx.wear.compose.material.ToggleButton
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.wear.compose.material.rememberPickerState
import androidx.compose.runtime.mutableStateOf
import android.content.Intent
import android.content.Context
import android.content.SharedPreferences
import com.example.phasig.MyService

//import com.example.phasig.R
import com.example.phasig.presentation.theme.PhasigTheme
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
            Text(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp),
                text = "Threshold: ${pkrItems[pkrState.selectedOption]}"
            )
            Picker(
                modifier = Modifier.size(100.dp, 100.dp),
                state = pkrState,
                contentDescription = contentDescription,
                userScrollEnabled = pkrEnabled,
            ) {
                Text(pkrItems[it])
            }

            ToggleButton(
                enabled = true,
                checked = btnChecked,
                onCheckedChange = {
                    btnChecked = it

                    if (btnChecked)
                    { // pause
                        pkrEnabled = true
                        ctx?.stopService(mysvcIntent)
                    }
                    else
                    { // play
                        pkrEnabled = false;
                        val threshold = pkrItems[pkrState.selectedOption].toDouble()
                        mysvcIntent.putExtra("threshold", threshold)
                        mysvcIntent.setAction("apply")

                        //ctx.startForegroundService(mysvcIntent)
                        ctx?.startService(mysvcIntent)
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter).padding(top = 10.dp)
            ) {
                Text("${btcap[if(btnChecked) 1 else 0]}")
            }
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android", null)
}