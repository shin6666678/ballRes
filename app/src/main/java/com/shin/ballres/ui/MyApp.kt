package com.shin.ballres.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.shin.ballres.feature.camera.cameraScreen
import com.shin.ballres.feature.main.mainScreen
import com.shin.ballres.feature.splash.SPLASH_ROUTE
import com.shin.ballres.feature.splash.splashScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp(
) {
    val navController = LocalNavController.current
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        NavHost(
            navController = navController,
            startDestination = SPLASH_ROUTE,
            modifier = Modifier.fillMaxSize()
        ) {
            splashScreen()
            mainScreen()
            cameraScreen()
        }
    }
}