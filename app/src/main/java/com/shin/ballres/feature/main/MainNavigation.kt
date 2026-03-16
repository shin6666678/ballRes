package com.shin.ballres.feature.main

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

const val MAIN_ROUTE = "main"


fun NavGraphBuilder.mainScreen() {
    composable(route = MAIN_ROUTE) {
        MainRoute()
    }
}

fun NavController.navigateToMain(navOptions: NavOptions? = null) {
    this.navigate(MAIN_ROUTE, navOptions)
}