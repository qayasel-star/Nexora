package com.registroinformatico.app

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Navy = Color(0xFF07111F)
private val Panel = Color(0xFF101E30)
private val Panel2 = Color(0xFF17283D)
private val Cyan = Color(0xFF48DDF5)
private val Blue = Color(0xFF528BFF)
private val Violet = Color(0xFF9A8CFF)
private val Green = Color(0xFF5DE0B0)
private val Orange = Color(0xFFFFB86B)
private val Muted = Color(0xFF9BAFC7)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash nativo: aparece antes de que Compose empiece a dibujar.
        // No mantenemos la pantalla artificialmente mientras la app ya está lista.
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Crear el canal y programar recordatorios persistentes; WorkManager los conserva tras reinicios.
        NexoraReminderWorker.createNotificationChannel(this)
        val reminder = PeriodicWorkRequestBuilder<NexoraReminderWorker>(2, TimeUnit.HOURS)
            .setInitialDelay(NexoraReminderWorker.delayUntilNextSlot(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            NexoraReminderWorker.WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, reminder
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 4102)
        }

        window.statusBarColor = android.graphics.Color.rgb(3, 10, 28)
        window.navigationBarColor = android.graphics.Color.rgb(3, 10, 28)

        // Guardar el contexto de la actividad antes de entrar en AnimatedContent.
        val activityContext = this

        setContent {
            var showSplash by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                // Solo una breve presentación visual; se elimina la espera artificial de 1,9 s.
                delay(350)
                showSplash = false
            }

            AnimatedContent(
                targetState = showSplash,
                transitionSpec = {
                    if (initialState && !targetState) {
                        (
                            fadeIn(animationSpec = tween(260)) +
                                slideInVertically(
                                    animationSpec = tween(260),
                                    initialOffsetY = { height -> height / 24 }
                                )
                        ) togetherWith fadeOut(animationSpec = tween(180))
                    } else {
                        fadeIn(animationSpec = tween(220)) togetherWith
                            fadeOut(animationSpec = tween(180))
                    }
                },
                label = "transicion_bienvenida_inicio"
            ) { mostrarBienvenida ->
                if (mostrarBienvenida) {
                    StartupScreen()
                } else {
                    RegistroApp(activityContext)
                }
            }
        }
    }
}

@Composable
private fun StartupScreen() {
    // Logo y nombre visibles desde el primer fotograma de Compose: sin fade-in inicial.
    val bg = Brush.verticalGradient(
        colors = listOf(Color(0xFF020817), Color(0xFF03132D), Color(0xFF02091A))
    )
    Box(modifier = Modifier.fillMaxSize().background(bg)) {
        // Decorative circuit-like traces near the screen edges.
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 0..7) {
                val y = size.height * (0.10f + i * 0.075f)
                drawLine(Color(0xFF00CFFF).copy(alpha = 0.13f), androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width * 0.10f, y + 22f), 1.2f)
                drawLine(Color(0xFF765BFF).copy(alpha = 0.12f), androidx.compose.ui.geometry.Offset(size.width * 0.90f, y + 12f), androidx.compose.ui.geometry.Offset(size.width, y), 1.2f)
            }
            for (i in 0..5) {
                val x = size.width * (0.12f + i * 0.15f)
                drawLine(Color(0xFF087DE0).copy(alpha = 0.07f), androidx.compose.ui.geometry.Offset(x, size.height * 0.03f), androidx.compose.ui.geometry.Offset(x, size.height * 0.08f), 1f)
            }
            drawLine(Color(0xFF00BFF3).copy(alpha = 0.24f), androidx.compose.ui.geometry.Offset(0f, size.height * 0.88f), androidx.compose.ui.geometry.Offset(size.width * 0.45f, size.height * 0.96f), 1.4f)
            drawLine(Color(0xFF554BFF).copy(alpha = 0.22f), androidx.compose.ui.geometry.Offset(size.width * 0.45f, size.height * 0.96f), androidx.compose.ui.geometry.Offset(size.width, size.height * 0.86f), 1.4f)
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.weight(0.45f))
            Box(
                modifier = Modifier.size(116.dp).background(
                    Brush.linearGradient(listOf(Color(0xFF00DDF8), Color(0xFF086FEA), Color(0xFF762DDA))),
                    RoundedCornerShape(27.dp)
                ).padding(2.dp).background(Color(0xFF03152F), RoundedCornerShape(25.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher),
                    contentDescription = "Logotipo de Nexora",
                    modifier = Modifier.fillMaxSize().padding(5.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "NEXORA",
                color = Color.White,
                fontSize = 29.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "GESTIÓN INTELIGENTE DE SOPORTE TÉCNICO",
                color = Color(0xFF9BDDF4),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.weight(0.72f))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(3) { index ->
                    Box(Modifier.size(if (index == 1) 18.dp else 6.dp, 4.dp).background(if (index == 1) Cyan else Blue.copy(alpha = 0.55f), RoundedCornerShape(4.dp)))
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("VERSIÓN 2.0 BETA 05", color = Color(0xFF6E91B8), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
        }
    }
}

data class Report(
    val id: Long = System.currentTimeMillis(), val person: String, val department: String,
    val software: String, val type: String, val priority: String, val problem: String,
    val diagnosis: String, val solution: String, val minutes: Int, val status: String,
    val date: Long = System.currentTimeMillis(), val technician: String = "", val notes: String = "", val closedAt: Long = 0L
)

private fun Report.toJson() = JSONObject().apply {
    put("id", id); put("person", person); put("department", department); put("software", software)
    put("type", type); put("priority", priority); put("problem", problem); put("diagnosis", diagnosis)
    put("solution", solution); put("minutes", minutes); put("status", status); put("date", date)
    put("technician", technician); put("notes", notes); put("closedAt", closedAt)
}
private fun JSONObject.str(k: String) = optString(k, "")
private fun JSONObject.toReport() = Report(optLong("id"), str("person"), str("department"), str("software"), str("type"), str("priority"), str("problem"), str("diagnosis"), str("solution"), optInt("minutes"), str("status"), optLong("date", System.currentTimeMillis()), str("technician"), str("notes"), optLong("closedAt", 0L))
private class ReportStore(context: Context) {
    private val prefs = context.getSharedPreferences("registro_informatico", Context.MODE_PRIVATE)
    fun load(): List<Report> = runCatching {
        val a = JSONArray(prefs.getString("reports", "[]"))
        (0 until a.length()).map { a.getJSONObject(it).toReport() }.sortedByDescending { it.date }
    }.getOrDefault(emptyList())
    fun save(list: List<Report>) { val a = JSONArray(); list.forEach { a.put(it.toJson()) }; prefs.edit().putString("reports", a.toString()).apply() }
}

private data class Tab(val label: String, val icon: ImageVector)
private val tabs = listOf(Tab("Inicio", Icons.Default.Dashboard), Tab("Reportes", Icons.Default.Assignment), Tab("Análisis", Icons.Default.QueryStats), Tab("Informes", Icons.Default.Assessment), Tab("Ajustes", Icons.Default.Settings))

@Composable
private fun RegistroApp(context: Context) {
    val store = remember { ReportStore(context) }
    var reports by remember { mutableStateOf(store.load()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Report?>(null) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("Todos") }
    fun persist(next: List<Report>) { reports = next.sortedByDescending { it.date }; store.save(reports) }
    var transferMessage by remember { mutableStateOf<String?>(null) }
    var pendingBackupImport by remember { mutableStateOf<List<Report>?>(null) }
    val exportCsvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) runCatching { context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { it.write("\uFEFF" + reportsToCsv(reports)) } }.onSuccess { transferMessage = "CSV exportado. Puedes abrirlo con Excel." }.onFailure { transferMessage = "No se pudo exportar el CSV: ${it.message}" }
    }
    val exportBackupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) runCatching { context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { out -> out.write(JSONArray().apply { reports.forEach { put(it.toJson()) } }.toString(2)) } }.onSuccess { transferMessage = "Copia de seguridad exportada." }.onFailure { transferMessage = "No se pudo crear la copia: ${it.message}" }
    }
    val importBackupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching { context.contentResolver.openInputStream(uri)!!.bufferedReader(Charsets.UTF_8).use { it.readText() } }.mapCatching { raw ->
            val arr = JSONArray(raw); (0 until arr.length()).map { arr.getJSONObject(it).toReport() }
        }.onSuccess { imported -> pendingBackupImport = imported }.onFailure { transferMessage = "No se pudo importar la copia. Selecciona un JSON exportado por esta aplicación." }
    }
    val importCsvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching { context.contentResolver.openInputStream(uri)!!.bufferedReader(Charsets.UTF_8).use { it.readText().removePrefix("\uFEFF") } }.mapCatching { csvToReports(it) }.onSuccess { imported -> persist(reports + imported); transferMessage = "Importados ${imported.size} reportes desde CSV." }.onFailure { transferMessage = "No se pudo leer el CSV. Usa el formato exportado por la aplicación." }
    }

    MaterialTheme(colorScheme = darkColorScheme(primary=Cyan, secondary=Blue, background=Navy, surface=Panel, onPrimary=Navy, onBackground=Color.White, onSurface=Color.White)) {
        Scaffold(containerColor=Navy, bottomBar = {
            NavigationBar(containerColor=Color(0xFF0A1727), contentColor=Muted, tonalElevation=0.dp) {
                tabs.forEachIndexed { i, t -> NavigationBarItem(selected=selectedTab==i, onClick={selectedTab=i}, icon={Icon(t.icon, null)}, label={Text(t.label, maxLines=1)}, colors=NavigationBarItemDefaults.colors(selectedIconColor=Cyan, selectedTextColor=Cyan, indicatorColor=Color(0xFF18384B), unselectedIconColor=Muted, unselectedTextColor=Muted)) }
            }
        }) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        val direction = if (targetState >= initialState) 1 else -1
                        (
                            fadeIn(animationSpec = tween(180)) +
                                slideInVertically(
                                    animationSpec = tween(220),
                                    initialOffsetY = { height -> height / 28 * direction }
                                )
                        ) togetherWith (
                            fadeOut(animationSpec = tween(130)) +
                                androidx.compose.animation.slideOutVertically(
                                    animationSpec = tween(180),
                                    targetOffsetY = { height -> -height / 32 * direction }
                                )
                        )
                    },
                    label = "transicion_secciones"
                ) { tab ->
                    when(tab) {
                        0 -> HomeScreen(reports, onNew={editing=null;showForm=true}, onReports={selectedTab=1}, onAnalysis={selectedTab=2}, onExport={selectedTab=3}, onSettings={selectedTab=4})
                        1 -> ReportsScreen(reports, query, {query=it}, filter, {filter=it}, onNew={editing=null;showForm=true}, onOpen={editing=it;showForm=true}, onStatus={r,s->
                            val now = System.currentTimeMillis()
                            persist(reports.map { item ->
                                if (item.id != r.id) item else if (s == "Resuelta" && item.status != "Resuelta") {
                                    item.copy(status=s, closedAt=now, minutes=((now-item.date).coerceAtLeast(0L)/60000L).toInt())
                                } else if (s != "Resuelta" && item.status == "Resuelta") {
                                    item.copy(status=s, closedAt=0L, minutes=0)
                                } else item.copy(status=s)
                            })
                        }, onDelete={r->persist(reports.filterNot{it.id==r.id})})
                        2 -> AnalysisScreen(reports)
                        3 -> ExportScreen(reports, onExportCsv={ exportCsvLauncher.launch("Nexora_Reportes.csv") })
                        else -> SettingsScreen(reports.size,
                            onExportCsv={ exportCsvLauncher.launch("Nexora_Reportes.csv") },
                            onExportBackup={ exportBackupLauncher.launch("Nexora_Copia_Seguridad.json") },
                            onImportBackup={ importBackupLauncher.launch(arrayOf("application/json", "text/*")) },
                            onImportCsv={ importCsvLauncher.launch(arrayOf("text/*", "application/vnd.ms-excel", "application/csv")) },
                            onClear={persist(emptyList())})
                    }
                }
                transferMessage?.let { msg -> AlertDialog(onDismissRequest={transferMessage=null}, title={Text("Transferencia de datos")}, text={Text(msg)}, confirmButton={TextButton(onClick={transferMessage=null}){Text("Aceptar")}}) }
                pendingBackupImport?.let { imported ->
                    AlertDialog(
                        onDismissRequest = { pendingBackupImport = null },
                        title = { Text("¿Restaurar esta copia?") },
                        text = { Text("La copia contiene ${imported.size} reportes. Al restaurarla, reemplazará los ${reports.size} reportes actuales. Si quieres conservarlos, cancela y exporta una copia primero.") },
                        confirmButton = {
                            TextButton(onClick = {
                                persist(imported)
                                pendingBackupImport = null
                                transferMessage = "Copia restaurada: ${imported.size} reportes."
                            }) { Text("Restaurar", color = Color(0xFFFF8796)) }
                        },
                        dismissButton = { TextButton(onClick = { pendingBackupImport = null }) { Text("Cancelar") } }
                    )
                }
                if(showForm) ReportForm(editing, onDismiss={showForm=false}, onSave={r -> persist(if(reports.any{it.id==r.id}) reports.map{if(it.id==r.id)r else it} else reports+r);showForm=false})
            }
        }
    }
}

@Composable private fun PageHeader(kicker:String, title:String, subtitle:String?=null) {
    Column(Modifier.fillMaxWidth().padding(bottom=18.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(40.dp).background(
                Brush.linearGradient(listOf(Color(0xFF00D9F5), Color(0xFF0878D9), Color(0xFF152E72))),
                RoundedCornerShape(13.dp)
            ), contentAlignment=Alignment.Center) {
                Icon(Icons.Default.Computer, null, tint=Color.White, modifier=Modifier.size(25.dp))
                Icon(Icons.Default.Build, null, tint=Cyan, modifier=Modifier.size(12.dp).align(Alignment.BottomEnd).offset(x=2.dp, y=2.dp))
            }
            Column { Text("NEXORA", style=MaterialTheme.typography.labelSmall, color=Cyan, fontWeight=FontWeight.ExtraBold, letterSpacing=1.2.sp); Text(kicker.uppercase(), style=MaterialTheme.typography.labelSmall, color=Muted) }
        }
        Spacer(Modifier.height(18.dp)); Text(title, style=MaterialTheme.typography.headlineMedium, fontWeight=FontWeight.Bold, color=Color.White)
        if(subtitle!=null) { Spacer(Modifier.height(4.dp)); Text(subtitle, color=Muted, style=MaterialTheme.typography.bodyMedium) }
    }
}
@Composable private fun AppCard(modifier:Modifier=Modifier, content:@Composable ColumnScope.()->Unit) { Column(
    modifier.fillMaxWidth()
        .background(Panel, RoundedCornerShape(20.dp))
        .padding(1.dp)
        .background(Brush.linearGradient(listOf(Color(0xFF23465F), Color(0xFF13263B))), RoundedCornerShape(19.dp))
        .padding(15.dp),
    content=content
) }
@Composable private fun SectionTitle(title:String, action:String?=null, onAction:(()->Unit)?=null) { Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.SpaceBetween) { Text(title, style=MaterialTheme.typography.titleMedium, fontWeight=FontWeight.SemiBold); if(action!=null) Text(action, color=Cyan, modifier=Modifier.clickable{onAction?.invoke()}, style=MaterialTheme.typography.labelLarge) } }
@Composable private fun StatCard(label:String, value:String, icon:ImageVector, tint:Color, modifier:Modifier=Modifier) { Column(modifier.background(Panel, RoundedCornerShape(18.dp)).padding(14.dp)) { Row(verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.SpaceBetween, modifier=Modifier.fillMaxWidth()) { Text(label, color=Muted, style=MaterialTheme.typography.labelMedium); Icon(icon,null,tint=tint,modifier=Modifier.size(19.dp)) }; Spacer(Modifier.height(12.dp)); Text(value, style=MaterialTheme.typography.headlineMedium, fontWeight=FontWeight.Bold); Spacer(Modifier.height(3.dp)); Box(Modifier.height(3.dp).fillMaxWidth().background(tint.copy(alpha=.25f), CircleShape)) { Box(Modifier.fillMaxWidth(.62f).fillMaxHeight().background(tint, CircleShape)) } } }
@Composable private fun HomeScreen(
    reports: List<Report>,
    onNew: () -> Unit,
    onReports: () -> Unit,
    onAnalysis: () -> Unit,
    onExport: () -> Unit,
    onSettings: () -> Unit
) {
    val total = reports.size
    val pending = reports.count { it.status.equals("Pendiente", true) || it.status.equals("Pendientes", true) }
    val inProgress = reports.count { it.status.equals("En proceso", true) || it.status.equals("En progreso", true) }
    val resolved = reports.count { it.status.equals("Resuelta", true) || it.status.equals("Resuelto", true) }
    var showNotifications by remember { mutableStateOf(false) }

    if (showNotifications) {
        AlertDialog(
            onDismissRequest = { showNotifications = false },
            containerColor = Panel,
            title = { Text("Notificaciones", color = Color.White) },
            text = {
                Text(
                    if (pending + inProgress == 0) "No tienes reportes pendientes. Todo está al día."
                    else "Tienes $pending reportes pendientes y $inProgress en proceso.",
                    color = Muted
                )
            },
            confirmButton = { TextButton(onClick = { showNotifications = false }) { Text("Aceptar", color = Cyan) } }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF06152A), Color(0xFF07182B), Navy))
        ).padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Abrir ajustes", tint = Color.White, modifier = Modifier.size(27.dp))
                }
                Box {
                    IconButton(onClick = { showNotifications = true }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = Color(0xFF9FEFFF), modifier = Modifier.size(27.dp))
                    }
                    if (pending + inProgress > 0) {
                        Box(Modifier.align(Alignment.TopEnd).padding(top = 7.dp, end = 7.dp).size(9.dp).background(Color(0xFFFF4F65), CircleShape))
                    }
                }
            }
            Column(Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)) {
                Text("Hola, Usuario", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(5.dp))
                Text("Nexora · Gestión de soporte técnico", fontSize = 15.sp, color = Color(0xFFB9CDE3))
            }
        }
        item {
            Image(
                painter = painterResource(id = R.drawable.nexora_banner),
                contentDescription = "Nexora: gestión inteligente de soporte técnico. Cada incidencia bajo control.",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(172.dp).background(Panel, RoundedCornerShape(20.dp))
            )
        }
        item {
            Column(
                Modifier.fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFF103B68), Color(0xFF0C294A), Color(0xFF102D48))), RoundedCornerShape(26.dp))
                    .padding(15.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Color(0xFF123F6A), Color(0xFF0A3155), Color(0xFF12456A))), RoundedCornerShape(19.dp)).padding(horizontal = 16.dp, vertical = 17.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Total reportes", color = Color(0xFFD7E9FB), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text(total.toString(), color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(Modifier.size(58.dp).background(Brush.radialGradient(listOf(Color(0xFF00DFFF), Color(0xFF0876BA), Color(0x00105A8A))), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Assignment, contentDescription = "Total de reportes", tint = Cyan, modifier = Modifier.size(33.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    HomeStatusPill("Pendientes", pending, Color(0xFFFFB13B), Modifier.weight(1f), Icons.Default.Schedule)
                    HomeStatusPill("En proceso", inProgress, Color(0xFF22D9F2), Modifier.weight(1f), Icons.Default.Sync)
                    HomeStatusPill("Resueltos", resolved, Color(0xFF47E49A), Modifier.weight(1f), Icons.Default.CheckCircle)
                }
                Spacer(Modifier.height(9.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    HomeStatusPill("Informes", total, Color(0xFFB7C8DF), Modifier.weight(1f), Icons.Default.Assessment, onClick = onExport)
                    HomeStatusPill("Urgentes", reports.count { it.priority.equals("Urgente", true) && !it.status.equals("Resuelta", true) }, Color(0xFFFF7187), Modifier.weight(1f), Icons.Default.Warning, onClick = onReports)
                }
            }
        }
        item {
            Text("Accesos rápidos", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(top = 3.dp, bottom = 0.dp))
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                HomeQuickAction("Nuevo reporte", Icons.Default.NoteAdd, Color(0xFF12D6F2), Modifier.weight(1f), onNew)
                HomeQuickAction("Reportes", Icons.Default.Assignment, Color(0xFF28CFF2), Modifier.weight(1f), onReports)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                HomeQuickAction("Análisis", Icons.Default.QueryStats, Color(0xFF23D2F0), Modifier.weight(1f), onAnalysis)
                HomeQuickAction("Informes", Icons.Default.Assessment, Color(0xFF28CFF2), Modifier.weight(1f), onExport)
            }
        }
    }
}

@Composable private fun HomeStatusPill(
    label: String,
    value: Int,
    tint: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .heightIn(min = 70.dp)
            .background(tint.copy(alpha = 0.11f), RoundedCornerShape(17.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 8.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(15.dp))
                Text(label, color = tint, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(4.dp))
            Text(value.toString(), color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable private fun HomeQuickAction(
    label: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.height(148.dp)
            .background(Brush.linearGradient(listOf(Color(0xFF112C48), Color(0xFF0D233B))), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(Modifier.size(66.dp).background(Brush.radialGradient(listOf(tint.copy(alpha = 0.30f), Color.Transparent)), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(42.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
private val BrushLike = Brush.linearGradient(
    colors = listOf(Color(0xFF123B58), Color(0xFF0D263F), Color(0xFF151E42))
)

@Composable private fun ReportsScreen(reports:List<Report>, query:String, onQuery:(String)->Unit, filter:String, onFilter:(String)->Unit, onNew:()->Unit, onOpen:(Report)->Unit, onStatus:(Report,String)->Unit, onDelete:(Report)->Unit) {
    val filtered=reports.filter { (query.isBlank() || listOf(it.person,it.department,it.software,it.problem,it.id.toString(),reportCode(it.id)).any{v->v.contains(query,true)}) && (filter=="Todos" || it.status==filter) }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal=18.dp), contentPadding=PaddingValues(top=20.dp,bottom=24.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item { PageHeader("Gestión", "Reportes", "Busca, filtra y da seguimiento a cada caso.") }
        item { OutlinedTextField(query,onQuery,Modifier.fillMaxWidth(),placeholder={Text("Buscar por software, persona, ID…")},leadingIcon={Icon(Icons.Default.Search,null)},singleLine=true,shape=RoundedCornerShape(16.dp),colors=fieldColors()) }
        item { Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement=Arrangement.spacedBy(7.dp)) { listOf("Todos","Pendiente","En proceso","Resuelta").forEach { f -> FilterChip(selected=filter==f,onClick={onFilter(f)},label={Text(f)},colors=FilterChipDefaults.filterChipColors(selectedContainerColor=Color(0xFF17394A),selectedLabelColor=Cyan)) } } }
        item { Row(verticalAlignment=Alignment.CenterVertically) { Text("${filtered.size} reportes", color=Muted); Spacer(Modifier.weight(1f)); Button(onClick=onNew, colors=ButtonDefaults.buttonColors(containerColor=Cyan,contentColor=Navy),shape=RoundedCornerShape(12.dp)) { Icon(Icons.Default.Add,null); Text("Nuevo") } } }
        if(filtered.isEmpty()) item { EmptyState("No hay coincidencias", "Prueba otra búsqueda o registra una incidencia.", Icons.Default.SearchOff) }
        items(filtered,key={it.id}) { r -> Column { ReportRow(r, onClick={onOpen(r)}); Spacer(Modifier.height(7.dp)); Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End, verticalAlignment=Alignment.CenterVertically) { var expanded by remember(r.id) { mutableStateOf(false) }; TextButton(onClick={expanded=true}) { Icon(Icons.Default.Sync,null); Text("Cambiar estado") }; DropdownMenu(expanded=expanded,onDismissRequest={expanded=false}) { listOf("Pendiente","En proceso","Resuelta").forEach { s-> DropdownMenuItem(text={Text(s)},onClick={onStatus(r,s);expanded=false}) } }; TextButton(onClick={onOpen(r)}) { Icon(Icons.Default.Edit,null); Text("Editar") }; TextButton(onClick={onDelete(r)}) { Icon(Icons.Default.DeleteOutline,null,tint=Color(0xFFFF7C8C)); Text("Eliminar",color=Color(0xFFFF7C8C)) } } } }
    }
}
@Composable private fun ReportRow(r:Report,onClick:()->Unit) {
    val tint=when(r.status){"Resuelta"->Green;"En proceso"->Blue;else->Orange}
    AppCard(Modifier.clickable{onClick()}) {
        Row(verticalAlignment=Alignment.Top) { Box(Modifier.size(42.dp).background(Color(0xFF1A3046),RoundedCornerShape(13.dp)),contentAlignment=Alignment.Center) { Icon(Icons.Default.Assignment, null, tint=Cyan) }; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(r.problem.ifBlank{r.type},fontWeight=FontWeight.SemiBold,maxLines=1,overflow=TextOverflow.Ellipsis); Text("${r.software.ifBlank{"Sin software"}} · ${r.department.ifBlank{"Sin departamento"}}",color=Muted,style=MaterialTheme.typography.bodySmall,maxLines=1,overflow=TextOverflow.Ellipsis); Spacer(Modifier.height(8.dp)); Row(verticalAlignment=Alignment.CenterVertically) { Text(reportCode(r.id),color=Muted,style=MaterialTheme.typography.labelSmall); Spacer(Modifier.width(8.dp)); Text(r.priority.uppercase(),color=if(r.priority=="Urgente")Color(0xFFFF7C8C) else Muted,style=MaterialTheme.typography.labelSmall); Spacer(Modifier.weight(1f)); Box(Modifier.background(tint.copy(alpha=.13f),RoundedCornerShape(7.dp)).padding(horizontal=8.dp, vertical=4.dp)) { Text(r.status.uppercase(),color=tint,style=MaterialTheme.typography.labelSmall,fontWeight=FontWeight.Bold) } } } }
    }
}
@Composable private fun AnalysisScreen(reports: List<Report>) {
    var period by remember { mutableStateOf("1 mes") }
    var periodMenuExpanded by remember { mutableStateOf(false) }
    var comparePrevious by remember { mutableStateOf(true) }
    val now = System.currentTimeMillis()
    val periodOptions = listOf("1 mes", "2 meses", "3 meses", "6 meses", "1 año", "Todo")
    fun subtractMonths(timestamp: Long, months: Int): Long = java.util.Calendar.getInstance().run {
        timeInMillis = timestamp
        add(java.util.Calendar.MONTH, -months)
        timeInMillis
    }
    val periodMonths = when (period) { "1 mes" -> 1; "2 meses" -> 2; "3 meses" -> 3; "6 meses" -> 6; "1 año" -> 12; else -> 0 }
    val periodStart = if (periodMonths > 0) subtractMonths(now, periodMonths) else Long.MIN_VALUE
    val previousStart = if (periodMonths > 0) subtractMonths(periodStart, periodMonths) else Long.MIN_VALUE
    // Para resueltos se usa la fecha de cierre; para abiertos, la fecha de apertura.
    // Así un caso abierto hace 45 días y cerrado ayer aparece en el período reciente.
    fun activityTimestamp(report: Report): Long = if (report.status == "Resuelta" && report.closedAt > 0L) report.closedAt else report.date
    val visibleReports = if (periodMonths > 0) reports.filter { activityTimestamp(it) >= periodStart && activityTimestamp(it) <= now } else reports
    val previousReports = if (periodMonths > 0) reports.filter { activityTimestamp(it) >= previousStart && activityTimestamp(it) < periodStart } else emptyList()
    val total = visibleReports.size
    val resolvedReports = visibleReports.filter { it.status == "Resuelta" }
    val pending = visibleReports.count { it.status == "Pendiente" }
    val inProgress = visibleReports.count { it.status == "En proceso" }
    val resolved = resolvedReports.size
    val resolutionRate = if (total == 0) 0 else (resolved * 100f / total).toInt()
    val durations = resolvedReports.map { r ->
        if (r.closedAt > r.date) ((r.closedAt - r.date) / 60000L).toInt().coerceAtLeast(0) else r.minutes.coerceAtLeast(0)
    }
    val avgMinutes = if (durations.isEmpty()) 0 else durations.average().toInt()
    val avgHours = avgMinutes / 60
    val avgRemainder = avgMinutes % 60
    val bySoftware = visibleReports.filter { it.software.isNotBlank() && it.software != "Otro" }
        .groupingBy { it.software }.eachCount().entries.sortedByDescending { it.value }.take(5)
    val byDepartment = visibleReports.filter { it.department.isNotBlank() }
        .groupingBy { it.department }.eachCount().entries.sortedByDescending { it.value }.take(5)
    val byTechnician = resolvedReports.filter { it.technician.isNotBlank() }
        .groupingBy { it.technician }.eachCount().entries.sortedByDescending { it.value }.take(5)
    val byPriority = visibleReports.filter { it.priority.isNotBlank() }
        .groupingBy { it.priority }.eachCount().entries.sortedByDescending { it.value }
    val oldestOpenHours = visibleReports.filter { it.status != "Resuelta" }
        .maxOfOrNull { ((now - it.date).coerceAtLeast(0L) / 3600000L) } ?: 0L

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 18.dp), contentPadding = PaddingValues(top = 20.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { PageHeader("Inteligencia operativa", "Análisis", "Indicadores reales calculados a partir de tus reportes.") }
        item {
            AppCard {
                Text("Período de análisis", color = Muted, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                Box {
                    OutlinedButton(onClick = { periodMenuExpanded = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.CalendarMonth, null, tint = Cyan)
                        Spacer(Modifier.width(8.dp))
                        Text(if (period == "Todo") "Todo el historial" else "Últimos $period", modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ExpandMore, null)
                    }
                    DropdownMenu(expanded = periodMenuExpanded, onDismissRequest = { periodMenuExpanded = false }) {
                        periodOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(if (option == "Todo") "Todo el historial" else "Últimos $option") },
                                leadingIcon = { if (period == option) Icon(Icons.Default.Check, null, tint = Cyan) },
                                onClick = { period = option; periodMenuExpanded = false; if (option == "Todo") comparePrevious = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(if (periodMonths > 0) "Período actual: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(periodStart))} – ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(now))}" else "Incluye todo el historial disponible", color = Muted, style = MaterialTheme.typography.bodySmall)
                if (periodMonths > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = comparePrevious, onCheckedChange = { comparePrevious = it }, colors = CheckboxDefaults.colors(checkedColor = Cyan))
                        Column(Modifier.weight(1f)) {
                            Text("Comparar con el período anterior", color = Color.White)
                            Text("Misma duración inmediatamente anterior", color = Muted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        if (periodMonths > 0 && comparePrevious) {
            val previousResolved = previousReports.count { it.status == "Resuelta" }
            val currentDelta = total - previousReports.size
            val resolvedDelta = resolved - previousResolved
            item {
                AppCard {
                    SectionTitle("Comparación con el período anterior")
                    Text("Actual: ${visibleReports.size} reportes · Anterior: ${previousReports.size} reportes", color = Muted, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text("Variación de reportes", color = Muted, style = MaterialTheme.typography.labelSmall)
                            Text(deltaLabel(currentDelta), color = if (currentDelta > 0) Orange else if (currentDelta < 0) Green else Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(percentDelta(visibleReports.size, previousReports.size), color = Muted, style = MaterialTheme.typography.bodySmall)
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Variación de resueltos", color = Muted, style = MaterialTheme.typography.labelSmall)
                            Text(deltaLabel(resolvedDelta), color = if (resolvedDelta > 0) Green else if (resolvedDelta < 0) Orange else Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(percentDelta(resolved, previousResolved), color = Muted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Text("Los reportes resueltos se agrupan por fecha de cierre; los que siguen abiertos, por fecha de apertura.", color = Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Reportes", total.toString(), Icons.Default.Dataset, Cyan, Modifier.weight(1f))
                StatCard("Resueltos", resolved.toString(), Icons.Default.CheckCircle, Green, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Pendientes", pending.toString(), Icons.Default.Schedule, Orange, Modifier.weight(1f))
                StatCard("En proceso", inProgress.toString(), Icons.Default.Autorenew, Blue, Modifier.weight(1f))
            }
        }
        item {
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).background(Cyan.copy(alpha = .12f), RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Speed, null, tint = Cyan, modifier = Modifier.size(27.dp))
                    }
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text("TIEMPO MEDIO DE RESOLUCIÓN", color = Muted, style = MaterialTheme.typography.labelSmall)
                        Text(if (durations.isEmpty()) "—" else if (avgHours > 0) "${avgHours} h ${avgRemainder} min" else "$avgMinutes min", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Calculado desde la apertura hasta el cierre", color = Muted, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Tasa de resolución", color = Muted, modifier = Modifier.weight(1f))
                    Text("$resolutionRate%", color = Green, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(7.dp))
                Box(Modifier.fillMaxWidth().height(9.dp).background(Color(0xFF25374B), CircleShape)) {
                    Box(Modifier.fillMaxWidth(resolutionRate / 100f).fillMaxHeight().background(Brush.horizontalGradient(listOf(Cyan, Green)), CircleShape))
                }
                Spacer(Modifier.height(8.dp))
                Text("${resolved} de ${total} reportes resueltos en el período seleccionado", color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            AppCard {
                SectionTitle("Estado de los reportes")
                Spacer(Modifier.height(12.dp))
                if (total == 0) Text("No hay reportes en este período.", color = Muted)
                else {
                    BarLine("Resueltos", resolved, total, Green)
                    BarLine("En proceso", inProgress, total, Blue)
                    BarLine("Pendientes", pending, total, Orange)
                }
            }
        }
        item {
            AppCard {
                SectionTitle("Incidencias por departamento")
                Spacer(Modifier.height(12.dp))
                if (byDepartment.isEmpty()) Text("Completa el departamento o unidad comercial en los reportes para ver dónde se concentran las incidencias.", color = Muted)
                else byDepartment.forEach { (name, count) -> BarLine(name, count, total, Cyan) }
            }
        }
        item {
            AppCard {
                SectionTitle("Incidencias por software o equipo")
                Spacer(Modifier.height(12.dp))
                if (bySoftware.isEmpty()) Text("Aún no hay datos de software o equipo suficientes.", color = Muted)
                else bySoftware.forEach { (name, count) -> BarLine(name, count, total, Violet) }
            }
        }
        item {
            AppCard {
                SectionTitle("Carga por prioridad")
                Spacer(Modifier.height(12.dp))
                if (byPriority.isEmpty()) Text("No hay prioridades registradas.", color = Muted)
                else byPriority.forEach { (name, count) -> BarLine(name, count, total, if (name.equals("Urgente", true) || name.equals("Alta", true)) Color(0xFFFF7C8C) else Orange) }
            }
        }
        item {
            AppCard {
                SectionTitle("Resoluciones por técnico")
                Spacer(Modifier.height(5.dp))
                Text("Cantidad de reportes cerrados por cada técnico en el período.", color = Muted, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(12.dp))
                if (byTechnician.isEmpty()) Text("Los resultados aparecerán cuando haya reportes resueltos con técnico responsable asignado.", color = Muted)
                else byTechnician.forEach { (name, count) -> BarLine(name, count, (byTechnician.maxOfOrNull { it.value } ?: 1).coerceAtLeast(1), Green) }
            }
        }
        item {
            AppCard {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Lightbulb, null, tint = Orange, modifier = Modifier.size(25.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Lectura de los datos", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text(when {
                            total == 0 -> "No hay reportes en el período seleccionado. Cambia el filtro o registra una incidencia para comenzar el análisis."
                            resolved == 0 -> "Todavía no hay reportes cerrados en este período. Al resolverlos se calculará el tiempo medio de solución."
                            pending + inProgress > 0 -> "Se ha resuelto el $resolutionRate% de los reportes. Hay ${pending + inProgress} casos abiertos; el más antiguo lleva aproximadamente ${oldestOpenHours} horas sin cerrar. Prioriza los casos urgentes y los de mayor antigüedad."
                            byDepartment.isNotEmpty() -> "Todos los reportes del período están resueltos. El departamento con más incidencias es ${byDepartment.first().key} (${byDepartment.first().value})."
                            else -> "Todos los reportes del período están resueltos. Continúa registrando departamento y técnico para obtener comparativas más completas."
                        }, color = Muted, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        item { Text("Nota: las estadísticas usan los datos guardados localmente en este teléfono. El tiempo medio se calcula solo con reportes resueltos.", color = Muted, style = MaterialTheme.typography.bodySmall) }
    }
}
private fun deltaLabel(value: Int): String = when { value > 0 -> "+$value"; else -> value.toString() }
private fun percentDelta(current: Int, previous: Int): String = when {
    previous == 0 && current == 0 -> "0% frente al período anterior"
    previous == 0 -> "Nuevo: sin registros en el período anterior"
    else -> {
        val pct = ((current - previous) * 100.0 / previous)
        val formatted = String.format(Locale.getDefault(), "%.1f", kotlin.math.abs(pct))
        val sign = if (pct > 0) "+" else if (pct < 0) "−" else ""
        "$sign$formatted% frente al período anterior"
    }
}

@Composable private fun BarLine(label:String,count:Int,total:Int,color:Color) { Column(Modifier.padding(bottom=12.dp)) { Row(verticalAlignment=Alignment.CenterVertically) { Text(label,Modifier.weight(1f),maxLines=1,overflow=TextOverflow.Ellipsis); Text(count.toString(),color=color,fontWeight=FontWeight.Bold) }; Spacer(Modifier.height(6.dp)); Box(Modifier.fillMaxWidth().height(7.dp).background(Color(0xFF25374B),CircleShape)) { Box(Modifier.fillMaxWidth(if(total==0)0f else (count.toFloat()/total).coerceIn(.02f,1f)).fillMaxHeight().background(color,CircleShape)) } } }

@Composable private fun ExportScreen(reports:List<Report>, onExportCsv: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal=18.dp),contentPadding=PaddingValues(top=20.dp,bottom=24.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
        item { PageHeader("Documentación", "Informes", "Prepara resúmenes exportables de la actividad.") }
        item { AppCard { Icon(Icons.Default.TableChart,null,tint=Cyan,modifier=Modifier.size(32.dp)); Spacer(Modifier.height(10.dp)); Text("Exportar registros CSV",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold); Spacer(Modifier.height(5.dp)); Text("Abre los datos en una hoja de cálculo para archivarlos o crear informes propios.",color=Muted); Spacer(Modifier.height(16.dp)); Button(onClick=onExportCsv,modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Cyan,contentColor=Navy),shape=RoundedCornerShape(12.dp)) { Icon(Icons.Default.FileDownload,null); Spacer(Modifier.width(8.dp)); Text("Guardar archivo CSV") } } }
        item { AppCard { SectionTitle("Resumen actual"); Spacer(Modifier.height(12.dp)); SummaryLine("Total de reportes",reports.size.toString()); SummaryLine("Pendientes",reports.count{it.status=="Pendiente"}.toString()); SummaryLine("En proceso",reports.count{it.status=="En proceso"}.toString()); SummaryLine("Resueltos",reports.count{it.status=="Resuelta"}.toString()) } }
        item { Text("El archivo CSV incluye los datos de cada incidencia. Los informes PDF y gráficos avanzados pueden incorporarse en una siguiente versión.",color=Muted,style=MaterialTheme.typography.bodySmall) }
    }
}
@Composable private fun SummaryLine(label:String,value:String) { Row(Modifier.fillMaxWidth().padding(vertical=7.dp),horizontalArrangement=Arrangement.SpaceBetween) { Text(label,color=Muted); Text(value,fontWeight=FontWeight.SemiBold) } }
private fun csvEscape(value: String): String = "\"${value.replace("\"", "\"\"").replace("\r", " ").replace("\n", " ")}\""
private fun reportsToCsv(reports: List<Report>): String {
    val header = listOf("id","fecha","persona","departamento","software","prioridad","problema","diagnostico","solucion","minutos","estado","tecnico","notas")
    val rows = reports.map { r -> listOf(r.id.toString(), SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.getDefault()).format(Date(r.date)),r.person,r.department,r.software,r.priority,r.problem,r.diagnosis,r.solution,r.minutes.toString(),r.status,r.technician,r.notes) }
    return (listOf(header) + rows).joinToString("\r\n") { row -> row.joinToString(";") { csvEscape(it) } }
}
private fun csvToReports(csv: String): List<Report> {
    val lines = csv.lineSequence().filter { it.isNotBlank() }.toList()
    if (lines.size < 2) return emptyList()
    fun parse(line: String): List<String> {
        val out = mutableListOf<String>(); val cell = StringBuilder(); var quoted = false; var i = 0
        while (i < line.length) { val c = line[i]; if (c == '\"') { if (quoted && i + 1 < line.length && line[i+1] == '\"') { cell.append('\"'); i++ } else quoted = !quoted } else if ((c == ';' || c == ',') && !quoted) { out.add(cell.toString()); cell.setLength(0) } else cell.append(c); i++ }; out.add(cell.toString()); return out
    }
    val header = parse(lines.first()).map { it.trim().lowercase(Locale.ROOT) }
    fun value(row: List<String>, name: String) = row.getOrNull(header.indexOf(name)).orEmpty()
    return lines.drop(1).mapNotNull { line -> runCatching { val r=parse(line); Report(person=value(r,"persona"),department=value(r,"departamento"),software=value(r,"software"),type=value(r,"tipo"),priority=value(r,"prioridad").ifBlank { "Normal" },problem=value(r,"problema"),diagnosis=value(r,"diagnostico"),solution=value(r,"solucion"),minutes=value(r,"minutos").toIntOrNull() ?: 0,status=value(r,"estado").ifBlank { "Pendiente" },technician=value(r,"tecnico"),notes=value(r,"notas")) }.getOrNull() }.filter { it.person.isNotBlank() || it.problem.isNotBlank() }
}

@Composable private fun SettingsScreen(count:Int,onExportCsv:()->Unit,onExportBackup:()->Unit,onImportBackup:()->Unit,onImportCsv:()->Unit,onClear:()->Unit) {
    var confirm by remember { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal=18.dp),contentPadding=PaddingValues(top=20.dp,bottom=24.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
        item { PageHeader("Centro de control", "Configuración", "Administra tus datos, copias de seguridad y preferencias.") }
        item { AppCard { Row(verticalAlignment=Alignment.CenterVertically) { Box(Modifier.size(48.dp).background(Brush.linearGradient(listOf(Cyan,Blue)),RoundedCornerShape(14.dp)),contentAlignment=Alignment.Center) { Icon(Icons.Default.Storage,null,tint=Navy,modifier=Modifier.size(27.dp)) }; Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { Text("Base de datos local",fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleMedium); Text("$count reportes en este dispositivo",color=Muted,style=MaterialTheme.typography.bodySmall); Text("Los datos permanecen en tu teléfono hasta que los exportes.",color=Muted,style=MaterialTheme.typography.bodySmall) } } } }
        item { Text("COPIAS DE SEGURIDAD",color=Cyan,style=MaterialTheme.typography.labelMedium,fontWeight=FontWeight.Bold) }
        item { AppCard { SettingsAction(Icons.Default.Backup,"Exportar copia completa","JSON · conserva todos los campos para restaurar",onExportBackup); Divider(color=Color(0xFF26374A)); SettingsAction(Icons.Default.Restore,"Importar / restaurar copia","Recupera una copia JSON exportada por la app",onImportBackup) } }
        item { Text("EXCEL Y HOJAS DE CÁLCULO",color=Cyan,style=MaterialTheme.typography.labelMedium,fontWeight=FontWeight.Bold) }
        item { AppCard { SettingsAction(Icons.Default.TableView,"Exportar para Excel","CSV UTF-8 con separador compatible con Excel",onExportCsv); Divider(color=Color(0xFF26374A)); SettingsAction(Icons.Default.FileOpen,"Importar desde CSV","Añade reportes desde un CSV con columnas exportadas por la app",onImportCsv); Spacer(Modifier.height(6.dp)); Text("Consejo: para Excel, abre el CSV usando UTF-8. La importación espera las columnas generadas por Nexora.",color=Muted,style=MaterialTheme.typography.bodySmall) } }
        item { AppCard {
            SectionTitle("Recordatorios automáticos")
            Spacer(Modifier.height(8.dp))
            Text("Nexora envía avisos al teléfono para revisar los reportes.", color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text("Lunes a sábado · 8:30 a. m. a 4:30 p. m. · cada 2 horas.", color = Muted, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(4.dp))
            Text("En Android 13 o posterior, permite las notificaciones cuando el sistema lo solicite. El ahorro de batería puede retrasar la ejecución.", color = Muted, style = MaterialTheme.typography.bodySmall)
        } }
        item { AppCard { SectionTitle("Acerca de la aplicación"); Spacer(Modifier.height(8.dp)); Text("NEXORA",fontWeight=FontWeight.SemiBold); Text("Versión 2.0 Beta 07 · Cada incidencia bajo control",color=Muted,style=MaterialTheme.typography.bodySmall) } }
        item { AppCard { Text("Zona de mantenimiento",color=Color(0xFFFF8796),fontWeight=FontWeight.SemiBold); Spacer(Modifier.height(5.dp)); Text("Eliminar todos los registros es irreversible. Exporta una copia antes de continuar.",color=Muted,style=MaterialTheme.typography.bodySmall); Spacer(Modifier.height(12.dp)); OutlinedButton(onClick={confirm=true},colors=ButtonDefaults.outlinedButtonColors(contentColor=Color(0xFFFF8796)),modifier=Modifier.fillMaxWidth()) { Icon(Icons.Default.DeleteForever,null); Spacer(Modifier.width(8.dp)); Text("Borrar todos los reportes") } } }
    }
    if(confirm) AlertDialog(onDismissRequest={confirm=false},title={Text("¿Eliminar todos los reportes?")},text={Text("Esta acción no se puede deshacer.")},confirmButton={TextButton(onClick={onClear();confirm=false}){Text("Eliminar",color=Color(0xFFFF8796))}},dismissButton={TextButton(onClick={confirm=false}){Text("Cancelar")}})
}
@Composable private fun SettingsAction(icon:ImageVector,title:String,subtitle:String,onClick:()->Unit) { Row(Modifier.fillMaxWidth().clickable{onClick()}.padding(vertical=12.dp),verticalAlignment=Alignment.CenterVertically) { Icon(icon,null,tint=Cyan); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(title,fontWeight=FontWeight.Medium); Text(subtitle,color=Muted,style=MaterialTheme.typography.bodySmall) }; Icon(Icons.Default.ChevronRight,null,tint=Muted) } }

@Composable private fun ReportForm(existing: Report?, onDismiss: () -> Unit, onSave: (Report) -> Unit) {
    val draftId = remember(existing?.id) { existing?.id ?: System.currentTimeMillis() }
    var person by remember { mutableStateOf(existing?.person.orEmpty()) }
    var department by remember { mutableStateOf(existing?.department.orEmpty()) }
    val softwareTypes = listOf("Windows", "Microsoft Office", "Golden", "Mercurio", "SQL", "Antivirus", "Navegador", "Impresora", "Otro")
    val initialSoftware = existing?.software.orEmpty()
    var software by remember { mutableStateOf(when { existing == null -> "Windows"; initialSoftware in softwareTypes -> initialSoftware; initialSoftware.isBlank() -> ""; else -> "Otro" }) }
    var customSoftware by remember { mutableStateOf(if (initialSoftware.isNotBlank() && initialSoftware !in softwareTypes) initialSoftware else "") }
    var type by remember { mutableStateOf(existing?.type ?: "Instalación") }
    var customType by remember { mutableStateOf(existing?.type?.takeUnless { it in listOf("Instalación", "Revisión", "Solución", "Mantenimiento", "Redes", "Hardware", "Otro") }.orEmpty()) }
    var priority by remember { mutableStateOf(existing?.priority ?: "Normal") }
    var problem by remember { mutableStateOf(existing?.problem.orEmpty()) }
    var diagnosis by remember { mutableStateOf(existing?.diagnosis.orEmpty()) }
    var solution by remember { mutableStateOf(existing?.solution.orEmpty()) }
    var technician by remember { mutableStateOf(existing?.technician.orEmpty()) }
    var notes by remember { mutableStateOf(existing?.notes.orEmpty()) }
    var minutes by remember { mutableStateOf(existing?.minutes?.toString() ?: "0") }
    var status by remember { mutableStateOf(existing?.status ?: "Pendiente") }
    var error by remember { mutableStateOf(false) }
    var showWorkDialog by remember { mutableStateOf(false) }
    var softwareExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var advancedExpanded by remember { mutableStateOf(false) }
    val workTypes = listOf("Instalación", "Revisión", "Solución", "Mantenimiento", "Redes", "Hardware", "Otro")
    val dateText = remember(existing?.date) { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(existing?.date ?: System.currentTimeMillis())) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Navy,
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().statusBarsPadding().background(Brush.horizontalGradient(listOf(Color(0xFF0C2947), Color(0xFF10243C)))).padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White) }
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(if (existing == null) "Nuevo Reporte" else "Editar Reporte", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("NEXORA · SOPORTE TÉCNICO", color = Cyan, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
                    }
                    Icon(Icons.Default.Assignment, null, tint = Cyan, modifier = Modifier.size(26.dp))
                }
                Column(
                    Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(4.dp, 24.dp).background(Cyan, RoundedCornerShape(4.dp)))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Información general", color = Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                            Text("Datos principales de la incidencia", color = Muted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    FormFieldLabel("ID del reporte · generado automáticamente")
                    Row(Modifier.fillMaxWidth().background(Panel2, RoundedCornerShape(14.dp)).padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Fingerprint, null, tint = Cyan)
                        Spacer(Modifier.width(12.dp))
                        Text(reportCode(draftId), color = Cyan, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        Text(if (existing == null) "NUEVO" else "REGISTRO", color = Muted, style = MaterialTheme.typography.labelSmall)
                    }
                    FormFieldLabel("Fecha de apertura")
                    Row(Modifier.fillMaxWidth().background(Panel2, RoundedCornerShape(14.dp)).padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, null, tint = Cyan)
                        Spacer(Modifier.width(12.dp))
                        Text(dateText, color = Color.White, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.weight(1f))
                        Text(if (existing == null) "HOY" else "REGISTRADA", color = Muted, style = MaterialTheme.typography.labelSmall)
                    }
                    FormFieldLabel("Software / equipo")
                    Box {
                        Row(Modifier.fillMaxWidth().background(Panel2, RoundedCornerShape(14.dp)).clickable { softwareExpanded = true }.padding(horizontal = 16.dp, vertical = 17.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Computer, null, tint = Cyan)
                            Spacer(Modifier.width(12.dp))
                            Text(if (software == "Otro" && customSoftware.isNotBlank()) customSoftware else software.ifBlank { "Seleccionar software o equipo" }, color = if (software.isBlank()) Muted else Color.White, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Icon(Icons.Default.ExpandMore, null, tint = Muted)
                        }
                        DropdownMenu(expanded = softwareExpanded, onDismissRequest = { softwareExpanded = false }, modifier = Modifier.background(Panel)) {
                            softwareTypes.forEach { option -> DropdownMenuItem(text = { Text(option, color = Color.White) }, onClick = { software = option; softwareExpanded = false }) }
                        }
                    }
                    if (software == "Otro") {
                        OutlinedTextField(
                            value = customSoftware,
                            onValueChange = { customSoftware = it.take(80); error = false },
                            label = { Text("Especifica software o equipo") },
                            placeholder = { Text("Ej. Ubuntu Server, escáner…") },
                            modifier = Modifier.fillMaxWidth(), singleLine = true,
                            shape = RoundedCornerShape(12.dp), colors = fieldColors()
                        )
                        if (error && customSoftware.isBlank()) {
                            Text("Este campo es obligatorio cuando seleccionas Otro.", color = Color(0xFFFF8796), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Surface(color = Panel, shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF213A55))) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EditNote, null, tint = Cyan)
                                Spacer(Modifier.width(8.dp))
                                Text("Descripción del problema o tarea", color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                            OutlinedTextField(
                                value = problem,
                                onValueChange = { problem = it.take(500); error = false },
                                placeholder = { Text("Escribe aquí la descripción…", color = Muted) },
                                modifier = Modifier.fillMaxWidth(), minLines = 4, maxLines = 6,
                                shape = RoundedCornerShape(12.dp), colors = fieldColors()
                            )
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                if (error && problem.isBlank()) Text("La descripción es obligatoria", color = Color(0xFFFF8796), style = MaterialTheme.typography.labelSmall)
                                else Text("Describe el error, solicitud o trabajo realizado", color = Muted, style = MaterialTheme.typography.labelSmall)
                                Text("${problem.length}/500", color = if (problem.length >= 480) Orange else Muted, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    FormFieldLabel("Estado")
                    Box {
                        Row(Modifier.fillMaxWidth().background(Panel2, RoundedCornerShape(14.dp)).clickable { statusExpanded = true }.padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(10.dp).background(when (status) { "Resuelta", "Resuelto" -> Green; "En proceso" -> Cyan; else -> Orange }, CircleShape))
                            Spacer(Modifier.width(12.dp))
                            Text(status, color = Color.White, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                            Icon(Icons.Default.ExpandMore, null, tint = Muted)
                        }
                        DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }, modifier = Modifier.background(Panel)) {
                            listOf("Pendiente", "En proceso", "Resuelta").forEach { option -> DropdownMenuItem(text = { Text(option, color = Color.White) }, onClick = { status = option; statusExpanded = false }) }
                        }
                    }
                    OutlinedTextField(person, { person = it }, label = { Text("Persona / usuario (opcional)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = fieldColors())
                    OutlinedTextField(department, { department = it }, label = { Text("Departamento o Unidad Comercial") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = fieldColors(), leadingIcon = { Icon(Icons.Default.Business, null) })
                    FormFieldLabel("Técnico responsable")
                    var technicianExpanded by remember { mutableStateOf(false) }
                    Box {
                        Row(Modifier.fillMaxWidth().background(Panel2, RoundedCornerShape(14.dp)).clickable { technicianExpanded = true }.padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Engineering, null, tint = Cyan)
                            Spacer(Modifier.width(12.dp))
                            Text(technician.ifBlank { "Seleccionar técnico" }, color = if (technician.isBlank()) Muted else Color.White, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ExpandMore, null, tint = Muted)
                        }
                        DropdownMenu(expanded = technicianExpanded, onDismissRequest = { technicianExpanded = false }, modifier = Modifier.background(Panel)) {
                            listOf("Yasel", "Getma", "Naybet", "Yanelis", "Selene").forEach { name ->
                                DropdownMenuItem(text = { Text(name, color = Color.White) }, onClick = { technician = name; technicianExpanded = false })
                            }
                        }
                    }
                    FormFieldLabel("Prioridad")
                    ChoiceChips(listOf("Baja", "Normal", "Alta", "Urgente"), priority) { priority = it }
                    OutlinedTextField(diagnosis, { diagnosis = it }, label = { Text("Diagnóstico") }, modifier = Modifier.fillMaxWidth(), minLines = 2, shape = RoundedCornerShape(12.dp), colors = fieldColors(), leadingIcon = { Icon(Icons.Default.ManageSearch, null) })
                    OutlinedTextField(notes, { notes = it }, label = { Text("Observaciones") }, modifier = Modifier.fillMaxWidth(), minLines = 2, shape = RoundedCornerShape(12.dp), colors = fieldColors(), leadingIcon = { Icon(Icons.Default.Notes, null) })
                    if (existing != null) {
                        val elapsed = if (existing.status == "Resuelta" && existing.closedAt > 0L) ((existing.closedAt-existing.date).coerceAtLeast(0L)/60000L).toInt() else ((System.currentTimeMillis()-existing.date).coerceAtLeast(0L)/60000L).toInt()
                        Surface(color = Panel, shape = RoundedCornerShape(12.dp)) {
                            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, null, tint = Cyan)
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text("Tiempo transcurrido", color = Color.White, fontWeight = FontWeight.SemiBold)
                                    Text(if (existing.status == "Resuelta") "Tiempo total hasta el cierre" else "Se calculará automáticamente al cerrar el reporte", color = Muted, style = MaterialTheme.typography.bodySmall)
                                }
                                Spacer(Modifier.weight(1f))
                                Text(formatDuration(elapsed), color = Cyan, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }
                Surface(modifier = Modifier.fillMaxWidth().imePadding(), color = Color(0xFF081727), shadowElevation = 12.dp) {
                    Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(0.85f).heightIn(min = 48.dp, max = 54.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2B4560))) {
                            Text("Cancelar", fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = {
                                if (problem.isBlank() || (software == "Otro" && customSoftware.isBlank())) {
                                    error = true
                                } else {
                                    val finalSoftware = if (software == "Otro") customSoftware.trim() else software.trim()
                                    val now = System.currentTimeMillis()
                                    val openedAt = existing?.date ?: now
                                    val closedAt = when {
                                        status != "Resuelta" -> 0L
                                        existing?.status == "Resuelta" && (existing.closedAt > 0L) -> existing.closedAt
                                        else -> now
                                    }
                                    val elapsedMinutes = if (closedAt > 0L) ((closedAt-openedAt).coerceAtLeast(0L)/60000L).toInt() else 0
                                    onSave(Report(
                                        id = draftId, person = person.trim(), department = department.trim(),
                                        software = finalSoftware, type = "", priority = priority, problem = problem.trim(),
                                        diagnosis = diagnosis.trim(), solution = "", minutes = elapsedMinutes,
                                        status = status, date = openedAt, technician = technician.trim(), notes = notes.trim(), closedAt = closedAt
                                    ))
                                }
                            },
                            modifier = Modifier.weight(1.25f).heightIn(min = 48.dp, max = 54.dp), shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Navy)
                        ) {
                            Icon(Icons.Default.Save, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Guardar reporte", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }


}

private fun reportCode(id: Long): String {
    val stamp = SimpleDateFormat("yyMMdd-HHmmss", Locale.US).format(Date(id))
    return "NX-$stamp-${(id % 1000L).toString().padStart(3, '0')}"
}

private fun formatDuration(minutes: Int): String {
    val safe = minutes.coerceAtLeast(0)
    val days = safe / 1440
    val hours = (safe % 1440) / 60
    val mins = safe % 60
    return when {
        days > 0 -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${mins}m"
        else -> "${mins} min"
    }
}

@Composable private fun FormFieldLabel(text: String) {
    Text(text, color = Color(0xFFD6E5F5), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
}

private fun workTypeIcon(type: String): ImageVector = when (type) {
    "Instalación" -> Icons.Default.Build
    "Revisión" -> Icons.Default.FactCheck
    "Solución" -> Icons.Default.Handyman
    "Mantenimiento" -> Icons.Default.HomeRepairService
    "Redes" -> Icons.Default.Lan
    "Hardware" -> Icons.Default.Memory
    else -> Icons.Default.Assignment
}

@Composable private fun ChoiceChips(options:List<String>,selected:String,onSelect:(String)->Unit) { Column(verticalArrangement=Arrangement.spacedBy(4.dp)) { options.chunked(4).forEach { row -> Row(horizontalArrangement=Arrangement.spacedBy(5.dp)) { row.forEach { item -> FilterChip(selected=item==selected,onClick={onSelect(item)},label={Text(item,style=MaterialTheme.typography.labelSmall)},colors=FilterChipDefaults.filterChipColors(selectedContainerColor=Color(0xFF17394A),selectedLabelColor=Cyan)) } } } } }
@Composable private fun EmptyState(title:String,subtitle:String,icon:ImageVector) { AppCard { Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center) { Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.padding(vertical=18.dp)) { Icon(icon,null,tint=Cyan,modifier=Modifier.size(36.dp)); Spacer(Modifier.height(10.dp)); Text(title,fontWeight=FontWeight.SemiBold); Spacer(Modifier.height(4.dp)); Text(subtitle,color=Muted,style=MaterialTheme.typography.bodySmall) } } } }
@Composable private fun fieldColors() = OutlinedTextFieldDefaults.colors(focusedBorderColor=Cyan, unfocusedBorderColor=Color(0xFF31465D), focusedLabelColor=Cyan, unfocusedLabelColor=Muted, cursorColor=Cyan, focusedTextColor=Color.White, unfocusedTextColor=Color.White)