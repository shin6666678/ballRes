package com.shin.ballres.feature.main

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.shin.ballres.ui.LocalNavController
import com.shin.ballres.feature.camera.navigateToCamera

@Composable
fun MainRoute() {
    val context = LocalContext.current
    val navController = LocalNavController.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasCameraPermission = isGranted
    }
    MainScreen(
        hasCameraPermission = hasCameraPermission,
        permissionLauncher = { permissionLauncher.launch(it) },
        onOpenCameraClick = navController::navigateToCamera
    )

}

@Composable
fun MainScreen(
    hasCameraPermission: Boolean,
    permissionLauncher: (String) -> Unit,
    onOpenCameraClick: () -> Unit
) {
    val bg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0A0A0A),
            Color(0xFF141414)
        )
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "BallRes",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "世间美好与你同在",
                    color = Color.White.copy(alpha = 0.72f)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .width(220.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable {
                            if (!hasCameraPermission) {
                                permissionLauncher(Manifest.permission.CAMERA)
                            } else onOpenCameraClick()
                        },
                    color = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFF00CC),
                                        Color(0xFF3333FF)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "打开摄像机",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = if (hasCameraPermission) "已获得相机权限" else "需要相机权限",
                    color = Color.White.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}