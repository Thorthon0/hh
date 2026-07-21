package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.DiseaseGuideline
import com.example.data.GuidelineRepository
import com.example.data.MedicationInfo
import com.example.data.PatientNote
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CLINICAL GUIDE 2026 / 临床诊疗规范",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Text(
                            text = "孕产内科诊疗助手",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.secondary
                ),
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(38.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(19.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "User profile",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(imageVector = if (currentTab == 0) Icons.Default.MenuBook else Icons.Outlined.MenuBook, contentDescription = "指南") },
                    label = { Text("诊疗指南", fontSize = 11.sp, fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Medium) },
                    modifier = Modifier.testTag("tab_guidelines")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(imageVector = if (currentTab == 1) Icons.Default.Calculate else Icons.Outlined.Calculate, contentDescription = "计算") },
                    label = { Text("临床计算", fontSize = 11.sp, fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Medium) },
                    modifier = Modifier.testTag("tab_calculator")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(imageVector = if (currentTab == 2) Icons.Default.Forum else Icons.Outlined.Forum, contentDescription = "顾问") },
                    label = { Text("智能顾问", fontSize = 11.sp, fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Medium) },
                    modifier = Modifier.testTag("tab_chat")
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(imageVector = if (currentTab == 3) Icons.Default.Assignment else Icons.Outlined.Assignment, contentDescription = "备忘") },
                    label = { Text("临床备忘", fontSize = 11.sp, fontWeight = if (currentTab == 3) FontWeight.Bold else FontWeight.Medium) },
                    modifier = Modifier.testTag("tab_notes")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                0 -> GuidelinesTab()
                1 -> CalculatorTab(viewModel)
                2 -> ChatTab(viewModel)
                3 -> NotesTab(viewModel)
            }
        }
    }
}

// ==========================================
// TAB 0: CLINICAL GUIDELINES
// ==========================================
@Composable
fun GuidelinesTab() {
    var selectedGuideline by remember { mutableStateOf<DiseaseGuideline?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredGuidelines = remember(searchQuery) {
        GuidelineRepository.guidelines.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.shortDesc.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Welcome and Search Header
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("搜索疾病名称、症状或指南关键字...", fontSize = 14.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedTextColor = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("guideline_search_input"),
            shape = RoundedCornerShape(24.dp)
        )

        // Quick Action Cards (from the Professional Polish HTML design layout)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Hypertension
            Card(
                onClick = { searchQuery = "高血压" },
                modifier = Modifier
                    .weight(1f)
                    .height(105.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFD1E4FF)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.MonitorHeart,
                        contentDescription = "Hypertension",
                        tint = Color(0xFF001D35),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "妊娠高血压\nHypertension",
                        color = Color(0xFF001D35),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 16.sp
                    )
                }
            }

            // Card 2: Gestational Diabetes
            Card(
                onClick = { searchQuery = "GDM" },
                modifier = Modifier
                    .weight(1f)
                    .height(105.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF3F3F9)
                ),
                border = BorderStroke(1.dp, Color(0xFFC4C7CF)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.Bloodtype,
                        contentDescription = "Gestational Diabetes",
                        tint = Color(0xFF44474E),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "妊娠糖尿病\nDiabetes GDM",
                        color = Color(0xFF1A1C1E),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Section header with custom style
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "2026年专家共识推荐 EXPERT CONSENSUS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                letterSpacing = 0.5.sp
            )
            Text(
                text = "查看全部",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0061A4),
                modifier = Modifier.clickable { searchQuery = "" }
            )
        }

        if (filteredGuidelines.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "No Result",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "未找到相关内科指南",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredGuidelines) { guideline ->
                    GuidelineCard(guideline = guideline) {
                        selectedGuideline = guideline
                    }
                }

                // Add the Clinical Standards Notice at the bottom of the list for great aesthetic balance!
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF001D35)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "CERTIFICATION 权威认证",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "Maternal Health Standards V.4",
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Verified Standards",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Detailed View
    selectedGuideline?.let { guideline ->
        GuidelineDetailDialog(
            guideline = guideline,
            onDismiss = { selectedGuideline = null }
        )
    }
}

@Composable
fun GuidelineCard(
    guideline: DiseaseGuideline,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("guideline_card_${guideline.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = guideline.category,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "2026 Update Icon",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "2026 更新",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = guideline.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = guideline.shortDesc,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "查看完整诊疗规范",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Arrow Forward",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidelineDetailDialog(
    guideline: DiseaseGuideline,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Dialog Header
                TopAppBar(
                    title = {
                        Text(
                            text = guideline.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                // Tabbed Sections in Scrollable Column
                var activeDetailTab by remember { mutableStateOf(0) }

                TabRow(selectedTabIndex = activeDetailTab) {
                    Tab(selected = activeDetailTab == 0, onClick = { activeDetailTab = 0 }) {
                        PaddingBox(text = "最新共识", isSelected = activeDetailTab == 0)
                    }
                    Tab(selected = activeDetailTab == 1, onClick = { activeDetailTab = 1 }) {
                        PaddingBox(text = "诊断依据", isSelected = activeDetailTab == 1)
                    }
                    Tab(selected = activeDetailTab == 2, onClick = { activeDetailTab = 2 }) {
                        PaddingBox(text = "治疗规范", isSelected = activeDetailTab == 2)
                    }
                    Tab(selected = activeDetailTab == 3, onClick = { activeDetailTab = 3 }) {
                        PaddingBox(text = "推荐用药", isSelected = activeDetailTab == 3)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        when (activeDetailTab) {
                            0 -> { // 2026 Consensus & Flow
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.TipsAndUpdates,
                                                    contentDescription = "Tips",
                                                    tint = MaterialTheme.colorScheme.tertiary
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "2026年最新指南进展与共识:",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = guideline.consensus2026,
                                                fontSize = 14.sp,
                                                lineHeight = 22.sp,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }
                                    }

                                    Text(
                                        text = "临床诊疗流程步骤",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    guideline.flowSteps.forEach { step ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .offset(y = 6.dp)
                                                    .background(
                                                        color = MaterialTheme.colorScheme.primary,
                                                        shape = RoundedCornerShape(4.dp)
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = step,
                                                fontSize = 14.sp,
                                                lineHeight = 20.sp,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                    }
                                }
                            }
                            1 -> { // Diagnostic Criteria
                                Column {
                                    Text(
                                        text = "妊娠期确诊指标与鉴别标准",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                    guideline.diagnosticCriteria.forEach { criterion ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            )
                                        ) {
                                            Text(
                                                text = criterion,
                                                modifier = Modifier.padding(14.dp),
                                                fontSize = 14.sp,
                                                lineHeight = 22.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                            2 -> { // Treatment Standards
                                Column {
                                    Text(
                                        text = "标准化临床干预与干预界限",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                    guideline.treatmentStandard.forEach { spec ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Check",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .offset(y = 2.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = spec,
                                                fontSize = 14.sp,
                                                lineHeight = 22.sp,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                    }
                                }
                            }
                            3 -> { // Medication Details
                                Column {
                                    Text(
                                        text = "临床用药安全与用法推荐",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                    guideline.medicationAdvice.forEach { med ->
                                        MedicationAdviceCard(med)
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
fun PaddingBox(text: String, isSelected: Boolean) {
    Box(
        modifier = Modifier.padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MedicationAdviceCard(med: MedicationInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = med.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.Default.MedicalInformation,
                    contentDescription = "Med",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = "推荐剂量: ",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = med.dosage,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = "临床指征: ",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = med.indications,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = "用药禁忌与注意事项: ",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = med.precautions,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.error,
                lineHeight = 18.sp
            )
        }
    }
}

// ==========================================
// TAB 1: CLINICAL CALCULATORS
// ==========================================
@Composable
fun CalculatorTab(viewModel: MainViewModel) {
    var subTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Calculator SubTabs
        ScrollableTabRow(
            selectedTabIndex = subTab,
            edgePadding = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Tab(selected = subTab == 0, onClick = { subTab = 0 }) {
                Box(modifier = Modifier.padding(12.dp)) { Text("GDM 糖耐量诊断", fontSize = 13.sp) }
            }
            Tab(selected = subTab == 1, onClick = { subTab = 1 }) {
                Box(modifier = Modifier.padding(12.dp)) { Text("子痫前期风险预测", fontSize = 13.sp) }
            }
            Tab(selected = subTab == 2, onClick = { subTab = 2 }) {
                Box(modifier = Modifier.padding(12.dp)) { Text("ICP 严重程度评估", fontSize = 13.sp) }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (subTab) {
                0 -> GDMCalculatorScreen(viewModel)
                1 -> HDPRiskCalculatorScreen(viewModel)
                2 -> ICPCalculatorScreen(viewModel)
            }
        }
    }
}

@Composable
fun GDMCalculatorScreen(viewModel: MainViewModel) {
    val fasting by viewModel.gdmFasting.collectAsState()
    val p1h by viewModel.gdm1h.collectAsState()
    val p2h by viewModel.gdm2h.collectAsState()
    val result by viewModel.gdmResult.collectAsState()
    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "口服葡萄糖耐量试验 (75g OGTT) 诊断工具",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "请输入实验室测定的血糖结果 (单位: mmol/L)。系统将自动分析是否确诊妊娠期糖尿病或疑似孕前糖尿病。",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = fasting,
                        onValueChange = { viewModel.setGdmFasting(it) },
                        label = { Text("空腹血糖 (FPG)") },
                        suffix = { Text("mmol/L") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("gdm_input_fasting"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = p1h,
                        onValueChange = { viewModel.setGdm1h(it) },
                        label = { Text("1小时血糖 (1-h PG)") },
                        suffix = { Text("mmol/L") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("gdm_input_1h"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = p2h,
                        onValueChange = { viewModel.setGdm2h(it) },
                        label = { Text("2小时血糖 (2-h PG)") },
                        suffix = { Text("mmol/L") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("gdm_input_2h"),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.clearGdm() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("重置")
                        }
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.calculateGDM()
                            },
                            modifier = Modifier.weight(1.5f).testTag("gdm_calc_btn")
                        ) {
                            Text("诊断计算")
                        }
                    }
                }
            }
        }

        result?.let { res ->
            item {
                ResultDisplayCard(res)
            }
        }
    }
}

@Composable
fun HDPRiskCalculatorScreen(viewModel: MainViewModel) {
    val highRiskSelected by viewModel.hdpHighRiskFactors.collectAsState()
    val modRiskSelected by viewModel.hdpModRiskFactors.collectAsState()
    val result by viewModel.hdpResult.collectAsState()

    val highFactors = listOf(
        "既往子痫前期史 (Preeclampsia History)",
        "慢性高血压 (Chronic Hypertension)",
        "1型或2型糖尿病 (Type 1/2 Diabetes)",
        "慢性肾脏疾病 (Chronic Kidney Disease)",
        "自身免疫性疾病 (自身抗体阳性/SLE/APS)",
        "本次妊娠为多胎妊娠 (Multiple Gestations)"
    )

    val modFactors = listOf(
        "初产妇 (Nulliparity)",
        "高龄孕妇 (年龄 ≥ 35 岁)",
        "肥胖症 (孕前 BMI ≥ 30 kg/m²)",
        "子痫前期家族史 (一级亲属病史)",
        "通过辅助生殖技术受孕 (IVF Pregnancy)",
        "妊娠间隔时间较长 (上一次怀孕间隔 > 10年)"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "子痫前期 (Preeclampsia) 临床风险预测及一级预防评估",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "请勾选孕妇符合的临床特征。系统将依据2026年最新预防指引进行分级，给出阿司匹林和小剂量补充钙剂等关键预防用药推荐。",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Text("【高危因素】 (符合1项或以上即属高危)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
        }

        items(highFactors) { factor ->
            val isChecked = highRiskSelected.contains(factor)
            Card(
                onClick = { viewModel.toggleHighRiskFactor(factor) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isChecked) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { viewModel.toggleHighRiskFactor(factor) },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.error)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = factor, fontSize = 13.sp)
                }
            }
        }

        item {
            Text("【中危因素】 (符合2项或以上即属高危)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
        }

        items(modFactors) { factor ->
            val isChecked = modRiskSelected.contains(factor)
            Card(
                onClick = { viewModel.toggleModRiskFactor(factor) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { viewModel.toggleModRiskFactor(factor) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = factor, fontSize = 13.sp)
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = { viewModel.clearHdp() }, modifier = Modifier.weight(1f)) {
                    Text("重置")
                }
                Button(onClick = { viewModel.calculateHDP() }, modifier = Modifier.weight(1.5f).testTag("hdp_calc_btn")) {
                    Text("评估风险")
                }
            }
        }

        result?.let { res ->
            item {
                ResultDisplayCard(res)
            }
        }
    }
}

@Composable
fun ICPCalculatorScreen(viewModel: MainViewModel) {
    val icpTba by viewModel.icpTba.collectAsState()
    val result by viewModel.icpResult.collectAsState()
    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "妊娠期肝内胆汁淤积症 (ICP) 严重度评定及分娩时机决策",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "请输入测定的空腹总胆汁酸 (TBA) 检测水平。系统将依据 2026 年最新共识中的胆汁酸阈值精细化分级，自动得出推荐的管理等级、胎儿监护要求和最佳分娩去向方案。",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = icpTba,
                        onValueChange = { viewModel.setIcpTba(it) },
                        label = { Text("总胆汁酸水平 (Serum TBA)") },
                        suffix = { Text("μmol/L") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("icp_input_tba"),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.clearIcp() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("重置")
                        }
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.calculateICP()
                            },
                            modifier = Modifier.weight(1.5f).testTag("icp_calc_btn")
                        ) {
                            Text("评估并推荐")
                        }
                    }
                }
            }
        }

        result?.let { res ->
            item {
                ResultDisplayCard(res)
            }
        }
    }
}

@Composable
fun ResultDisplayCard(result: CalculatorResult) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (result.isUrgent) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (result.isUrgent) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.outline
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calculator_result")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (result.isUrgent) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = "Icon",
                    tint = if (result.isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = result.status,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (result.isUrgent) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = result.explanation,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = if (result.isUrgent) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
            )

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = if (result.isUrgent) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
            )

            Text(
                text = "临床指南处置建议:",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (result.isUrgent) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = result.recommendation,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = if (result.isUrgent) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

// ==========================================
// TAB 2: AI CLINICAL CONSULTANT
// ==========================================
@Composable
fun ChatTab(viewModel: MainViewModel) {
    val history by viewModel.chatHistory.collectAsState()
    val isLoading by viewModel.isChatLoading.collectAsState()
    var textInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val quickQuestions = listOf(
        "子痫前期小剂量阿司匹林预防指征?",
        "2026年妊娠期肝内胆汁淤积治疗目标?",
        "妊娠合并甲状腺疾病TSH控制目标?"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Medical Disclaimer
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Disclaimer Shield",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "提示：本顾问仅供学术讨论与临床参考，严禁作为实际医疗处方替代！",
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Chat Bubble List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(history) { (text, isUser) ->
                ChatBubble(text = text, isUser = isUser)
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("智能顾问思考中...", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Quick Questions row
        if (history.size <= 1) {
            Text(
                text = "热门诊疗提问:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                quickQuestions.forEach { question ->
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                focusManager.clearFocus()
                                viewModel.sendMessage(question)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = question,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Message Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { viewModel.clearChat() },
                modifier = Modifier.testTag("chat_clear_btn")
            ) {
                Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear Chat", tint = MaterialTheme.colorScheme.error)
            }

            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("咨询妊娠期疑难杂症与药理指南...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_message_input"),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3,
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank() && !isLoading) {
                                focusManager.clearFocus()
                                viewModel.sendMessage(textInput)
                                textInput = ""
                            }
                        },
                        enabled = textInput.isNotBlank() && !isLoading,
                        modifier = Modifier.testTag("chat_send_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (textInput.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun ChatBubble(text: String, isUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Icon(
                imageVector = Icons.Default.SupportAgent,
                contentDescription = "AI",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(end = 6.dp, top = 4.dp)
                    .size(24.dp)
            )
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .padding(12.dp)
        ) {
            Text(
                text = text,
                color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.5.sp,
                lineHeight = 20.sp
            )
        }

        if (isUser) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Clinician",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .padding(start = 6.dp, top = 4.dp)
                    .size(24.dp)
            )
        }
    }
}

// ==========================================
// TAB 3: CLINICAL NOTES (ROOM DATABASE)
// ==========================================
@Composable
fun NotesTab(viewModel: MainViewModel) {
    val notesList by viewModel.allNotes.collectAsState()
    var isAddingNote by remember { mutableStateOf(false) }

    // Forms and detail overlays
    var viewNoteDetails by remember { mutableStateOf<PatientNote?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddingNote = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("notes_add_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Memo")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "临床诊疗备忘录",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "可在本地安全存储和追溯患者状态、随访进度或诊疗案例点滴。数据仅保存在您本机的安全隔离区中。",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (notesList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Empty Notes",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "临床备忘录空空如也",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "点击右下角按钮，记录孕产妇患者的健康状态或案例随访笔记吧。",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(notesList) { note ->
                        PatientNoteItem(
                            note = note,
                            onCardClick = { viewNoteDetails = note },
                            onDeleteClick = { viewModel.deleteNoteById(note.id) },
                            onFavoriteClick = { viewModel.updateNote(note.copy(isFavorite = !note.isFavorite)) }
                        )
                    }
                }
            }
        }
    }

    // Modal dialogs
    if (isAddingNote) {
        AddEditNoteDialog(
            onDismiss = { isAddingNote = false },
            onSave = { note ->
                viewModel.insertNote(note)
                isAddingNote = false
            }
        )
    }

    viewNoteDetails?.let { note ->
        PatientNoteDetailsDialog(
            note = note,
            onDismiss = { viewNoteDetails = null }
        )
    }
}

@Composable
fun PatientNoteItem(
    note: PatientNote,
    onCardClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val dateString = remember(note.timestamp) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        sdf.format(Date(note.timestamp))
    }

    Card(
        onClick = onCardClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("patient_note_item_${note.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (note.conditionType) {
                            "GDM" -> Icons.Default.Bloodtype
                            "Preeclampsia" -> Icons.Default.MonitorHeart
                            "ICP" -> Icons.Default.Sick
                            else -> Icons.Default.Notes
                        },
                        contentDescription = "Condition Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ID: ${note.patientId}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${note.gestationalWeeks}周",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (note.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Fav",
                            tint = if (note.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(24.dp).testTag("delete_note_btn_${note.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = note.clinicalNotes,
                maxLines = 2,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = dateString,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteDialog(
    onDismiss: () -> Unit,
    onSave: (PatientNote) -> Unit
) {
    var patientId by remember { mutableStateOf("") }
    var gestationalWeeks by remember { mutableStateOf("") }
    var conditionType by remember { mutableStateOf("GDM") }
    var clinicalNotes by remember { mutableStateOf("") }

    // Condition metrics
    var sbp by remember { mutableStateOf("") }
    var dbp by remember { mutableStateOf("") }
    var fastingGluc by remember { mutableStateOf("") }
    var bileAcid by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "新建临床病例备忘",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = patientId,
                        onValueChange = { patientId = it },
                        label = { Text("患者标识ID (或代称)") },
                        modifier = Modifier.fillMaxWidth().testTag("note_input_patient_id"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = gestationalWeeks,
                        onValueChange = { gestationalWeeks = it },
                        label = { Text("妊娠周数") },
                        suffix = { Text("周") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("note_input_weeks"),
                        singleLine = true
                    )
                }

                item {
                    Text("核心内科并发症类型:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("GDM", "Preeclampsia", "ICP", "Other").forEach { type ->
                            val isSelected = conditionType == type
                            val displayText = when (type) {
                                "GDM" -> "GDM"
                                "Preeclampsia" -> "子痫前期"
                                "ICP" -> "ICP"
                                else -> "其他内科"
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = { conditionType = type },
                                label = { Text(displayText, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Dynamic inputs based on type
                if (conditionType == "Preeclampsia") {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = sbp,
                                onValueChange = { sbp = it },
                                label = { Text("收缩压 (SBP)") },
                                suffix = { Text("mmHg") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = dbp,
                                onValueChange = { dbp = it },
                                label = { Text("舒张压 (DBP)") },
                                suffix = { Text("mmHg") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                }

                if (conditionType == "GDM") {
                    item {
                        OutlinedTextField(
                            value = fastingGluc,
                            onValueChange = { fastingGluc = it },
                            label = { Text("空腹血糖测定") },
                            suffix = { Text("mmol/L") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                if (conditionType == "ICP") {
                    item {
                        OutlinedTextField(
                            value = bileAcid,
                            onValueChange = { bileAcid = it },
                            label = { Text("总胆汁酸水平 (TBA)") },
                            suffix = { Text("μmol/L") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = clinicalNotes,
                        onValueChange = { clinicalNotes = it },
                        label = { Text("临床描述 / 处置方案 / 备忘细节") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("note_input_text"),
                        maxLines = 5
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                            Text("取消")
                        }
                        Button(
                            onClick = {
                                if (patientId.isNotBlank() && clinicalNotes.isNotBlank()) {
                                    val note = PatientNote(
                                        patientId = patientId,
                                        gestationalWeeks = gestationalWeeks.toIntOrNull() ?: 0,
                                        conditionType = conditionType,
                                        bloodPressureSystolic = sbp.toIntOrNull(),
                                        bloodPressureDiastolic = dbp.toIntOrNull(),
                                        bloodGlucoseFasting = fastingGluc.toDoubleOrNull(),
                                        bileAcidValue = bileAcid.toDoubleOrNull(),
                                        clinicalNotes = clinicalNotes
                                    )
                                    onSave(note)
                                }
                            },
                            enabled = patientId.isNotBlank() && clinicalNotes.isNotBlank(),
                            modifier = Modifier.weight(1.5f).testTag("note_save_btn")
                        ) {
                            Text("保存记录")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PatientNoteDetailsDialog(
    note: PatientNote,
    onDismiss: () -> Unit
) {
    val dateString = remember(note.timestamp) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        sdf.format(Date(note.timestamp))
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "病例详情",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("患者标识 ID", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        Text(note.patientId, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Column {
                        Text("当前妊娠周数", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        Text("${note.gestationalWeeks} 周", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("病例核心分类", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        Text(
                            text = when (note.conditionType) {
                                "GDM" -> "妊娠期糖尿病 (GDM)"
                                "Preeclampsia" -> "子痫前期 (Preeclampsia)"
                                "ICP" -> "肝内胆汁淤积症 (ICP)"
                                else -> "其他内科合并症"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Dynamic clinical values
                if (note.conditionType == "Preeclampsia" && (note.bloodPressureSystolic != null || note.bloodPressureDiastolic != null)) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                        Row(modifier = Modifier.padding(10.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("血压状况：", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${note.bloodPressureSystolic ?: "--"} / ${note.bloodPressureDiastolic ?: "--"} mmHg", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (note.conditionType == "GDM" && note.bloodGlucoseFasting != null) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                        Row(modifier = Modifier.padding(10.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("空腹血糖值：", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${note.bloodGlucoseFasting} mmol/L", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (note.conditionType == "ICP" && note.bileAcidValue != null) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                        Row(modifier = Modifier.padding(10.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("血清总胆汁酸 (TBA)：", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${note.bileAcidValue} μmol/L", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Column {
                    Text("临床描述与治疗建议：", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = note.clinicalNotes,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Divider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "记录于 $dateString",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Button(onClick = onDismiss) {
                        Text("关闭")
                    }
                }
            }
        }
    }
}
