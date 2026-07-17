package com.example

import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.core.*
import com.example.data.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CatisViewModel
import com.example.viewmodel.SiteProgressPhoto
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CatisApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun CatisApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel: CatisViewModel = viewModel()

    val activeProject by viewModel.activeProjectCode.collectAsState()
    val activeRole by viewModel.activeRole.collectAsState()

    var activeTab by remember { mutableStateOf("Dashboard") }

    // Navigation form state (for pre-filling from QR system)
    var activeFormToOpen by remember { mutableStateOf<String?>(null) }
    var prefilledData by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    val projects = listOf("CATIS-P001", "CATIS-P002", "CATIS-P003")
    val roles = listOf(
        "Storekeeper",
        "Engineer",
        "Security Personnel",
        "Client Management",
        "CATIS Administrator"
    )

    // Project Name mapping
    val projectName = when (activeProject) {
        "CATIS-P001" -> "LMT Warehouse (Lagos Terminal)"
        "CATIS-P002" -> "Civil Steel (Abuja Industrial)"
        "CATIS-P003" -> "Infra Road (Kano Route 4)"
        else -> "Unknown Project"
    }

    val sharedPrefs = remember { context.getSharedPreferences("catis_onboarding_prefs_v1", android.content.Context.MODE_PRIVATE) }
    var showTour by remember { mutableStateOf(!sharedPrefs.getBoolean("tour_completed_v1", false)) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
        // --- Custom Hub Header ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = "CATIS Logo",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "CATIS",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                            Text(
                                text = "Operational Intelligence",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            )
                        }
                    }

                    // Active Tab Indicator / Status & Help
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { showTour = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "App Tour",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "LIVE UPDATE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Project and Role Selectors ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Project Dropdown Selector
                    var projectExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { projectExpanded = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Business,
                                        contentDescription = "Project",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = activeProject,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                            }
                        }
                        DropdownMenu(
                            expanded = projectExpanded,
                            onDismissRequest = { projectExpanded = false }
                        ) {
                            projects.forEach { proj ->
                                DropdownMenuItem(
                                    text = { Text(proj) },
                                    onClick = {
                                        viewModel.setProject(proj)
                                        projectExpanded = false
                                        Toast.makeText(context, "Switched to $proj", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }

                    // Role Dropdown Selector
                    var roleExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1.2f)) {
                        OutlinedButton(
                            onClick = { roleExpanded = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Role",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = activeRole.substringBefore(" "),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                            }
                        }
                        DropdownMenu(
                            expanded = roleExpanded,
                            onDismissRequest = { roleExpanded = false }
                        ) {
                            roles.forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(r) },
                                    onClick = {
                                        viewModel.setRole(r)
                                        roleExpanded = false
                                        Toast.makeText(context, "Role: $r", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                // Project subtitle info
                Text(
                    text = projectName,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        // --- Screen Content Area with Animations ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (activeTab) {
                "Dashboard" -> DashboardScreen(viewModel = viewModel)
                "Forms" -> FormsScreen(
                    viewModel = viewModel,
                    activeFormToOpen = activeFormToOpen,
                    prefilledData = prefilledData,
                    onFormHandled = {
                        activeFormToOpen = null
                        prefilledData = emptyMap()
                    }
                )
                "Databases" -> DatabasesScreen(viewModel = viewModel)
                "QR Codes" -> QrScreen(
                    viewModel = viewModel,
                    onTriggerForm = { formName, data ->
                        prefilledData = data
                        activeFormToOpen = formName
                        activeTab = "Forms"
                    }
                )
            }
        }

        // --- Navigation Bar ---
        NavigationBar(
            containerColor = Color(0xFFF3F4F9),
            tonalElevation = 8.dp
        ) {
            NavigationBarItem(
                selected = activeTab == "Dashboard",
                onClick = { activeTab = "Dashboard" },
                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                label = { Text("Dashboard") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            NavigationBarItem(
                selected = activeTab == "Forms",
                onClick = { activeTab = "Forms" },
                icon = { Icon(Icons.Default.EditNote, contentDescription = "Forms") },
                label = { Text("Forms") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            NavigationBarItem(
                selected = activeTab == "Databases",
                onClick = { activeTab == "Databases" },
                icon = { Icon(Icons.Default.Storage, contentDescription = "Databases") },
                label = { Text("Databases") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            NavigationBarItem(
                selected = activeTab == "QR Codes",
                onClick = { activeTab = "QR Codes" },
                icon = { Icon(Icons.Default.QrCode, contentDescription = "QR Codes") },
                label = { Text("QR System") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        } // Closes NavigationBar
    } // Closes Column

        if (showTour) {
            CatisTourOverlay(
                onDismiss = {
                    showTour = false
                    sharedPrefs.edit().putBoolean("tour_completed_v1", true).apply()
                }
            )
        }
    } // Closes Box
}

// ==========================================
// SCREEN 1: DASHBOARD (Executive Intelligence Hub)
// ==========================================
@Composable
fun DashboardScreen(viewModel: CatisViewModel) {
    val txs by viewModel.projectTransactions.collectAsState()
    val updates by viewModel.projectStructuralUpdates.collectAsState()
    val equipment by viewModel.projectEquipmentLogs.collectAsState()
    val security by viewModel.projectSecurityLogs.collectAsState()
    val incidents by viewModel.projectIncidentLogs.collectAsState()

    // --- Stock Balance Engine calculations ---
    val cementDelivered = txs.filter { it.materialCode.contains("Cement") && it.transactionType == "DELIVERY" }.sumOf { it.quantity }
    val cementIssued = txs.filter { it.materialCode.contains("Cement") && it.transactionType == "ISSUE" }.sumOf { it.quantity }
    val cementBalance = cementDelivered - cementIssued

    val graniteDelivered = txs.filter { it.materialCode.contains("Granite") && it.transactionType == "DELIVERY" }.sumOf { it.quantity }
    val graniteIssued = txs.filter { it.materialCode.contains("Granite") && it.transactionType == "ISSUE" }.sumOf { it.quantity }
    val graniteBalance = graniteDelivered - graniteIssued

    val sandDelivered = txs.filter { it.materialCode.contains("Sand") && it.transactionType == "DELIVERY" }.sumOf { it.quantity }
    val sandIssued = txs.filter { it.materialCode.contains("Sand") && it.transactionType == "ISSUE" }.sumOf { it.quantity }
    val sandBalance = sandDelivered - sandIssued

    val rebarDelivered = txs.filter { it.materialCode.contains("Rebar") && it.transactionType == "DELIVERY" }.sumOf { it.quantity }
    val rebarIssued = txs.filter { it.materialCode.contains("Rebar") && it.transactionType == "ISSUE" }.sumOf { it.quantity }
    val rebarBalance = rebarDelivered - rebarIssued

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Section: Live Metrics Summary Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Structural Progress",
                    value = if (updates.isNotEmpty()) "${updates.first().progressPercent}%" else "0%",
                    subtitle = if (updates.isNotEmpty()) updates.first().status else "Pending",
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Security Gate Feed",
                    value = "${security.size} Logs",
                    subtitle = if (security.isNotEmpty()) security.first().activityType else "No activity",
                    icon = Icons.Default.Security,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Section: Live Stock Balance Engine (MANDATORY per manual page 43 & Section 1.22)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Stock Balance Engine",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            Icons.Default.Inventory2,
                            contentDescription = "Stock",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "Calculated live from material deliveries and issues.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))

                    StockBalanceRow(materialName = "MAT-001 Cement", balance = cementBalance, unit = "Bags", deliveries = cementDelivered, issues = cementIssued)
                    StockBalanceRow(materialName = "MAT-002 Granite", balance = graniteBalance, unit = "Tonnes", deliveries = graniteDelivered, issues = graniteIssued)
                    StockBalanceRow(materialName = "MAT-003 Sharp Sand", balance = sandBalance, unit = "m³", deliveries = sandDelivered, issues = sandIssued)
                    StockBalanceRow(materialName = "MAT-004 Rebar", balance = rebarBalance, unit = "Tonnes", deliveries = rebarDelivered, issues = rebarIssued)
                }
            }
        }

        // Section: Recharts-Style Site Progress Bar Chart
        item {
            RechartsBarGraphCard(viewModel = viewModel)
        }

        // Section: Structural Progress Monitor
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Structural Progress Monitor",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (updates.isEmpty()) {
                        EmptyStateIndicator("No structural updates available for this project.")
                    } else {
                        updates.forEach { update ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${update.elementCode} - ${update.elementName}",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${update.progressPercent}%",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { update.progressPercent / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Status: ${update.status}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "Engr: ${update.updatedBy}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                            }
                        }
                    }
                }
            }
        }

        // Section: Site Equipment Monitor
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Site Equipment Status",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (equipment.isEmpty()) {
                        EmptyStateIndicator("No equipment registered for this project.")
                    } else {
                        equipment.forEach { eq ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "${eq.equipmentCode} ${eq.equipmentName}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                    Text(text = "Operator: ${eq.operatorName} | Loc: ${eq.location}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }

                                val badgeColor = when (eq.status) {
                                    "Active" -> Color(0xFF4CAF50)
                                    "Idle" -> Color(0xFFFFEB3B)
                                    "Breakdown" -> Color(0xFFF44336)
                                    else -> Color.Gray
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(badgeColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = eq.status.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (eq.status == "Idle") Color(0xFFD4AF37) else badgeColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        }
                    }
                }
            }
        }

        // Section: Incidents Monitor
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Recent Operational Incidents",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (incidents.isEmpty()) {
                        EmptyStateIndicator("All systems clear. No incidents logged.")
                    } else {
                        incidents.forEach { inc ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = inc.type, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                when (inc.severity) {
                                                    "Critical" -> Color.Red.copy(alpha = 0.15f)
                                                    "High" -> Color.Red.copy(alpha = 0.1f)
                                                    "Medium" -> Color.Yellow.copy(alpha = 0.15f)
                                                    else -> Color.Gray.copy(alpha = 0.15f)
                                                }
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = inc.severity.uppercase(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = when (inc.severity) {
                                                    "Critical", "High" -> Color.Red
                                                    "Medium" -> Color(0xFFFF9800)
                                                    else -> Color.Gray
                                                },
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                                Text(text = inc.description, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Status: ${inc.resolutionStatus}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                    Text(text = "Responsible: ${inc.responsiblePersonnel}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                                HorizontalDivider(modifier = Modifier.padding(top = 6.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                            }
                        }
                    }
                }
            }
        }

        item {
            SiteProgressPhotoJournal(viewModel = viewModel)
        }

        item {
            SessionReportExportCard(viewModel = viewModel)
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemInDarkTheme()) Color(0xFF1E2126) else Color(0xFFEEF0F5)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "COMPLIANCE AND AUDIT NOTE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = if (isSystemInDarkTheme()) Color(0xFFAEC6FF) else Color(0xFF74777F),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = "“All material deliveries must include photo verification. Laminated QR labels must remain clean and clearly visible at all designated zones to ensure seamless transaction verification.”",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            lineHeight = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SimulatedSitePhoto(zoneName: String, timestamp: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(180.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F1A24), Color(0xFF1B2E3C))
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val gridSize = 30.dp.toPx()
            val gridPaintColor = Color(0xFF415F91).copy(alpha = 0.15f)
            
            var x = 0f
            while (x < width) {
                drawLine(
                    color = gridPaintColor,
                    start = androidx.compose.ui.geometry.Offset(x, 0f),
                    end = androidx.compose.ui.geometry.Offset(x, height),
                    strokeWidth = 1.dp.toPx()
                )
                x += gridSize
            }
            
            var y = 0f
            while (y < height) {
                drawLine(
                    color = gridPaintColor,
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
                y += gridSize
            }

            val centerX = width / 2f
            val centerY = height / 2f
            val crosshairSize = 40.dp.toPx()
            
            drawCircle(
                color = Color(0xFF415F91).copy(alpha = 0.4f),
                radius = 50.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )

            drawCircle(
                color = Color(0xFF1E6D32).copy(alpha = 0.3f),
                radius = 12.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(centerX, centerY)
            )

            drawLine(
                color = Color(0xFF415F91).copy(alpha = 0.5f),
                start = androidx.compose.ui.geometry.Offset(centerX - crosshairSize, centerY),
                end = androidx.compose.ui.geometry.Offset(centerX + crosshairSize, centerY),
                strokeWidth = 2.dp.toPx()
            )

            drawLine(
                color = Color(0xFF415F91).copy(alpha = 0.5f),
                start = androidx.compose.ui.geometry.Offset(centerX, centerY - crosshairSize),
                end = androidx.compose.ui.geometry.Offset(centerX, centerY + crosshairSize),
                strokeWidth = 2.dp.toPx()
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color(0xFF86D593))
                    )
                    Text(
                        text = "SIM_FEED_ON",
                        color = Color(0xFF86D593),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp
                        )
                    )
                }

                Text(
                    text = "GPS: 6.5244 N, 3.3792 E",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    )
                )
            }

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(8.dp)
            ) {
                Text(
                    text = "ZONE: ${zoneName.uppercase()}",
                    color = Color(0xFFAEC6FF),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = "SYS_TIME: $timestamp",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    )
                )
                Text(
                    text = "METADATA: AUTO_AUDIT_VERIFIED",
                    color = Color(0xFF86D593),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteProgressPhotoJournal(viewModel: CatisViewModel) {
    val photos by viewModel.siteProgressPhotos.collectAsState()
    val activeProject by viewModel.activeProjectCode.collectAsState()

    var photoDescription by remember { mutableStateOf("") }
    var selectedZone by remember { mutableStateOf("Zone A: Foundation") }
    var useSimulation by remember { mutableStateOf(true) }
    var isZoneDropdownExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val siteZones = listOf(
        "Zone A: Foundation",
        "Zone B: Superstructure",
        "Zone C: Excavation",
        "Zone D: Finishing",
        "Zone E: Gate & Logistics"
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.addProgressPhoto(
                projectCode = activeProject,
                description = if (photoDescription.trim().isEmpty()) "Site progress visual verification" else photoDescription,
                progressZone = selectedZone,
                bitmap = bitmap,
                isSimulated = false
            )
            photoDescription = ""
            Toast.makeText(context, "Progress photo captured & timestamped!", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open camera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission is required to capture photos.", Toast.LENGTH_SHORT).show()
        }
    }

    val onCaptureClick = {
        if (useSimulation) {
            viewModel.addProgressPhoto(
                projectCode = activeProject,
                description = if (photoDescription.trim().isEmpty()) "Audited progress checkpoint visual" else photoDescription,
                progressZone = selectedZone,
                bitmap = null,
                isSimulated = true
            )
            photoDescription = ""
            Toast.makeText(context, "Simulated photo feed captured & timestamped!", Toast.LENGTH_SHORT).show()
        } else {
            val isPermissionGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (isPermissionGranted) {
                try {
                    cameraLauncher.launch(null)
                } catch (e: Exception) {
                    Toast.makeText(context, "Could not open camera: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                permissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Site Progress Photo Journal",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Capture, timestamp, and audit live site progress. Stored locally for your current session.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Camera",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { isZoneDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Zone: $selectedZone",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isZoneDropdownExpanded,
                        onDismissRequest = { isZoneDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f).background(MaterialTheme.colorScheme.surface)
                    ) {
                        siteZones.forEach { zone ->
                            DropdownMenuItem(
                                text = { Text(zone) },
                                onClick = {
                                    selectedZone = zone
                                    isZoneDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = photoDescription,
                    onValueChange = { photoDescription = it },
                    placeholder = { Text("Progress description (e.g. pouring slab concrete...)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Simulate Camera Feed",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Recommended for browser streaming preview",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Switch(
                        checked = useSimulation,
                        onCheckedChange = { useSimulation = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }

                Button(
                    onClick = onCaptureClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Capture"
                        )
                        Text(
                            text = if (useSimulation) "CAPTURE SIMULATED PROGRESS" else "LAUNCH DEVICE CAMERA",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Captured Session Photos (${photos.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (photos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No site photos captured for this session yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    photos.asReversed().forEach { photo ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (photo.isSimulated || photo.bitmap == null) {
                                    SimulatedSitePhoto(zoneName = photo.progressZone, timestamp = photo.timestamp)
                                } else {
                                    Image(
                                        bitmap = photo.bitmap.asImageBitmap(),
                                        contentDescription = "Site progress captured",
                                        modifier = Modifier
                                            .height(180.dp)
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(100.dp))
                                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = photo.progressZone.uppercase(),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 0.5.sp
                                                    )
                                                )
                                            }
                                            Text(
                                                text = photo.timestamp,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = photo.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteProgressPhoto(photo.id) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Photo",
                                            tint = MaterialTheme.colorScheme.error
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
}

@Composable
fun MetricCard(title: String, value: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun StockBalanceRow(materialName: String, balance: Double, unit: String, deliveries: Double, issues: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = materialName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text(text = "Total Deliveries: ${deliveries.toInt()} | Issues: ${issues.toInt()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "${balance.toInt()} $unit",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(
                        if (balance > 50) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (balance <= 50) "REORDER" else "ADEQUATE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (balance > 50) Color(0xFF2E7D32) else Color(0xFFC62828),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}

@Composable
fun EmptyStateIndicator(msg: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = msg, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}


// ==========================================
// SCREEN 2: FORMS (Operational Transaction Interfaces)
// ==========================================
@Composable
fun FormsScreen(
    viewModel: CatisViewModel,
    activeFormToOpen: String?,
    prefilledData: Map<String, String>,
    onFormHandled: () -> Unit
) {
    val activeRole by viewModel.activeRole.collectAsState()
    val activeProject by viewModel.activeProjectCode.collectAsState()

    var showDeliveryForm by remember { mutableStateOf(false) }
    var showIssueForm by remember { mutableStateOf(false) }
    var showStructuralForm by remember { mutableStateOf(false) }
    var showEquipmentForm by remember { mutableStateOf(false) }
    var showSecurityForm by remember { mutableStateOf(false) }
    var showIncidentForm by remember { mutableStateOf(false) }

    // Intercept navigation from QR System
    LaunchedEffect(activeFormToOpen) {
        if (activeFormToOpen != null) {
            when (activeFormToOpen) {
                "Delivery" -> showDeliveryForm = true
                "Issue" -> showIssueForm = true
                "Structural" -> showStructuralForm = true
                "Equipment" -> showEquipmentForm = true
                "Security" -> showSecurityForm = true
                "Incident" -> showIncidentForm = true
            }
            onFormHandled()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                text = "Operational Submission Forms",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = "Ensure your selected Role matches the form's access level. Submitting any form updates databases and dashboards automatically.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Form 1: Material Delivery Form (Section 3.1.6 - Detailed 15 questions!)
        item {
            val isAuthorized = activeRole in listOf("Storekeeper", "CATIS Administrator")
            FormRowItem(
                title = "Material Delivery Form",
                description = "Record incoming construction materials arriving at site gates. Primary: Storekeeper.",
                isAuthorized = isAuthorized,
                requiredRole = "Storekeeper",
                icon = Icons.Default.LocalShipping,
                onClick = { showDeliveryForm = true }
            )
        }

        // Form 2: Material Issue Form
        item {
            val isAuthorized = activeRole in listOf("Storekeeper", "CATIS Administrator")
            FormRowItem(
                title = "Material Issue Form",
                description = "Record stock issues and usage allocations to specific activities. Primary: Storekeeper.",
                isAuthorized = isAuthorized,
                requiredRole = "Storekeeper",
                icon = Icons.Default.RemoveShoppingCart,
                onClick = { showIssueForm = true }
            )
        }

        // Form 3: Structural Progress Form
        item {
            val isAuthorized = activeRole in listOf("Engineer", "CATIS Administrator")
            FormRowItem(
                title = "Structural Progress Form",
                description = "Log physical work milestones (foundations, columns, structural slabs). Primary: Engineer.",
                isAuthorized = isAuthorized,
                requiredRole = "Engineer",
                icon = Icons.Default.Construction,
                onClick = { showStructuralForm = true }
            )
        }

        // Form 4: Equipment Status Form
        item {
            val isAuthorized = activeRole in listOf("Engineer", "Storekeeper", "CATIS Administrator")
            FormRowItem(
                title = "Equipment Status Form",
                description = "Log deployment, breakdown, or maintenance of active site machinery. Primary: Officer.",
                isAuthorized = isAuthorized,
                requiredRole = "Engineer/Storekeeper",
                icon = Icons.Default.Agriculture,
                onClick = { showEquipmentForm = true }
            )
        }

        // Form 5: Security Gate Verification Form
        item {
            val isAuthorized = activeRole in listOf("Security Personnel", "CATIS Administrator")
            FormRowItem(
                title = "Security Gate Form",
                description = "Log vehicles, deliveries, and gate access verification logs. Primary: Security.",
                isAuthorized = isAuthorized,
                requiredRole = "Security Personnel",
                icon = Icons.Default.DoorSliding,
                onClick = { showSecurityForm = true }
            )
        }

        // Form 6: Incident Report Form
        item {
            FormRowItem(
                title = "Incident Report Form",
                description = "Report leakages, variance, breakdowns, or breaches. Primary: Security/Staff.",
                isAuthorized = true, // Open to all roles
                requiredRole = "Any Role",
                icon = Icons.Default.ReportProblem,
                onClick = { showIncidentForm = true }
            )
        }
    }

    // Dialog 1: Material Delivery Form (Full 15 Questions!)
    if (showDeliveryForm) {
        MaterialDeliveryDialog(
            viewModel = viewModel,
            prefilledData = prefilledData,
            onDismiss = { showDeliveryForm = false }
        )
    }

    // Dialog 2: Material Issue Form
    if (showIssueForm) {
        MaterialIssueDialog(
            viewModel = viewModel,
            onDismiss = { showIssueForm = false }
        )
    }

    // Dialog 3: Structural Progress Form
    if (showStructuralForm) {
        StructuralUpdateDialog(
            viewModel = viewModel,
            prefilledData = prefilledData,
            onDismiss = { showStructuralForm = false }
        )
    }

    // Dialog 4: Equipment Status Form
    if (showEquipmentForm) {
        EquipmentLogDialog(
            viewModel = viewModel,
            prefilledData = prefilledData,
            onDismiss = { showEquipmentForm = false }
        )
    }

    // Dialog 5: Security Gate Form
    if (showSecurityForm) {
        SecurityLogDialog(
            viewModel = viewModel,
            onDismiss = { showSecurityForm = false }
        )
    }

    // Dialog 6: Incident Report Form
    if (showIncidentForm) {
        IncidentLogDialog(
            viewModel = viewModel,
            onDismiss = { showIncidentForm = false }
        )
    }
}

@Composable
fun FormRowItem(title: String, description: String, isAuthorized: Boolean, requiredRole: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isAuthorized) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isAuthorized) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else Color.Gray.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isAuthorized) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isAuthorized) MaterialTheme.colorScheme.onSurface else Color.Gray
                    )
                    // Lock icon if not authorized
                    if (!isAuthorized) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Restricted",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Access: $requiredRole",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isAuthorized) MaterialTheme.colorScheme.secondary else Color.Red,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "OPEN FORM",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isAuthorized) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }
        }
    }
}


// --- 1. Material Delivery Form Dialog ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDeliveryDialog(viewModel: CatisViewModel, prefilledData: Map<String, String>, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val activeProject by viewModel.activeProjectCode.collectAsState()

    // 15 State Variables corresponding to 15 fields
    var projectCode by remember { mutableStateOf(activeProject) }
    var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var time by remember { mutableStateOf(SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())) }
    var supplierName by remember { mutableStateOf(prefilledData["supplier"] ?: "Dangote Cement") }
    var vehicleNumber by remember { mutableStateOf(prefilledData["vehicle"] ?: "GGE-245XA") }
    var materialCode by remember { mutableStateOf(prefilledData["material"] ?: "MAT-001 Cement") }
    var quantityString by remember { mutableStateOf(prefilledData["quantity"] ?: "300") }
    var unitOfMeasurement by remember { mutableStateOf(prefilledData["unit"] ?: "Bags") }
    var storageLocation by remember { mutableStateOf(prefilledData["location"] ?: "Cement Store") }
    var qtyVerificationStatus by remember { mutableStateOf("Fully Verified") }
    var materialCondition by remember { mutableStateOf("Good Condition") }
    var receivedBy by remember { mutableStateOf("Ibrahim Musa") }
    var securityVerification by remember { mutableStateOf("Yes") }
    var hasPhoto by remember { mutableStateOf(true) } // Simulated photo upload toggle
    var remarks by remember { mutableStateOf("") }

    val projectList = listOf("CATIS-P001", "CATIS-P002", "CATIS-P003")
    val suppliers = listOf("Dangote Cement", "Lafarge", "XYZ Aggregates")
    val materialCodes = listOf("MAT-001 Cement", "MAT-002 Granite", "MAT-003 Sharp Sand", "MAT-004 Rebar")
    val units = listOf("Bags", "m³", "Tonnes", "Pieces", "Litres")
    val locations = listOf("Cement Store", "Aggregate Zone", "Rebar Yard", "Diesel Storage")
    val verifyStatuses = listOf("Fully Verified", "Partially Verified", "Verification Discrepancy Observed")
    val conditions = listOf("Good Condition", "Minor Damage Observed", "Significant Damage Observed")
    val staffList = listOf("Ibrahim Musa", "David Okoye", "Samuel Bello")

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Material Delivery Form",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = { onDismiss() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Scrollable Form content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Q1: Project Code Dropdown
                    DropdownField(label = "1. Select Project", selected = projectCode, options = projectList) { projectCode = it }

                    // Q2: Delivery Date
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("2. Delivery Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Q3: Delivery Time
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("3. Delivery Time") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Q4: Supplier Name
                    DropdownField(label = "4. Select Supplier", selected = supplierName, options = suppliers) { supplierName = it }

                    // Q5: Vehicle Number
                    OutlinedTextField(
                        value = vehicleNumber,
                        onValueChange = { vehicleNumber = it },
                        label = { Text("5. Vehicle Registration Number") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Q6: Material Code
                    DropdownField(label = "6. Select Material Type", selected = materialCode, options = materialCodes) { materialCode = it }

                    // Q7: Quantity (Number validation!)
                    OutlinedTextField(
                        value = quantityString,
                        onValueChange = {
                            if (it.isEmpty() || it.toDoubleOrNull() != null) {
                                quantityString = it
                            }
                        },
                        label = { Text("7. Enter Quantity Delivered (Numbers Only)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Q8: Unit of Measurement
                    DropdownField(label = "8. Select Unit of Measurement", selected = unitOfMeasurement, options = units) { unitOfMeasurement = it }

                    // Q9: Storage Location
                    DropdownField(label = "9. Select Storage Location", selected = storageLocation, options = locations) { storageLocation = it }

                    // Q10: Quantity Verification Status (Radio buttons)
                    Text("10. Was delivery quantity verified?", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    verifyStatuses.forEach { status ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { qtyVerificationStatus = status }
                        ) {
                            RadioButton(selected = qtyVerificationStatus == status, onClick = { qtyVerificationStatus = status })
                            Text(text = status, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    // Q11: Material Condition
                    Text("11. Condition of Delivered Material", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    conditions.forEach { cond ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { materialCondition = cond }
                        ) {
                            RadioButton(selected = materialCondition == cond, onClick = { materialCondition = cond })
                            Text(text = cond, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    // Q12: Received By
                    DropdownField(label = "12. Name of Storekeeper Receiving Material", selected = receivedBy, options = staffList) { receivedBy = it }

                    // Q13: Was Security Verification Completed?
                    Text("13. Was Security Verification Completed?", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Row {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { securityVerification = "Yes" }) {
                            RadioButton(selected = securityVerification == "Yes", onClick = { securityVerification = "Yes" })
                            Text("Yes")
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { securityVerification = "No" }) {
                            RadioButton(selected = securityVerification == "No", onClick = { securityVerification = "No" })
                            Text("No")
                        }
                    }

                    // Q14: Photo Evidence Simulation (MANDATORY Section 3.1.6)
                    Text("14. Upload Delivery Photos (Section 1.12 Evidence)", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (hasPhoto) "✓ PHOTO_ATTACHED_GGE-245XA.PNG" else "Attach Mock Verification Photo",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(text = "Captures: Vehicle, material type, unloading process.", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Button(
                                onClick = { hasPhoto = !hasPhoto },
                                modifier = Modifier.padding(top = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text(if (hasPhoto) "Remove Photo" else "Simulate Snap")
                            }
                        }
                    }

                    // Q15: Remarks
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("15. Additional Observations / Remarks") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }

                // Submit Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = { onDismiss() }, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val qty = quantityString.toDoubleOrNull()
                            if (qty == null || qty <= 0) {
                                Toast.makeText(context, "Please enter a valid numeric quantity.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.submitMaterialDelivery(
                                projectCode = projectCode,
                                date = date,
                                time = time,
                                supplierName = supplierName,
                                vehicleNumber = vehicleNumber,
                                materialCode = materialCode,
                                quantity = qty,
                                unit = unitOfMeasurement,
                                storageLocation = storageLocation,
                                verificationStatus = qtyVerificationStatus,
                                condition = materialCondition,
                                receivedBy = receivedBy,
                                securityVerified = securityVerification,
                                photoPath = if (hasPhoto) "assets/mock_delivery.png" else null,
                                remarks = remarks.ifBlank { "Delivery of $materialCode verified at $storageLocation." }
                            )
                            Toast.makeText(context, "Delivery Logged into Database!", Toast.LENGTH_LONG).show()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Submit Transaction")
                    }
                }
            }
        }
    }
}


// --- 2. Material Issue Form Dialog ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialIssueDialog(viewModel: CatisViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val activeProject by viewModel.activeProjectCode.collectAsState()

    var projectCode by remember { mutableStateOf(activeProject) }
    var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var time by remember { mutableStateOf(SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())) }
    var recipientActivity by remember { mutableStateOf("Foundation Casting") }
    var materialCode by remember { mutableStateOf("MAT-001 Cement") }
    var quantityString by remember { mutableStateOf("10") }
    var unitOfMeasurement by remember { mutableStateOf("Bags") }
    var storageLocation by remember { mutableStateOf("Cement Store") }
    var issuedBy by remember { mutableStateOf("Ibrahim Musa") }
    var remarks by remember { mutableStateOf("") }

    val projectList = listOf("CATIS-P001", "CATIS-P002", "CATIS-P003")
    val materialCodes = listOf("MAT-001 Cement", "MAT-002 Granite", "MAT-003 Sharp Sand", "MAT-004 Rebar")
    val units = listOf("Bags", "m³", "Tonnes", "Pieces", "Litres")
    val locations = listOf("Cement Store", "Aggregate Zone", "Rebar Yard", "Diesel Storage")
    val activities = listOf("Foundation Casting", "Structural Slabs", "Columns Work", "Site Operations")
    val staffList = listOf("Ibrahim Musa", "David Okoye", "Samuel Bello")

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Material Issue Form",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DropdownField(label = "Project", selected = projectCode, options = projectList) { projectCode = it }
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Time") }, modifier = Modifier.fillMaxWidth())
                    DropdownField(label = "Recipient Activity", selected = recipientActivity, options = activities) { recipientActivity = it }
                    DropdownField(label = "Material Type", selected = materialCode, options = materialCodes) { materialCode = it }

                    OutlinedTextField(
                        value = quantityString,
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) quantityString = it },
                        label = { Text("Quantity to Issue") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownField(label = "Unit", selected = unitOfMeasurement, options = units) { unitOfMeasurement = it }
                    DropdownField(label = "Storage Location", selected = storageLocation, options = locations) { storageLocation = it }
                    DropdownField(label = "Issued By", selected = issuedBy, options = staffList) { issuedBy = it }
                    OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Remarks") }, modifier = Modifier.fillMaxWidth())
                }

                Row(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { onDismiss() }, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = {
                            val qty = quantityString.toDoubleOrNull()
                            if (qty == null || qty <= 0) {
                                Toast.makeText(context, "Enter a valid quantity", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.submitMaterialIssue(
                                projectCode = projectCode,
                                date = date,
                                time = time,
                                recipient = recipientActivity,
                                materialCode = materialCode,
                                quantity = qty,
                                unit = unitOfMeasurement,
                                storageLocation = storageLocation,
                                loggedBy = issuedBy,
                                remarks = remarks.ifBlank { "Issued $qty $unitOfMeasurement to $recipientActivity." }
                            )
                            Toast.makeText(context, "Stock Issue Saved!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) { Text("Issue Stock") }
                }
            }
        }
    }
}


// --- 3. Structural Progress Update Dialog ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StructuralUpdateDialog(viewModel: CatisViewModel, prefilledData: Map<String, String>, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val activeProject by viewModel.activeProjectCode.collectAsState()

    var projectCode by remember { mutableStateOf(activeProject) }
    var elementCode by remember { mutableStateOf(prefilledData["elementCode"] ?: "ST-001") }
    var elementName by remember { mutableStateOf(prefilledData["elementName"] ?: "Foundation Section") }
    var status by remember { mutableStateOf("In Progress") }
    var progressPercent by remember { mutableFloatStateOf(45f) }
    var engineerName by remember { mutableStateOf("Engr. David Okoye") }
    var remarks by remember { mutableStateOf("") }

    val projectList = listOf("CATIS-P001", "CATIS-P002", "CATIS-P003")
    val elementCodes = listOf("ST-001", "ST-002", "ST-003", "ST-004")
    val elementNames = mapOf(
        "ST-001" to "Foundation Section",
        "ST-002" to "Ground Floor Columns",
        "ST-003" to "First Floor Beams",
        "ST-004" to "Roofing Framework"
    )
    val statuses = listOf("Not Started", "In Progress", "Completed", "Pending Inspection")

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Structural Progress Form",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DropdownField(label = "Project Code", selected = projectCode, options = projectList) { projectCode = it }
                    DropdownField(label = "Element Code", selected = elementCode, options = elementCodes) {
                        elementCode = it
                        elementName = elementNames[it] ?: "Structural Block"
                    }
                    OutlinedTextField(value = elementName, onValueChange = {}, label = { Text("Element Name (Auto)") }, readOnly = true, modifier = Modifier.fillMaxWidth())
                    DropdownField(label = "Status", selected = status, options = statuses) { status = it }

                    Text("Progress Percent: ${progressPercent.toInt()}%", fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = progressPercent,
                        onValueChange = { progressPercent = it },
                        valueRange = 0f..100f,
                        steps = 20,
                        colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                    )

                    OutlinedTextField(value = engineerName, onValueChange = { engineerName = it }, label = { Text("Inspecting Engineer") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Remarks") }, modifier = Modifier.fillMaxWidth())
                }

                Row(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { onDismiss() }, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = {
                            viewModel.submitStructuralUpdate(
                                projectCode = projectCode,
                                elementCode = elementCode,
                                elementName = elementName,
                                status = status,
                                progressPercent = progressPercent.toInt(),
                                updatedBy = engineerName,
                                remarks = remarks.ifBlank { "Element $elementCode progress set to ${progressPercent.toInt()}%." }
                            )
                            Toast.makeText(context, "Structural Update Logged!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) { Text("Save Progress") }
                }
            }
        }
    }
}


// --- 4. Equipment Log Form Dialog ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentLogDialog(viewModel: CatisViewModel, prefilledData: Map<String, String>, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val activeProject by viewModel.activeProjectCode.collectAsState()

    var projectCode by remember { mutableStateOf(activeProject) }
    var equipmentCode by remember { mutableStateOf(prefilledData["equipmentCode"] ?: "EQ-001") }
    var equipmentName by remember { mutableStateOf(prefilledData["equipmentName"] ?: "Caterpillar Excavator") }
    var status by remember { mutableStateOf("Active") }
    var operatorName by remember { mutableStateOf("Kabiru Sani") }
    var location by remember { mutableStateOf("Batching Yard") }
    var remarks by remember { mutableStateOf("") }

    val projectList = listOf("CATIS-P001", "CATIS-P002", "CATIS-P003")
    val equipmentCodes = listOf("EQ-001", "EQ-002", "EQ-003")
    val equipmentNames = mapOf(
        "EQ-001" to "Caterpillar Excavator",
        "EQ-002" to "Concrete Mixer Truck",
        "EQ-003" to "Tower Crane ST-5"
    )
    val statuses = listOf("Active", "Idle", "Breakdown", "Under Maintenance")

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Equipment Status Form",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DropdownField(label = "Project Code", selected = projectCode, options = projectList) { projectCode = it }
                    DropdownField(label = "Equipment Code", selected = equipmentCode, options = equipmentCodes) {
                        equipmentCode = it
                        equipmentName = equipmentNames[it] ?: "Machinery"
                    }
                    OutlinedTextField(value = equipmentName, onValueChange = {}, label = { Text("Equipment Name (Auto)") }, readOnly = true, modifier = Modifier.fillMaxWidth())
                    DropdownField(label = "Current Status", selected = status, options = statuses) { status = it }
                    OutlinedTextField(value = operatorName, onValueChange = { operatorName = it }, label = { Text("Operator Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Active Location") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Remarks") }, modifier = Modifier.fillMaxWidth())
                }

                Row(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { onDismiss() }, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = {
                            viewModel.submitEquipmentLog(
                                projectCode = projectCode,
                                equipmentCode = equipmentCode,
                                equipmentName = equipmentName,
                                status = status,
                                operatorName = operatorName,
                                location = location,
                                remarks = remarks.ifBlank { "Status of $equipmentCode updated to $status." }
                            )
                            Toast.makeText(context, "Equipment Status Saved!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) { Text("Save Status") }
                }
            }
        }
    }
}


// --- 5. Security Gate Log Form Dialog ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityLogDialog(viewModel: CatisViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val activeProject by viewModel.activeProjectCode.collectAsState()

    var projectCode by remember { mutableStateOf(activeProject) }
    var gateCode by remember { mutableStateOf("SG-001") }
    var activityType by remember { mutableStateOf("VEHICLE_IN") }
    var vehicleNumber by remember { mutableStateOf("GGE-245XA") }
    var description by remember { mutableStateOf("") }
    var verifiedBy by remember { mutableStateOf("Officer Samuel") }

    val projectList = listOf("CATIS-P001", "CATIS-P002", "CATIS-P003")
    val gates = listOf("SG-001 Main Gate", "SG-002 Store Gate", "SG-003 North Gate")
    val types = listOf("VEHICLE_IN", "VEHICLE_OUT", "PERSONNEL_IN", "PERSONNEL_OUT")

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Security Gate Form",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DropdownField(label = "Project Code", selected = projectCode, options = projectList) { projectCode = it }
                    DropdownField(label = "Gate Code", selected = gateCode, options = gates) { gateCode = it.substringBefore(" ") }
                    DropdownField(label = "Activity Type", selected = activityType, options = types) { activityType = it }
                    OutlinedTextField(value = vehicleNumber, onValueChange = { vehicleNumber = it }, label = { Text("Vehicle Registration / ID") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Purpose / Description") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = verifiedBy, onValueChange = { verifiedBy = it }, label = { Text("Verified By Security") }, modifier = Modifier.fillMaxWidth())
                }

                Row(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { onDismiss() }, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = {
                            viewModel.submitSecurityLog(
                                projectCode = projectCode,
                                gateCode = gateCode,
                                activityType = activityType,
                                description = description.ifBlank { "Gate verification logged for vehicle $vehicleNumber" },
                                vehicleNumber = vehicleNumber,
                                verifiedBy = verifiedBy
                            )
                            Toast.makeText(context, "Security Log Saved!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) { Text("Save Gate Log") }
                }
            }
        }
    }
}


// --- 6. Incident Log Form Dialog ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentLogDialog(viewModel: CatisViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val activeProject by viewModel.activeProjectCode.collectAsState()

    var projectCode by remember { mutableStateOf(activeProject) }
    var type by remember { mutableStateOf("Material Variance") }
    var description by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf("Medium") }
    var responsiblePersonnel by remember { mutableStateOf("Ibrahim Musa") }
    var resolutionStatus by remember { mutableStateOf("Open") }

    val projectList = listOf("CATIS-P001", "CATIS-P002", "CATIS-P003")
    val incidentTypes = listOf("Unauthorised Material Movement", "Material Variance", "Equipment Breakdown", "Security Breach", "Reporting Noncompliance")
    val severities = listOf("Low", "Medium", "High", "Critical")
    val statuses = listOf("Open", "Under Investigation", "Resolved")

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Incident Report Form",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DropdownField(label = "Project Code", selected = projectCode, options = projectList) { projectCode = it }
                    DropdownField(label = "Incident Category", selected = type, options = incidentTypes) { type = it }
                    DropdownField(label = "Severity Level", selected = severity, options = severities) { severity = it }
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Incident Description / Cause") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    OutlinedTextField(value = responsiblePersonnel, onValueChange = { responsiblePersonnel = it }, label = { Text("Reporter / Personnel") }, modifier = Modifier.fillMaxWidth())
                    DropdownField(label = "Initial Status", selected = resolutionStatus, options = statuses) { resolutionStatus = it }
                }

                Row(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { onDismiss() }, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (description.isBlank()) {
                                Toast.makeText(context, "Please enter a description.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.submitIncidentLog(
                                projectCode = projectCode,
                                type = type,
                                description = description,
                                severity = severity,
                                responsiblePersonnel = responsiblePersonnel,
                                resolutionStatus = resolutionStatus
                            )
                            Toast.makeText(context, "Incident Logged & Escaled!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) { Text("File Incident") }
                }
            }
        }
    }
}


// --- Helper Dropdown Field Composable ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(label: String, selected: String, options: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                        onSelected(opt)
                        expanded = false
                    }
                )
            }
        }
    }
}


// ==========================================
// SCREEN 3: DATABASES (Clean, Auditable Registers)
// ==========================================
@Composable
fun DatabasesScreen(viewModel: CatisViewModel) {
    var activeSubTab by remember { mutableStateOf("Material Ledger") }
    var searchQuery by remember { mutableStateOf("") }

    val txs by viewModel.projectTransactions.collectAsState()
    val updates by viewModel.projectStructuralUpdates.collectAsState()
    val equipment by viewModel.projectEquipmentLogs.collectAsState()
    val security by viewModel.projectSecurityLogs.collectAsState()
    val incidents by viewModel.projectIncidentLogs.collectAsState()

    val subTabs = listOf("Material Ledger", "Structural Database", "Equipment Reg", "Security Logs", "Incident Logs")

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal Scrollable Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            subTabs.forEach { tab ->
                val selected = activeSubTab == tab
                Button(
                    onClick = {
                        activeSubTab = tab
                        searchQuery = "" // Reset search on tab change
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = if (!selected) BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)) else null,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = tab, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search database records...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            shape = RoundedCornerShape(8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (activeSubTab) {
                "Material Ledger" -> {
                    val filteredList = txs.filter {
                        it.materialCode.contains(searchQuery, ignoreCase = true) ||
                                it.supplierOrRecipient.contains(searchQuery, ignoreCase = true) ||
                                it.location.contains(searchQuery, ignoreCase = true) ||
                                it.loggedBy.contains(searchQuery, ignoreCase = true)
                    }
                    if (filteredList.isEmpty()) {
                        item { EmptyStateIndicator("No material ledger records found.") }
                    } else {
                        items(filteredList) { item ->
                            MaterialLedgerItem(item)
                        }
                    }
                }
                "Structural Database" -> {
                    val filteredList = updates.filter {
                        it.elementCode.contains(searchQuery, ignoreCase = true) ||
                                it.elementName.contains(searchQuery, ignoreCase = true) ||
                                it.updatedBy.contains(searchQuery, ignoreCase = true) ||
                                it.status.contains(searchQuery, ignoreCase = true)
                    }
                    if (filteredList.isEmpty()) {
                        item { EmptyStateIndicator("No structural database records found.") }
                    } else {
                        items(filteredList) { item ->
                            StructuralDatabaseItem(item)
                        }
                    }
                }
                "Equipment Reg" -> {
                    val filteredList = equipment.filter {
                        it.equipmentCode.contains(searchQuery, ignoreCase = true) ||
                                it.equipmentName.contains(searchQuery, ignoreCase = true) ||
                                it.operatorName.contains(searchQuery, ignoreCase = true) ||
                                it.status.contains(searchQuery, ignoreCase = true)
                    }
                    if (filteredList.isEmpty()) {
                        item { EmptyStateIndicator("No equipment registry records found.") }
                    } else {
                        items(filteredList) { item ->
                            EquipmentDatabaseItem(item)
                        }
                    }
                }
                "Security Logs" -> {
                    val filteredList = security.filter {
                        it.gateCode.contains(searchQuery, ignoreCase = true) ||
                                it.vehicleNumber.contains(searchQuery, ignoreCase = true) ||
                                it.verifiedBy.contains(searchQuery, ignoreCase = true) ||
                                it.activityType.contains(searchQuery, ignoreCase = true)
                    }
                    if (filteredList.isEmpty()) {
                        item { EmptyStateIndicator("No security logs found.") }
                    } else {
                        items(filteredList) { item ->
                            SecurityDatabaseItem(item)
                        }
                    }
                }
                "Incident Logs" -> {
                    val filteredList = incidents.filter {
                        it.type.contains(searchQuery, ignoreCase = true) ||
                                it.description.contains(searchQuery, ignoreCase = true) ||
                                it.responsiblePersonnel.contains(searchQuery, ignoreCase = true) ||
                                it.severity.contains(searchQuery, ignoreCase = true)
                    }
                    if (filteredList.isEmpty()) {
                        item { EmptyStateIndicator("No incident records found.") }
                    } else {
                        items(filteredList) { item ->
                            IncidentDatabaseItem(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MaterialLedgerItem(item: MaterialTransaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (item.transactionType == "DELIVERY") Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = item.transactionType,
                        tint = if (item.transactionType == "DELIVERY") Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.transactionType,
                        fontWeight = FontWeight.Bold,
                        color = if (item.transactionType == "DELIVERY") Color(0xFF4CAF50) else Color(0xFFF44336),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(text = "${item.date} ${item.time}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${item.quantity.toInt()} ${item.unit} of ${item.materialCode}",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = if (item.transactionType == "DELIVERY") "Supplier: ${item.supplierOrRecipient} | Vehicle: ${item.vehicleNumber}"
                else "Allocation Target: ${item.supplierOrRecipient}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Storage: ${item.location} | Logged by: ${item.loggedBy}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                // Photo available indicator
                if (item.photoPath != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PHOTO ATTACHED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            if (item.remarks.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        .padding(6.dp)
                ) {
                    Text(text = "Remarks: ${item.remarks}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun StructuralDatabaseItem(item: StructuralUpdate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${item.elementCode} - ${item.elementName}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${item.progressPercent}%",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { item.progressPercent / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Status: ${item.status}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Text(text = "Log: ${item.updatedBy}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            if (item.remarks.isNotBlank()) {
                Text(text = "Remarks: ${item.remarks}", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
fun EquipmentDatabaseItem(item: EquipmentLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "${item.equipmentCode} - ${item.equipmentName}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(text = item.status, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Operator: ${item.operatorName} | Active Location: ${item.location}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            if (item.remarks.isNotBlank()) {
                Text(text = "Remarks: ${item.remarks}", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
fun SecurityDatabaseItem(item: SecurityLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Gate: ${item.gateCode} | Type: ${item.activityType}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                Text(text = "Vehicle: ${item.vehicleNumber}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = item.description, style = MaterialTheme.typography.bodyMedium)
            Text(text = "Verified by: ${item.verifiedBy}", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun IncidentDatabaseItem(item: IncidentLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = item.type, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(text = item.severity.uppercase(), fontWeight = FontWeight.Bold, color = Color.Red, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = item.description, style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Status: ${item.resolutionStatus}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Text(text = "Assigned: ${item.responsiblePersonnel}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}


// ==========================================
// SCREEN 4: QR SYSTEM (Simplified Verification Workflows)
// ==========================================
@Composable
fun QrScreen(viewModel: CatisViewModel, onTriggerForm: (String, Map<String, String>) -> Unit) {
    var showScanDialog by remember { mutableStateOf(false) }
    var selectedQrForScan by remember { mutableStateOf<Map<String, String>?>(null) }

    // Hardcoded QR Deployment tags (Section 1.23 QR DEPLOYMENT STAGE)
    val qrDeployments = listOf(
        mapOf(
            "code" to "ST-001",
            "type" to "Structural Element",
            "name" to "Foundation Section",
            "details" to "Location: Sector A Block 3. Main structure foundation casting.",
            "formTarget" to "Structural",
            "prefill_elementCode" to "ST-001",
            "prefill_elementName" to "Foundation Section"
        ),
        mapOf(
            "code" to "EQ-001",
            "type" to "Site Equipment",
            "name" to "Caterpillar Excavator",
            "details" to "Asset ID: CATIS-EQ-01. Used for ground foundation work.",
            "formTarget" to "Equipment",
            "prefill_equipmentCode" to "EQ-001",
            "prefill_equipmentName" to "Caterpillar Excavator"
        ),
        mapOf(
            "code" to "MZ-001",
            "type" to "Material Zone",
            "name" to "Cement Store",
            "details" to "Main Warehouse Store. Standardized code: CATIS_P001_LMT_WAREHOUSE.",
            "formTarget" to "Delivery",
            "prefill_location" to "Cement Store",
            "prefill_material" to "MAT-001 Cement",
            "prefill_unit" to "Bags"
        ),
        mapOf(
            "code" to "SG-001",
            "type" to "Security Gate",
            "name" to "Main Gated Access",
            "details" to "Gate Alpha. Main entry point for logistics vehicles and personnel verification.",
            "formTarget" to "Security",
            "prefill_gateCode" to "SG-001"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "QR Code Verification System",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.Start)
        )
        Text(
            text = "In CATIS operations, QR labels are laminated and installed at site zones (Section 1.13). Scan below to fast-track verification forms.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
            modifier = Modifier.align(Alignment.Start)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(qrDeployments) { qr ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // QR Art Box
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.QrCode, contentDescription = "QR Icon", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                Text(text = qr["code"] ?: "", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = qr["type"] ?: "", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                            Text(text = qr["name"] ?: "", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(text = qr["details"] ?: "", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                            Button(
                                onClick = {
                                    selectedQrForScan = qr
                                    showScanDialog = true
                                },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .align(Alignment.End),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = "Scan", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Simulate Scan", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Scan Success Dialog
    if (showScanDialog && selectedQrForScan != null) {
        val qr = selectedQrForScan!!
        Dialog(onDismissRequest = { showScanDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color.Green.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Verified", tint = Color.Green, modifier = Modifier.size(32.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "QR Code Verified successfully!", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.Green)
                    Text(text = "Code: ${qr["code"]}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                    Text(text = "Type: ${qr["type"]}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Text(text = "Target Zone: ${qr["name"]}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                    Spacer(modifier = Modifier.height(16.dp))

                    val prefillTarget = qr["formTarget"] ?: "Delivery"
                    Button(
                        onClick = {
                            showScanDialog = false
                            // Prefill and open form
                            val payload = qr.filterKeys { it.startsWith("prefill_") }
                                .mapKeys { it.key.removePrefix("prefill_") }
                            onTriggerForm(prefillTarget, payload)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Pre-fill $prefillTarget Form")
                    }

                    OutlinedButton(
                        onClick = { showScanDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

// ==========================================
// RECHARTS BAR GRAPH COMPONENT FOR SITE PROGRESS
// ==========================================

data class ProgressChartItem(
    val id: String,
    val code: String,
    val name: String,
    val progress: Int,
    val status: String,
    val updatedBy: String,
    val remarks: String,
    val isMock: Boolean = false
)

@Composable
fun RechartsBarGraphCard(viewModel: CatisViewModel) {
    val updates by viewModel.projectStructuralUpdates.collectAsState()
    val activeProject by viewModel.activeProjectCode.collectAsState()

    var selectedFilter by remember { mutableStateOf("All") }
    var selectedPhaseId by remember { mutableStateOf<String?>(null) }

    // Fallback/Mock reference phases to populate when DB is empty
    val referenceItems = remember(activeProject) {
        listOf(
            ProgressChartItem(
                id = "ref-1",
                code = "ST-REF-01",
                name = "Site Mobilisation & Prep",
                progress = 100,
                status = "Completed",
                updatedBy = "Engr. David Okoye",
                remarks = "All temporary site housing and initial clearing complete.",
                isMock = true
            ),
            ProgressChartItem(
                id = "ref-2",
                code = "ST-REF-02",
                name = "Foundation Excavation",
                progress = 85,
                status = "In Progress",
                updatedBy = "Engr. David Okoye",
                remarks = "Soil excavation 85% complete, concrete blinding scheduled.",
                isMock = true
            ),
            ProgressChartItem(
                id = "ref-3",
                code = "ST-REF-03",
                name = "Ground Slab Casting",
                progress = 40,
                status = "In Progress",
                updatedBy = "Engr. David Okoye",
                remarks = "Reinforcement works active. Initial ready-mix concrete order placed.",
                isMock = true
            ),
            ProgressChartItem(
                id = "ref-4",
                code = "ST-REF-04",
                name = "Ground Floor Columns",
                progress = 15,
                status = "In Progress",
                updatedBy = "Engr. David Okoye",
                remarks = "Shoring erected, steel cage layout started.",
                isMock = true
            ),
            ProgressChartItem(
                id = "ref-5",
                code = "ST-REF-05",
                name = "First Floor Decking",
                progress = 0,
                status = "Not Started",
                updatedBy = "Engr. David Okoye",
                remarks = "Scheduled upon columns completion.",
                isMock = true
            )
        )
    }

    // Adapt DB updates into chart items
    val dbItems = updates.map {
        ProgressChartItem(
            id = it.id.toString(),
            code = it.elementCode,
            name = it.elementName,
            progress = it.progressPercent,
            status = it.status,
            updatedBy = it.updatedBy,
            remarks = it.remarks,
            isMock = false
        )
    }

    val isDatabaseEmpty = dbItems.isEmpty()
    val rawItems = if (isDatabaseEmpty) referenceItems else dbItems

    // Filter items based on selected filter
    val filteredItems = remember(rawItems, selectedFilter) {
        if (selectedFilter == "All") {
            rawItems
        } else {
            rawItems.filter { it.status.equals(selectedFilter, ignoreCase = true) }
        }
    }

    // Safely retrieve currently selected item
    val selectedItem = remember(filteredItems, selectedPhaseId) {
        filteredItems.find { it.id == selectedPhaseId } ?: filteredItems.firstOrNull()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Site Progress Analytics",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Interactive phase completion visualizer (Recharts-Style Engine)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Database indicator badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(
                            if (isDatabaseEmpty) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isDatabaseEmpty) "REFERENCE BASELINE" else "LIVE DB RECS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isDatabaseEmpty) MaterialTheme.colorScheme.onTertiaryContainer
                                    else MaterialTheme.colorScheme.onPrimaryContainer,
                            letterSpacing = 0.5.sp,
                            fontSize = 8.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Chips Scrollable Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val filters = listOf("All", "In Progress", "Completed", "Pending Inspection", "Not Started")
                filters.forEach { filterName ->
                    val isSelected = selectedFilter == filterName
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedFilter = filterName
                            // Reset selection if the current one is filtered out
                            selectedPhaseId = null
                        },
                        label = { Text(filterName, style = MaterialTheme.typography.bodySmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Bar Chart Visual Canvas
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                            RoundedCornerShape(16.dp)
                        )
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "No items matching filter",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No phases match '$selectedFilter' status filter.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(185.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            RoundedCornerShape(16.dp)
                        )
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Y-Axis Labels Column
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(bottom = 20.dp), // align with bars base
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        listOf("100%", "75%", "50%", "25%", "0%").forEach { label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Dotted division line representing Y-Axis
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Scrollable Horizontal Grid containing Bars
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        filteredItems.forEach { item ->
                            val isSelected = selectedItem?.id == item.id

                            // Bar Column structure
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(52.dp)
                                    .clickable(
                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        selectedPhaseId = item.id
                                    }
                            ) {
                                // Bar Height Animation (elastic physics, similar to high-fidelity SVG/Recharts transitions)
                                val animatedHeight by animateDpAsState(
                                    targetValue = (125 * (item.progress / 100f)).dp,
                                    animationSpec = spring(
                                        dampingRatio = 0.62f,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )

                                // Bar Color Animation (transitions background color seamlessly when state changes)
                                val barColorTarget = when (item.status) {
                                    "Completed" -> Color(0xFF2E7D32) // Emerald Green
                                    "In Progress" -> Color(0xFFE65100) // Sunset Orange
                                    "Pending Inspection" -> Color(0xFF7B1FA2) // Royal Purple
                                    else -> Color(0xFF78909C) // Slate Grey
                                }
                                val animatedColor by animateColorAsState(
                                    targetValue = barColorTarget,
                                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                                )

                                // Transform scale animation when active item changes (replicating CSS transform scale)
                                val animatedScale by animateFloatAsState(
                                    targetValue = if (isSelected) 1.12f else 1.0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )

                                // Opacity animation (replicating CSS opacity transitions)
                                val animatedAlpha by animateFloatAsState(
                                    targetValue = if (isSelected || selectedItem == null) 1.0f else 0.5f,
                                    animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
                                )

                                Box(
                                    modifier = Modifier
                                        .height(130.dp)
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            scaleX = animatedScale
                                            scaleY = animatedScale
                                            alpha = animatedAlpha
                                        },
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    // Track Background
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(16.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                            .border(
                                                width = if (isSelected) 1.5.dp else 0.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                    )

                                    // Colored Value Bar
                                    Box(
                                        modifier = Modifier
                                            .height(animatedHeight)
                                            .width(16.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        animatedColor.copy(alpha = 0.9f),
                                                        animatedColor
                                                    )
                                                )
                                            )
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // X-Axis Code label
                                Text(
                                    text = item.code,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chart Color Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Legend Rows
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    LegendItem(color = Color(0xFF2E7D32), label = "Completed")
                    LegendItem(color = Color(0xFFE65100), label = "In Progress")
                    LegendItem(color = Color(0xFF7B1FA2), label = "Pending")
                    LegendItem(color = Color(0xFF78909C), label = "Not Started")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Tooltip Info Box / Selected Phase Details
            if (selectedItem != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = selectedItem.code,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    )
                                }
                                Text(
                                    text = selectedItem.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Dynamic Status Badge
                            val badgeColor = when (selectedItem.status) {
                                "Completed" -> Color(0xFFE8F5E9)
                                "In Progress" -> Color(0xFFFFF3E0)
                                "Pending Inspection" -> Color(0xFFF3E5F5)
                                else -> Color(0xFFECEFF1)
                            }
                            val badgeTextColor = when (selectedItem.status) {
                                "Completed" -> Color(0xFF2E7D32)
                                "In Progress" -> Color(0xFFE65100)
                                "Pending Inspection" -> Color(0xFF7B1FA2)
                                else -> Color(0xFF455A64)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(badgeColor)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = selectedItem.status,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = badgeTextColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Percent row with Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { selectedItem.progress / 100f },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(100.dp)),
                                color = when (selectedItem.status) {
                                    "Completed" -> Color(0xFF2E7D32)
                                    "In Progress" -> Color(0xFFE65100)
                                    "Pending Inspection" -> Color(0xFF7B1FA2)
                                    else -> Color(0xFF78909C)
                                },
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )
                            Text(
                                text = "${selectedItem.progress}% Complete",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (selectedItem.remarks.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "“${selectedItem.remarks}”",
                                style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Inspector: ${selectedItem.updatedBy}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )

                            if (selectedItem.isMock) {
                                Text(
                                    text = "💡 Interactive Demonstration",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💡 Tap any phase bar above to view deep visual reports.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ==========================================
// SESSION REPORTING & JSON EXPORT COMPONENT
// ==========================================

fun generateJsonReport(
    projectCode: String,
    role: String,
    updates: List<StructuralUpdate>,
    photos: List<SiteProgressPhoto>
): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val timestamp = sdf.format(Date())

    val jsonBuilder = StringBuilder()
    jsonBuilder.append("{\n")
    
    // 1. Metadata Block
    jsonBuilder.append("  \"report_metadata\": {\n")
    jsonBuilder.append("    \"generated_at\": \"$timestamp\",\n")
    jsonBuilder.append("    \"project_code\": \"$projectCode\",\n")
    jsonBuilder.append("    \"reporter_role\": \"$role\",\n")
    jsonBuilder.append("    \"session_status\": \"ACTIVE\",\n")
    jsonBuilder.append("    \"exporter_version\": \"CATIS-1.2.0\"\n")
    jsonBuilder.append("  },\n")

    // 2. Site Progress Block
    jsonBuilder.append("  \"site_progress_phases\": [\n")
    if (updates.isEmpty()) {
        // Fallback reference baseline
        val referenceBaseline = listOf(
            mapOf("code" to "ST-REF-01", "name" to "Site Mobilisation & Prep", "progress" to "100", "status" to "Completed", "remarks" to "All temporary site housing and initial clearing complete."),
            mapOf("code" to "ST-REF-02", "name" to "Foundation Excavation", "progress" to "85", "status" to "In Progress", "remarks" to "Soil excavation 85% complete, concrete blinding scheduled."),
            mapOf("code" to "ST-REF-03", "name" to "Ground Slab Casting", "progress" to "40", "status" to "In Progress", "remarks" to "Reinforcement works active. Initial ready-mix concrete order placed."),
            mapOf("code" to "ST-REF-04", "name" to "Ground Floor Columns", "progress" to "15", "status" to "In Progress", "remarks" to "Shoring erected, steel cage layout started."),
            mapOf("code" to "ST-REF-05", "name" to "First Floor Decking", "progress" to "0", "status" to "Not Started", "remarks" to "Scheduled upon columns completion.")
        )
        referenceBaseline.forEachIndexed { index, map ->
            jsonBuilder.append("    {\n")
            jsonBuilder.append("      \"phase_code\": \"${map["code"]}\",\n")
            jsonBuilder.append("      \"phase_name\": \"${map["name"]}\",\n")
            jsonBuilder.append("      \"completion_percentage\": ${map["progress"]},\n")
            jsonBuilder.append("      \"status\": \"${map["status"]}\",\n")
            jsonBuilder.append("      \"remarks\": \"${escapeJson(map["remarks"] ?: "")}\"\n")
            jsonBuilder.append("    }")
            if (index < referenceBaseline.size - 1) jsonBuilder.append(",")
            jsonBuilder.append("\n")
        }
    } else {
        updates.forEachIndexed { index, item ->
            jsonBuilder.append("    {\n")
            jsonBuilder.append("      \"phase_code\": \"${item.elementCode}\",\n")
            jsonBuilder.append("      \"phase_name\": \"${item.elementName}\",\n")
            jsonBuilder.append("      \"completion_percentage\": ${item.progressPercent},\n")
            jsonBuilder.append("      \"status\": \"${item.status}\",\n")
            jsonBuilder.append("      \"updated_by\": \"${escapeJson(item.updatedBy)}\",\n")
            jsonBuilder.append("      \"remarks\": \"${escapeJson(item.remarks)}\"\n")
            jsonBuilder.append("    }")
            if (index < updates.size - 1) jsonBuilder.append(",")
            jsonBuilder.append("\n")
        }
    }
    jsonBuilder.append("  ],\n")

    // 3. Photo Journal Block
    jsonBuilder.append("  \"photo_journal_records\": [\n")
    photos.forEachIndexed { index, item ->
        jsonBuilder.append("    {\n")
        jsonBuilder.append("      \"photo_id\": \"${item.id}\",\n")
        jsonBuilder.append("      \"timestamp\": \"${item.timestamp}\",\n")
        jsonBuilder.append("      \"progress_zone\": \"${escapeJson(item.progressZone)}\",\n")
        jsonBuilder.append("      \"description\": \"${escapeJson(item.description)}\",\n")
        jsonBuilder.append("      \"capture_mode\": \"${if (item.isSimulated) "SIMULATED" else "CAMERA"}\"\n")
        jsonBuilder.append("    }")
        if (index < photos.size - 1) jsonBuilder.append(",")
        jsonBuilder.append("\n")
    }
    jsonBuilder.append("  ]\n")

    jsonBuilder.append("}")
    return jsonBuilder.toString()
}

private fun escapeJson(str: String): String {
    return str.replace("\\", "\\\\")
              .replace("\"", "\\\"")
              .replace("\n", "\\n")
              .replace("\r", "\\r")
              .replace("\t", "\\t")
}

@Composable
fun SessionReportExportCard(viewModel: CatisViewModel) {
    val context = LocalContext.current
    val updates by viewModel.projectStructuralUpdates.collectAsState()
    val photos by viewModel.siteProgressPhotos.collectAsState()
    val activeProject by viewModel.activeProjectCode.collectAsState()
    val role by viewModel.activeRole.collectAsState()

    var isPreviewExpanded by remember { mutableStateOf(false) }

    val jsonString = remember(activeProject, role, updates, photos) {
        generateJsonReport(activeProject, role, updates, photos)
    }

    // Launcher for file download (CreateDocument contract)
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray())
                }
                Toast.makeText(context, "Report saved to device successfully!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Report",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Session Reporting & Export",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Audit-ready JSON visualizers & document generation",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "PROJECT CODE",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = activeProject,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PROGRESS PHASES",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (updates.isEmpty()) "5 (Demo)" else "${updates.size} (Live)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "SESSION PHOTOS",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${photos.size} Captured",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Download Button
            Button(
                onClick = {
                    try {
                        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        val filename = "CATIS_Progress_Report_${activeProject}_${sdf.format(Date())}.json"
                        createDocumentLauncher.launch(filename)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Cannot open storage: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download"
                    )
                    Text(
                        text = "GENERATE & DOWNLOAD JSON REPORT",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Actions (Share and Copy)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        try {
                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "CATIS Progress Report - $activeProject")
                                putExtra(android.content.Intent.EXTRA_TEXT, jsonString)
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Progress JSON Report"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error sharing: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            modifier = Modifier.size(16.dp)
                        )
                        Text(text = "SHARE REPORT", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    }
                }

                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(jsonString))
                        Toast.makeText(context, "JSON Report copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(16.dp)
                        )
                        Text(text = "COPY JSON", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Expandable Preview Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isPreviewExpanded = !isPreviewExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Preview Generated JSON Payload",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = if (isPreviewExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = "Toggle Preview",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isPreviewExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F1216))
                        .border(1.dp, Color(0xFF2C313C), RoundedCornerShape(12.dp))
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    Text(
                        text = jsonString,
                        color = Color(0xFF86D593),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }
    }
}

// ==========================================
// SYSTEM ONBOARDING TOUR
// ==========================================

data class TourStep(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val highlightDesc: String
)

@Composable
fun CatisTourOverlay(
    onDismiss: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    
    val steps = listOf(
        TourStep(
            title = "Welcome to CATIS Hub",
            description = "Your active pane of glass for real-time site compliance, material logistics, and remote tracking.",
            icon = Icons.Default.Build,
            highlightDesc = "Use the dropdowns at the top of the hub to seamlessly toggle projects & roles."
        ),
        TourStep(
            title = "Progress Analytics",
            description = "View site completion analytics instantly. Tap on any animated progress bar to drill down and read inspector compliance remarks.",
            icon = Icons.Default.Dashboard,
            highlightDesc = "Use the filter chips to isolate Completed, In Progress, or Pending tasks."
        ),
        TourStep(
            title = "Digital Audit Reports",
            description = "Execute compliant material arrivals, daily logs, and security checks. Forms adapt automatically based on your active role.",
            icon = Icons.Default.Description,
            highlightDesc = "Role-specific workflows lock compliance logs with cryptographically secure QR scanning."
        ),
        TourStep(
            title = "JSON Document Generation",
            description = "Need to export reporting data? Instantly generate, preview, copy, or download structured compliance payloads for remote management reporting.",
            icon = Icons.Default.Download,
            highlightDesc = "Tap \"Generate & Download JSON Report\" to save audit-ready files directly to your device."
        )
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Progress & Step counter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CATIS System Tour",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        steps.forEachIndexed { idx, _ ->
                            Box(
                                modifier = Modifier
                                    .size(if (idx == currentStep) 16.dp else 8.dp, 8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (idx == currentStep) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    )
                            )
                        }
                    }
                }

                // Crossfaded step transition
                Crossfade(
                    targetState = currentStep,
                    animationSpec = tween(durationMillis = 300),
                    label = "tourCrossfade"
                ) { stepIndex ->
                    val stepData = steps[stepIndex]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Icon Frame
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = stepData.icon,
                                contentDescription = stepData.title,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Title
                        Text(
                            text = stepData.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Description
                        Text(
                            text = stepData.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 20.sp
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Helper Context Highlight
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Tip",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stepData.highlightDesc,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Navigation Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 0) {
                        TextButton(onClick = { currentStep-- }) {
                            Text(
                                text = "BACK",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    } else {
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = "SKIP",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    
                    Button(
                        onClick = {
                            if (currentStep < steps.size - 1) {
                                currentStep++
                            } else {
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = if (currentStep == steps.size - 1) "GET STARTED" else "CONTINUE",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
