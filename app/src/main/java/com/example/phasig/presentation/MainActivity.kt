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
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.widget.Toast
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
import androidx.wear.compose.material.rememberPickerState
import com.example.phasig.MyService
import com.example.phasig.presentation.theme.PhasigTheme
import com.starry.greenstash.ui.common.ExpandableCard
import java.text.DecimalFormat
import java.util.Calendar
import java.util.GregorianCalendar


class MyServiceStopAlarm : BroadcastReceiver {
    var alarmMgr: AlarmManager? = null
    var pi: PendingIntent? = null
    //private val REMINDER_BUNDLE = "MyReminderBundle"

    // this constructor is called by the alarm manager.
    constructor()

    // you can use this constructor to create the alarm.
    //  Just pass in the main activity as the context,
    //  any extras you'd like to get later when triggered
    //  and the timeout
    constructor(context: Context, intent: Intent, delay: Int) {
        alarmMgr =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        var vpi: PendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
//        alarmMgr!![AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay] = vpi
        alarmMgr!!.set(
            AlarmManager.RTC_WAKEUP,
            SystemClock.currentThreadTimeMillis() + delay,
            vpi
        );
        pi = vpi
    }

    override fun onReceive(context: Context, intent: Intent) {
        // here you can get the extras you passed in when creating the alarm
        //intent.getBundleExtra(REMINDER_BUNDLE));
        Toast.makeText(context, "Alarm went off", Toast.LENGTH_SHORT).show()
        context.stopService(intent)
    }

    public fun cancel() {
        pi?.let { alarmMgr?.cancel(it) }
    }

}

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

class MainActivity : ComponentActivity() {
    var sharedPref: SharedPreferences ?= null
    var pkrState: PickerState ?= null
    var optionalTimePickerStateBegin: OptionalTimePickerState ?= null
    var optionalTimePickerStateEnd: OptionalTimePickerState ?= null
    var islrVibrationLevel: Int by mutableStateOf(0)
    var islrVibrationDuration: Long by mutableStateOf(0)

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

            putInt("tpkrBegH", optionalTimePickerStateBegin!!.timePickerState.hourState.selectedOption)
            putInt("tpkrBegM", optionalTimePickerStateBegin!!.timePickerState.minuteState.selectedOption)
            putBoolean("tpkrBegEnabled", optionalTimePickerStateBegin!!.tpkrEnabled)

            putInt("tpkrEndH", optionalTimePickerStateEnd!!.timePickerState.hourState.selectedOption)
            putInt("tpkrEndM", optionalTimePickerStateEnd!!.timePickerState.minuteState.selectedOption)
            putBoolean("tpkrEndEnabled", optionalTimePickerStateEnd!!.tpkrEnabled)

            putInt("islrVibrationLevel", islrVibrationLevel)
            putLong("islrVibrationDuration", islrVibrationDuration)
            apply()
        }
        super.onPause();
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun WearApp(greetingName: String, ctx: Context?) {
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


    var mssa: MyServiceStopAlarm? = null
    var alarmManager: AlarmManager? = null
    var contentIntent: PendingIntent? = null

    val listState = rememberScalingLazyListState()
    //var expandedState by remember { mutableStateOf(false) }
    // begin
    @Composable
    fun rememberTimePickerState(
        initiallySelectedOptionH: Int,
        initiallySelectedOptionM: Int
    ) = remember { TimePickerState(initiallySelectedOptionH, initiallySelectedOptionM) }

    @Composable
    fun rememberOptionalTimePickerState(
        initiallySelectedOptionH: Int,
        initiallySelectedOptionM: Int,
        initialEnabled: Boolean
    ) = remember { OptionalTimePickerState(initiallySelectedOptionH, initiallySelectedOptionM, initialEnabled) }

    var optionalTimePickerStateBegin = rememberOptionalTimePickerState(
        sharedPref!!.getInt("tpkrBegH", 4),
        sharedPref!!.getInt("tpkrBegM", 0),
        sharedPref!!.getBoolean("tpkrBegEnabled", false)
    )
    var optionalTimePickerStateEnd = rememberOptionalTimePickerState(
        sharedPref!!.getInt("tpkrEndH", 7),
        sharedPref!!.getInt("tpkrEndM", 0),
        sharedPref!!.getBoolean("tpkrEndEnabled", false)
    )

    var islrVibrationLevel by remember { mutableStateOf(sharedPref!!.getInt("islrVibrationLevel", 255)) }
    var islrVibrationDuration by remember { mutableStateOf(sharedPref.getLong("islrVibrationDuration", 375)) }

    if(ctx != null) {
        val activity = ctx as MainActivity

        activity.sharedPref = sharedPref
        activity.pkrState = pkrState
        activity.optionalTimePickerStateBegin = optionalTimePickerStateBegin
        activity.optionalTimePickerStateEnd = optionalTimePickerStateEnd
        activity.islrVibrationLevel = islrVibrationLevel
        activity.islrVibrationDuration = islrVibrationDuration
    }

    PhasigTheme {
        val state = rememberSwipeToDismissBoxState()

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
            fun MainContent(): Unit
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
                                                    modifier = Modifier.size(
                                                        64.dp,
                                                        100.dp
                                                    ),
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
                                        with(optionalTimePickerStateBegin) {
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
                                                    optionalTimePickerStateBegin
                                                )
                                            }
                                        }
                                    }

                                    item {
                                        with(optionalTimePickerStateEnd) {
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
                                                    optionalTimePickerStateEnd
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
                                                    value = islrVibrationLevel.toFloat(),
                                                    onValueChange = {
                                                        islrVibrationLevel = it.toInt()
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
                                                    value = islrVibrationDuration.toFloat(),
                                                    onValueChange = {
                                                        islrVibrationDuration =
                                                            it.toLong()
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
                            //if (selectedPage == 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colors.background)
                            ) {
                                Button(
                                    enabled = true,
                                    onClick = {

                                        btnChecked = !btnChecked

                                        if (btnChecked) { // pause
                                            mssa?.cancel()
                                            contentIntent?.let { alarmManager?.cancel(it) }
                                            /*Handler(Looper.getMainLooper()).removeCallbacksAndMessages(
                                                tokenSS
                                            );
                                            Handler(Looper.getMainLooper()).removeCallbacksAndMessages(
                                                tokenSFS
                                            );*/
                                            pkrEnabled = true
                                            ctx?.stopService(mysvcIntent)
                                        } else { // play
                                            pkrEnabled = false;
                                            val threshold =
                                                pkrItems[pkrState.selectedOption].toDouble()

                                            mysvcIntent.putExtra("threshold", threshold)
                                            mysvcIntent.putExtra(
                                                "islrVibrationLevel",
                                                islrVibrationLevel
                                            )
                                            mysvcIntent.putExtra(
                                                "islrVibrationDuration",
                                                islrVibrationDuration
                                            )
                                            mysvcIntent.setAction("apply")

                                            fun StartMainWork()
                                            {
                                                ctx?.startForegroundService(
                                                    mysvcIntent
                                                )
                                            }

                                            fun StopMainWork()
                                            {
                                                ctx?.stopService(mysvcIntent)
                                            }

                                            fun GetDelayMsecFromNow(
                                                h: Int,
                                                m: Int
                                            ): Long {
                                                val cal = GregorianCalendar()
                                                val om =
                                                    (cal[Calendar.HOUR] * 60) + cal[Calendar.MINUTE]
                                                var nm = (h * 60) + m

                                                if (nm < om) {
                                                    nm += 24 * 60
                                                }

                                                val delayMsec =
                                                    ((nm - om) * 60 - cal[Calendar.SECOND]) * 1000

                                                return delayMsec.toLong()
                                            }

                                            with(optionalTimePickerStateBegin!!)
                                            {
                                                val delay = GetDelayMsecFromNow(
                                                    timePickerState.hourState.selectedOption,
                                                    timePickerState.minuteState.selectedOption
                                                )

                                                if (tpkrEnabled) {
                                                    /*val startServiceIntent: Intent = Intent(
                                                        ctx,
                                                        MyService::class.java
                                                    )*/
                                                    var ci = PendingIntent.getService(
                                                        ctx,
                                                        0,
                                                        mysvcIntent, //startServiceIntent,
                                                        PendingIntent.FLAG_CANCEL_CURRENT or
                                                                PendingIntent.FLAG_IMMUTABLE
                                                    )
                                                    alarmManager = ctx?.getSystemService(
                                                        Context.ALARM_SERVICE
                                                    ) as AlarmManager

                                                    alarmManager?.set(
                                                        AlarmManager.RTC_WAKEUP,
                                                        SystemClock.currentThreadTimeMillis() + delay,
                                                        ci
                                                    );
                                                    contentIntent = ci
                                                } else {
                                                    /*ctx?.startForegroundService(
                                                        mysvcIntent
                                                    )*/
                                                    StartMainWork()
                                                }
                                            }

                                            with(optionalTimePickerStateEnd!!)
                                            {
                                                if (tpkrEnabled) {
                                                    if (ctx != null) {
                                                        mssa = MyServiceStopAlarm(
                                                            ctx,
                                                            mysvcIntent,
                                                            GetDelayMsecFromNow(
                                                                timePickerState.hourState.selectedOption,
                                                                timePickerState.minuteState.selectedOption
                                                            ).toInt()
                                                        )
                                                    }
                                                    /*Handler(Looper.getMainLooper()).postDelayed(
                                                        {
                                                            //Do something at end time
                                                            btnChecked = !btnChecked
                                                            pkrEnabled = true
                                                            //ctx?.stopService(mysvcIntent)
                                                            StopMainWork()
                                                        },
                                                        tokenSS,
                                                        GetDelayMsecFromNow(
                                                            timePickerState.hourState.selectedOption,
                                                            timePickerState.minuteState.selectedOption
                                                        )
                                                    )*/
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(top = 1.dp)
                                ) {
                                    Text("${btcap[if (btnChecked) 1 else 0]}")
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
    WearApp("Preview Android", null)
}