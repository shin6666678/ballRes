package com.shin.ballres.feature.camera

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import android.view.Surface
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin

/**
 * 球面上的一个点（世界坐标系：X=东, Y=北, Z=上/反重力）
 */
data class SpherePoint(
    val id: Int,
    val lon: Float,   // 经度 0..360
    val lat: Float,   // 纬度 -90..90
    val x: Float,     // 世界 X
    val y: Float,     // 世界 Y
    val z: Float      // 世界 Z (重力反方向=上)
)

object Config {
    const val SPHERE_RADIUS = 60f
    const val CAMERA_DISTANCE = SPHERE_RADIUS * 4f
    const val FOCAL_LENGTH = 400f
    const val RENDER_SCALE = 1.25f          // 纯渲染缩放：让球体/点更大，不影响逻辑计算
    const val LON_STEP = 12f
    const val LAT_STEP = 12f
    const val HIGHLIGHT_DEGREE = 30f        // ±30° 点亮范围
    const val BASE_DOT_RADIUS = 1.05f       // 未点亮时的点半径（整体更大）
    const val LIT_SCALE = 1.15f             // 点亮后放大倍数（减弱“泡泡感”）
    const val ANIM_DURATION_MS = 300L       // 点亮动画时长
}

/**
 * 生成球面点，极点沿世界 Z 轴（与重力方向对齐）
 */
fun generateSpherePoints(): List<SpherePoint> {
    val points = mutableListOf<SpherePoint>()
    var id = 0
    val r = Config.SPHERE_RADIUS

    var lat = -90f + Config.LAT_STEP
    while (lat < 90f) {
        val latRad = Math.toRadians(lat.toDouble()).toFloat()
        val zVal = r * sin(latRad)
        val cosLat = cos(latRad)

        var lon = 0f
        while (lon < 360f) {
            val lonRad = Math.toRadians(lon.toDouble()).toFloat()
            val xVal = r * cosLat * cos(lonRad)
            val yVal = r * cosLat * sin(lonRad)
            points.add(SpherePoint(id++, lon, lat, xVal, yVal, zVal))
            lon += Config.LON_STEP
        }
        lat += Config.LAT_STEP
    }
    // 南极、北极
    points.add(SpherePoint(id++, 0f, -90f, 0f, 0f, -r))
    points.add(SpherePoint(id, 0f, 90f, 0f, 0f, r))
    return points
}

@Composable
fun GuidanceSphere(
    modifier: Modifier = Modifier,
    key: Int = 0
) {
    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val rotationSensor = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }

    val points = remember { generateSpherePoints() }

    // ---- 状态 ----
    // 旋转矩阵（List 可正确触发 recompose）
    var rotMatrix by remember {
        mutableStateOf(listOf(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f))
    }
    // 已点亮的点 -> 首次点亮时间戳 (elapsedRealtime ms)
    val litTimestamps: SnapshotStateMap<Int, Long> = remember(key) {
        mutableStateMapOf()
    }

    // 持续帧驱动，确保动画流畅
    var frameNanos by remember { mutableLongStateOf(0L) }
    LaunchedEffect(key) {
        while (isActive) {
            withFrameNanos { nanos -> frameNanos = nanos }
        }
    }

    // ---- 传感器 ----
    DisposableEffect(sensorManager, rotationSensor, key) {
        val rawR = FloatArray(9)
        val remappedR = FloatArray(9)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type != Sensor.TYPE_ROTATION_VECTOR) return
                SensorManager.getRotationMatrixFromVector(rawR, event.values)

                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                @Suppress("DEPRECATION")
                val displayRotation = wm.defaultDisplay.rotation

                when (displayRotation) {
                    Surface.ROTATION_0 -> SensorManager.remapCoordinateSystem(
                        rawR, SensorManager.AXIS_X, SensorManager.AXIS_Y, remappedR
                    )
                    Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(
                        rawR, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, remappedR
                    )
                    Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(
                        rawR, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y, remappedR
                    )
                    Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(
                        rawR, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, remappedR
                    )
                    else -> System.arraycopy(rawR, 0, remappedR, 0, 9)
                }

                // 更新旋转矩阵 state
                rotMatrix = remappedR.toList()

                // ---- 命中检测 ----
                // 近面球心方向 = device +Z in world = (R[2], R[5], R[8])
                // 即面向相机的那半球的中心点方向
                val fwdX = remappedR[2]
                val fwdY = remappedR[5]
                val fwdZ = remappedR[8]

                val highlightCos = cos(
                    Math.toRadians(Config.HIGHLIGHT_DEGREE.toDouble())
                ).toFloat()

                val now = SystemClock.elapsedRealtime()
                val radius = Config.SPHERE_RADIUS

                for (p in points) {
                    // 点方向（归一化） = (px, py, pz) / radius
                    val dot = (fwdX * p.x + fwdY * p.y + fwdZ * p.z) / radius
                    if (dot > highlightCos && p.id !in litTimestamps) {
                        litTimestamps[p.id] = now
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(
            listener, rotationSensor, SensorManager.SENSOR_DELAY_GAME
        )
        onDispose { sensorManager.unregisterListener(listener) }
    }

    // ---- 绘制 ----
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 读取 state 以订阅更新
            val rot = rotMatrix
            @Suppress("UNUSED_VARIABLE")
            val tick = frameNanos // 确保每帧重绘

            val cx = size.width / 2f
            val cy = size.height / 2f

            // 旋转矩阵元素
            val r0 = rot[0]; val r1 = rot[1]; val r2 = rot[2]
            val r3 = rot[3]; val r4 = rot[4]; val r5 = rot[5]
            val r6 = rot[6]; val r7 = rot[7]; val r8 = rot[8]

            // 相机位置 = device_Z_in_world * D = (R[2], R[5], R[8]) * D
            val camX = r2 * Config.CAMERA_DISTANCE
            val camY = r5 * Config.CAMERA_DISTANCE
            val camZ = r8 * Config.CAMERA_DISTANCE

            val nowMs = SystemClock.elapsedRealtime()

            // 收集可见点用于深度排序
            data class VisiblePoint(
                val screenX: Float,
                val screenY: Float,
                val viewZ: Float,   // 深度（越负越远）
                val scale: Float,   // 透视缩放
                val ndotv: Float,   // 0..1 球面朝向相机程度
                val id: Int
            )

            val visible = mutableListOf<VisiblePoint>()

            for (p in points) {
                // 背面剔除: dot(point, device_Z_in_world) > 0 → 面向相机
                val facing = (p.x * r2 + p.y * r5 + p.z * r8) / Config.SPHERE_RADIUS
                if (facing <= 0f) continue

                // 世界坐标 → 相机相对坐标
                val relX = p.x - camX
                val relY = p.y - camY
                val relZ = p.z - camZ

                // 乘 R^T (转置) → 视图空间
                // view = R^T * rel → 行变列
                val vx = r0 * relX + r3 * relY + r6 * relZ
                val vy = r1 * relX + r4 * relY + r7 * relZ
                val vz = r2 * relX + r5 * relY + r8 * relZ

                // vz < 0 表示在相机前方
                if (vz >= -1f) continue

                val perspScale = (Config.FOCAL_LENGTH / (-vz)) * Config.RENDER_SCALE
                val sx = cx + vx * perspScale
                val sy = cy - vy * perspScale   // 翻转Y：世界上=屏幕上

                visible.add(VisiblePoint(sx, sy, vz, perspScale, facing.coerceIn(0f, 1f), p.id))
            }

            // 从远到近排序（先画远处的）
            visible.sortBy { it.viewZ }

            // 画球体背景圆
            val bgRadius =
                (Config.SPHERE_RADIUS * Config.FOCAL_LENGTH / Config.CAMERA_DISTANCE) * Config.RENDER_SCALE
            val sphereCenter = Offset(cx, cy)
            val lightCenter = Offset(cx - bgRadius * 0.35f, cy - bgRadius * 0.35f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF6A6A6A).copy(alpha = 0.92f),
                        Color(0xFF1A1A1A).copy(alpha = 0.98f)
                    ),
                    center = lightCenter,
                    radius = bgRadius * 1.35f
                ),
                radius = bgRadius,
                center = sphereCenter
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.06f),
                radius = bgRadius,
                center = sphereCenter
            )

            // 让点“直接画在球上”：把点裁剪在球体轮廓内，避免边缘产生悬浮感
            val sphereRect = Rect(
                left = sphereCenter.x - bgRadius,
                top = sphereCenter.y - bgRadius,
                right = sphereCenter.x + bgRadius,
                bottom = sphereCenter.y + bgRadius
            )
            val sphereClip = Path().apply { addOval(sphereRect) }

            clipPath(sphereClip) {
                for (vp in visible) {
                    val litTime = litTimestamps[vp.id]
                    val depthScale = vp.scale.coerceIn(0.55f, 2.6f)
                    val nd = vp.ndotv.coerceIn(0f, 1f)

                    val paintedScale = 0.25f + 0.75f * nd * nd
                    val baseDotR = Config.BASE_DOT_RADIUS * depthScale * paintedScale

                    val shade = 0.55f + 0.45f * nd
                    val dotVal = (0.78f + 0.22f * shade).coerceIn(0f, 1f)

                    if (litTime != null) {
                        val elapsed = nowMs - litTime
                        val t = (elapsed.toFloat() / Config.ANIM_DURATION_MS).coerceIn(0f, 1f)
                        val eased = 1f - (1f - t) * (1f - t) * (1f - t)

                        val dotRadius = baseDotR * (1f + (Config.LIT_SCALE - 1f) * eased)
                        val alpha = (0.10f + 0.35f * eased).coerceIn(0f, 0.55f)

                        drawCircle(
                            color = Color(dotVal, dotVal, dotVal, alpha),
                            radius = dotRadius,
                            center = Offset(vp.screenX, vp.screenY)
                        )
                    } else {
                        val alpha = (0.06f + 0.10f * nd).coerceIn(0f, 0.18f)
                        drawCircle(
                            color = Color(dotVal, dotVal, dotVal, alpha),
                            radius = baseDotR,
                            center = Offset(vp.screenX, vp.screenY)
                        )
                    }
                }
            }
        }
    }
}
