package com.shin.ballres.feature.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shin.ballres.event.EventManager
import com.shin.ballres.ui.LocalNavController


@Composable
fun CameraRoute() {
    val navController = LocalNavController.current
    CameraScreen( onBackClick = {
        EventManager.trackEvent("camera_closed")
        navController.popBackStack()
    })
}

@Composable
fun CameraScreen(
    onBackClick: () -> Unit
) {
    var highlightCount by remember { mutableIntStateOf(0) }
    var lastHighlightCount by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(Unit) {
        EventManager.trackEvent("camera_opened")
    }
    
    LaunchedEffect(highlightCount) {
        if (highlightCount >= 10 && lastHighlightCount < 10) {
            EventManager.trackEvent("highlight_threshold_reached", mapOf("count" to highlightCount))
        }
        lastHighlightCount = highlightCount
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        CameraPreviewView(modifier = Modifier.fillMaxSize())

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onBackClick() },
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.35f)),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Camera"
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.45f)
                .fillMaxHeight(0.25f)
                .navigationBarsPadding()
                .padding(bottom = 32.dp)
                .border(
                    width = 0.8.dp,
                    color = Color.White.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(32.dp)
                ),
            shape = RoundedCornerShape(32.dp),
            color = Color.White.copy(alpha = 0.12f),
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GuidanceSphere(
                        modifier = Modifier.fillMaxSize(),
                        onHighlightCountChanged = { count ->
                            highlightCount = count
                        }
                    )
                }
            }
        }
    }
}
