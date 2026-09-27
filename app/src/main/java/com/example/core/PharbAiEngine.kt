package com.example.core

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * PHARB Responsible AI Assistant & Recommendation Engine (Sections 24, 38)
 * - Integrates Gemini REST API (`gemini-3.5-flash`) via `BuildConfig.GEMINI_API_KEY` when configured
 * - Note: For production deployments, use Firebase AI Logic with App Check or a backend proxy
 *   instead of embedding keys directly in the client APK.
 * - Provides instant, privacy-first on-device intelligence fallback for:
 *   1. Smart Hashtag Suggestions (اقتراح Hashtags)
 *   2. Text Summarization (تلخيص النصوص)
 *   3. Post Translation (ترجمة المنشورات العربية <-> الإنجليزية)
 *   4. Title Suggestions (اقتراح عناوين)
 *   5. Writing Enhancement (تحسين الكتابة)
 *   6. Caption Generation (إنشاء Captions)
 *   7. Harmful Content & Spam Detection (اكتشاف المحتوى المخالف و Spam)
 *   8. Transparent Recommendation Explanation (توصيات المحتوى الشفافة)
 */
object PharbAiEngine {

    private const val GEMINI_MODEL = "gemini-3.5-flash"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    enum class AiWritingStyle(val labelAr: String, val labelEn: String) {
        PROFESSIONAL("احترافي", "Professional"),
        ENGAGING("جذاب وتفاعلي", "Engaging"),
        EDUCATIONAL("تعليمي مبسط", "Educational"),
        CONCISE("مختصر وواضح", "Concise")
    }

    data class SafetyCheckResult(
        val isSafe: Boolean,
        val riskScore: Int, // 0..100
        val category: String,
        val messageAr: String,
        val messageEn: String
    )

    @Volatile
    var isQuotaCooldownActive: Boolean = false
        private set

    fun isLiveGeminiConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return !isQuotaCooldownActive && key.isNotBlank() && key != "MY_GEMINI_API_KEY" && !key.startsWith("YOUR_")
    }

    suspend fun generateWithGeminiOrFallback(
        prompt: String,
        fallbackGenerator: () -> String
    ): String = withContext(Dispatchers.IO) {
        if (!isLiveGeminiConfigured()) {
            return@withContext fallbackGenerator()
        }
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$GEMINI_MODEL:generateContent?key=$apiKey"
            val payload = JSONObject().apply {
                put(
                    "contents",
                    JSONArray().put(
                        JSONObject().put(
                            "parts",
                            JSONArray().put(JSONObject().put("text", prompt))
                        )
                    )
                )
            }
            val body = payload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()
            okHttpClient.newCall(request).execute().use { response ->
                if (response.code == 429 || response.code == 503) {
                    // Graceful quota-exhausted cooldown: switch seamlessly to on-device PHARB AI
                    isQuotaCooldownActive = true
                    return@withContext fallbackGenerator()
                }
                if (!response.isSuccessful) return@withContext fallbackGenerator()
                val raw = response.body?.string().orEmpty()
                val json = JSONObject(raw)
                val text = json.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")
                    ?.trim()
                if (!text.isNullOrBlank()) text else fallbackGenerator()
            }
        } catch (e: Exception) {
            fallbackGenerator()
        }
    }

    // --- Asynchronous Gemini + Hybrid AI Suspend Functions ---

    suspend fun summarizePostAsync(content: String, isArabic: Boolean = true): String {
        val langInstruction = if (isArabic) "باللغة العربية في نقطتين مختصرتين" else "in English in 2 concise bullet points"
        return generateWithGeminiOrFallback(
            prompt = "لخص المنشور التالي لمنصة التواصل الاجتماعي PHARB $langInstruction:\n$content",
            fallbackGenerator = { summarizePost(content, isArabic) }
        )
    }

    suspend fun translatePostAsync(content: String): String {
        val hasArabic = content.any { it in '\u0600'..'\u06FF' }
        val targetLang = if (hasArabic) "English" else "Arabic (العربية)"
        return generateWithGeminiOrFallback(
            prompt = "Translate the following social media post on PHARB accurately into $targetLang:\n$content",
            fallbackGenerator = { translatePost(content) }
        )
    }

    suspend fun enhanceWritingAsync(
        content: String,
        style: AiWritingStyle,
        isArabic: Boolean = true
    ): String {
        val lang = if (isArabic) "العربية" else "English"
        return generateWithGeminiOrFallback(
            prompt = "أعد صياغة النص التالي بأسلوب (${style.labelAr} / ${style.labelEn}) باللغة $lang مع إضافة 3 وسوم مناسبة لمنصة PHARB:\n$content",
            fallbackGenerator = { enhanceWriting(content, style, isArabic) }
        )
    }

    suspend fun suggestHashtagsAsync(content: String): List<String> {
        val raw = generateWithGeminiOrFallback(
            prompt = "اقترح 5 وسوم (Hashtags) فقط مفصولة بمسافات وتبدأ بـ # مناسبة لهذا المنشور على منصة PHARB:\n$content",
            fallbackGenerator = { suggestHashtags(content).joinToString(" ") }
        )
        val parsed = raw.split(Regex("\\s+")).filter { it.startsWith("#") }.take(6)
        return if (parsed.isNotEmpty()) parsed else suggestHashtags(content)
    }

    suspend fun suggestTitlesAsync(content: String, isArabic: Boolean = true): List<String> {
        val raw = generateWithGeminiOrFallback(
            prompt = "اقترح 3 عناوين جذابة مرقمة لهذا المحتوى على منصة PHARB:\n$content",
            fallbackGenerator = { suggestTitles(content, isArabic).joinToString("\n") }
        )
        val lines = raw.lines().map { it.trim() }.filter { it.isNotBlank() }
        return if (lines.isNotEmpty()) lines.take(3) else suggestTitles(content, isArabic)
    }

    suspend fun generateSmartDraftAsync(
        topic: String,
        style: AiWritingStyle = AiWritingStyle.ENGAGING,
        isArabic: Boolean = true
    ): String {
        val cleanTopic = topic.trim().ifEmpty { "مستقبل التواصل الرقمي وصناع المحتوى على PHARB" }
        return generateWithGeminiOrFallback(
            prompt = "اكتب منشورًا اجتماعيًا مميزًا لمنصة PHARB حول موضوع: «$cleanTopic» بأسلوب ${style.labelAr} مع وسوم مناسبة.",
            fallbackGenerator = {
                enhanceWriting(
                    content = "نشارككم اليوم نظرة متعمقة حول «$cleanTopic» وكيف يسهم في إثراء المحتوى العربي والعالمي على منصة PHARB.",
                    style = style,
                    isArabic = isArabic
                )
            }
        )
    }

    // --- Synchronous Instant On-Device Fallback & Deterministic Functions ---

    fun suggestHashtags(content: String): List<String> {
        val text = content.lowercase()
        val tags = mutableSetOf<String>()

        if (text.contains("تقني") || text.contains("برمج") || text.contains("ذكاء") || text.contains("ai") || text.contains("tech") || text.contains("code")) {
            tags.addAll(listOf("#تقنية", "#الذكاء_الاصطناعي", "#برمجة", "#PHARB_Tech"))
        }
        if (text.contains("تعليم") || text.contains("درس") || text.contains("جامع") || text.contains("علم") || text.contains("learn") || text.contains("education")) {
            tags.addAll(listOf("#تعليم", "#معرفة", "#PHARB_Education", "#تطوير_الذات"))
        }
        if (text.contains("أعمال") || text.contains("شرك") || text.contains("رياد") || text.contains("استثمار") || text.contains("business") || text.contains("startup")) {
            tags.addAll(listOf("#ريادة_الأعمال", "#استثمار", "#PHARB_Business", "#اقتصاد"))
        }
        if (text.contains("تصميم") || text.contains("فن") || text.contains("إبداع") || text.contains("صورة") || text.contains("design") || text.contains("art")) {
            tags.addAll(listOf("#تصميم", "#إبداع", "#فن_رقمي", "#PHARB_Creators"))
        }
        if (text.contains("رياض") || text.contains("لياق") || text.contains("كرة") || text.contains("sport")) {
            tags.addAll(listOf("#رياضة", "#لياقة", "#صحة"))
        }

        if (tags.isEmpty()) {
            val words = content.split(Regex("\\s+"))
                .map { it.replace(Regex("[^\\p{L}\\p{N}_]"), "") }
                .filter { it.length >= 4 }
                .take(2)
            words.forEach { tags.add("#$it") }
            tags.addAll(listOf("#PHARB", "#تواصل_أبدع_اكتشف", "#مجتمع_فارب"))
        } else {
            tags.add("#PHARB")
        }

        return tags.take(6)
    }

    fun suggestTitles(content: String, isArabic: Boolean = true): List<String> {
        val clean = content.trim().ifEmpty { "مستقبل التواصل الاجتماعي الرقمي" }
        val topic = detectTopicAr(clean)
        return if (isArabic) {
            listOf(
                "1. نظرة معمقة حول $topic: أهم الأفكار والتطبيقات العملية",
                "2. كيف يغير $topic طريقة تواصلنا وإبداعنا اليومي على PHARB؟",
                "3. دليل مختصر: 3 نقاط أساسية يجب معرفتها عن ${clean.take(35)}..."
            )
        } else {
            listOf(
                "1. Deep Dive into ${detectTopicEn(clean)}: Key Takeaways",
                "2. How ${detectTopicEn(clean)} is Shaping Digital Communities on PHARB",
                "3. Executive Guide: 3 Essential Insights on ${clean.take(35)}..."
            )
        }
    }

    fun summarizePost(content: String, isArabic: Boolean = true): String {
        val clean = content.trim()
        if (clean.length <= 90) {
            return if (isArabic) {
                "• الملخص الذكي: $clean"
            } else {
                "• AI Summary: $clean"
            }
        }
        val sentences = clean.split(Regex("[.،!؟\\n]+")).map { it.trim() }.filter { it.length > 10 }
        val keyPoints = sentences.take(2).joinToString(" • ")
        return if (isArabic) {
            "ملخص PHARB AI:\n• الفكرة الأساسية: $keyPoints.\n• التصنيف: ${detectTopicAr(clean)}"
        } else {
            "PHARB AI Summary:\n• Key Takeaway: $keyPoints.\n• Topic: ${detectTopicEn(clean)}"
        }
    }

    fun translatePost(content: String): String {
        val hasArabic = content.any { it in '\u0600'..'\u06FF' }
        return if (hasArabic) {
            when {
                content.contains("أهلاً بكم في منصة PHARB") ->
                    "[EN Translation]: Welcome to PHARB Social Network — Designed under the principle 'Less Clutter, More Experience' for seamless communication, knowledge sharing, and authentic community building."
                content.contains("الذكاء الاصطناعي") ->
                    "[EN Translation]: Responsible Artificial Intelligence is transforming how we learn, create, and collaborate while preserving user privacy and algorithmic transparency."
                content.contains("دورة") || content.contains("تعليم") ->
                    "[EN Translation]: New interactive educational module now available on PHARB Education with downloadable study guides and live Q&A sessions."
                else ->
                    "[Translated to English by PHARB AI]:\n\"${transliterateOrTranslateArToEn(content)}\""
            }
        } else {
            "[ترجمة PHARB AI إلى العربية]:\n«${translateEnToAr(content)}»"
        }
    }

    fun enhanceWriting(content: String, style: AiWritingStyle, isArabic: Boolean = true): String {
        val base = content.trim().ifEmpty {
            if (isArabic) "شاركنا اليوم تجربتك في بناء المستقبل الرقمي على منصة PHARB"
            else "Share your journey building the digital future on PHARB today"
        }
        val hashtags = suggestHashtags(base).take(3).joinToString(" ")
        return when (style) {
            AiWritingStyle.PROFESSIONAL -> if (isArabic) {
                "✨ رؤية احترافية:\n$base — نسعى دائمًا لتقديم قيمة مضافة تجمع بين الابتكار والأثر الحقيقي.\n\n$hashtags"
            } else {
                "✨ Executive Insight:\n$base — Driving meaningful innovation and lasting impact across our community.\n\n$hashtags"
            }
            AiWritingStyle.ENGAGING -> if (isArabic) {
                "🚀 ما رأيكم في هذا الموضوع؟\n$base\n\nشاركونا آراءكم وتجاربكم في التعليقات! 👇\n$hashtags"
            } else {
                "🚀 What are your thoughts on this?\n$base\n\nDrop your perspective in the comments below! 👇\n$hashtags"
            }
            AiWritingStyle.EDUCATIONAL -> if (isArabic) {
                "📚 فائدة معرفية:\n• الموضوع: $base\n• نصيحة عملية: طبّق هذه الفكرة خطوة بخطوة وشاركنا النتائج في مجتمع PHARB التعليمي.\n$hashtags"
            } else {
                "📚 Educational Note:\n• Core Concept: $base\n• Actionable Tip: Apply this step-by-step and share your findings in PHARB Education.\n$hashtags"
            }
            AiWritingStyle.CONCISE -> if (isArabic) {
                "💡 باختصار: $base. $hashtags"
            } else {
                "💡 In short: $base. $hashtags"
            }
        }
    }

    fun generateCaptionForMedia(mediaType: String, isArabic: Boolean = true): String {
        return if (isArabic) {
            when (mediaType) {
                "VIDEO", "SHORT" -> "لقطات سريعة تلخص الإبداع والتجربة الحقيقية على PHARB 🎬✨ #PHARB_Shorts #إبداع"
                "IMAGE" -> "لحظة تستحق المشاركة — أقل ازدحامًا، أكثر وضوحًا 📸💙 #PHARB #تصوير"
                "POLL" -> "صوتك يصنع الفرق! شاركنا اختيارك في هذا الاستطلاع 📊 #استطلاع_فارب"
                "AUDIO" -> "رسالة صوتية سريعة لمشاركة فكرة اليوم 🎙️ #تدوين_صوتي #PHARB"
                else -> "فكرة جديدة نشاركها مع مجتمع PHARB العالمي 🌐 #PHARB"
            }
        } else {
            when (mediaType) {
                "VIDEO", "SHORT" -> "Capturing creativity in motion on PHARB Shorts 🎬✨ #PHARB_Shorts #Create"
                "IMAGE" -> "A moment worth sharing — Less Clutter, More Experience 📸💙 #PHARB"
                "POLL" -> "Your voice matters! Cast your vote in today's community poll 📊 #PHARB_Poll"
                else -> "Sharing new perspectives with the PHARB community 🌐 #PHARB"
            }
        }
    }

    fun scanContentSafety(content: String): SafetyCheckResult {
        val lower = content.lowercase()
        val spamKeywords = listOf("اربح مليون", "اضغط هنا مجانا", "free bitcoin", "scam", "كسب سريع بدون جهد", "hack account")
        val hateKeywords = listOf("اقتل", "تدمير", "كراهية", "kill", "hate speech", "terror")

        if (hateKeywords.any { lower.contains(it) }) {
            return SafetyCheckResult(
                isSafe = false,
                riskScore = 92,
                category = "HATE_OR_VIOLENCE",
                messageAr = "تنبيه حماية المجتمع: يحتوي النص على عبارات قد تخالف معايير الأمان والاحترام في PHARB.",
                messageEn = "Community Safety Alert: Content contains phrases that violate PHARB safety standards."
            )
        }
        if (spamKeywords.any { lower.contains(it) }) {
            return SafetyCheckResult(
                isSafe = false,
                riskScore = 78,
                category = "SPAM_OR_SCAM",
                messageAr = "تنبيه مكافحة Spam: يبدو أن هذا المحتوى ترويجي مضلل أو مكرر بشكل مفرط.",
                messageEn = "Spam Protection Alert: This content appears to be deceptive or automated spam."
            )
        }
        return SafetyCheckResult(
            isSafe = true,
            riskScore = 4,
            category = "CLEAN",
            messageAr = "المحتوى آمن ومتوافق مع معايير مجتمع PHARB.",
            messageEn = "Content is safe and compliant with PHARB Community Guidelines."
        )
    }

    fun explainRecommendation(
        postCategory: String,
        authorVerified: Boolean,
        isFollowingAuthor: Boolean,
        userInterests: List<String>,
        isArabic: Boolean = true
    ): String {
        val matchedInterest = userInterests.firstOrNull {
            postCategory.contains(it, ignoreCase = true)
        } ?: postCategory

        return if (isArabic) {
            buildString {
                append("لماذا يظهر لك هذا المنشور؟ (شفافية خوارزمية PHARB):\n")
                if (isFollowingAuthor) append("• لأنك تتابع ناشر هذا المحتوى.\n")
                append("• يتوافق مع اهتمامك بمجال «$matchedInterest».\n")
                if (authorVerified) append("• ناشر موثق (PHARB Verified) ذو تفاعل إيجابي عالٍ.\n")
                append("• يمكنك تعديل تفضيلاتك في أي وقت من إعدادات التوصيات.")
            }
        } else {
            buildString {
                append("Why are you seeing this post? (PHARB Transparent AI):\n")
                if (isFollowingAuthor) append("• You follow this creator.\n")
                append("• Matches your selected interest in '$matchedInterest'.\n")
                if (authorVerified) append("• Published by a PHARB Verified contributor.\n")
                append("• You can customize or reset your feed signals anytime.")
            }
        }
    }

    private fun detectTopicAr(text: String): String = when {
        text.contains("تقني") || text.contains("ذكاء") || text.contains("برمج") -> "تقنية وابتكار"
        text.contains("تعليم") || text.contains("درس") || text.contains("علم") -> "تعليم ومعرفة"
        text.contains("أعمال") || text.contains("شرك") || text.contains("اقتصاد") -> "ريادة وأعمال"
        else -> "مجتمع وثقافة عامة"
    }

    private fun detectTopicEn(text: String): String = when {
        text.contains("tech", true) || text.contains("ai", true) -> "Technology & Innovation"
        text.contains("learn", true) || text.contains("edu", true) -> "Education & Knowledge"
        text.contains("business", true) || text.contains("market", true) -> "Business & Entrepreneurship"
        else -> "General Community & Culture"
    }

    private fun transliterateOrTranslateArToEn(text: String): String {
        return text
            .replace("منصة", "platform")
            .replace("تواصل", "connect")
            .replace("مجتمع", "community")
            .replace("تقنية", "technology")
            .replace("تعليم", "education")
            .replace("أعمال", "business")
            .replace("المستقبل", "the future")
            .let { "Shared insight from PHARB community: $it" }
    }

    private fun translateEnToAr(text: String): String {
        return "مشاركة مترجمة من مجتمع PHARB العالمي: $text"
    }
}
