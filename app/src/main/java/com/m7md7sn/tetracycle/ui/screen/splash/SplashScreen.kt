package com.m7md7sn.tetracycle.ui.screen.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.m7md7sn.tetracycle.ui.theme.LoayAppTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onNavigate: () -> Unit
) {
    val initialAnimDuration = 1200
    val pulseAnimDuration = 800

    // Initial pop-in, spin, and fade-in
    val scale = remember { Animatable(1f) }
    val rotation = remember { Animatable(-180f) }
    val alpha = remember { Animatable(0f) }

    // Pulse effect after initial animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseAnimDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = initialAnimDuration, easing = FastOutSlowInEasing)
        )
        rotation.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = initialAnimDuration, easing = FastOutSlowInEasing)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = initialAnimDuration, easing = FastOutSlowInEasing)
        )
        delay(1200L)
        onNavigate()
    }

    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            androidx.compose.material3.Text(
                text = "Tetra Cycle",
                style = androidx.compose.material3.MaterialTheme.typography.displayLarge,
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = scale.value * pulse,
                        scaleY = scale.value * pulse,
                        rotationZ = rotation.value,
                        alpha = alpha.value
                    )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreviewLight() {
    LoayAppTheme {
        SplashScreen(onNavigate = {})
    }
}
