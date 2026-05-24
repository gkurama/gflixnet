package com.example.ui.screens

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.sin

suspend fun playCinematicSplashSound() {
    withContext(Dispatchers.Default) {
        val sampleRate = 44100
        val durationMs = 5000
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val generatedSnd = ShortArray(numSamples)
        
        // Linear Congruential Generator for high-fidelity drum acoustic-skin impact transients
        var randomState = 123456789L
        
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            
            // Fast LCG random step to generate warm white noise in range [-1.0, 1.0]
            randomState = (randomState * 1103515245L + 12345L) and 0x7fffffffL
            val noise = (randomState.toDouble() / 2147483647.0) * 2.0 - 1.0
            
            // --- CINEMATIC DRUM ROLL / TAIKO SECTIONS (TAMBORES INIZIALI FORTES) ---
            
            // Drum Hit 1 (t = 0.15s) - Powerful introductory floor tom
            val dt1 = t - 0.15
            val drum1 = if (dt1 >= 0) {
                val body = sin(2 * PI * (45.0 + 45.0 * kotlin.math.exp(-15.0 * dt1)) * dt1) * kotlin.math.exp(-7.0 * dt1)
                val rattle = noise * kotlin.math.exp(-45.0 * dt1) * 0.15
                (body + rattle) * 0.70
            } else 0.0
            
            // Drum Hit 2 (t = 0.50s) - Secondary double-tap
            val dt2 = t - 0.50
            val drum2 = if (dt2 >= 0) {
                val body = sin(2 * PI * (55.0 + 45.0 * kotlin.math.exp(-18.0 * dt2)) * dt2) * kotlin.math.exp(-8.0 * dt2)
                val rattle = noise * kotlin.math.exp(-50.0 * dt2) * 0.12
                (body + rattle) * 0.55
            } else 0.0
            
            // Drum Hit 3 (t = 0.85s) - Accent build tom
            val dt3 = t - 0.85
            val drum3 = if (dt3 >= 0) {
                val body = sin(2 * PI * (58.0 + 42.0 * kotlin.math.exp(-20.0 * dt3)) * dt3) * kotlin.math.exp(-8.5 * dt3)
                val rattle = noise * kotlin.math.exp(-55.0 * dt3) * 0.12
                (body + rattle) * 0.55
            } else 0.0
            
            // Grand Climax Drum Hit 4 (t = 1.25s) - MEGA THUNDER DRUM IMPACT
            val dt4 = t - 1.25
            val drum4 = if (dt4 >= 0) {
                val body = (sin(2 * PI * (36.0 + 44.0 * kotlin.math.exp(-12.0 * dt4)) * dt4) + 
                            0.5 * sin(2 * PI * 18.0 * dt4)) * kotlin.math.exp(-4.5 * dt4)
                val rattle = noise * kotlin.math.exp(-35.0 * dt4) * 0.22
                (body + rattle) * 0.85
            } else 0.0
            
            // --- CINEMATIC BACKING ELEMENTS ---
            
            // Tension riser sweep up to the main hit
            val riserEnv = if (t in 0.0..1.25) {
                (t / 1.25) * (t / 1.25) * 0.20
            } else if (t > 1.25) {
                0.20 * kotlin.math.exp(-4.0 * (t - 1.25))
            } else 0.0
            val riserFreq = 35.0 + 165.0 * (t / 1.25).coerceIn(0.0, 1.0)
            val riser = sin(2 * PI * riserFreq * t) * riserEnv
            
            // --- SMOOTH & LUSH CELESTIAL PAD (SUAVE NO FINAL DA ABERTURA) ---
            val dtPad = t - 1.25
            val padEnv = if (dtPad >= 0) {
                if (dtPad < 1.0) {
                    // gentle swell
                    val fraction = dtPad / 1.0
                    sin(PI * fraction / 2.0) * 0.35
                } else {
                    // incredibly smooth slow exponential fade out
                    0.35 * kotlin.math.exp(-1.1 * (dtPad - 1.0))
                }
            } else 0.0
            
            // Majestic celestial G-major organ/chord pad that blooms softly after the initial massive impact
            val pad = if (dtPad >= 0) {
                (
                    sin(2 * PI * 196.00 * dtPad) * 0.45 + // G3 fundamental
                    sin(2 * PI * 293.66 * dtPad) * 0.35 + // D4 perfect fifth
                    sin(2 * PI * 392.00 * dtPad) * 0.30 + // G4 octave
                    sin(2 * PI * 493.88 * dtPad) * 0.20 + // B4 major third
                    sin(2 * PI * 587.33 * dtPad) * 0.15 + // D5 octave fifth
                    sin(2 * PI * 1318.51 * dtPad) * 0.08   // High chime shimmer
                ) * padEnv
            } else 0.0
            
            // Warm base sub-tone tracking the G pad
            val subBass = if (dtPad >= 0) {
                sin(2 * PI * 49.00 * dtPad) * 0.25 * padEnv
            } else 0.0
            
            // Combine all sound elements with precise master level
            val doubleVal = drum1 + drum2 + drum3 + drum4 + riser + pad + subBass
            
            // Gently fade out the entire audio channel block towards the absolute end
            val overallFade = if (t > 4.5) {
                (5.0 - t) / 0.5
            } else {
                1.0
            }
            
            val finalVal = (doubleVal * overallFade * 0.65 * Short.MAX_VALUE).toInt()
            generatedSnd[i] = finalVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        
        try {
            val audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                numSamples * 2,
                AudioTrack.MODE_STATIC
            )
            if (audioTrack.state == AudioTrack.STATE_INITIALIZED) {
                audioTrack.write(generatedSnd, 0, numSamples)
                audioTrack.play()
                delay(durationMs.toLong())
                try {
                    audioTrack.stop()
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            } else {
                delay(durationMs.toLong())
            }
            try {
                audioTrack.release()
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            delay(durationMs.toLong())
        }
    }
}

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var stage by remember { mutableStateOf(1) } // 1: Initial Pulse, 2: Logo entry, 3: Vertical lines transition, 4: Fade out

    // State triggers for smooth animations
    val pulseScaleTrigger = rememberInfiniteTransition()
    val pulseScale by pulseScaleTrigger.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val pulseAlpha by pulseScaleTrigger.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Logo overshoot/spring animation
    val logoScale = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }
    val verticalLinesAlpha = remember { Animatable(0f) }
    val overallAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Play cinematic high-fidelity intro sound
        launch {
            playCinematicSplashSound()
        }

        // Stage 1: Small pulsing neon dot starts (very fast cinematic build-up)
        delay(1250) // Synchronized with the massive climax hit at t = 1.25s
        
        // Stage 2: Logo springs into action and shines during the build-up
        stage = 2
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800, easing = EaseOutQuad)
            )
        }
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        
        delay(2000) // Keep the logo glowing as the deep sub-bass rises and drops!
        
        // Stage 3: Vertical neon stripes cascade in perfect harmony with the chime ringing
        stage = 3
        verticalLinesAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = EaseInQuad)
        )
        
        delay(1300) // Let the chime ring out for another 1.3 seconds
        
        // Stage 4: Smooth fade out prior to home transition
        stage = 4
        overallAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
        )
        
        // Finish splash
        onSplashComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .alpha(overallAlpha.value)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Render layers depending on the active stage
        if (stage == 1) {
            // High fidelity pulsing dot with rich neon gradient
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .scale(pulseScale)
                    .alpha(pulseAlpha)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00D2FF), // Neon sky blue
                                Color(0xFF9D4EDD).copy(alpha = 0.5f), // Neon purple
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        if (stage >= 2) {
            // The G logo from resources (`gflixnet_new_launcher_1779532290_1779532279892.png`)
            Image(
                painter = painterResource(id = R.drawable.gflixnet_new_launcher_1779532290_1779532279892),
                contentDescription = "Gflixnet Vignette G Logo",
                modifier = Modifier
                    .size(160.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
            )
        }

        // Stage 3 Vertical Scanning / Waterfall lines transition
        if (stage >= 3) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(verticalLinesAlpha.value),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Let's create gorgeous thin vertical lines that glow and animate vertically
                val listWidths = listOf(2.dp, 3.dp, 1.5.dp, 4.dp, 2.5.dp, 1.dp, 3.dp, 1.5.dp, 2.dp)
                val listGradients = listOf(
                    listOf(Color(0xFF00D2FF), Color.Transparent),
                    listOf(Color(0xFF9D4EDD), Color.Transparent),
                    listOf(Color(0xFF00D2FF), Color(0xFF9D4EDD), Color.Transparent),
                    listOf(Color(0xFF9D4EDD), Color.Transparent),
                    listOf(Color(0xFF00D2FF), Color.Transparent),
                    listOf(Color(0xFF9D4EDD), Color(0xFF00D2FF), Color.Transparent),
                    listOf(Color(0xFF00D2FF), Color.Transparent),
                    listOf(Color(0xFF9D4EDD), Color.Transparent),
                    listOf(Color(0xFF00D2FF), Color.Transparent)
                )

                listWidths.forEachIndexed { index, width ->
                    val lineOffset = remember { Animatable(-600f) }
                    LaunchedEffect(Unit) {
                        // Stagger the slide-downs for high-fidelity movement!
                        delay(index * 80L)
                        lineOffset.animateTo(
                            targetValue = 600f,
                            animationSpec = tween(1200, easing = FastOutLinearInEasing)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(width)
                            .offset(y = lineOffset.value.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listGradients[index % listGradients.size]
                                )
                            )
                            .alpha(0.65f)
                    )
                }
            }
        }
    }
}
