package com.shin.ballres.feature.splash

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val SPLASH_ROUTE="splash"

fun NavGraphBuilder.splashScreen(){
    composable(SPLASH_ROUTE){
        SplashRoute()
    }
}