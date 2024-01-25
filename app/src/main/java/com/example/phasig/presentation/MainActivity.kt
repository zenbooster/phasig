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
import androidx.wear.compose.material.ToggleButton
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.wear.compose.material.rememberPickerState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import com.example.phasig.MyService

//import com.example.phasig.R
import com.example.phasig.presentation.theme.PhasigTheme
import java.text.DecimalFormat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp("Android")
        }
    }
}

@Composable
fun WearApp(greetingName: String) {
    val btcap = listOf("❚❚", "▶")
    val df = DecimalFormat("#.##")
    val pkrItems = List(101) { df.format(it) }
    val pkrState = rememberPickerState(pkrItems.size)
    var pkrEnabled by remember { mutableStateOf(true) }
    val contentDescription by remember { derivedStateOf { "${pkrState.selectedOption + 1}" } }
    var btnChecked by remember { mutableStateOf(true) }
    //val mysvcIntent: Intent by lazy { Intent(this, MyService::class.java) }
    val ctx = LocalContext.current
    val mysvcIntent: Intent by lazy { Intent(ctx, MyService::class.java) }

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
                userScrollEnabled = pkrEnabled
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
                        ctx.stopService(mysvcIntent)
                    }
                    else
                    { // play
                        pkrEnabled = false;
                        val threshold = pkrItems[pkrState.selectedOption].toDouble()
                        mysvcIntent.putExtra("threshold", threshold)
                        mysvcIntent.setAction("apply")
                        //ctx.startForegroundService(mysvcIntent)
                        ctx.startService(mysvcIntent)
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter).padding(top = 10.dp)
            ) {
                Text("${btcap[if(btnChecked) 1 else 0]}")
            }
        }
    }
}

/*@Composable
fun Greeting(greetingName: String) {
    Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary,
            text = stringResource(R.string.hello_world, greetingName)
    )
}*/

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}