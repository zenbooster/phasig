/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.phasig.presentation

//import com.example.phasig.R
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.foundation.SwipeToDismissValue
import androidx.wear.compose.foundation.edgeSwipeToDismiss
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberSwipeToDismissBoxState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Checkbox
import androidx.wear.compose.material.HorizontalPageIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.InlineSlider
import androidx.wear.compose.material.InlineSliderDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PageIndicatorState
import androidx.wear.compose.material.Picker
import androidx.wear.compose.material.PickerState
import androidx.wear.compose.material.SwipeToDismissBox
import androidx.wear.compose.material.Text
import com.example.phasig.presentation.theme.PhasigTheme
import com.starry.greenstash.ui.common.ExpandableCard
import java.text.DecimalFormat
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val my_intent = Intent(context, MainActivity::class.java)
        // попробовать FLAG_ACTIVITY_RESET_TASK_IF_NEEDED вместо FLAG_ACTIVITY_SINGLE_TOP
        //my_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP) // You need this if starting
        my_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED) // You need this if starting
        context.startActivity(my_intent)

        context.startForegroundService(Core.mysvcIntent)
    }
}

class Core {
    companion object {
        var sharedPref : SharedPreferences? = null
        val df = DecimalFormat("#.##")
        var btnChecked = mutableStateOf(true)
        var pkrIdx : Int = 12
        val pkrItems = List(101) { df.format(it) }
        var pkrState : PickerState = PickerState(pkrItems.size, pkrIdx)
        var optionalTimePickerStateBegin : OptionalTimePickerState = OptionalTimePickerState()
        var optionalTimePickerStateEnd : OptionalTimePickerState = OptionalTimePickerState()
        var islrVibrationLevel = 255
        var islrVibrationDuration = 375L
        var mysvcIntent: Intent = Intent()

        fun init(ctx : Context?)
        {
            if(ctx != null) {
                sharedPref = ctx.getSharedPreferences("myPref", Context.MODE_PRIVATE)
                pkrIdx = sharedPref!!.getInt("pkrIdx", 12)
                pkrState = PickerState(pkrItems.size, pkrIdx)

                optionalTimePickerStateBegin = OptionalTimePickerState(
                    sharedPref!!.getInt("tpkrBegH", 4),
                    sharedPref!!.getInt("tpkrBegM", 0),
                    sharedPref!!.getBoolean("tpkrBegEnabled", false)
                )
                optionalTimePickerStateEnd = OptionalTimePickerState(
                    sharedPref!!.getInt("tpkrEndH", 7),
                    sharedPref!!.getInt("tpkrEndM", 0),
                    sharedPref!!.getBoolean("tpkrEndEnabled", false)
                )

                islrVibrationLevel = sharedPref!!.getInt("islrVibrationLevel", 255)
                islrVibrationDuration = sharedPref!!.getLong("islrVibrationDuration", 375L)

                mysvcIntent.setClass(ctx, MyService::class.java)
            }
        }

        fun save() {
            if(sharedPref != null) {
                with(sharedPref!!.edit())
                {
                    putInt("pkrIdx", pkrState.selectedOption)

                    putInt(
                        "tpkrBegH",
                        optionalTimePickerStateBegin.timePickerState.hourState.selectedOption
                    )
                    putInt(
                        "tpkrBegM",
                        optionalTimePickerStateBegin.timePickerState.minuteState.selectedOption
                    )
                    putBoolean("tpkrBegEnabled", optionalTimePickerStateBegin.tpkrEnabled)

                    putInt(
                        "tpkrEndH",
                        optionalTimePickerStateEnd.timePickerState.hourState.selectedOption
                    )
                    putInt(
                        "tpkrEndM",
                        optionalTimePickerStateEnd.timePickerState.minuteState.selectedOption
                    )
                    putBoolean("tpkrEndEnabled", optionalTimePickerStateEnd.tpkrEnabled)

                    putInt("islrVibrationLevel", islrVibrationLevel)
                    putLong("islrVibrationDuration", islrVibrationDuration)
                    apply()
                }
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp(this)
            //WearApp(null)
        }
    }

    override fun onPause()
    {
        Core.save()
        super.onPause()
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun WearApp(ctx: Context?) {
    val btcap = listOf("❚❚", "▶")
    var pkrEnabled by remember { mutableStateOf(true) }
    val contentDescription by remember { derivedStateOf { "${Core.pkrState.selectedOption + 1}" } }
    val btnChecked : MutableState<Boolean> = Core.btnChecked
    val myAlarmIntent: Intent by lazy { Intent(ctx, AlarmReceiver::class.java) }
    var piAlarm : PendingIntent? = null

    var alarmManager: AlarmManager? = null
    var piMySvcK: PendingIntent? = null

    val listState = rememberScalingLazyListState()

    Core.init(ctx)

    //var expandedState by remember { mutableStateOf(false) }
    // begin

    PhasigTheme {
        val maxPages = 2
        var selectedPage by remember { mutableStateOf(0) }

        val pagerState = rememberPagerState(pageCount = { maxPages })

        val pageIndicatorState: PageIndicatorState = remember {
            object : PageIndicatorState {
                override val pageOffset: Float
                    get() = 0f
                override val selectedPage: Int
                    get() = selectedPage
                override val pageCount: Int
                    get() = maxPages
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val state = rememberSwipeToDismissBoxState()

            @Composable
            fun MainContent()
            {
                HorizontalPager(
                    state = pagerState
                ) { page ->
                    selectedPage = pagerState.currentPage

                    when (page) {
                        1 -> {
                            //if (selectedPage == 1) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colors.background)
                            )
                            {
                                ScalingLazyColumn(
                                    //contentPadding = PaddingValues(top = 1.dp),
                                    state = listState,
                                    modifier = Modifier
                                        .padding(top = 1.dp)
                                        .fillMaxWidth()
                                ) {
                                    item {
                                        ExpandableCard(title = "Threshold: ${Core.pkrItems[Core.pkrState.selectedOption]}") {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center,
                                            ) {
                                                Picker(
                                                    modifier = Modifier.size(
                                                        64.dp,
                                                        100.dp
                                                    ),
                                                    state = Core.pkrState,
                                                    contentDescription = contentDescription,
                                                    userScrollEnabled = pkrEnabled,
                                                ) {
                                                    Text(
                                                        //text = "%02d".format(pkrItems[it]),
                                                        text = Core.pkrItems[it],
                                                        fontSize = 32.sp
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    item {
                                        with(Core.optionalTimePickerStateBegin) {
                                            ExpandableCard(
                                                title = "begin at " +
                                                        if (tpkrEnabled) {
                                                            "%02d:".format(
                                                                timePickerState.hourState.selectedOption
                                                            ) +
                                                                    "%02d".format(
                                                                        timePickerState.minuteState.selectedOption
                                                                    )
                                                        } else {
                                                            "now"
                                                        }
                                            ) {
                                                OptionalTimePicker(
                                                    "Use time:",
                                                    Core.optionalTimePickerStateBegin
                                                )
                                            }
                                        }
                                    }

                                    item {
                                        with(Core.optionalTimePickerStateEnd) {
                                            ExpandableCard(
                                                title = "end at " +
                                                        if (tpkrEnabled) {
                                                            "%02d:".format(
                                                                timePickerState.hourState.selectedOption
                                                            ) +
                                                                    "%02d".format(
                                                                        timePickerState.minuteState.selectedOption
                                                                    )
                                                        } else {
                                                            "never"
                                                        }
                                            ) {
                                                OptionalTimePicker(
                                                    "Use time:",
                                                    Core.optionalTimePickerStateEnd
                                                )
                                            }
                                        }
                                    }

                                    item {
                                        ExpandableCard(title = "vibration") {
                                            Column() {
                                                Text(
                                                    modifier = Modifier
                                                        //.align(Alignment.Center)
                                                        .padding(top = 1.dp),
                                                    text = "Level:"
                                                )

                                                InlineSlider(
                                                    value = Core.islrVibrationLevel.toFloat(),
                                                    onValueChange = {
                                                        Core.islrVibrationLevel = it.toInt()
                                                    },
                                                    increaseIcon = {
                                                        Icon(
                                                            InlineSliderDefaults.Increase,
                                                            "Increase"
                                                        )
                                                    },
                                                    decreaseIcon = {
                                                        Icon(
                                                            InlineSliderDefaults.Decrease,
                                                            "Decrease"
                                                        )
                                                    },
                                                    valueRange = 0f..255.0f,
                                                    steps = 8,
                                                    segmented = true
                                                )

                                                Text(
                                                    modifier = Modifier
                                                        //.align(Alignment.Center)
                                                        .padding(top = 1.dp),
                                                    text = "Duration:"
                                                )

                                                InlineSlider(
                                                    value = Core.islrVibrationDuration.toFloat(),
                                                    onValueChange = {
                                                        Core.islrVibrationDuration = it.toLong()
                                                    },
                                                    increaseIcon = {
                                                        Icon(
                                                            InlineSliderDefaults.Increase,
                                                            "Increase"
                                                        )
                                                    },
                                                    decreaseIcon = {
                                                        Icon(
                                                            InlineSliderDefaults.Decrease,
                                                            "Decrease"
                                                        )
                                                    },
                                                    valueRange = 0f..1000.0f,
                                                    steps = 8,
                                                    segmented = true
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        0 -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colors.background)
                            ) {
                                Button(
                                    enabled = true,
                                    onClick = {
                                        fun StartMainWork()
                                        {
                                            ctx?.startForegroundService(
                                                Core.mysvcIntent
                                            )
                                        }

                                        fun StopMainWork()
                                        {
                                            ctx?.stopService(Core.mysvcIntent)
                                        }

                                        btnChecked.value = !btnChecked.value

                                        if (btnChecked.value) { // pause
                                            piMySvcK?.let { alarmManager?.cancel(it) }
                                            //piMySvc?.let { alarmManager?.cancel(it) }
                                            piAlarm?.let { alarmManager?.cancel(it) }
                                            pkrEnabled = true
                                            StopMainWork()
                                        } else { // play
                                            pkrEnabled = false
                                            val threshold =
                                                Core.pkrItems[Core.pkrState.selectedOption]
                                                    .toDouble()

                                            Core.mysvcIntent.putExtra("threshold", threshold)
                                            Core.mysvcIntent.putExtra(
                                                "islrVibrationLevel",
                                                Core.islrVibrationLevel
                                            )
                                            Core.mysvcIntent.putExtra(
                                                "islrVibrationDuration",
                                                Core.islrVibrationDuration
                                            )
                                            Core.mysvcIntent.setAction("apply")

                                            fun GetDelayMsecFromTime(
                                                h: Int,
                                                m: Int
                                            ): Long {
                                                val cal = Calendar.getInstance()
                                                val ch = cal[Calendar.HOUR_OF_DAY]
                                                val cm = cal[Calendar.MINUTE]
                                                val cs = cal[Calendar.SECOND]
                                                val cmin = (ch * 60) + cm
                                                var nmin = (h * 60) + m

                                                if (nmin < cmin) {
                                                    nmin += 24 * 60
                                                }

                                                val delayMsec =
                                                    ((nmin - cmin) * 60 - cs) * 1000

                                                return delayMsec.toLong()
                                            }

                                            with(Core.optionalTimePickerStateBegin)
                                            {
                                                if (tpkrEnabled) {
                                                    if (ctx != null) {
                                                        val delay = GetDelayMsecFromTime(
                                                            timePickerState.hourState.selectedOption,
                                                            timePickerState.minuteState.selectedOption
                                                        )

                                                        alarmManager = ctx.getSystemService(
                                                            Context.ALARM_SERVICE
                                                        ) as AlarmManager

                                                        /*alarmManager?.setExactAndAllowWhileIdle(
                                                            AlarmManager.RTC_WAKEUP,
                                                            System.currentTimeMillis() + delay,
                                                            pi
                                                        );*/

                                                        val pia = PendingIntent.getBroadcast(
                                                            ctx,
                                                            0,
                                                            myAlarmIntent,
                                                            PendingIntent.FLAG_CANCEL_CURRENT or
                                                                    PendingIntent.FLAG_IMMUTABLE
                                                        )

                                                        alarmManager?.setAlarmClock(
                                                            AlarmManager.AlarmClockInfo(
                                                                System.currentTimeMillis() + delay,
                                                                null
                                                            ), pia)
                                                        piAlarm = pia
                                                        //piMySvc = pi
                                                    }
                                                } else {
                                                    /*ctx?.startForegroundService(
                                                        Core.mysvcIntent
                                                    )*/
                                                    StartMainWork()
                                                }
                                            }

                                            with(Core.optionalTimePickerStateEnd)
                                            {
                                                if (tpkrEnabled) {
                                                    if (ctx != null) {
                                                        val delayEnd = GetDelayMsecFromTime(
                                                            timePickerState.hourState.selectedOption,
                                                            timePickerState.minuteState.selectedOption
                                                        )

                                                        val mysvcKIntent = Intent(
                                                            ctx,
                                                            MyServiceKiller::class.java
                                                        )
                                                        val actIntent = Intent(
                                                            ctx, MainActivity::class.java
                                                        )
                                                        mysvcKIntent.putExtra("victim", Core.mysvcIntent)
                                                        mysvcKIntent.putExtra("actIntent", actIntent)
                                                        mysvcKIntent.setAction("apply")

                                                        val pi = PendingIntent.getService(
                                                            ctx,
                                                            0,
                                                            mysvcKIntent,
                                                            PendingIntent.FLAG_CANCEL_CURRENT or
                                                                    PendingIntent.FLAG_IMMUTABLE
                                                        )
                                                        alarmManager = ctx.getSystemService(
                                                            Context.ALARM_SERVICE
                                                        ) as AlarmManager

                                                        alarmManager?.setExactAndAllowWhileIdle(
                                                            AlarmManager.RTC_WAKEUP,
                                                            System.currentTimeMillis() + delayEnd,
                                                            pi
                                                        )
                                                        /*alarmManager?.setAlarmClock(
                                                            AlarmManager.AlarmClockInfo(
                                                                System.currentTimeMillis() + delayEnd,
                                                                null
                                                            ), pi)*/
                                                        piMySvcK = pi
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(top = 1.dp)
                                ) {
                                    Text(btcap[if (btnChecked.value) 1 else 0])
                                }
                            }
                        }
                    }
                }

                HorizontalPageIndicator(
                    pageIndicatorState = pageIndicatorState,
                    selectedColor = Color(0xFFc75f00)
                )
            }

            SwipeToDismissBox(state = state)
            { bg ->
                if (!bg) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .edgeSwipeToDismiss(state)
                    ) {
                        MainContent()
                    }
                }
            }

            LaunchedEffect(state.currentValue) {
                if (state.currentValue == SwipeToDismissValue.Dismissed) {
                    val activity = ctx as MainActivity
                    activity.finishAffinity()
                }
            }
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp(null)
}