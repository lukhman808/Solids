package com.example.solid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.solid.ui.theme.SolidTheme
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateMapOf
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.graphics.drawscope.Stroke
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.ListItemDefaults
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.text.style.TextAlign
import java.time.YearMonth
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Offset
import android.app.DatePickerDialog
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SolidTheme {
                val vm: WorkoutViewModel = viewModel()
                val rootNavController = rememberNavController()
                val bottomNavController = rememberNavController()
                val currentRoute = remember { mutableStateOf("sessions_home") }

                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color(0xFF1A1A1A),
                            tonalElevation = 0.dp
                        ) {
                            listOf(
                                Triple("dashboard", Icons.Default.Star, "DASHBOARD"),
                                Triple("sessions_home", Icons.Default.Home, "SESSIONS"),
                                Triple("tools", Icons.Default.List, "TOOLS"),
                                Triple("profile", Icons.Default.DateRange, "PROFILE")
                            ).forEach { (route, icon, label) ->
                                NavigationBarItem(
                                    selected = currentRoute.value == route,
                                    onClick = {
                                        if (route == "sessions_home" && currentRoute.value == "sessions_home") {
                                            rootNavController.popBackStack("sessions", inclusive = false)
                                        }
                                        currentRoute.value = route
                                        bottomNavController.navigate(route) {
                                            popUpTo(bottomNavController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(icon, contentDescription = label) },
                                    label = { Text(label) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        selectedTextColor = Color.White,
                                        indicatorColor = Color(0xFF2A2A2A),
                                        unselectedIconColor = Color(0xFF888888),
                                        unselectedTextColor = Color(0xFF888888),
                                    )
                                )
                            }
                        }
                    }
                ) { padding ->
                    NavHost(
                        navController = bottomNavController,
                        startDestination = "sessions_home",
                        modifier = Modifier.padding(padding)
                    ) {
                        composable("sessions_home") {
                            NavHost(
                                navController = rootNavController,
                                startDestination = "sessions"
                            ) {
                                composable("sessions") {
                                    SessionsScreen(
                                        vm = vm,
                                        onSessionClick = { sessionId ->
                                            rootNavController.navigate("workouts/$sessionId")
                                        },
                                        onBodyWeightClick = {
                                            rootNavController.navigate("bodyweight_detail")
                                        }
                                    )
                                }
                                composable("workouts/{sessionId}") { backStackEntry ->
                                    val sessionId = backStackEntry.arguments?.getString("sessionId")?.toInt() ?: 0
                                    WorkoutsScreen(
                                        vm = vm,
                                        sessionId = sessionId,
                                        onBack = { rootNavController.popBackStack() },
                                        onWorkoutClick = { workoutId, exerciseName ->
                                            rootNavController.navigate("sets/$workoutId/$exerciseName")
                                        }
                                    )
                                }
                                composable("sets/{workoutId}/{exerciseName}") { backStackEntry ->
                                    val workoutId = backStackEntry.arguments?.getString("workoutId")?.toInt() ?: 0
                                    val exerciseName = backStackEntry.arguments?.getString("exerciseName") ?: ""
                                    SetsScreen(
                                        vm = vm,
                                        workoutId = workoutId,
                                        exerciseName = exerciseName,
                                        onBack = { rootNavController.popBackStack() }
                                    )
                                }
                                composable("bodyweight_detail") {
                                    BodyWeightDetailScreen(
                                        vm = vm,
                                        onBack = { rootNavController.popBackStack() }
                                    )
                                }
                            }
                        }
                        composable("dashboard") { DashboardScreen(vm = vm) }
                        composable("tools") { PlaceholderScreen("TOOLS") }
                        composable("profile") { ProfileScreen(vm = vm) }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(title, fontSize = 40.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(vm: WorkoutViewModel) {
    val cards by vm.dashboardCards.collectAsState()
    var showTypeChooser by remember { mutableStateOf(false) }
    var showScopeChooser by remember { mutableStateOf(false) }
    var pendingCardType by remember { mutableStateOf<String?>(null) }
    var sessionNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var exerciseNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var noteTypes by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        vm.ensureDefaultDashboardCard()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text("DASHBOARD", style = MaterialTheme.typography.titleLarge, fontSize = 80.sp)
            })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cards, key = { it.id }) { card ->
                DashboardCardView(card = card, vm = vm)
            }
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable {
                            sessionNames = null.let { emptyList() } // reset, refetch below
                            showTypeChooser = true
                        },
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("ADD CARD", color = Color.Black, fontSize = 16.sp)
                    }
                }
            }
        }
    }

    if (showTypeChooser) {
        AlertDialog(
            containerColor = Color(0xFF1A1A1A),
            onDismissRequest = { showTypeChooser = false },
            title = { Text("Add Card") },
            text = {
                Column {
                    listOf("CALENDAR", "GRAPH").forEach { type ->
                        TextButton(
                            onClick = {
                                pendingCardType = type
                                showTypeChooser = false
                                showScopeChooser = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(type, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTypeChooser = false }) { Text("Cancel") }
            }
        )
    }

    if (showScopeChooser && pendingCardType != null) {
        LaunchedEffect(showScopeChooser) {
            sessionNames = vm.getSessionSuggestions()
            exerciseNames = vm.getExerciseSuggestions()
            noteTypes = vm.getCustomNoteTypes()
        }
        val isGraph = pendingCardType == "GRAPH"
        AlertDialog(
            containerColor = Color(0xFF1A1A1A),
            onDismissRequest = {
                showScopeChooser = false
                pendingCardType = null
            },
            title = { Text("What to track?") },
            text = {
                Column(modifier = Modifier.heightIn(max = 400.dp)) {
                    LazyColumn {
                        if (!isGraph) {
                            item {
                                TextButton(onClick = {
                                    vm.addDashboardCard(pendingCardType!!, "ALL_WORKOUTS", null)
                                    showScopeChooser = false
                                    pendingCardType = null
                                }, modifier = Modifier.fillMaxWidth()) {
                                    Text("All Workouts", modifier = Modifier.fillMaxWidth())
                                }
                            }
                            item {
                                Text("Sessions", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
                            }
                            items(sessionNames) { name ->
                                TextButton(onClick = {
                                    vm.addDashboardCard(pendingCardType!!, "SESSION_NAME", name)
                                    showScopeChooser = false
                                    pendingCardType = null
                                }, modifier = Modifier.fillMaxWidth()) {
                                    Text(name, modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                        item {
                            Text("Exercises", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
                        }
                        items(exerciseNames) { name ->
                            TextButton(onClick = {
                                vm.addDashboardCard(pendingCardType!!, "EXERCISE_NAME", name)
                                showScopeChooser = false
                                pendingCardType = null
                            }, modifier = Modifier.fillMaxWidth()) {
                                Text(name, modifier = Modifier.fillMaxWidth())
                            }
                        }
                        item {
                            Text("Other", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
                        }
                        item {
                            TextButton(onClick = {
                                vm.addDashboardCard(pendingCardType!!, "BODYWEIGHT", null)
                                showScopeChooser = false
                                pendingCardType = null
                            }, modifier = Modifier.fillMaxWidth()) {
                                Text("Body Weight", modifier = Modifier.fillMaxWidth())
                            }
                        }
                        if (!isGraph) {
                            items(noteTypes) { type ->
                                TextButton(onClick = {
                                    vm.addDashboardCard(pendingCardType!!, "NOTE_TYPE", type)
                                    showScopeChooser = false
                                    pendingCardType = null
                                }, modifier = Modifier.fillMaxWidth()) {
                                    Text(type, modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showScopeChooser = false
                    pendingCardType = null
                }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardCardView(card: DashboardCard, vm: WorkoutViewModel) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val scopeLabel = card.scopeValue ?: when (card.scopeType) {
        "ALL_WORKOUTS" -> "All Workouts"
        "BODYWEIGHT" -> "Body Weight"
        else -> card.scopeType
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = { showDeleteConfirm = true }
            )
    ) {
        when (card.cardType) {
            "CALENDAR" -> CalendarCard(title = scopeLabel, scopeType = card.scopeType, scopeValue = card.scopeValue, vm = vm)
            "GRAPH" -> {
                GraphCard(
                    title = scopeLabel,
                    scopeType = card.scopeType,
                    scopeValue = card.scopeValue,
                    metric = card.metric,
                    onMetricChange = { newMetric -> vm.updateDashboardCardMetric(card, newMetric) },
                    vm = vm
                )
            }
        }
    }
    if (showDeleteConfirm) {
        AlertDialog(
            containerColor = Color(0xFF1A1A1A),
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Remove card?") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteDashboardCard(card)
                    showDeleteConfirm = false
                }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun MilestoneImage(fileName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }
    AsyncImage(
        model = "file:///android_asset/$fileName",
        contentDescription = null,
        imageLoader = imageLoader,
        modifier = modifier
    )
}


val sessionColorPalette = listOf(
    Color(0xFF6200EE), Color(0xFF03DAC5), Color(0xFFFF5722), Color(0xFF4CAF50),
    Color(0xFF2196F3), Color(0xFFFF9800), Color(0xFFE91E63), Color(0xFF9C27B0)
)

@Composable
fun CalendarCard(title: String, scopeType: String, scopeValue: String?, vm: WorkoutViewModel, modifier: Modifier = Modifier) {
    var monthOffset by remember(scopeType, scopeValue) { mutableStateOf(0) }
    var highlights by remember { mutableStateOf<Map<LocalDate, Color>>(emptyMap()) }

    LaunchedEffect(scopeType, scopeValue) {
        highlights = vm.getCalendarHighlights(scopeType, scopeValue)
    }

    val displayedMonth = remember(monthOffset) { YearMonth.now().plusMonths(monthOffset.toLong()) }
    val firstDay = displayedMonth.atDay(1)
    val daysInMonth = displayedMonth.lengthOfMonth()
    val startOffset = firstDay.dayOfWeek.value % 7

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title.uppercase(), fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { monthOffset-- }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous month")
                }
                Text(
                    "${displayedMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${displayedMonth.year}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { monthOffset++ }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next month")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                    Text(it, fontSize = 12.sp, color = Color(0xFF888888), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }
            val totalCells = startOffset + daysInMonth
            val rows = (totalCells + 6) / 7
            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val dayNum = cellIndex - startOffset + 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (dayNum in 1..daysInMonth) {
                                val date = displayedMonth.atDay(dayNum)
                                val color = highlights[date]
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(color ?: Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        dayNum.toString(),
                                        fontSize = 12.sp,
                                        color = if (color != null) Color.Black else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


val graphColorPalette = listOf(
    Color(0xFF03DAC5), Color(0xFFFF5722), Color(0xFF4CAF50), Color(0xFF2196F3),
    Color(0xFFFF9800), Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFFFFEB3B)
)

@Composable
fun GraphCard(
    title: String,
    scopeType: String,
    scopeValue: String?,
    metric: String,
    onMetricChange: (String) -> Unit,
    vm: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    var curves by remember { mutableStateOf<List<GraphCurve>>(emptyList()) }

    LaunchedEffect(scopeType, scopeValue, metric) {
        curves = vm.getGraphData(scopeType, scopeValue, metric)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title.uppercase(), fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))

            if (scopeType != "BODYWEIGHT") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("WEIGHT", "REPS", "VOLUME").forEach { option ->
                        FilterChip(
                            selected = metric == option,
                            onClick = { onMetricChange(option) },
                            label = { Text(option) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (curves.isEmpty() || curves.all { it.points.isEmpty() }) {
                Text("No data yet", color = Color(0xFF888888), fontSize = 14.sp)
            } else {
                val allPoints = curves.flatMap { it.points }
                val minY = allPoints.minOf { it.second }
                val maxY = allPoints.maxOf { it.second }
                val yRange = if (maxY == minY) 1f else maxY - minY
                val maxX = curves.flatMap { it.points }.maxOf { it.first }.coerceAtLeast(1f)

                Row {
                    Column(
                        modifier = Modifier
                            .height(160.dp)
                            .padding(end = 4.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatWeight(maxY), fontSize = 10.sp, color = Color(0xFF888888))
                        Text(formatWeight(minY), fontSize = 10.sp, color = Color(0xFF888888))
                    }
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        curves.forEachIndexed { i, curve ->
                            val color = graphColorPalette[i % graphColorPalette.size]
                            val sorted = curve.points.sortedBy { it.first }
                            val path = Path()
                            sorted.forEachIndexed { idx, (x, y) ->
                                val px = if (maxX == 0f) size.width / 2 else (x / maxX) * size.width
                                val py = size.height - ((y - minY) / yRange) * size.height
                                if (idx == 0) path.moveTo(px, py) else path.lineTo(px, py)
                            }
                            drawPath(path, color = color, style = Stroke(width = 3.dp.toPx()))
                            sorted.forEach { (x, y) ->
                                val px = if (maxX == 0f) size.width / 2 else (x / maxX) * size.width
                                val py = size.height - ((y - minY) / yRange) * size.height
                                drawCircle(color = color, radius = 4.dp.toPx(), center = Offset(px, py))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    curves.forEachIndexed { i, curve ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(graphColorPalette[i % graphColorPalette.size], RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Set ${curve.setIndex}", fontSize = 12.sp, color = Color(0xFFAAAAAA))
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SessionsScreen(vm: WorkoutViewModel, onSessionClick: (Int) -> Unit, onBodyWeightClick: () -> Unit) {
    val feed by vm.feed.collectAsState()
    var showChooserDialog by remember { mutableStateOf(false) }
    var showSessionDialog by remember { mutableStateOf(false) }
    var showBodyWeightDialog by remember { mutableStateOf(false) }
    var showCustomNameDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var pendingNoteType by remember { mutableStateOf<String?>(null) }
    var sessionName by remember { mutableStateOf("") }
    var bodyWeightValue by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedKey by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf("ALL") }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(feed) {
        suggestions = vm.getSessionSuggestions()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text("SESSIONS", style = MaterialTheme.typography.titleLarge, fontSize = 80.sp)
            })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(top = padding.calculateTopPadding())) {
            val customTypes = feed.filterIsInstance<FeedItem.NoteItem>()
                .map { it.entry.typeName }
                .filter { it != "CARDIO" && it != "NUTRITION" }
                .distinct()
            val filterOptions = listOf("ALL", "WORKOUT", "BODYWEIGHT", "CARDIO", "NUTRITION") + customTypes

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { option ->
                    FilterChip(
                        selected = selectedFilter == option,
                        onClick = { selectedFilter = option },
                        label = { Text(option) }
                    )
                }
            }

            val filteredFeed = feed.filter { item ->
                when (selectedFilter) {
                    "ALL" -> true
                    "WORKOUT" -> item is FeedItem.SessionItem
                    "BODYWEIGHT" -> item is FeedItem.BodyWeightItem
                    else -> item is FeedItem.NoteItem && item.entry.typeName == selectedFilter
                }
            }

            LaunchedEffect(filteredFeed.size) {
                if (filteredFeed.isNotEmpty()) {
                    listState.scrollToItem(filteredFeed.size)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { selectedKey = null })
                    }
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filteredFeed, key = {
                        when (it) {
                            is FeedItem.SessionItem -> "session:${it.session.id}"
                            is FeedItem.BodyWeightItem -> "bw:${it.entry.id}"
                            is FeedItem.NoteItem -> "note:${it.entry.id}"
                        }
                    }) { item ->
                        when (item) {
                            is FeedItem.SessionItem -> {
                                val key = "session:${item.session.id}"
                                SessionCard(
                                    session = item.session, vm = vm,
                                    isSelected = selectedKey == key,
                                    onSelect = { selectedKey = key },
                                    onDeselect = { selectedKey = null },
                                    onClick = { onSessionClick(item.session.id) }
                                )
                            }
                            is FeedItem.BodyWeightItem -> {
                                val key = "bw:${item.entry.id}"
                                BodyWeightCard(
                                    entry = item.entry, vm = vm,
                                    isSelected = selectedKey == key,
                                    onSelect = { selectedKey = key },
                                    onDeselect = { selectedKey = null },
                                    onClick = onBodyWeightClick
                                )
                            }
                            is FeedItem.NoteItem -> {
                                val key = "note:${item.entry.id}"
                                NoteEntryCard(
                                    entry = item.entry, vm = vm,
                                    isSelected = selectedKey == key,
                                    onSelect = { selectedKey = key },
                                    onDeselect = { selectedKey = null }
                                )
                            }
                        }
                    }
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable { showChooserDialog = true },
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("ADD SESSION", color = Color.Black, fontSize = 16.sp)
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (showChooserDialog) {
        AlertDialog(
            containerColor = Color(0xFF1A1A1A),
            onDismissRequest = { showChooserDialog = false },
            title = { Text("Add") },
            text = {
                Column {
                    listOf("WORKOUT", "BODY WEIGHT", "CARDIO", "NUTRITION", "CUSTOM").forEach { option ->
                        TextButton(
                            onClick = {
                                showChooserDialog = false
                                when (option) {
                                    "WORKOUT" -> showSessionDialog = true
                                    "BODY WEIGHT" -> showBodyWeightDialog = true
                                    "CARDIO" -> { pendingNoteType = "CARDIO"; showNoteDialog = true }
                                    "NUTRITION" -> { pendingNoteType = "NUTRITION"; showNoteDialog = true }
                                    "CUSTOM" -> showCustomNameDialog = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(option, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showChooserDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showSessionDialog) {
        AlertDialog(
            containerColor = Color(0xFF1A1A1A),
            onDismissRequest = { showSessionDialog = false },
            title = { Text("New Session") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (suggestions.isNotEmpty()) {
                        Text("Previous sessions:", style = MaterialTheme.typography.labelMedium)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            suggestions.forEach { suggestion ->
                                SuggestionChip(
                                    onClick = {
                                        scope.launch {
                                            val workouts = vm.getWorkoutsForSessionName(suggestion)
                                            vm.addSessionWithWorkouts(suggestion, workouts)
                                            suggestions = vm.getSessionSuggestions()
                                            showSessionDialog = false
                                        }
                                    },
                                    label = { Text(suggestion) }
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                    OutlinedTextField(
                        value = sessionName,
                        onValueChange = { sessionName = it },
                        label = { Text("Or type a new name") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (sessionName.isNotBlank()) {
                        scope.launch {
                            vm.addSessionWithWorkouts(sessionName, emptyList())
                            suggestions = vm.getSessionSuggestions()
                            sessionName = ""
                            showSessionDialog = false
                        }
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showSessionDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showBodyWeightDialog) {
        AlertDialog(
            containerColor = Color(0xFF1A1A1A),
            onDismissRequest = {
                showBodyWeightDialog = false
                bodyWeightValue = ""
            },
            title = { Text("Log Body Weight") },
            text = {
                OutlinedTextField(
                    value = bodyWeightValue,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) bodyWeightValue = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val w = bodyWeightValue.toFloatOrNull()
                    if (w != null) {
                        vm.addBodyWeightEntry(w)
                        bodyWeightValue = ""
                        showBodyWeightDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBodyWeightDialog = false
                    bodyWeightValue = ""
                }) { Text("Cancel") }
            }
        )
    }

    if (showCustomNameDialog) {
        var customTypeName by remember { mutableStateOf("") }
        AlertDialog(
            containerColor = Color(0xFF1A1A1A),
            onDismissRequest = { showCustomNameDialog = false },
            title = { Text("New Card Type") },
            text = {
                OutlinedTextField(
                    value = customTypeName,
                    onValueChange = { customTypeName = it },
                    label = { Text("Type name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (customTypeName.isNotBlank()) {
                        pendingNoteType = customTypeName.uppercase()
                        showCustomNameDialog = false
                        showNoteDialog = true
                    }
                }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = { showCustomNameDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showNoteDialog && pendingNoteType != null) {
        var noteText by remember { mutableStateOf("") }
        AlertDialog(
            containerColor = Color(0xFF1A1A1A),
            onDismissRequest = {
                showNoteDialog = false
                pendingNoteType = null
            },
            title = { Text("New ${pendingNoteType}") },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Note") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.addNoteEntry(pendingNoteType!!, noteText)
                    showNoteDialog = false
                    pendingNoteType = null
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showNoteDialog = false
                    pendingNoteType = null
                }) { Text("Cancel") }
            }
        )
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SessionCard(
    session: Session,
    vm: WorkoutViewModel,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDeselect: () -> Unit,
    onClick: () -> Unit
) {
    val workouts by vm.getWorkoutsForSession(session.id).collectAsState(emptyList())
    val allSets = remember { mutableStateMapOf<Int, List<WorkoutSet>>() }
    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember(showEditDialog) { mutableStateOf(session.name) }

    val colorPalette = listOf(
        Color(0xFF6200EE),
        Color(0xFF03DAC5),
        Color(0xFFFF5722),
        Color(0xFF4CAF50),
        Color(0xFF2196F3),
        Color(0xFFFF9800),
        Color(0xFFE91E63),
        Color(0xFF9C27B0)
    )
    val cardColor = colorPalette[session.color % colorPalette.size]
    val borderColor = if (isSystemInDarkTheme()) Color.White else Color.Black
    val cardShape = RoundedCornerShape(16.dp)

    workouts.forEach { workout ->
        val sets by vm.getSetsForWorkout(workout.id).collectAsState(emptyList())
        allSets[workout.id] = sets
    }

    Column {
        Card(
            shape = cardShape,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .combinedClickable(
                    onClick = { if (isSelected) onDeselect() else onClick() },
                    onLongClick = onSelect
                ),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isSelected -> if (isSystemInDarkTheme()) Color(0xFF333333) else Color(0xFFE0E0E0)
                    isSystemInDarkTheme() -> Color(0xFF1A1A1A)
                    else -> Color.White
                }
            ),
            border = if (isSelected) BorderStroke(2.dp, borderColor) else null
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = session.name.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 60.sp,
                        lineHeight = 60.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        modifier = Modifier
                            .weight(1f)
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                delayMillis = 1000,
                                velocity = 50.dp
                            )
                    )
                    Column(horizontalAlignment = Alignment.Start) {
                        val dateParts = session.date.split(" ")
                        dateParts.forEach { part ->
                            Text(
                                text = part,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 20.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                if (workouts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    workouts.forEach { workout ->
                        val workoutSets = allSets[workout.id] ?: emptyList()
                        if (workoutSets.isEmpty()) {
                            Text(
                                text = workout.exerciseName.uppercase(),
                                fontSize = 20.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Row {
                                Text(
                                    text = "${workout.exerciseName.uppercase()}  ",
                                    fontSize = 20.sp,
                                    lineHeight = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = buildSetsAnnotatedString(workoutSets),
                                    fontSize = 20.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Canvas(modifier = Modifier.size(20.dp)) {
                        drawCircle(color = cardColor)
                    }
                }
            }
        }

        if (isSelected) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    showEditDialog = true
                    onDeselect()
                }) { Text("EDIT") }
                TextButton(onClick = {
                    vm.deleteSession(session)
                    onDeselect()
                }) { Text("DELETE") }
            }
        }
    }

    if (showEditDialog) {
        val context = LocalContext.current
        var pickedDate by remember(showEditDialog) {
            mutableStateOf(
                Instant.ofEpochMilli(session.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            )
        }
        AlertDialog(
            containerColor = Color(0xFF1A1A1A),
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Session") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Session name") }
                    )
                    OutlinedButton(onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                pickedDate = LocalDate.of(year, month + 1, dayOfMonth)
                            },
                            pickedDate.year, pickedDate.monthValue - 1, pickedDate.dayOfMonth
                        ).show()
                    }) {
                        Text(pickedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editName.isNotBlank()) {
                        vm.updateSession(session, editName, pickedDate)
                        showEditDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkoutsScreen(vm: WorkoutViewModel, sessionId: Int, onBack: () -> Unit, onWorkoutClick: (Int, String) -> Unit) {
    val workouts by vm.getWorkoutsForSession(sessionId).collectAsState(emptyList())
    var previousSession by remember { mutableStateOf<Session?>(null) }

    LaunchedEffect(sessionId) {
        previousSession = vm.getPreviousSessionOfSameName(sessionId)
    }
    val allSets = remember { mutableStateMapOf<Int, List<WorkoutSet>>() }
    var showDialog by remember { mutableStateOf(false) }
    var exerciseName by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var filteredSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedWorkoutId by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()
    var sessionName by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(sessionId) {
        sessionName = vm.getSessionName(sessionId)
    }

    LaunchedEffect(workouts) {
        suggestions = vm.getExerciseSuggestions()
    }

    workouts.forEach { workout ->
        val sets by vm.getSetsForWorkout(workout.id).collectAsState(emptyList())
        allSets[workout.id] = sets
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exercises") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Exercise")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { selectedWorkoutId = null })
                }
        ) {
            LazyColumn(
                contentPadding = padding,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(workouts) { workout ->
                    WorkoutCard(
                        workout = workout,
                        vm = vm,
                        sets = allSets[workout.id] ?: emptyList(),
                        isSelected = selectedWorkoutId == workout.id,
                        onSelect = { selectedWorkoutId = workout.id },
                        onDeselect = { selectedWorkoutId = null },
                        onClick = { onWorkoutClick(workout.id, workout.exerciseName) }
                    )
                }
                previousSession?.let { prevSession ->
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "LAST TIME",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFAAAAAA),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    item {
                        SessionCard(
                            session = prevSession,
                            vm = vm,
                            isSelected = false,
                            onSelect = {},
                            onDeselect = {},
                            onClick = {}
                        )
                    }
                }

                sessionName?.let { name ->
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        CalendarCard(title = "$name Calendar", scopeType = "SESSION_NAME", scopeValue = name, vm = vm)
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            containerColor = Color(0xFF1A1A1A),
            onDismissRequest = {
                showDialog = false
                exerciseName = ""
                filteredSuggestions = emptyList()
            },
            title = { Text("New Exercise") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = exerciseName,
                        onValueChange = {
                            exerciseName = it
                            filteredSuggestions = if (it.isBlank()) emptyList()
                            else suggestions.filter { s ->
                                s.contains(it, ignoreCase = true)
                            }
                        },
                        label = { Text("Exercise name") }
                    )
                    if (filteredSuggestions.isNotEmpty()) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredSuggestions.forEach { suggestion ->
                                SuggestionChip(
                                    onClick = {
                                        vm.addWorkout(sessionId, suggestion)
                                        exerciseName = ""
                                        filteredSuggestions = emptyList()
                                        showDialog = false
                                    },
                                    label = { Text(suggestion) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (exerciseName.isNotBlank()) {
                        vm.addWorkout(sessionId, exerciseName)
                        exerciseName = ""
                        filteredSuggestions = emptyList()
                        showDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    exerciseName = ""
                    filteredSuggestions = emptyList()
                }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WorkoutCard(
    workout: Workout,
    vm: WorkoutViewModel,
    sets: List<WorkoutSet>,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDeselect: () -> Unit,
    onClick: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember(showEditDialog) { mutableStateOf(workout.exerciseName) }
    val borderColor = if (isSystemInDarkTheme()) Color.White else Color.Black
    val cardShape = RoundedCornerShape(16.dp)

    Column {
        Card(
            shape = cardShape,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .combinedClickable(
                    onClick = { if (isSelected) onDeselect() else onClick() },
                    onLongClick = onSelect
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) {
                    if (isSystemInDarkTheme()) Color(0xFF333333) else Color(0xFFE0E0E0)
                } else MaterialTheme.colorScheme.surface
            ),
            border = if (isSelected) BorderStroke(2.dp, borderColor) else null
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workout.exerciseName,
                        style = MaterialTheme.typography.titleSmall
                    )
                    if (sets.isNotEmpty()) {
                        Text(
                            text = buildSetsAnnotatedString(sets),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        if (isSelected) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    showEditDialog = true
                    onDeselect()
                }) { Text("EDIT") }
                TextButton(onClick = {
                    vm.deleteWorkout(workout)
                    onDeselect()
                }) { Text("DELETE") }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            containerColor = Color(0xFF1A1A1A),
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Exercise") },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Exercise name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editName.isNotBlank()) {
                        vm.updateWorkout(workout, editName)
                        showEditDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun SetsScreen(vm: WorkoutViewModel, workoutId: Int, exerciseName: String, onBack: () -> Unit) {
    val sets by vm.getSetsForWorkout(workoutId).collectAsState(emptyList())
    var showDialog by remember { mutableStateOf(false) }
    var reps by remember(showDialog) { mutableStateOf("") }
    var weight by remember(showDialog) { mutableStateOf("") }
    var selectedSetId by remember { mutableStateOf<Int?>(null) }
    var graphMetric by remember { mutableStateOf("WEIGHT") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = exerciseName,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(
                            iterations = Int.MAX_VALUE,
                            delayMillis = 1000,
                            velocity = 50.dp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Set")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { selectedSetId = null })
                }
        ) {
            LazyColumn(
                contentPadding = padding,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(sets) { index, set ->
                    SetRow(
                        index = index,
                        set = set,
                        vm = vm,
                        isSelected = selectedSetId == set.id,
                        onSelect = { selectedSetId = set.id },
                        onDeselect = { selectedSetId = null }
                    )
                }
                item {
                    WorkoutNoteCard(vm = vm, exerciseName = exerciseName)
                }

                item {
                    CalendarCard(title = "$exerciseName Calendar", scopeType = "EXERCISE_NAME", scopeValue = exerciseName, vm = vm)
                }

                item {
                    GraphCard(
                        title = "$exerciseName Progress",
                        scopeType = "EXERCISE_NAME",
                        scopeValue = exerciseName,
                        metric = graphMetric,
                        onMetricChange = { graphMetric = it },
                        vm = vm
                    )
                }
            }
        }
    }

    if (showDialog) {
        var isPartial by remember { mutableStateOf(false) }
        var difficulty by remember { mutableStateOf<String?>(null) }
        var repsError by remember { mutableStateOf(false) }
        val weightFocusRequester = remember { FocusRequester() }
        val repsFocusRequester = remember { FocusRequester() }

        AlertDialog(
            containerColor = Color(0xFF1A1A1A),
            onDismissRequest = {
                showDialog = false
                reps = ""
                weight = ""
            },
            title = { Text("Add Set") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) weight = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { repsFocusRequester.requestFocus() }
                        ),
                        modifier = Modifier.focusRequester(weightFocusRequester)
                    )
                    OutlinedTextField(
                        value = reps,
                        onValueChange = {
                            if (it.all { c -> c.isDigit() }) reps = it
                            repsError = false
                        },
                        label = { Text("Reps") },
                        isError = repsError,
                        supportingText = {
                            if (repsError) Text("Enter a rep count")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val w = weight.toFloatOrNull()
                                val r = reps.toIntOrNull()
                                if (r == null) {
                                    repsError = true
                                } else if (w != null) {
                                    vm.addSet(workoutId, r, w, isPartial, difficulty)
                                    reps = ""
                                    weight = ""
                                    showDialog = false
                                }
                            }
                        ),
                        modifier = Modifier.focusRequester(repsFocusRequester)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isPartial = !isPartial }
                    ) {
                        Checkbox(checked = isPartial, onCheckedChange = { isPartial = it })
                        Text("Partial set")
                    }
                    Text("Difficulty (optional)", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val options = listOf(
                            "EASY" to Color(0xFF4CAF50),
                            "OKAY" to Color(0xFFFFEB3B),
                            "HARD" to Color(0xFFFF9800),
                            "FAILURE" to Color(0xFFF44336)
                        )
                        options.forEach { (label, color) ->
                            FilterChip(
                                selected = difficulty == label,
                                onClick = { difficulty = if (difficulty == label) null else label },
                                label = { Text(label, color = color) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val w = weight.toFloatOrNull()
                    val r = reps.toIntOrNull()
                    if (r == null) {
                        repsError = true
                    } else if (w != null) {
                        vm.addSet(workoutId, r, w, isPartial, difficulty)
                        reps = ""
                        weight = ""
                        showDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    reps = ""
                    weight = ""
                }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SetRow(
    index: Int,
    set: WorkoutSet,
    vm: WorkoutViewModel,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDeselect: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editReps by remember(showEditDialog) { mutableStateOf(set.reps.toString()) }
    var editWeight by remember(showEditDialog) { mutableStateOf(set.weight.toString()) }
    var editIsPartial by remember(showEditDialog) { mutableStateOf(set.isPartial) }
    var editDifficulty by remember(showEditDialog) { mutableStateOf(set.difficulty) }
    var editRepsError by remember(showEditDialog) { mutableStateOf(false) }
    val borderColor = Color.White
    val cardShape = RoundedCornerShape(16.dp)

    Column {
        Card(
            shape = cardShape,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp)
                .combinedClickable(
                    onClick = { showEditDialog = true },
                    onLongClick = onSelect
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) Color(0xFF333333) else Color(0xFF1A1A1A)
            ),
            border = if (isSelected) BorderStroke(2.dp, borderColor) else null
        ) {
            ListItem(
                headlineContent = {
                    Text(if (set.isPartial) "Partial Set" else "Set ${index + 1}")
                },
                supportingContent = {
                    Text(
                        text = "${set.weight}kg × ${set.reps} reps",
                        color = difficultyColor(set.difficulty)
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
        }

        if (isSelected) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    showEditDialog = true
                    onDeselect()
                }) { Text("EDIT") }
                TextButton(onClick = {
                    vm.deleteSet(set)
                    onDeselect()
                }) { Text("DELETE") }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            containerColor = Color(0xFF1A1A1A),
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Set") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editWeight,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) editWeight = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = editReps,
                        onValueChange = {
                            if (it.all { c -> c.isDigit() }) editReps = it
                            editRepsError = false
                        },
                        label = { Text("Reps") },
                        isError = editRepsError,
                        supportingText = {
                            if (editRepsError) Text("Enter a rep count")
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { editIsPartial = !editIsPartial }
                    ) {
                        Checkbox(checked = editIsPartial, onCheckedChange = { editIsPartial = it })
                        Text("Partial set")
                    }
                    Text("Difficulty (optional)", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val options = listOf(
                            "EASY" to Color(0xFF4CAF50),
                            "OKAY" to Color(0xFFFFEB3B),
                            "HARD" to Color(0xFFFF9800),
                            "FAILURE" to Color(0xFFF44336)
                        )
                        options.forEach { (label, color) ->
                            FilterChip(
                                selected = editDifficulty == label,
                                onClick = { editDifficulty = if (editDifficulty == label) null else label },
                                label = { Text(label, color = color) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val r = editReps.toIntOrNull()
                    val w = editWeight.toFloatOrNull()
                    if (r == null) {
                        editRepsError = true
                    } else if (w != null) {
                        vm.updateSet(set, r, w, editIsPartial, editDifficulty)
                        showEditDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }
}



@Composable
fun WorkoutNoteCard(vm: WorkoutViewModel, exerciseName: String) {
    val note by vm.getNoteForExercise(exerciseName).collectAsState(initial = null)
    var isEditing by remember { mutableStateOf(false) }
    var noteText by remember(isEditing) { mutableStateOf(note?.note ?: "") }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { if (!isEditing) isEditing = true },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "NOTE",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFAAAAAA)
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (isEditing) {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Note for $exerciseName") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = {
                        isEditing = false
                        noteText = note?.note ?: ""
                    }) { Text("Cancel") }
                    TextButton(onClick = {
                        vm.saveNote(exerciseName, noteText)
                        isEditing = false
                    }) { Text("Save") }
                }
            } else {
                Text(
                    text = if (note?.note.isNullOrBlank()) "Tap to add a note" else note!!.note,
                    color = if (note?.note.isNullOrBlank()) Color(0xFF888888) else Color.White
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BodyWeightCard(
    entry: BodyWeightEntry,
    vm: WorkoutViewModel,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDeselect: () -> Unit,
    onClick: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editWeight by remember(showEditDialog) { mutableStateOf(entry.weight.toString()) }
    val borderColor = if (isSystemInDarkTheme()) Color.White else Color.Black
    val cardShape = RoundedCornerShape(16.dp)

    Column {
        Card(
            shape = cardShape,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .combinedClickable(
                    onClick = { if (isSelected) onDeselect() else onClick() },
                    onLongClick = onSelect
                ),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isSelected -> if (isSystemInDarkTheme()) Color(0xFF333333) else Color(0xFFE0E0E0)
                    isSystemInDarkTheme() -> Color(0xFF1A1A1A)
                    else -> Color.White
                }
            ),
            border = if (isSelected) BorderStroke(2.dp, borderColor) else null
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "BODY WEIGHT",
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 20.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Column(horizontalAlignment = Alignment.Start) {
                        val dateParts = entry.date.split(" ")
                        dateParts.forEach { part ->
                            Text(
                                text = part,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 20.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${entry.weight}KG",
                    fontSize = 60.sp,
                    lineHeight = 60.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (isSelected) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    showEditDialog = true
                    onDeselect()
                }) { Text("EDIT") }
                TextButton(onClick = {
                    vm.deleteBodyWeightEntry(entry)
                    onDeselect()
                }) { Text("DELETE") }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            containerColor = Color(0xFF1A1A1A),
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Body Weight") },
            text = {
                OutlinedTextField(
                    value = editWeight,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) editWeight = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val w = editWeight.toFloatOrNull()
                    if (w != null) {
                        vm.updateBodyWeightEntry(entry, w)
                        showEditDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyWeightDetailScreen(vm: WorkoutViewModel, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Body Weight") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                GraphCard(
                    title = "Body Weight Progress",
                    scopeType = "BODYWEIGHT",
                    scopeValue = null,
                    metric = "WEIGHT",
                    onMetricChange = {},
                    vm = vm
                )
            }
            item { CalendarCard(title = "Body Weight Calendar", scopeType = "BODYWEIGHT", scopeValue = null, vm = vm) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NoteEntryCard(
    entry: NoteEntry,
    vm: WorkoutViewModel,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDeselect: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editNote by remember(showEditDialog) { mutableStateOf(entry.note) }
    val cardShape = RoundedCornerShape(16.dp)

    Column {
        Card(
            shape = cardShape,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .combinedClickable(
                    onClick = { if (isSelected) onDeselect() },
                    onLongClick = onSelect
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) Color(0xFF333333) else Color(0xFF1A1A1A)
            ),
            border = if (isSelected) BorderStroke(2.dp, Color.White) else null
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = entry.typeName.uppercase(),
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Column(horizontalAlignment = Alignment.Start) {
                        entry.date.split(" ").forEach { part ->
                            Text(
                                text = part,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = entry.note,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (isSelected) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { showEditDialog = true; onDeselect() }) { Text("EDIT") }
                TextButton(onClick = { vm.deleteNoteEntry(entry); onDeselect() }) { Text("DELETE") }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            containerColor = Color(0xFF1A1A1A),
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit ${entry.typeName}") },
            text = {
                OutlinedTextField(
                    value = editNote,
                    onValueChange = { editNote = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Note") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.updateNoteEntry(entry, editNote)
                    showEditDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(vm: WorkoutViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var pendingImportContent by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/markdown")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val content = vm.exportBackup()
                context.contentResolver.openOutputStream(uri)?.use { it.write(content.toByteArray()) }
                statusMessage = "Backup saved"
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                if (content != null) {
                    pendingImportContent = content
                    showRestoreConfirm = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text("PROFILE", style = MaterialTheme.typography.titleLarge, fontSize = 80.sp)
            })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("BACKUP", fontSize = 16.sp, color = Color(0xFFAAAAAA))
            OutlinedButton(
                onClick = { exportLauncher.launch("solids_backup.md") },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Export Backup") }

            OutlinedButton(
                onClick = { importLauncher.launch("text/markdown") },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Import Backup") }

            statusMessage?.let {
                Text(it, color = Color(0xFF4CAF50), fontSize = 14.sp)
            }
        }
    }

    if (showRestoreConfirm && pendingImportContent != null) {
        AlertDialog(
            containerColor = Color(0xFF1A1A1A),
            onDismissRequest = {
                showRestoreConfirm = false
                pendingImportContent = null
            },
            title = { Text("Import Backup") },
            text = { Text("Replace all current data, or merge with what's already here?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val ok = vm.restoreBackup(pendingImportContent!!)
                        statusMessage = if (ok) "Restored — all data replaced" else "Import failed: unrecognized file"
                        showRestoreConfirm = false
                        pendingImportContent = null
                    }
                }) { Text("Replace All") }
            },
            dismissButton = {
                TextButton(onClick = {
                    scope.launch {
                        val ok = vm.mergeBackup(pendingImportContent!!)
                        statusMessage = if (ok) "Merged into existing data" else "Import failed: unrecognized file"
                        showRestoreConfirm = false
                        pendingImportContent = null
                    }
                }) { Text("Merge") }
            }
        )
    }
}

data class Milestone(
    val name: String,
    val weightKg: Float,
    val fileName: String
)

val milestones = listOf(
    Milestone("Average Person", 100f, "milestone_person.svg"),
    Milestone("Polar Bear", 500f, "milestone_polar_bear.svg"),
    Milestone("Small Car", 1000f, "milestone_small_car.svg"),
    Milestone("Grizzly Bear", 2000f, "milestone_grizzly_bear.svg"),
    Milestone("Large Car", 3500f, "milestone_large_car.svg"),
    Milestone("Elephant", 5000f, "milestone_elephant.svg"),
    Milestone("T-Rex", 8000f, "milestone_trex.svg"),
    Milestone("Double Decker Bus", 12000f, "milestone_bus.svg"),
    Milestone("Humpback Whale", 20000f, "milestone_humpback_whale.svg"),
    Milestone("Boeing 737", 30000f, "milestone_boeing.svg"),
    Milestone("Blue Whale", 50000f, "milestone_blue_whale.svg"),
    Milestone("Eiffel Tower", 75000f, "milestone_eiffel_tower.svg"),
    Milestone("Space Shuttle", 100000f, "milestone_space_shuttle.svg"),
    Milestone("Titanic", 150000f, "milestone_titanic.svg"),
    Milestone("Statue of Liberty", 200000f, "milestone_statue_of_liberty.svg"),
    Milestone("Aircraft Carrier", 300000f, "milestone_aircraft_carrier.svg"),
    Milestone("Golden Gate Bridge", 500000f, "milestone_golden_gate.svg"),
    Milestone("ISS", 420000f, "milestone_iss.svg"),
    Milestone("Empire State Building", 1000000f, "milestone_empire_state.svg"),
    Milestone("Great Pyramid", 2000000f, "milestone_pyramid.svg")
)


fun toSuperscript(number: Int): String {
    val superscripts = mapOf(
        '0' to "\u2070", '1' to "\u00B9", '2' to "\u00B2",
        '3' to "\u00B3", '4' to "\u2074", '5' to "\u2075",
        '6' to "\u2076", '7' to "\u2077", '8' to "\u2078",
        '9' to "\u2079"
    )
    return number.toString().map { superscripts[it] ?: it }.joinToString("")
}

fun formatWeight(weight: Float): String {
    return if (weight == weight.toInt().toFloat()) {
        weight.toInt().toString()
    } else {
        weight.toString().trimEnd('0').trimEnd('.')
    }
}



fun difficultyColor(difficulty: String?): Color {
    return when (difficulty) {
        "EASY" -> Color(0xFF4CAF50)
        "OKAY" -> Color(0xFFFFEB3B)
        "HARD" -> Color(0xFFFF9800)
        "FAILURE" -> Color(0xFFF44336)
        else -> Color.White
    }
}

fun buildSetsAnnotatedString(sets: List<WorkoutSet>): AnnotatedString {
    return buildAnnotatedString {
        sets.forEachIndexed { index, set ->
            if (index > 0) append(" ")
            val notation = "${formatWeight(set.weight)}${toSuperscript(set.reps)}"
            withStyle(SpanStyle(color = difficultyColor(set.difficulty))) {
                if (set.isPartial) {
                    append("(")
                    append(notation)
                    append(")")
                } else {
                    append(notation)
                }
            }
        }
    }
}