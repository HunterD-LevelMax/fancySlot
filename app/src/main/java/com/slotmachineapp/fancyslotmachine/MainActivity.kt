package com.slotmachineapp.fancyslotmachine

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.core.content.edit
import com.slotmachineapp.fancyslotmachine.ui.theme.FancySlotMachineTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor
import kotlin.random.Random

private val Bg = Color(0xFF0F150E)
private val Surface = Color(0xFF1E1E1E)
private val SurfaceLow = Color(0xFF171D16)
private val SurfaceHighest = Color(0xFF30362E)
private val Outline = Color(0x4D757575)
private val Primary = Color(0xFF4CAF50)
private val OnSurface = Color(0xFFDEE4D9)
private val OnSurfaceVariant = Color(0xFFBECAB9)

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

@Stable
class ReelState(private val symbolCount: Int) {
    val offset = Animatable(0f)
    var isSpinning by mutableStateOf(false)

    suspend fun spin(targetIndex: Int, duration: Int) {
        isSpinning = true
        val startValue = offset.value
        val spins = 40f
        val currentMod = ((startValue % symbolCount) + symbolCount) % symbolCount
        val diff = (targetIndex - currentMod + symbolCount) % symbolCount
        val target = startValue + spins + diff

        offset.animateTo(
            targetValue = target,
            animationSpec = tween(durationMillis = duration, easing = CubicBezierEasing(0.2f, 0.9f, 0.3f, 1f))
        )
        offset.animateTo(target + 0.15f, spring(stiffness = Spring.StiffnessLow))
        offset.animateTo(target, spring(stiffness = Spring.StiffnessMedium))
        isSpinning = false
    }
}

@Preview(showBackground = true, name = "main", showSystemUi = false)
@Composable
fun SlotMachineScreen(soundManager: FancySoundManager? = null) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val haptic = LocalHapticFeedback.current
    val prefs = remember { context.getSharedPreferences("slot_prefs", Context.MODE_PRIVATE) }

    val symbolResIds = remember {
        listOf(
            R.drawable.seven,
            R.drawable.cherry,
            R.drawable.diamond,
            R.drawable.bell,
            R.drawable.star,
            R.drawable.watermelon,
            R.drawable.crown,
            R.drawable.treasure,
            R.drawable.clover_4,
            R.drawable.heart,
            R.drawable.grapes,
            R.drawable.orange
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
    var turboMode by remember { mutableStateOf(false) }
    var activePaylines by remember { mutableIntStateOf(3) }
    var autoSpinLimit by remember { mutableIntStateOf(0) }
    var autoSpinRemaining by remember { mutableIntStateOf(0) }
    var winMessage by remember { mutableStateOf("") }
    var showWin by remember { mutableStateOf(false) }
    var animatedWin by remember { mutableLongStateOf(0L) }
    var showPaytable by remember { mutableStateOf(false) }
    val currentResult = remember { mutableStateListOf(0, 0, 0, 0) }
    var winningSymbolIndex by remember { mutableIntStateOf(-1) }
    val winningPaylines = remember { mutableStateListOf<List<Int>>() }
    var totalSpins by remember { mutableLongStateOf(0L) }
    var totalWinsCount by remember { mutableLongStateOf(0L) }
    var totalWagered by remember { mutableLongStateOf(0L) }
    var totalWon by remember { mutableLongStateOf(0L) }

    val scope = rememberCoroutineScope()
    val betOptions = remember { listOf(10L, 50L, 100L, 250L, 500L, 1000L, 2500L, 5000L) }

    val onSpinRequest = {
        if (!isSpinningGlobal && balance >= currentBet) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            soundManager?.playKnock()
            scope.launch {
                isSpinningGlobal = true
                showWin = false
                winningSymbolIndex = -1
                winningPaylines.clear()
                balance -= currentBet
                totalSpins += 1
                totalWagered += currentBet

                val results = List(4) { Random.nextInt(symbolResIds.size) }
                currentResult.clear()
                currentResult.addAll(results)
                coroutineScope {
                    reelStates.forEachIndexed { index, state ->
                        launch {
                            val baseDuration = if (turboMode) 850 else 2000
                            val stepDuration = if (turboMode) 140 else 350
                            state.spin(results[index], baseDuration + index * stepDuration)
                            soundManager?.playKnock()
                        }
                    }
                }

                val winData = evaluatePaylines(
                    centerResults = results,
                    bet = currentBet,
                    activePaylines = activePaylines,
                    symbolCount = symbolResIds.size,
                    context = context
                )
                lastWin = winData.totalWin
                winMessage = winData.message
                winningSymbolIndex = winData.highlightSymbolIndex
                winningPaylines.clear()
                winningPaylines.addAll(winData.winningLines)

                if (lastWin > 0) {
                    totalWinsCount += 1
                    totalWon += lastWin
                    if (lastWin > highScore) {
                        highScore = lastWin
                        prefs.edit { putLong("high_score", highScore) }
                    }
                    appendWinHistory(prefs, lastWin, winMessage)
                    soundManager?.playGetMoney()
                    showWin = true
                    delay(if (turboMode) 1200 else 2200)
                    showWin = false
                }

                isSpinningGlobal = false
                if (isAutoSpin && autoSpinRemaining > 0) autoSpinRemaining -= 1
                if (isAutoSpin && autoSpinRemaining == 0 && autoSpinLimit > 0) isAutoSpin = false
            }
        }
    }

    LaunchedEffect(isAutoSpin) {
        while (isAutoSpin) {
            if (!isSpinningGlobal && balance >= currentBet) {
                onSpinRequest()
                delay(if (turboMode) 1200 else 3200)
            } else if (balance < currentBet) {
                isAutoSpin = false
            }
            delay(100)
        }
    }

    LaunchedEffect(lastWin, showWin) {
        if (showWin && lastWin > 0) {
            animatedWin = 0
            val steps = 18
            repeat(steps) { step ->
                animatedWin = (lastWin * (step + 1)) / steps
                delay(30)
            }
        } else {
            animatedWin = lastWin
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        TopBar()
        StatsBar(balance = balance, bet = currentBet, win = animatedWin)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                UtilityStrip(
                    isLandscape = isLandscape,
                    activePaylines = activePaylines,
                    turboMode = turboMode,
                    onSetPaylines = { activePaylines = it },
                    onToggleTurbo = { turboMode = !turboMode },
                    onOpenHistory = { context.startActivity(Intent(context, HistoryActivity::class.java)) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                ReelDeck(
                    reelStates = reelStates,
                    painters = painters,
                    result = currentResult,
                    winningSymbolIndex = winningSymbolIndex,
                    highlightWin = lastWin > 0,
                    winningPaylines = winningPaylines
                )
                Spacer(modifier = Modifier.height(14.dp))
                WinBanner(showWin = showWin, winMessage = winMessage, amount = lastWin, currentBet = currentBet)
                Spacer(modifier = Modifier.height(10.dp))
                SessionStrip(
                    totalSpins = totalSpins,
                    totalWinsCount = totalWinsCount,
                    totalWagered = totalWagered,
                    totalWon = totalWon
                )
                Spacer(modifier = Modifier.height(8.dp))
                MissionsStrip(totalSpins = totalSpins, totalWinsCount = totalWinsCount, highScore = highScore)
            }
        }

        ControlPanel(
            currentBet = currentBet,
            isAutoSpin = isAutoSpin,
            canSpin = !isSpinningGlobal && balance >= currentBet,
            autoSpinRemaining = autoSpinRemaining,
            onDecreaseBet = {
                val i = betOptions.indexOf(currentBet)
                if (i > 0) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    soundManager?.playSelectValue()
                    currentBet = betOptions[i - 1]
                }
            },
            onIncreaseBet = {
                val i = betOptions.indexOf(currentBet)
                if (i < betOptions.lastIndex) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    soundManager?.playSelectValue()
                    currentBet = betOptions[i + 1]
                }
            },
            onSpin = onSpinRequest,
            onToggleAuto = { limit ->
                if (isAutoSpin) {
                    isAutoSpin = false
                    autoSpinRemaining = 0
                    autoSpinLimit = 0
                } else {
                    autoSpinLimit = limit
                    autoSpinRemaining = limit
                    isAutoSpin = true
                }
            }
        )

        FooterBar(highScore = highScore, onPaytableClick = { showPaytable = true })
    }

    if (showPaytable) {
        PaytableDialog(onDismiss = { showPaytable = false })
    }
}

@Composable
private fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.app_name).uppercase(),
            color = Primary,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun StatsBar(balance: Long, bet: Long, win: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceLow)
            .border(1.dp, Outline)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            StatItem(stringResource(R.string.balance), balance.toString(), OnSurface)
            StatItem(stringResource(R.string.bet), bet.toString(), OnSurface)
        }
        StatItem(stringResource(R.string.win), win.toString(), Primary, alignEnd = true)
    }
}

@Composable
private fun StatItem(label: String, value: String, valueColor: Color, alignEnd: Boolean = false) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(label, color = OnSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
        Text(value, color = valueColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun ReelDeck(
    reelStates: List<ReelState>,
    painters: List<Painter>,
    result: List<Int>,
    winningSymbolIndex: Int,
    highlightWin: Boolean,
    winningPaylines: List<List<Int>>
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1A1A))
            .border(1.dp, Outline, RoundedCornerShape(16.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        val gap = 4.dp
        val cellSize = ((maxWidth - gap * 3f) / 4f).coerceAtMost(82.dp)
        val reelWidth = (cellSize * 4) + (gap * 3)

        Box(modifier = Modifier.width(reelWidth)) {
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                reelStates.forEachIndexed { index, state ->
                    val winningRowsForReel = winningPaylines.map { it[index] }.toSet()
                    SlotReelColumn(
                        state = state,
                        painters = painters,
                        width = cellSize,
                        cellSize = cellSize,
                        rows = 3,
                        centerSymbolIndex = result.getOrNull(index) ?: 0,
                        winningRows = if (highlightWin) winningRowsForReel else emptySet()
                    )
                }
            }
            if (highlightWin && winningPaylines.isNotEmpty()) {
                PaylineOverlay(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cellSize * 3),
                    winningPaylines = winningPaylines,
                    reelCount = reelStates.size,
                    cellSize = cellSize,
                    gap = gap
                )
            }
        }
    }
}

@Composable
private fun PaylineOverlay(
    modifier: Modifier,
    winningPaylines: List<List<Int>>,
    reelCount: Int,
    cellSize: Dp,
    gap: Dp
) {
    val density = LocalDensity.current
    val cellPx = with(density) { cellSize.toPx() }
    val gapPx = with(density) { gap.toPx() }
    val strokePx = with(density) { 3.dp.toPx() }

    Canvas(modifier = modifier) {
        val glowColor = Primary.copy(alpha = 0.55f)
        val lineColor = Primary.copy(alpha = 0.9f)

        winningPaylines.forEach { line ->
            if (line.size != reelCount) return@forEach
            val points = line.mapIndexed { reelIndex, row ->
                val x = reelIndex * (cellPx + gapPx) + cellPx / 2f
                val y = row * cellPx + cellPx / 2f
                Offset(x, y)
            }

            for (i in 0 until points.lastIndex) {
                drawLine(
                    color = glowColor,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = strokePx * 2.4f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = lineColor,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = strokePx,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun SlotReelColumn(
    state: ReelState,
    painters: List<Painter>,
    width: Dp,
    cellSize: Dp,
    rows: Int,
    centerSymbolIndex: Int,
    winningRows: Set<Int>
) {
    val offset = state.offset.value
    val totalHeight = cellSize * rows
    val centerScale = remember { Animatable(1f) }

    // Анимация пульсации для выигрышных символов
    val pulseScale = remember { Animatable(1f) }
    LaunchedEffect(winningRows.isNotEmpty()) {
        if (winningRows.isNotEmpty()) {
            pulseScale.animateTo(
                targetValue = 1.12f,
                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                    animation = tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                )
            )
        } else {
            pulseScale.snapTo(1f)
        }
    }

    LaunchedEffect(state.isSpinning) {
        if (!state.isSpinning) {
            centerScale.snapTo(1f)
            centerScale.animateTo(1.08f, tween(durationMillis = 85))
            centerScale.animateTo(1f, tween(durationMillis = 130))
        }
    }

    Box(
        modifier = Modifier
            .width(width)
            .height(totalHeight)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.35f))
    ) {
        val cellPx = with(LocalDensity.current) { cellSize.toPx() }
        val frac = offset - floor(offset)

        for (row in -1..3) {
            val symIdx = ((centerSymbolIndex + row - 1) % painters.size + painters.size) % painters.size
            val y = (row - frac) * cellPx
            val isWinningCell = winningRows.contains(row)

            Box(
                modifier = Modifier
                    .size(cellSize)
                    .align(Alignment.TopCenter)
                    .graphicsLayer {
                        translationY = y
                        alpha = 1f
                    }
                    .border(
                        width = if (isWinningCell) 2.dp else 0.dp,
                        color = if (isWinningCell) Primary else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painters[symIdx],
                    contentDescription = null,
                    modifier = Modifier
                        .graphicsLayer {
                            val baseScale = if (row == 1) centerScale.value else 1f
                            val pulse = if (isWinningCell) pulseScale.value else 1f
                            scaleX = baseScale * pulse
                            scaleY = baseScale * pulse
                        }
                        .blur(if (state.isSpinning) 0.5.dp else 0.dp)
                        .fillMaxSize()
                        .padding(8.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cellSize)
                .align(Alignment.TopCenter)
                .background(Color.Black.copy(alpha = 0.5f))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cellSize)
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.5f))
        )
    }
}

@Composable
private fun WinBanner(showWin: Boolean, winMessage: String, amount: Long, currentBet: Long = 100) {
    val multiplier = if (currentBet > 0) amount.toFloat() / currentBet else 0f
    val bannerColor = when {
        multiplier >= 50f -> Color(0xFFFFD700) // Gold for Jackpot
        multiplier >= 20f -> Color(0xFF00E676) // Bright Green for Big Win
        else -> Primary
    }
    val bannerScale by animateFloatAsState(if (showWin) 1.1f else 1f, label = "winScale")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = showWin,
            enter = fadeIn() + androidx.compose.animation.expandVertically(),
            exit = fadeOut()
        ) {
            Text(
                text = if (amount > 0) "$winMessage +$amount" else "",
                color = bannerColor,
                fontSize = if (multiplier >= 20f) 22.sp else 18.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    scaleX = bannerScale
                    scaleY = bannerScale
                }
            )
        }
    }
}

@Composable
private fun ControlPanel(
    currentBet: Long,
    isAutoSpin: Boolean,
    canSpin: Boolean,
    autoSpinRemaining: Int,
    onDecreaseBet: () -> Unit,
    onIncreaseBet: () -> Unit,
    onSpin: () -> Unit,
    onToggleAuto: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceLow)
            .border(1.dp, Outline)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "${stringResource(R.string.bet)} $currentBet", color = OnSurfaceVariant, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            RoundAdjustButton("-", enabled = canSpin || isAutoSpin.not(), onClick = onDecreaseBet)
            SpinButton(enabled = canSpin, onClick = onSpin, onLongClick = { onToggleAuto(Int.MAX_VALUE) })
            RoundAdjustButton("+", enabled = canSpin || isAutoSpin.not(), onClick = onIncreaseBet)
        }

        Spacer(modifier = Modifier.height(12.dp))
        AutoSpinPanel(
            isAutoSpin = isAutoSpin,
            autoSpinRemaining = autoSpinRemaining,
            onToggleAuto = onToggleAuto
        )
    }
}

@Composable
private fun RoundAdjustButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(SurfaceHighest)
            .border(1.dp, Outline, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = if (enabled) OnSurface else OnSurfaceVariant, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SpinButton(enabled: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "spin")

    Box(
        modifier = Modifier
            .widthIn(min = 180.dp)
            .height(64.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(99.dp))
            .background(if (enabled) Primary else SurfaceHighest)
            .shadow(
                if (enabled) 18.dp else 0.dp,
                RoundedCornerShape(99.dp),
                spotColor = Primary.copy(alpha = 0.35f)
            )
            .combinedClickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(R.string.spin), color = Color(0xFF00390A), fontWeight = FontWeight.Bold, fontSize = 22.sp, letterSpacing = 1.sp)
    }
}

@Composable
private fun PillButton(
    text: String,
    enabled: Boolean,
    horizontalPadding: Dp = 24.dp,
    verticalPadding: Dp = 8.dp,
    textSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(SurfaceHighest)
            .border(1.dp, Outline, RoundedCornerShape(99.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = OnSurface, fontSize = textSize, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FooterBar(highScore: Long, onPaytableClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceLow)
            .border(1.dp, Outline)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(stringResource(R.string.info), color = OnSurfaceVariant, fontSize = 12.sp)
        Text(stringResource(R.string.best, highScore), color = Primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Text(
            stringResource(R.string.paytable),
            color = Primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onPaytableClick)
        )
    }
}

@Composable
private fun UtilityStrip(
    isLandscape: Boolean,
    activePaylines: Int,
    turboMode: Boolean,
    onSetPaylines: (Int) -> Unit,
    onToggleTurbo: () -> Unit,
    onOpenHistory: () -> Unit
) {
    val horizontalPadding = if (isLandscape) 4.dp else 12.dp
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                listOf(1, 3, 5).forEach { lines ->
                    PillButton(
                        text = "LINES $lines",
                        enabled = true,
                        horizontalPadding = 12.dp,
                        verticalPadding = 7.dp,
                        textSize = 12.sp,
                        onClick = { onSetPaylines(lines) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                PillButton(
                    text = if (turboMode) "TURBO ON" else "TURBO",
                    enabled = true,
                    horizontalPadding = 14.dp,
                    verticalPadding = 7.dp,
                    textSize = 12.sp,
                    onClick = onToggleTurbo
                )
                PillButton(
                    text = "HISTORY",
                    enabled = true,
                    horizontalPadding = 14.dp,
                    verticalPadding = 7.dp,
                    textSize = 12.sp,
                    onClick = onOpenHistory
                )
            }
        }
        Text(
            text = "Active paylines: $activePaylines",
            color = OnSurfaceVariant,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun SessionStrip(totalSpins: Long, totalWinsCount: Long, totalWagered: Long, totalWon: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .background(SurfaceLow, RoundedCornerShape(10.dp))
            .border(1.dp, Outline, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatItem("SPINS", totalSpins.toString(), OnSurface)
        StatItem("HITS", totalWinsCount.toString(), OnSurface)
        StatItem("WAGERED", totalWagered.toString(), OnSurface)
        StatItem("WON", totalWon.toString(), Primary)
    }
}

@Composable
private fun MissionsStrip(totalSpins: Long, totalWinsCount: Long, highScore: Long) {
    val missions = listOf(
        "10 SPINS" to (totalSpins >= 10),
        "3 WINS" to (totalWinsCount >= 3),
        "BIG x500" to (highScore >= 500)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        missions.forEach { (title, done) ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (done) Primary.copy(alpha = 0.25f) else SurfaceHighest)
                    .border(1.dp, if (done) Primary else Outline, RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (done) "$title ✓" else title,
                    color = if (done) Primary else OnSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun AutoSpinPanel(
    isAutoSpin: Boolean,
    autoSpinRemaining: Int,
    onToggleAuto: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
            listOf(10, 25).forEach { count ->
                PillButton(
                    text = if (isAutoSpin && autoSpinRemaining in 1..count) "AUTO $autoSpinRemaining" else "AUTO $count",
                    enabled = true,
                    horizontalPadding = 12.dp,
                    verticalPadding = 7.dp,
                    textSize = 12.sp,
                    onClick = { onToggleAuto(count) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
            PillButton(
                text = "AUTO 50",
                enabled = true,
                horizontalPadding = 12.dp,
                verticalPadding = 7.dp,
                textSize = 12.sp,
                onClick = { onToggleAuto(50) }
            )
            PillButton(
                text = if (isAutoSpin) stringResource(R.string.stop) else "AUTO ∞",
                enabled = true,
                horizontalPadding = 12.dp,
                verticalPadding = 7.dp,
                textSize = 12.sp,
                onClick = { onToggleAuto(Int.MAX_VALUE) }
            )
        }
    }
}

@Composable
private fun PaytableDialog(onDismiss: () -> Unit) {
    val payouts = remember {
        listOf(
            "7 / Crown x4" to "500x (4), 50x (3)",
            "Any symbol x4" to "200x",
            "Any symbol x3" to "15x",
            "Double pair (2+2)" to "30x",
            "One pair (2)" to "2x"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceLow)
                .border(1.dp, Outline, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text("PAYTABLE", color = Primary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            payouts.forEach { (combo, mult) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(combo, color = OnSurface, fontSize = 14.sp)
                    Text(mult, color = Primary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Tap anywhere to close",
                color = OnSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

private fun appendWinHistory(prefs: android.content.SharedPreferences, amount: Long, message: String) {
    val old = prefs.getString("win_history", "").orEmpty()
    val entry = "${System.currentTimeMillis()}|$amount|$message"
    val combined = if (old.isBlank()) entry else "$entry;$old"
    val trimmed = combined.split(";").take(60).joinToString(";")
    prefs.edit { putString("win_history", trimmed) }
}

private data class PaylineWinData(
    val totalWin: Long,
    val message: String,
    val highlightSymbolIndex: Int,
    val winningLines: List<List<Int>>
)

private fun evaluatePaylines(
    centerResults: List<Int>,
    bet: Long,
    activePaylines: Int,
    symbolCount: Int,
    context: Context
): PaylineWinData {
    val paylines = when (activePaylines) {
        5 -> listOf(
            listOf(1, 1, 1, 1), // center
            listOf(0, 0, 0, 0), // top
            listOf(2, 2, 2, 2), // bottom
            listOf(0, 1, 2, 1), // down V
            listOf(2, 1, 0, 1)  // up V
        )
        3 -> listOf(
            listOf(1, 1, 1, 1), // center
            listOf(0, 0, 0, 0), // top
            listOf(2, 2, 2, 2)  // bottom
        )
        else -> listOf(listOf(1, 1, 1, 1)) // center only
    }

    var totalWin = 0L
    var wonLines = 0
    var bestMessage = ""
    var bestLineWin = 0L
    var highlightSymbol = -1
    val wonPaylines = mutableListOf<List<Int>>()

    paylines.forEach { line ->
        val lineSymbols = centerResults.mapIndexed { reelIndex, center ->
            symbolAtRow(center, line[reelIndex], symbolCount)
        }
        val lineResult = evaluateLine(lineSymbols, bet, context)
        if (lineResult.first > 0L) {
            totalWin += lineResult.first
            wonLines += 1
            wonPaylines.add(line)
            if (lineResult.first > bestLineWin) {
                bestLineWin = lineResult.first
                bestMessage = lineResult.second
                highlightSymbol = dominantSymbol(lineSymbols)
            }
        }
    }

    val message = when {
        totalWin <= 0L -> ""
        wonLines > 1 -> "$bestMessage x$wonLines"
        else -> bestMessage
    }

    return PaylineWinData(
        totalWin = totalWin,
        message = message,
        highlightSymbolIndex = highlightSymbol,
        winningLines = wonPaylines
    )
}

private fun symbolAtRow(centerSymbol: Int, row: Int, symbolCount: Int): Int {
    val shifted = when (row) {
        0 -> centerSymbol - 1
        2 -> centerSymbol + 1
        else -> centerSymbol
    }
    return ((shifted % symbolCount) + symbolCount) % symbolCount
}

private fun dominantSymbol(symbols: List<Int>): Int {
    return symbols.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: -1
}

private fun evaluateLine(results: List<Int>, bet: Long, context: Context): Pair<Long, String> {
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
        counts.size == 2 && counts.values.all { it == 2 } -> Pair(bet * 30, context.getString(R.string.double_pair))
        maxSame == 2 -> Pair(bet * 2, context.getString(R.string.win))
        else -> Pair(0L, "")
    }
}
