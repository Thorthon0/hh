package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PatientNote
import com.example.data.PatientNoteRepository
import com.example.data.gemini.GeminiChatService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PatientNoteRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PatientNoteRepository(database.patientNoteDao())
    }

    // --- Local DB Patient Notes ---
    val allNotes: StateFlow<List<PatientNote>> = repository.allItemsStateFlow(viewModelScope)

    private fun PatientNoteRepository.allItemsStateFlow(scope: kotlinx.coroutines.CoroutineScope) =
        allNotes.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insertNote(note: PatientNote) {
        viewModelScope.launch {
            repository.insert(note)
        }
    }

    fun updateNote(note: PatientNote) {
        viewModelScope.launch {
            repository.update(note)
        }
    }

    fun deleteNoteById(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    // --- Gemini Chat Assistant State ---
    private val _chatHistory = MutableStateFlow<List<Pair<String, Boolean>>>(
        listOf(
            Pair("您好！我是您的 孕产内科智能顾问 🩺。我可以为您提供2026年最新的妇女妊娠期相关的合并症、并发症诊疗规范、疑难杂症专家共识进展、以及临床合理用药建议。请问有什么我可以帮您的？", false)
        )
    )
    val chatHistory: StateFlow<List<Pair<String, Boolean>>> = _chatHistory.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    fun sendMessage(message: String) {
        if (message.isBlank()) return

        // Append user message
        val updatedHistory = _chatHistory.value + Pair(message, true)
        _chatHistory.value = updatedHistory
        _isChatLoading.value = true

        viewModelScope.launch {
            val response = GeminiChatService.getResponse(updatedHistory)
            _chatHistory.value = _chatHistory.value + Pair(response, false)
            _isChatLoading.value = false
        }
    }

    fun clearChat() {
        _chatHistory.value = listOf(
            Pair("您好！我是您的 孕产内科智能顾问 🩺。我可以为您提供2026年最新的妇女妊娠期相关的合并症、并发症诊疗规范、疑难杂症专家共识进展、以及临床合理用药建议。请问有什么我可以帮您的？", false)
        )
    }

    // --- GDM Calculator State ---
    private val _gdmFasting = MutableStateFlow("")
    val gdmFasting = _gdmFasting.asStateFlow()

    private val _gdm1h = MutableStateFlow("")
    val gdm1h = _gdm1h.asStateFlow()

    private val _gdm2h = MutableStateFlow("")
    val gdm2h = _gdm2h.asStateFlow()

    private val _gdmResult = MutableStateFlow<CalculatorResult?>(null)
    val gdmResult = _gdmResult.asStateFlow()

    fun setGdmFasting(value: String) { _gdmFasting.value = value }
    fun setGdm1h(value: String) { _gdm1h.value = value }
    fun setGdm2h(value: String) { _gdm2h.value = value }

    fun calculateGDM() {
        val fasting = _gdmFasting.value.toDoubleOrNull()
        val p1h = _gdm1h.value.toDoubleOrNull()
        val p2h = _gdm2h.value.toDoubleOrNull()

        if (fasting == null && p1h == null && p2h == null) {
            _gdmResult.value = CalculatorResult(
                isUrgent = false,
                status = "输入无效",
                explanation = "请至少输入空腹、服糖后1小时或2小时的其中一项血糖测定数值 (单位: mmol/L)。",
                recommendation = "参考标准：空腹限制为 5.1，1小时限制为 10.0，2小时限制为 8.5。"
            )
            return
        }

        var isGdm = false
        var isPgdm = false
        val reasons = mutableListOf<String>()

        if (fasting != null) {
            if (fasting >= 7.0) {
                isPgdm = true
                reasons.add("空腹血糖明显升高 (FPG ≥7.0 mmol/L) 达到孕前糖尿病 (PGDM) 诊断阈值")
            } else if (fasting >= 5.1) {
                isGdm = true
                reasons.add("空腹血糖升高 (FPG ${fasting} mmol/L ≥ 5.1 mmol/L)")
            }
        }

        if (p1h != null && p1h >= 10.0) {
            isGdm = true
            reasons.add("口服糖耐量 (75g OGTT) 服糖后1小时血糖升高 (${p1h} mmol/L ≥ 10.0 mmol/L)")
        }

        if (p2h != null) {
            if (p2h >= 11.1) {
                isPgdm = true
                reasons.add("服糖后2小时血糖严重升高 (${p2h} mmol/L ≥ 11.1 mmol/L)")
            } else if (p2h >= 8.5) {
                isGdm = true
                reasons.add("口服糖耐量 (75g OGTT) 服糖后2小时血糖升高 (${p2h} mmol/L ≥ 8.5 mmol/L)")
            }
        }

        when {
            isPgdm -> {
                _gdmResult.value = CalculatorResult(
                    isUrgent = true,
                    status = "疑似孕前合并糖尿病 (PGDM)",
                    explanation = "检测数值已达到非孕期糖尿病的确诊上限：\n" + reasons.joinToString("\n• ", "• "),
                    recommendation = "1. 建议尽快检测糖化血红蛋白 (HbA1c) 评估长期血糖水平。\n" +
                            "2. 需立即启动内分泌科与产科的多学科联合会诊 (MDT)。\n" +
                            "3. 立即实施医学营养治疗 (MNT) 并进行每日血糖自我监测 (FPG <5.3, 餐后2h <6.7 mmol/L)，必要时立即启用胰岛素治疗。"
                )
            }
            isGdm -> {
                _gdmResult.value = CalculatorResult(
                    isUrgent = false,
                    status = "妊娠期糖尿病确诊 (GDM)",
                    explanation = "符合2026临床共识的GDM诊断条件（75g OGTT一例或多例超标）：\n" + reasons.joinToString("\n• ", "• "),
                    recommendation = "1. 开启医学营养治疗 (MNT) 与运动管理，维持碳水占比 45-50%，少量多餐。\n" +
                            "2. 监测每日指尖血血糖（空腹和三餐后2小时）。\n" +
                            "3. 控制目标：空腹 <5.3 mmol/L，餐后2小时 <6.7 mmol/L。\n" +
                            "4. 若进行1-2周饮食与运动调整后仍有超过30%的血糖值超标，应积极启动胰岛素治疗。"
                )
            }
            else -> {
                _gdmResult.value = CalculatorResult(
                    isUrgent = false,
                    status = "糖耐量测定正常 (Normal)",
                    explanation = "所有录入的血糖检测指标均低于妊娠期糖耐量异常诊断限值。",
                    recommendation = "继续维持健康的孕期均衡膳食，规律运动，保持适宜的体重增幅。建议产后继续按常规要求进行保健。"
                )
            }
        }
    }

    fun clearGdm() {
        _gdmFasting.value = ""
        _gdm1h.value = ""
        _gdm2h.value = ""
        _gdmResult.value = null
    }

    // --- Preeclampsia Calculator State ---
    private val _hdpHighRiskFactors = MutableStateFlow(setOf<String>())
    val hdpHighRiskFactors = _hdpHighRiskFactors.asStateFlow()

    private val _hdpModRiskFactors = MutableStateFlow(setOf<String>())
    val hdpModRiskFactors = _hdpModRiskFactors.asStateFlow()

    private val _hdpResult = MutableStateFlow<CalculatorResult?>(null)
    val hdpResult = _hdpResult.asStateFlow()

    fun toggleHighRiskFactor(factor: String) {
        val current = _hdpHighRiskFactors.value.toMutableSet()
        if (current.contains(factor)) current.remove(factor) else current.add(factor)
        _hdpHighRiskFactors.value = current
    }

    fun toggleModRiskFactor(factor: String) {
        val current = _hdpModRiskFactors.value.toMutableSet()
        if (current.contains(factor)) current.remove(factor) else current.add(factor)
        _hdpModRiskFactors.value = current
    }

    fun calculateHDP() {
        val highCount = _hdpHighRiskFactors.value.size
        val modCount = _hdpModRiskFactors.value.size

        if (highCount == 0 && modCount == 0) {
            _hdpResult.value = CalculatorResult(
                isUrgent = false,
                status = "未选择风险因子",
                explanation = "请根据孕妇的实际病史和状况，勾选上述高危或中危临床特征。",
                recommendation = "子痫前期重在早期预防，需在孕12-16周完成风险筛查。"
            )
            return
        }

        val isHighRisk = highCount >= 1 || modCount >= 2

        if (isHighRisk) {
            _hdpResult.value = CalculatorResult(
                isUrgent = true,
                status = "子痫前期 (Preeclampsia) 高风险人群",
                explanation = "已检出关键危险因子：\n" +
                        "• 高危因素 ${highCount} 项\n" +
                        "• 中危因素 ${modCount} 项\n" +
                        "根据 2026年最新临床预防共识，该孕妇发生子痫前期的概率显著升高。",
                recommendation = "【一级预防和用药建议】：\n" +
                        "1. 强烈建议于妊娠 12-16 周（最迟不超过孕20周）开始每日口服小剂量阿司匹林 (75-150 mg)，建议睡前服用，持续至孕36周。\n" +
                        "2. 补充足量钙剂：自孕早期起补充碳酸钙或柠檬酸钙 (1.5-2.0 g/天)，可显著降低膳食缺钙人群的血压异常概率。\n" +
                        "3. 紧密监测：此后每次产检均应精准测定血压及尿蛋白，留意有无双下肢水肿、头痛、视物模糊等子痫前期先兆症状。"
            )
        } else {
            _hdpResult.value = CalculatorResult(
                isUrgent = false,
                status = "中/低风险状态",
                explanation = "已检出中危因素 ${modCount} 项，暂无主要高危病史。暂不符合阿司匹林药物干预的强制推荐标准。",
                recommendation = "1. 正常进行产前常规保健与教育。\n" +
                        "2. 指导合理饮食，低盐低脂，避免体重增长过快。\n" +
                        "3. 每次常规产检均测血压，若在妊娠晚期出现血压 ≥140/90 mmHg，需及时收治评估。"
            )
        }
    }

    fun clearHdp() {
        _hdpHighRiskFactors.value = emptySet()
        _hdpModRiskFactors.value = emptySet()
        _hdpResult.value = null
    }

    // --- ICP Calculator State ---
    private val _icpTba = MutableStateFlow("")
    val icpTba = _icpTba.asStateFlow()

    private val _icpResult = MutableStateFlow<CalculatorResult?>(null)
    val icpResult = _icpResult.asStateFlow()

    fun setIcpTba(value: String) { _icpTba.value = value }

    fun calculateICP() {
        val tba = _icpTba.value.toDoubleOrNull()
        if (tba == null || tba < 0.0) {
            _icpResult.value = CalculatorResult(
                isUrgent = false,
                status = "输入无效",
                explanation = "请输入血清总胆汁酸 (TBA) 测定值（必须为正数，单位: μmol/L）。",
                recommendation = "正常孕妇血清总胆汁酸 (TBA) 水平通常 <10 μmol/L。"
            )
            return
        }

        when {
            tba < 10.0 -> {
                _icpResult.value = CalculatorResult(
                    isUrgent = false,
                    status = "总胆汁酸正常 (TBA <10 μmol/L)",
                    explanation = "总胆汁酸水平在正常妊娠限度内 (${tba} μmol/L)。",
                    recommendation = "若孕妇皮肤瘙痒症状明显且查无原发皮疹，建议在一周后复查总胆汁酸 (TBA) 和肝功能。注意排除其他过敏、妊娠期瘙痒症或接触性皮炎。"
                )
            }
            tba < 40.0 -> {
                _icpResult.value = CalculatorResult(
                    isUrgent = false,
                    status = "轻度妊娠期肝内胆汁淤积症 (Mild ICP)",
                    explanation = "总胆汁酸水平处于轻度异常范围 (10 - 39.9 μmol/L)。突发不明原因死胎风险较低。",
                    recommendation = "1. 首选口服药物：熊去氧胆酸 (UDCA) 10-15 mg/kg/d (常用 250mg, Tid)，可有效下调胆汁酸并缓解瘙痒。\n" +
                            "2. 监测：每1-2周复查空腹 TBA 和肝酶活性 (ALT, AST)。\n" +
                            "3. 胎儿监护：自孕32-34周开始启动定期胎心监护 (NST)，指导孕妇自我数胎动。\n" +
                            "4. 分娩时机：在母胎状况稳定的前提下，可期待随访至孕 37-38 周适时终止妊娠。"
                )
            }
            tba < 100.0 -> {
                _icpResult.value = CalculatorResult(
                    isUrgent = true,
                    status = "重度妊娠期肝内胆汁淤积症 (Severe ICP)",
                    explanation = "总胆汁酸水平明显升高 (40 - 99.9 μmol/L)。围产儿发生早产、胎儿窘迫、胎粪吸入综合征的风险显著增加。",
                    recommendation = "1. 积极药物治疗：足量熊去氧胆酸 (UDCA) 治疗，若胆汁酸持续上涨或瘙痒剧烈，可联合腺苷蛋氨酸 (SAMe) 静滴。\n" +
                            "2. 胎儿监护：自孕28-30周开始高频 NST 监护，严密追踪产科彩超及脐动脉血流。\n" +
                            "3. 促进胎肺成熟：若在34周后分娩，应提前给予地塞米松促胎肺成熟。\n" +
                            "4. 分娩选择：2026推荐分娩时机在孕 36-37 周。若药物无效或出现胎儿窘迫迹象需立即剖宫产。"
                )
            }
            else -> {
                _icpResult.value = CalculatorResult(
                    isUrgent = true,
                    status = "极重度妊娠期肝内胆汁淤积症 (Extreme Severe ICP)",
                    explanation = "总胆汁酸极高 (${tba} μmol/L ≥ 100 μmol/L)！处于2026版共识的绝对高危警戒区，突发围产儿宫内死亡（死胎）风险数十倍升高，且常常无法被 NST 提前预测！",
                    recommendation = "1. 【强制干预】：【必须立即收住院管理】。\n" +
                            "2. 加强胎儿监测：实施高频或连续胎心率监测，完善超声生物物理评分。\n" +
                            "3. 药物全火力：最大耐受量熊去氧胆酸口服联合腺苷蛋氨酸静脉给药，并监测凝血功能（酌情补充维生素 K1）。\n" +
                            "4. 分娩时机：立即启动地塞米松进行胎肺成熟度促化，并在孕 35-36 周终止妊娠，不宜期待超过36周。"
                )
            }
        }
    }

    fun clearIcp() {
        _icpTba.value = ""
        _icpResult.value = null
    }
}

data class CalculatorResult(
    val isUrgent: Boolean,
    val status: String,
    val explanation: String,
    val recommendation: String
)
