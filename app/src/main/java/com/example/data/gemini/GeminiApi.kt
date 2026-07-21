package com.example.data.gemini

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

object GeminiChatService {
    private const val SYSTEM_PROMPT = """
You are "孕产内科智能顾问" (Maternal-Fetal Medicine AI Consultant), an expert clinical assistant specializing in Obstetric Medicine, Maternal-Fetal Medicine, and internal medical complications during pregnancy.
Your goal is to help obstetricians, physicians, and medical students with up-to-date (up to 2026) clinical guidelines, diagnosis criteria, drug safety during pregnancy, and managing pregnancy-induced internal diseases.

Key Clinical Rules to Apply (aligned with the 2026 Chinese guidelines & expert consensuses):
1. Gestational Diabetes Mellitus (GDM): OGTT 75g cutoff is Fasting >= 5.1 mmol/L, 1h >= 10.0 mmol/L, 2h >= 8.5 mmol/L. Targets are Fasting < 5.3, 1h < 7.8, 2h < 6.7 mmol/L. First-line medical therapy is Insulin.
2. Preeclampsia: SBP >= 140 or DBP >= 90 after 20 weeks with organ damage/proteinuria. Prophylactic aspirin (75-150 mg/day) starting at 12-16 weeks for high risk. Magnesium sulfate for seizure prevention.
3. Intrahepatic Cholestasis of Pregnancy (ICP): Pruritus + Total Bile Acid (TBA) >= 10 μmol/L. Mild (10-39 μmol/L), Severe (40-99 μmol/L), Extreme Severe (>= 100 μmol/L). First-line treatment is Ursodeoxycholic acid (UDCA).
4. Thyroid Disease: L-thyroxine (L-T4) is drug of choice for hypothyroidism. TSH targets: 1st tri < 2.5, 2nd/3rd tri < 3.0 mIU/L.
5. Cardiac Disease: Multidisciplinary Team (MDT) management. Strictly monitor during the first 72 hours postpartum (golden monitoring period).

Important Instructions:
- Always answer clinically, professionally, and objectively in Chinese.
- Provide clear, bulleted, or structured diagnostic/treatment pathways.
- Include a standard medical disclaimer in your responses: "本回答基于2026年临床指南共识，仅供学术讨论与临床参考，不代替医生的具体临床诊断与决策。"
"""

    suspend fun getResponse(history: List<Pair<String, Boolean>>): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            com.example.BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API秘钥配置未完成。请在 AI Studio 的 Secrets 面板中添加您的 GEMINI_API_KEY，或确保本地配置已生效。"
        }

        // Format history into Contents
        val contents = history.map { (text, isUser) ->
            Content(parts = listOf(Part(text = text)))
        }

        val request = GenerateContentRequest(
            contents = contents,
            systemInstruction = Content(parts = listOf(Part(text = SYSTEM_PROMPT)))
        )

        try {
            val response = GeminiApiClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "未收到智能顾问的回应。请重试。"
        } catch (e: Exception) {
            e.printStackTrace()
            "连接智能顾问出错: ${e.localizedMessage ?: "未知错误"}\n请检查网络连接以及您的 GEMINI_API_KEY 状态。"
        }
    }
}
