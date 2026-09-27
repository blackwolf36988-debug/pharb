package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.PharbAiEngine
import com.example.data.local.PostEntity
import com.example.ui.theme.PharbLiveRed
import kotlinx.coroutines.launch

/**
 * PHARB Creation Studio (Sections 9, 24, 33, 36)
 * Supports creating:
 * - Posts (Text, Image, Multi-Image, Video, GIF, Link, Poll, Voice Note, Question, File)
 * - 24h Stories
 * - Live Broadcasts
 * - Responsible AI Assistant (Hashtags, Writing Style Enhancer, Caption Generator, Anti-Spam Scanner)
 * - Offline Drafts Queue
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PharbCreateContentScreen(
    drafts: List<PostEntity>,
    isArabic: Boolean,
    onCreatePost: (
        content: String,
        postType: String,
        linkUrl: String,
        pollOptions: List<String>,
        audioSec: Int,
        fileAttachment: String,
        hashtags: String,
        mentions: String,
        location: String,
        audience: String,
        category: String,
        asDraft: Boolean
    ) -> Unit,
    onPublishDraft: (PostEntity) -> Unit,
    onCreateStory: (caption: String, mediaType: String, musicTrack: String, question: String) -> Unit,
    onStartLiveStream: (title: String, category: String) -> Unit
) {
    var creationModeTab by remember { mutableIntStateOf(0) } // 0: Post, 1: 24h Story, 2: PHARB Live
    var postContent by remember { mutableStateOf("") }
    var selectedPostType by remember { mutableStateOf("TEXT") }
    var hashtagsInput by remember { mutableStateOf("") }
    var mentionsInput by remember { mutableStateOf("") }
    var locationInput by remember { mutableStateOf("الرياض") }
    var audience by remember { mutableStateOf("PUBLIC") }
    var category by remember { mutableStateOf("تقنية") }
    var linkUrl by remember { mutableStateOf("") }
    var pollOpt1 by remember { mutableStateOf("نعم، أؤيد بشدة") }
    var pollOpt2 by remember { mutableStateOf("يحتاج لمزيد من الدراسة") }
    var pollOpt3 by remember { mutableStateOf("لدي اقتراح آخر") }
    var fileTitle by remember { mutableStateOf("PHARB_Study_Notes.pdf") }

    // Story fields
    var storyCaption by remember { mutableStateOf("") }
    var storyType by remember { mutableStateOf("IMAGE") }
    var storyMusic by remember { mutableStateOf("PHARB Ambient Horizon") }
    var storyQuestion by remember { mutableStateOf("") }

    // Live fields
    var liveTitle by remember { mutableStateOf("") }
    var liveCategory by remember { mutableStateOf("تعليم وتقنية") }

    val safetyReport = remember(postContent) {
        PharbAiEngine.scanContentSafety(postContent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = if (isArabic) "استوديو الإنشاء الذكي • PHARB Create" else "PHARB Creation Studio",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(10.dp))

        TabRow(selectedTabIndex = creationModeTab) {
            Tab(
                selected = creationModeTab == 0,
                onClick = { creationModeTab = 0 },
                text = { Text("منشور متكامل") },
                modifier = Modifier.testTag("create_tab_post")
            )
            Tab(
                selected = creationModeTab == 1,
                onClick = { creationModeTab = 1 },
                text = { Text("قصة Story 24h") },
                modifier = Modifier.testTag("create_tab_story")
            )
            Tab(
                selected = creationModeTab == 2,
                onClick = { creationModeTab = 2 },
                text = { Text("بث مباشر Live 🔴") },
                modifier = Modifier.testTag("create_tab_live")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (creationModeTab) {
            0 -> {
                // 10 Post Types Selector (Section 9)
                Text("اختر نوع المنشور:", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "TEXT" to "📝 نصي",
                        "IMAGE" to "🖼️ صورة",
                        "MULTI_IMAGE" to "🗂️ عدة صور",
                        "VIDEO" to "🎬 فيديو",
                        "POLL" to "📊 استطلاع",
                        "AUDIO" to "🎙️ صوتي",
                        "QUESTION" to "❓ سؤال",
                        "FILE" to "📄 ملف PDF",
                        "LINK" to "🔗 رابط",
                        "GIF" to "✨ GIF"
                    ).forEach { (typeKey, label) ->
                        FilterChip(
                            selected = selectedPostType == typeKey,
                            onClick = { selectedPostType = typeKey },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = postContent,
                    onValueChange = { postContent = it },
                    label = { Text("شارك أفكارك، معرفتك، أو سؤالك مع مجتمع PHARB...") },
                    minLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_post_content_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Responsible AI Writing & Hashtag Tools (Section 24 — Gemini 3.5 Flash + On-Device Hybrid AI)
                val coroutineScope = rememberCoroutineScope()
                var selectedAiStyle by remember { mutableStateOf(PharbAiEngine.AiWritingStyle.PROFESSIONAL) }
                var aiSuggestedTitles by remember { mutableStateOf<List<String>>(emptyList()) }
                var isAiLoading by remember { mutableStateOf(false) }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "مساعد الذكاء الاصطناعي المسؤول (PHARB AI • Gemini)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = if (PharbAiEngine.isLiveGeminiConfigured()) "Gemini 3.5 Flash متصل ✨" else "ذكاء PHARB الهجين الفوري ⚡",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "اختر أسلوب الصياغة الذكي:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PharbAiEngine.AiWritingStyle.entries.forEach { style ->
                                FilterChip(
                                    selected = selectedAiStyle == style,
                                    onClick = { selectedAiStyle = style },
                                    label = { Text(if (isArabic) style.labelAr else style.labelEn) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    hashtagsInput = PharbAiEngine.suggestHashtags(postContent).joinToString(" ")
                                    coroutineScope.launch {
                                        isAiLoading = true
                                        hashtagsInput = PharbAiEngine.suggestHashtagsAsync(postContent).joinToString(" ")
                                        isAiLoading = false
                                    }
                                },
                                modifier = Modifier.testTag("ai_suggest_hashtags_button")
                            ) {
                                Text("اقتراح #Hashtags")
                            }
                            OutlinedButton(
                                onClick = {
                                    postContent = PharbAiEngine.enhanceWriting(
                                        postContent,
                                        selectedAiStyle,
                                        isArabic
                                    )
                                    coroutineScope.launch {
                                        isAiLoading = true
                                        postContent = PharbAiEngine.enhanceWritingAsync(
                                            postContent,
                                            selectedAiStyle,
                                            isArabic
                                        )
                                        isAiLoading = false
                                    }
                                },
                                modifier = Modifier.testTag("ai_enhance_writing_button")
                            ) {
                                Text("تحسين الصياغة بالذكاء الاصطناعي")
                            }
                            OutlinedButton(
                                onClick = {
                                    aiSuggestedTitles = PharbAiEngine.suggestTitles(postContent, isArabic)
                                    coroutineScope.launch {
                                        isAiLoading = true
                                        aiSuggestedTitles = PharbAiEngine.suggestTitlesAsync(postContent, isArabic)
                                        isAiLoading = false
                                    }
                                },
                                modifier = Modifier.testTag("ai_suggest_titles_button")
                            ) {
                                Text("اقتراح عناوين جذابة")
                            }
                            OutlinedButton(
                                onClick = {
                                    postContent = PharbAiEngine.generateCaptionForMedia(selectedPostType, isArabic)
                                },
                                modifier = Modifier.testTag("ai_generate_caption_button")
                            ) {
                                Text("توليد Caption ذكي")
                            }
                            OutlinedButton(
                                onClick = {
                                    postContent = PharbAiEngine.translatePost(postContent)
                                    coroutineScope.launch {
                                        isAiLoading = true
                                        postContent = PharbAiEngine.translatePostAsync(postContent)
                                        isAiLoading = false
                                    }
                                },
                                modifier = Modifier.testTag("ai_translate_draft_button")
                            ) {
                                Text("ترجمة فورية (عربي/EN)")
                            }
                        }

                        if (isAiLoading) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "جارٍ المعالجة عبر محرك PHARB AI...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (aiSuggestedTitles.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "عناوين مقترحة (اضغط لاختيار عنوان):",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            aiSuggestedTitles.forEach { titleOption ->
                                TextButton(
                                    onClick = {
                                        postContent = "$titleOption\n\n$postContent".trim()
                                        aiSuggestedTitles = emptyList()
                                    }
                                ) {
                                    Text(titleOption, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Security,
                                contentDescription = null,
                                tint = if (safetyReport.isSafe) MaterialTheme.colorScheme.tertiary else PharbLiveRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = safetyReport.messageAr,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ملاحظة أمان للإنتاج: يُدار مفتاح GEMINI_API_KEY عبر لوحة Secrets للتجارب، وللإنتاج التجاري يُنصح باستخدام Firebase AI مع App Check أو خادم وسيط آمن.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Dynamic fields for Poll, Link, File, or Audio
                if (selectedPostType == "POLL") {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("خيارات الاستطلاع:", style = MaterialTheme.typography.labelLarge)
                    OutlinedTextField(
                        value = pollOpt1,
                        onValueChange = { pollOpt1 = it },
                        label = { Text("الخيار الأول") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = pollOpt2,
                        onValueChange = { pollOpt2 = it },
                        label = { Text("الخيار الثاني") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = pollOpt3,
                        onValueChange = { pollOpt3 = it },
                        label = { Text("الخيار الثالث") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (selectedPostType == "FILE") {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = fileTitle,
                        onValueChange = { fileTitle = it },
                        label = { Text("اسم الملف المرفق (مفحوص ضد البرمجيات الخبيثة)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (selectedPostType == "LINK") {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = linkUrl,
                        onValueChange = { linkUrl = it },
                        label = { Text("الرابط الخارجي (HTTPS)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hashtagsInput,
                        onValueChange = { hashtagsInput = it },
                        label = { Text("الوسوم (#Hashtags)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = mentionsInput,
                        onValueChange = { mentionsInput = it },
                        label = { Text("إشارة (@Mentions)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = locationInput,
                        onValueChange = { locationInput = it },
                        label = { Text("الموقع الاختياري") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("التصنيف") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("تحديد الجمهور (Audience):", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "PUBLIC" to "🌐 عام للجميع",
                        "FOLLOWERS" to "👥 المتابعون فقط",
                        "PRIVATE" to "🔒 أنا فقط"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = audience == key,
                            onClick = { audience = key },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (postContent.isNotBlank()) {
                                onCreatePost(
                                    postContent,
                                    selectedPostType,
                                    linkUrl,
                                    if (selectedPostType == "POLL") listOf(pollOpt1, pollOpt2, pollOpt3) else emptyList(),
                                    45,
                                    fileTitle,
                                    hashtagsInput,
                                    mentionsInput,
                                    locationInput,
                                    audience,
                                    category,
                                    true
                                )
                                postContent = ""
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("save_draft_button")
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حفظ كمسودة")
                    }

                    Button(
                        onClick = {
                            if (postContent.isNotBlank()) {
                                onCreatePost(
                                    postContent,
                                    selectedPostType,
                                    linkUrl,
                                    if (selectedPostType == "POLL") listOf(pollOpt1, pollOpt2, pollOpt3) else emptyList(),
                                    45,
                                    fileTitle,
                                    hashtagsInput,
                                    mentionsInput,
                                    locationInput,
                                    audience,
                                    category,
                                    false
                                )
                                postContent = ""
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("publish_post_button")
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("نشر الآن")
                    }
                }

                // Saved Offline Drafts Queue (Section 33)
                if (drafts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("المسودات المحفوظة (${drafts.size}):", style = MaterialTheme.typography.titleMedium)
                    drafts.forEach { draft ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(draft.content, modifier = Modifier.weight(1f), maxLines = 2)
                                TextButton(onClick = { onPublishDraft(draft) }) {
                                    Text("نشر المسودة")
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Create 24h Story
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("إضافة قصة جديدة في PHARB Stories (24 ساعة)", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = storyCaption,
                            onValueChange = { storyCaption = it },
                            label = { Text("نص القصة أو اللحظة اليومية") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = storyMusic,
                            onValueChange = { storyMusic = it },
                            label = { Text("المقطع الصوتي المرخص (اختياري)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = storyQuestion,
                            onValueChange = { storyQuestion = it },
                            label = { Text("ملصق سؤال أو استطلاع تفاعلي للمتابعين") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                if (storyCaption.isNotBlank()) {
                                    onCreateStory(storyCaption, storyType, storyMusic, storyQuestion)
                                    storyCaption = ""
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("publish_story_button")
                        ) {
                            Text("نشر القصة لمدة 24 ساعة ✨")
                        }
                    }
                }
            }

            2 -> {
                // Start PHARB Live Broadcast
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LiveTv, contentDescription = null, tint = PharbLiveRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("بدء بث مباشر على PHARB Live", style = MaterialTheme.typography.titleLarge)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = liveTitle,
                            onValueChange = { liveTitle = it },
                            label = { Text("عنوان البث المباشر") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = liveCategory,
                            onValueChange = { liveCategory = it },
                            label = { Text("تصنيف البث (تعليم، تقنية، حوار، أعمال)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                val finalTitle = liveTitle.ifBlank { "لقاء مباشر مع مجتمع PHARB" }
                                onStartLiveStream(finalTitle, liveCategory)
                                liveTitle = ""
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("start_live_stream_button")
                        ) {
                            Text("بدء البث المباشر الآن 🔴")
                        }
                    }
                }
            }
        }
    }
}
