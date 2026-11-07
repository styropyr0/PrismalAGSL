package com.styropyr0.prismaltest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.styropyr0.prismal.PrismalBackdrop
import com.styropyr0.prismal.shapes.PrismalRoundedRectangle
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

private data class BatteryInfo(
    val percent: Int,
    val charging: Boolean,
    val temperatureC: Float?,
)

private fun Intent.toBatteryInfo(): BatteryInfo {
    val level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = getIntExtra(BatteryManager.EXTRA_SCALE, 100)
    val status = getIntExtra(BatteryManager.EXTRA_STATUS, -1)
    val temp = getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
    return BatteryInfo(
        percent = if (level >= 0 && scale > 0) (level * 100f / scale).roundToInt() else 0,
        charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL,
        temperatureC = if (temp == Int.MIN_VALUE) null else temp / 10f,
    )
}

@Composable
private fun rememberBatteryInfo(): BatteryInfo {
    val context = LocalContext.current
    val filter = remember { IntentFilter(Intent.ACTION_BATTERY_CHANGED) }
    var info by remember {
        mutableStateOf(
            context.registerReceiver(null, filter)?.toBatteryInfo() ?: BatteryInfo(0, false, null)
        )
    }
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                info = intent.toBatteryInfo()
            }
        }
        if (Build.VERSION.SDK_INT >= 33) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        onDispose { context.unregisterReceiver(receiver) }
    }
    return info
}

/** Latest values of [sensorType], or `null` when the device has no such sensor. */
@Composable
private fun rememberSensorValues(sensorType: Int): FloatArray? {
    val context = LocalContext.current
    val manager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val sensor = remember(sensorType) { manager.getDefaultSensor(sensorType) }
    var values by remember(sensorType) { mutableStateOf<FloatArray?>(null) }

    if (sensor != null) {
        DisposableEffect(sensor) {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    values = event.values.copyOf()
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
            onDispose { manager.unregisterListener(listener) }
        }
    }
    return if (sensor == null) null else values ?: FloatArray(3)
}

/** Live device readings grouped in glass: battery, tilt bubble, light and pressure. */
@Composable
fun DeviceSensorsSection(
    backdrop: PrismalBackdrop,
    params: GlassPlaygroundParams,
    luminance: () -> Float,
) {
    val battery = rememberBatteryInfo()
    val accel = rememberSensorValues(Sensor.TYPE_ACCELEROMETER)
    val light = rememberSensorValues(Sensor.TYPE_LIGHT)
    val pressure = rememberSensorValues(Sensor.TYPE_PRESSURE)

    IosGlassGroup(backdrop = backdrop, params = params, luminance = luminance) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = IosLayout.groupInnerPadding,
                    vertical = IosLayout.rowVerticalPadding
                )
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Battery",
                    style = IosTheme.body,
                    color = IosTheme.colors.label,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${battery.percent}%" + if (battery.charging) " · Charging" else "",
                    style = IosTheme.body,
                    color = IosTheme.colors.secondaryLabel
                )
            }
            Box(Modifier.height(10.dp))
            PlaygroundGlassProgressBar(
                progress = { battery.percent / 100f },
                backdrop = backdrop,
                params = params,
                luminance = luminance,
            )
        }

        battery.temperatureC?.let {
            IosGroupDivider()
            IosListRow(title = "Battery Temperature", value = "%.1f °C".format(it))
        }

        if (accel != null) {
            IosGroupDivider()
            TiltRow(accel, backdrop, params, luminance)
        }

        if (light != null) {
            IosGroupDivider()
            IosListRow(title = "Ambient Light", value = "${light[0].roundToInt()} lx")
        }

        if (pressure != null && pressure[0] > 0f) {
            IosGroupDivider()
            IosListRow(title = "Air Pressure", value = "%.1f hPa".format(pressure[0]))
        }
    }
}

@Composable
private fun TiltRow(
    accel: FloatArray,
    backdrop: PrismalBackdrop,
    params: GlassPlaygroundParams,
    luminance: () -> Float,
) {
    val (x, y, z) = accel
    val pitch = Math.toDegrees(atan2(y, sqrt(x * x + z * z)).toDouble()).toFloat()
    val roll = Math.toDegrees(atan2(-x, z).toDouble()).toFloat()

    val density = LocalDensity.current
    val trackSize = 92.dp
    val bubbleSize = 34.dp
    val travelPx = with(density) { ((trackSize - bubbleSize) / 2).toPx() }
    val bx = (-x / 9.81f).coerceIn(-1f, 1f) * travelPx
    val by = (y / 9.81f).coerceIn(-1f, 1f) * travelPx

    Row(
        Modifier
            .fillMaxWidth()
            .padding(
                horizontal = IosLayout.groupInnerPadding,
                vertical = IosLayout.rowVerticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("Tilt", style = IosTheme.body, color = IosTheme.colors.label)
            Text(
                "Pitch ${pitch.roundToInt()}°  ·  Roll ${roll.roundToInt()}°",
                style = IosTheme.footnote,
                color = IosTheme.colors.secondaryLabel
            )
        }
        PlaygroundGlassSurface(
            backdrop = backdrop,
            params = params,
            luminance = luminance,
            shape = { PrismalRoundedRectangle(params.cornerRadiusDp.dp) },
            modifier = Modifier.size(trackSize),
        ) {
            Box(Modifier.size(trackSize), contentAlignment = Alignment.Center) {
                PlaygroundGlassSurface(
                    backdrop = backdrop,
                    params = params,
                    luminance = luminance,
                    shape = { CircleShape },
                    modifier = Modifier
                        .size(bubbleSize)
                        .offset { IntOffset(bx.roundToInt(), by.roundToInt()) },
                )
            }
        }
    }
}
