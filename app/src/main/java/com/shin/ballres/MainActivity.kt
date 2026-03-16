package com.shin.ballres

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.compose.rememberNavController
import com.shin.ballres.ui.LocalNavController
import com.shin.ballres.ui.MyApp
import com.shin.ballres.ui.theme.BallResTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            CompositionLocalProvider(
                LocalNavController provides navController,
            ) {
                BallResTheme {
                    MyApp()
                }
            }
        }
    }
}


