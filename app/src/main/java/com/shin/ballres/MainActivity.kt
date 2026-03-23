package com.shin.ballres

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.compose.rememberNavController
import com.shin.ballres.event.sender.impl.ConsoleSender
import com.shin.ballres.event.EventManager
import com.shin.ballres.event.sender.impl.NetworkSender
import com.shin.ballres.ui.LocalNavController
import com.shin.ballres.ui.MyApp
import com.shin.ballres.ui.theme.BallResTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        EventManager.registerSender(ConsoleSender())
        EventManager.registerSender(NetworkSender())
        
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


