package com.slotmachineapp.fancyslotmachine

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.slotmachineapp.fancyslotmachine.ui.theme.FancySlotMachineTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

// --- FANCY NEON PALETTE ---
val BgDeep = Color(0xFF05010A)
val BgGlow = Color(0xFF1A0533)
val FancyMagenta = Color(0xFFFF00FF)
val FancyCyan = Color(0xFF00FBFF)
val FancyGold = Color(0xFFFDBB2D)
val FancyPurple = Color(0xFF6E2CF5)
val GlassBg = Color(0x1AFFFFFF)
val GlassBorder = Color(0x33FFFFFF)

class MainActivity : ComponentActivity() {
    private lateinit var soundManager: FancySoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        soundManager = FancySoundManager(this)
        enableEdgeToEdge()
        setContent {
            FancySlotMachineTheme {
                SlotMachineScreen(soundManager)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        soundManager.startMusic()
    }

    override fun onPause() {
        super.onPause()
        soundManager.pauseMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}

/**
 * Улучшенное состояние барабана с более плавной физикой и "эффектом пружины"
 */
@Stable
class ReelState(val symbolCount: Int) {
    val offset = Animatable(0f)
    var isSpinning by mutableStateOf(false)

    suspend fun spin(targetIndex: Int, duration: Int) {
        isSpinning = true
        val startValue = offset.value
        val spins = 40f
        val currentMod = ((startValue % symbolCount) + symbolCount) % symbolCount
        val diff = (targetIndex - currentMod + symbolCount) % symbolCount
        val target = startValue + spins + diff

        // Основное вращение с замедлением в конце
        offset.animateTo(
            targetValue = target,
            animationSpec = tween(
                durationMillis = duration,
                easing = CubicBezierEasing(0.2f, 0.9f, 0.3f, 1f)
            )
        )

        // Отскок для эффекта реальности
        offset.animateTo(target + 0.15f, spring(stiffness = Spring.StiffnessLow))
        offset.animateTo(target, spring(stiffness = Spring.StiffnessMedium))
        isSpinning = false
    }
}

/**
 * Динамический анимированный фон в стиле Fancy
 */
@Composable
fun BackgroundLayer() {
    val transition = rememberInfiniteTransition(label = "bg")
    val animScale by transition.animateFloat(
        initialValue = 1f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(10000), RepeatMode.Reverse), label = "scale"
    )
    val rotation by transition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(60000, easing = LinearEasing)), label = "rot"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = animScale
                    scaleY = animScale
                    this.rotationZ = rotation
                }) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(BgGlow, Color.Transparent),
                    center = center,
                    radius = size.minDimension
                )
            )
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(
                        FancyPurple.copy(0.15f),
                        FancyMagenta.copy(0.15f),
                        FancyCyan.copy(0.15f),
                        FancyPurple.copy(0.15f)
                    )
                ),
                radius = size.maxDimension
            )
        }
        // Виньетка
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, BgDeep.copy(0.8f), BgDeep))
                )
        )
    }
}

@Preview
@Composable
fun SlotMachineScreen(soundManager: FancySoundManager? = null) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val prefs = remember { context.getSharedPreferences("slot_prefs", Context.MODE_PRIVATE) }
    
    val symbolResIds = remember {
        listOf(
            R.drawable.seven, R.drawable.cherry, R.drawable.diamond, R.drawable.bell,
            R.drawable.star, R.drawable.watermelon, R.drawable.crown, R.drawable.treasure,
            R.drawable.clover_4, R.drawable.heart, R.drawable.grapes, R.drawable.orange
        )
    }
    val painters = symbolResIds.map { painterResource(it) }
    val reelStates = remember { List(4) { ReelState(symbolResIds.size) } }

    var balance by remember { mutableLongStateOf(25000) }
    var currentBet by remember { mutableLongStateOf(100) }
    var lastWin by remember { mutableLongStateOf(0) }
    var highScore by remember { mutableLongStateOf(prefs.getLong("high_score", 0L)) }
    var isSpinningGlobal by remember { mutableStateOf(false) }
    var isAutoSpin by remember { mutableStateOf(false) }
    var winMessage by remember { mutableStateOf("") }
    var showWin by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val betOptions = remember { listOf(10L, 50L, 100L, 250L, 500L, 1000L, 2500L, 5000L) }

    val onSpinRequest = {
        if (!isSpinningGlobal && balance >= currentBet) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            soundManager?.playKnock()
            scope.launch {
                isSpinningGlobal = true
                showWin = false
                balance -= currentBet

                val results = List(4) { Random.nextInt(symbolResIds.size) }

                coroutineScope {
                    reelStates.forEachIndexed { index, state ->
                        launch {
                            state.spin(results[index], 2000 + index * 400)
                            soundManager?.playKnock()
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }
                }

                val winData = evaluateWin(results, currentBet, context)
                lastWin = winData.first
                winMessage = winData.second

                if (lastWin > 0) {
                    if (lastWin > highScore) {
                        highScore = lastWin
                        prefs.edit { putLong("high_score", highScore) }
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    soundManager?.playGetMoney()
                    showWin = true
                    delay(3000)
                    showWin = false
                }

                isSpinningGlobal = false
            }
        }
    }

    // Логика автоматического вращения
    LaunchedEffect(isAutoSpin) {
        while (isAutoSpin) {
            if (!isSpinningGlobal && balance >= currentBet) {
                onSpinRequest()
                delay(3500) // Пауза между авто-спинами
            } else if (balance < currentBet) {
                isAutoSpin = false
            }
            delay(100)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundLayer()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ВЕРХНЕЕ МЕНЮ
            HeaderMenu(balance, lastWin, highScore)

            if (isLandscape) {
                // АЛЬБОМНЫЙ РЕЖИМ (СИММЕТРИЧНЫЙ)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ЛЕВАЯ ПАНЕЛЬ (СТАВКИ)
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.bet_label).uppercase(),
                            color = Color.White.copy(0.5f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        BetControlButton("+", enabled = !isSpinningGlobal && !isAutoSpin && currentBet < betOptions.last()) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            soundManager?.playSelectValue()
                            val currentIndex = betOptions.indexOf(currentBet)
                            if (currentIndex < betOptions.size - 1) currentBet = betOptions[currentIndex + 1]
                        }
                        Text(
                            text = currentBet.toString(),
                            color = FancyCyan,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            style = TextStyle(shadow = Shadow(color = FancyCyan, blurRadius = 15f)),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                        BetControlButton("-", enabled = !isSpinningGlobal && !isAutoSpin && currentBet > betOptions.first()) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            soundManager?.playSelectValue()
                            val currentIndex = betOptions.indexOf(currentBet)
                            if (currentIndex > 0) currentBet = betOptions[currentIndex - 1]
                        }
                    }

                    // ЦЕНТРАЛЬНАЯ ПАНЕЛЬ (ПОБЕДА И СЛОТЫ)
                    Column(
                        modifier = Modifier.weight(3f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // ТЕКСТ ПОБЕДЫ В UI
                        WinStatusInfo(winMessage, lastWin, showWin)
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(modifier = Modifier.fancyFrame()) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                reelStates.forEach { SlotReelComponent(it, painters, reelHeight = 210.dp) }
                            }
                        }
                    }

                    // ПРАВАЯ ПАНЕЛЬ (КНОПКА SPIN)
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (isAutoSpin) {
                            FancyActionBtn(
                                text = stringResource(R.string.stop),
                                enabled = true,
                                modifier = Modifier.width(130.dp),
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    isAutoSpin = false
                                }
                            )
                        } else {
                            FancyActionBtn(
                                text = stringResource(R.string.spin),
                                enabled = !isSpinningGlobal && balance >= currentBet,
                                modifier = Modifier.width(130.dp),
                                onLongClick = {
                                    if (!isSpinningGlobal && balance >= currentBet) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        isAutoSpin = true
                                    }
                                },
                                onClick = onSpinRequest
                            )
                        }
                    }
                }
            } else {
                // ПОРТРЕТНЫЙ РЕЖИМ (СИММЕТРИЧНЫЙ)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    // ТЕКСТ ПОБЕДЫ В UI
                    Box(modifier = Modifier.height(80.dp), contentAlignment = Alignment.Center) {
                        WinStatusInfo(winMessage, lastWin, showWin)
                    }

                    // ОСНОВНОЙ КОРПУС СЛОТ-МАШИНЫ
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .fancyFrame()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            reelStates.forEach { state ->
                                SlotReelComponent(state, painters)
                            }
                        }
                    }

                    // УПРАВЛЕНИЕ И СТАВКИ
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.bet_label).uppercase(),
                            color = Color.White.copy(0.5f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            BetControlButton(
                                "-",
                                enabled = !isSpinningGlobal && !isAutoSpin && currentBet > betOptions.first()
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                soundManager?.playSelectValue()
                                val currentIndex = betOptions.indexOf(currentBet)
                                if (currentIndex > 0) currentBet = betOptions[currentIndex - 1]
                            }

                            Spacer(modifier = Modifier.width(32.dp))

                            Text(
                                text = currentBet.toString(),
                                color = FancyCyan,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                style = TextStyle(shadow = Shadow(color = FancyCyan, blurRadius = 15f)),
                                modifier = Modifier.widthIn(min = 100.dp),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.width(32.dp))

                            BetControlButton(
                                "+",
                                enabled = !isSpinningGlobal && !isAutoSpin && currentBet < betOptions.last()
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                soundManager?.playSelectValue()
                                val currentIndex = betOptions.indexOf(currentBet)
                                if (currentIndex < betOptions.size - 1) currentBet =
                                    betOptions[currentIndex + 1]
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isAutoSpin) {
                            FancyActionBtn(
                                text = stringResource(R.string.stop),
                                enabled = true,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    isAutoSpin = false
                                }
                            )
                        } else {
                            FancyActionBtn(
                                text = stringResource(R.string.spin),
                                enabled = !isSpinningGlobal && balance >= currentBet,
                                onLongClick = {
                                    if (!isSpinningGlobal && balance >= currentBet) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        isAutoSpin = true
                                    }
                                },
                                onClick = onSpinRequest
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Система выигрышных стратегий
 */
fun evaluateWin(results: List<Int>, bet: Long, context: Context): Pair<Long, String> {
    val counts = results.groupingBy { it }.eachCount()
    val maxSame = counts.values.maxOrNull() ?: 0
    val winSym = counts.filterValues { it == maxSame }.keys.firstOrNull() ?: -1

    return when {
        maxSame == 4 -> {
            val mult = if (winSym == 0 || winSym == 6) 500 else 200
            Pair(bet * mult, context.getString(R.string.jackpot))
        }

        maxSame == 3 -> {
            val mult = if (winSym == 0 || winSym == 6) 50 else 15
            Pair(bet * mult, context.getString(R.string.big_win))
        }

        counts.size == 2 && counts.values.all { it == 2 } -> {
            Pair(bet * 30, context.getString(R.string.double_pair))
        }

        maxSame == 2 -> {
            Pair(bet * 2, context.getString(R.string.win))
        }

        else -> Pair(0L, "")
    }
}

@Composable
fun HeaderMenu(balance: Long, lastWin: Long, highScore: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(GlassBg)
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        InfoTile(stringResource(R.string.credits_label), balance.toString(), FancyCyan)
        InfoTile(stringResource(R.string.last_win_label), lastWin.toString(), FancyMagenta)
        InfoTile(stringResource(R.string.best_label), highScore.toString(), FancyGold)
    }
}

@Composable
fun InfoTile(label: String, value: String, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(
            value, 
            color = accent, 
            fontSize = 24.sp, 
            fontWeight = FontWeight.Black,
            style = TextStyle(
                shadow = Shadow(
                    color = accent,
                    blurRadius = 15f
                )
            )
        )
    }
}

@Composable
fun WinStatusInfo(msg: String, amount: Long, visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text(
                text = msg,
                color = FancyGold,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                style = TextStyle(shadow = Shadow(color = FancyGold, blurRadius = 15f))
            )
            Text(
                text = stringResource(R.string.win_amount_prefix, amount),
                color = FancyCyan,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(shadow = Shadow(color = FancyCyan, blurRadius = 5f))
            )
        }
    }
}

@Composable
fun SlotReelComponent(
    state: ReelState, 
    painters: List<Painter>, 
    reelHeight: androidx.compose.ui.unit.Dp = 260.dp
) {
    val offset = state.offset.value

    Box(
        modifier = Modifier
            .size(width = 82.dp, height = reelHeight)
            .background(Color(0xFF0D0D0D), RoundedCornerShape(16.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
    ) {
        val slotH = with(LocalDensity.current) { reelHeight.toPx() / 3 }
        val baseIdx = offset.toInt()

        for (i in baseIdx - 2..baseIdx + 2) {
            val symIdx = ((i % painters.size) + painters.size) % painters.size
            val dist = abs(i - offset)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = ((i - offset) * slotH / LocalDensity.current.density).dp)
                    .graphicsLayer {
                        alpha = (1.2f - dist).coerceIn(0.1f, 1f)
                        scaleX = (1.1f - dist * 0.15f).coerceIn(0.7f, 1f)
                        scaleY = scaleX
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painters[symIdx],
                    contentDescription = null,
                    modifier = Modifier.size(if (reelHeight < 250.dp) 60.dp else 70.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        // Блик стекла
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(0.5f),
                                Color.Transparent,
                                Color.Black.copy(0.5f)
                            )
                        )
                    )
                    drawRect(
                        Brush.linearGradient(
                            listOf(Color.White.copy(0.06f), Color.Transparent),
                            end = Offset(size.width, size.height)
                        )
                    )
                })
    }
}

@Composable
fun BetControlButton(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.85f else 1f, label = "s")

    Box(
        modifier = Modifier
            .size(56.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .border(2.dp, if (enabled) FancyCyan else GlassBorder, CircleShape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = if (enabled) Color.White else Color.Gray,
            fontWeight = FontWeight.Black,
            fontSize = 28.sp
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FancyActionBtn(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier.fillMaxWidth(0.75f),
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.92f else 1f, label = "p")

    val brush = if (enabled) Brush.horizontalGradient(listOf(FancyMagenta, FancyPurple))
    else Brush.linearGradient(listOf(Color.Gray, Color.DarkGray))

    Box(
        modifier = modifier
            .height(75.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(24.dp, CircleShape, spotColor = FancyMagenta)
            .background(brush, CircleShape)
            .border(2.dp, Color.White.copy(0.4f), CircleShape)
            .combinedClickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text, color = Color.White,
            fontSize = 32.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp
        )
    }
}

fun Modifier.fancyFrame() = this
    .background(Color(0xFF1A1A1A), RoundedCornerShape(32.dp))
    .border(
        6.dp,
        Brush.sweepGradient(listOf(FancyMagenta, FancyCyan, FancyGold, FancyMagenta)),
        RoundedCornerShape(32.dp)
    )
    .padding(6.dp)
    .shadow(35.dp, RoundedCornerShape(32.dp), spotColor = FancyPurple)
