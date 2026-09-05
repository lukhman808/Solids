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
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.Image
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.ColorMatrix
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
                                Triple("stats", Icons.Default.Star, "STATS"),
                                Triple("sessions_home", Icons.Default.Home, "SESSIONS"),
                                Triple("data", Icons.Default.List, "DATA"),
                                Triple("calendar", Icons.Default.DateRange, "CALENDAR")
                            ).forEach { (route, icon, label) ->
                                NavigationBarItem(
                                    selected = currentRoute.value == route,
                                    onClick = { /* unchanged */ },
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
                            }
                        }
                        composable("stats") { StatsScreen(vm = vm) }
                        composable("data") { PlaceholderScreen("DATA") }
                        composable("calendar") { PlaceholderScreen("CALENDAR") }
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


@Composable
fun StatsScreen(vm: WorkoutViewModel) {
    var totalWeight by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        totalWeight = vm.getTotalWeightLifted()
    }

    val currentMilestoneIndex = milestones.indexOfLast { totalWeight >= it.weightKg }
    val currentMilestone = if (currentMilestoneIndex >= 0) milestones[currentMilestoneIndex] else null
    val nextMilestone = milestones.getOrNull(currentMilestoneIndex + 1)

    val progress = if (currentMilestone != null && nextMilestone != null) {
        (totalWeight - currentMilestone.weightKg) / (nextMilestone.weightKg - currentMilestone.weightKg)
    } else if (currentMilestone == null && nextMilestone != null) {
        totalWeight / nextMilestone.weightKg
    } else 1f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "STATS",
            fontSize = 56.sp,
            lineHeight = 56.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "TOTAL LIFTED: ${totalWeight.toInt()}KG",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current milestone card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemInDarkTheme()) Color(0xFF1A1A1A) else Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "YOU HAVE LIFTED THE WEIGHT OF",
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (currentMilestone != null) {
                        MilestoneImage(
                            fileName = currentMilestone.fileName,
                            modifier = Modifier.size(100.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            currentMilestone.name.uppercase(),
                            fontSize = 16.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "${currentMilestone.weightKg.toInt()}KG",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Text(
                            "KEEP LIFTING!",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Next milestone card with circular progress
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemInDarkTheme()) Color(0xFF1A1A1A) else Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "NEXT MILESTONE",
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (nextMilestone != null) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(100.dp)
                        ) {
                            Canvas(modifier = Modifier.size(100.dp)) {
                                val strokeWidth = 8.dp.toPx()
                                drawArc(
                                    color = Color.Gray.copy(alpha = 0.3f),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth)
                                )
                                drawArc(
                                    color = Color(0xFF03DAC5),
                                    startAngle = -90f,
                                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }
                            MilestoneImage(
                                fileName = nextMilestone.fileName,
                                modifier = Modifier.size(70.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            nextMilestone.name.uppercase(),
                            fontSize = 16.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "${nextMilestone.weightKg.toInt()}KG",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Text(
                            "MAX REACHED!",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SessionsScreen(vm: WorkoutViewModel, onSessionClick: (Int) -> Unit) {
    val feed by vm.feed.collectAsState()
    var showChooserDialog by remember { mutableStateOf(false) }
    var showSessionDialog by remember { mutableStateOf(false) }
    var showBodyWeightDialog by remember { mutableStateOf(false) }
    var sessionName by remember { mutableStateOf("") }
    var bodyWeightValue by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedKey by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(feed) {
        suggestions = vm.getSessionSuggestions()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text("SESSIONS", style = MaterialTheme.typography.titleLarge, fontSize = 80.sp)
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showChooserDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { selectedKey = null })
                }
        ) {
            LazyColumn(
                contentPadding = padding,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(feed, key = {
                    when (it) {
                        is FeedItem.SessionItem -> "session:${it.session.id}"
                        is FeedItem.BodyWeightItem -> "bw:${it.entry.id}"
                    }
                }) { item ->
                    when (item) {
                        is FeedItem.SessionItem -> {
                            val key = "session:${item.session.id}"
                            SessionCard(
                                session = item.session,
                                vm = vm,
                                isSelected = selectedKey == key,
                                onSelect = { selectedKey = key },
                                onDeselect = { selectedKey = null },
                                onClick = { onSessionClick(item.session.id) }
                            )
                        }
                        is FeedItem.BodyWeightItem -> {
                            val key = "bw:${item.entry.id}"
                            BodyWeightCard(
                                entry = item.entry,
                                vm = vm,
                                isSelected = selectedKey == key,
                                onSelect = { selectedKey = key },
                                onDeselect = { selectedKey = null }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showChooserDialog) {
        AlertDialog(
            onDismissRequest = { showChooserDialog = false },
            title = { Text("Add") },
            text = { Text("What do you want to log?") },
            confirmButton = {
                TextButton(onClick = {
                    showChooserDialog = false
                    showSessionDialog = true
                }) { Text("WORKOUT") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showChooserDialog = false
                    showBodyWeightDialog = true
                }) { Text("BODY WEIGHT") }
            }
        )
    }

    if (showSessionDialog) {
        AlertDialog(
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
    var editDate by remember(showEditDialog) { mutableStateOf(session.date) }

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
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Session") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Session name") }
                    )
                    OutlinedTextField(
                        value = editDate,
                        onValueChange = { editDate = it },
                        label = { Text("Date") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editName.isNotBlank() && editDate.isNotBlank()) {
                        vm.updateSession(session, editName, editDate)
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
            }
        }
    }

    if (showDialog) {
        AlertDialog(
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    onClick = { if (isSelected) onDeselect() },
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
    onDeselect: () -> Unit
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
                    onClick = { if (isSelected) onDeselect() },
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
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 60.sp,
                        lineHeight = 60.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier
                            .weight(1f)
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                delayMillis = 1000,
                                velocity = 50.dp
                            )
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
                    fontSize = 20.sp,
                    lineHeight = 20.sp,
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