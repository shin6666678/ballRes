package com.shin.ballres.feature.camera

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val CAMERA_ROUTE = "chat"


fun NavGraphBuilder.cameraScreen() {
    composable(route = CAMERA_ROUTE) {
        CameraRoute()
    }
}

fun NavController.navigateToCamera() {
    this.navigate(CAMERA_ROUTE)
}