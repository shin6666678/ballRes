package com.shin.ballres.feature.splash

import androidx.compose.runtime.Composable
import com.shin.ballres.ui.LocalNavController
import com.shin.ballres.feature.main.navigateToMain

@Composable
fun SplashRoute(){
    val navController= LocalNavController.current
    navController.navigateToMain()
}