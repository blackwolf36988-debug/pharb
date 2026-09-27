package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.ui.theme.PharbAvatar
import com.example.ui.theme.PharbDeepNavy
import com.example.ui.theme.PharbLiveRed
import com.example.ui.theme.PharbOnlineGreen
import com.example.ui.theme.PharbSoftBlue
import com.example.ui.viewmodel.ActiveCallState

@Composable
fun PharbMessengerScreen(
    conversations: List<ConversationEntity>,
    allMessages: List<MessageEntity>,
    selectedConversationId: Long?,
    onSelectConversation: (Long?) -> Unit,
    onSendMessage: (ConversationEntity, String, String, String, String) -> Unit,
    onReactMessage: (MessageEntity, String) -> Unit,
    onDeleteMessage: (Long) -> Unit,
    onCreateConversation: (String, String, Boolean) -> Unit,
    onStartCall: (String, String, Boolean, Boolean) -> Unit
) {
    val activeConv = conversations.find { it.id == selectedConversationId }
    var showNewChatDialog by remember { mutableStateOf(false) }

    if (activeConv != null) {
        BackHandler { onSelectConversation(null) }
        val convMessages = allMessages.filter { it.conversationId == activeConv.id }
        var messageInput by remember { mutableStateOf("") }
        var replyPreview by remember { mutableStateOf("") }
        var attachmentType by remember { mutableStateOf("TEXT") }
        var attachmentMeta by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Chat Top Header with Online / Last Seen / Typing + Voice/Video Call buttons
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    IconButton(onClick = { onSelectConversation(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                    PharbAvatar(
                        name = activeConv.title,
                        avatarColorHex = activeConv.participantAvatarColor,
                        size = 42.dp,
                        isOnline = activeConv.isOnline
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(activeConv.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = if (activeConv.isTyping) "يكتب الآن... ✍️" else activeConv.lastSeenStatus,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (activeConv.isOnline) PharbOnlineGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            onStartCall(activeConv.title, activeConv.participantUsername, false, activeConv.isGroup)
                        },
                        modifier = Modifier.testTag("start_voice_call_button")
                    ) {
                        Icon(Icons.Filled.Call, contentDescription = "مكالمة صوتية", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(
                        onClick = {
                            onStartCall(activeConv.title, activeConv.participantUsername, true, activeConv.isGroup)
                        },
                        modifier = Modifier.testTag("start_video_call_button")
                    ) {
                        Icon(Icons.Filled.Videocam, contentDescription = "مكالمة مرئية", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Messages List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(convMessages, key = { it.id }) { msg ->
                    val align = if (msg.isFromMe) Alignment.End else Alignment.Start
                    Column(
                        horizontalAlignment = align,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(
                            shape = RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp,
                                bottomStart = if (msg.isFromMe) 18.dp else 4.dp,
                                bottomEnd = if (msg.isFromMe) 4.dp else 18.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (msg.isFromMe) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth(0.84f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (!msg.isFromMe && activeConv.isGroup) {
                                    Text(
                                        text = msg.senderName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (msg.replyToPreview.isNotBlank()) {
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 6.dp)
                                    ) {
                                        Text(
                                            text = "ردًا على: ${msg.replyToPreview}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (msg.isFromMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(6.dp),
                                            maxLines = 1
                                        )
                                    }
                                }

                                if (msg.mediaMeta.isNotBlank()) {
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.18f),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 6.dp)
                                    ) {
                                        Text(
                                            text = "📎 ${msg.messageType}: ${msg.mediaMeta}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (msg.isFromMe) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = msg.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (msg.isFromMe) Color.White else MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Quick message reaction bar
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf("💙", "🔥", "👍").forEach { emoji ->
                                            Text(
                                                text = emoji,
                                                modifier = Modifier.clickable { onReactMessage(msg, emoji) }
                                            )
                                        }
                                        if (msg.reactionEmoji.isNotBlank()) {
                                            Surface(
                                                shape = RoundedCornerShape(999.dp),
                                                color = Color.White.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "التفاعل: ${msg.reactionEmoji}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (msg.isFromMe) Color.White else MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { replyPreview = msg.content },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Reply,
                                                contentDescription = "رد",
                                                tint = if (msg.isFromMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                        if (msg.isFromMe) {
                                            IconButton(
                                                onClick = { onDeleteMessage(msg.id) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.DeleteOutline,
                                                    contentDescription = "حذف الرسالة",
                                                    tint = Color.White.copy(alpha = 0.85f),
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Filled.DoneAll,
                                                contentDescription = msg.deliveryStatus,
                                                tint = Color.White,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Reply / Attachment Preview bar
            if (replyPreview.isNotBlank() || attachmentMeta.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (replyPreview.isNotBlank()) "الرد على: $replyPreview" else "مرفق جاهز: $attachmentMeta",
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                        TextButton(onClick = {
                            replyPreview = ""
                            attachmentMeta = ""
                            attachmentType = "TEXT"
                        }) {
                            Text("إلغاء")
                        }
                    }
                }
            }

            // Attachment Quick Chips (Voice, File, Image, Sticker)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = attachmentType == "VOICE",
                    onClick = {
                        attachmentType = "VOICE"
                        attachmentMeta = "رسالة صوتية (0:18 ثانية • Opus)"
                    },
                    leadingIcon = { Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(15.dp)) },
                    label = { Text("صوتية") }
                )
                FilterChip(
                    selected = attachmentType == "IMAGE",
                    onClick = {
                        attachmentType = "IMAGE"
                        attachmentMeta = "صورة عالية الدقة HD"
                    },
                    leadingIcon = { Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(15.dp)) },
                    label = { Text("صورة") }
                )
                FilterChip(
                    selected = attachmentType == "FILE",
                    onClick = {
                        attachmentType = "FILE"
                        attachmentMeta = "مستند مشفر PDF"
                    },
                    leadingIcon = { Icon(Icons.Filled.AttachFile, contentDescription = null, modifier = Modifier.size(15.dp)) },
                    label = { Text("ملف") }
                )
                FilterChip(
                    selected = attachmentType == "STICKER",
                    onClick = {
                        attachmentType = "STICKER"
                        attachmentMeta = "ملصق PHARB الرسمي ✨"
                    },
                    label = { Text("ملصق / GIF") }
                )
            }

            // Input Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    placeholder = { Text("اكتب رسالة فورية مشفرة...") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("messenger_input_field")
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (messageInput.isNotBlank() || attachmentMeta.isNotBlank()) {
                            onSendMessage(
                                activeConv,
                                messageInput.ifBlank { "تم إرسال مرفق ($attachmentMeta)" },
                                attachmentType,
                                attachmentMeta,
                                replyPreview
                            )
                            messageInput = ""
                            replyPreview = ""
                            attachmentMeta = ""
                            attachmentType = "TEXT"
                        }
                    },
                    modifier = Modifier.testTag("messenger_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "إرسال الرسالة",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    } else {
        // Conversations List View
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("messenger_conversations_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PHARB Messenger",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "محادثات فورية فردية وجماعية • WebSocket & WebRTC",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { showNewChatDialog = true },
                        modifier = Modifier.testTag("new_conversation_button")
                    ) {
                        Icon(Icons.Filled.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("محادثة جديدة")
                    }
                }
            }

            items(conversations, key = { it.id }) { conv ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectConversation(conv.id) }
                        .testTag("conversation_item_${conv.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        PharbAvatar(
                            name = conv.title,
                            avatarColorHex = conv.participantAvatarColor,
                            size = 50.dp,
                            isOnline = conv.isOnline
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(conv.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = if (conv.isTyping) "يكتب الآن..." else conv.lastSeenStatus,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (conv.isOnline) PharbOnlineGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = conv.lastMessagePreview,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (conv.unreadCount > 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Badge { Text("${conv.unreadCount}") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewChatDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newHandle by remember { mutableStateOf("") }
        var isGroupChat by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showNewChatDialog = false },
            title = { Text("بدء محادثة فردية أو جماعية جديدة") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("اسم الشخص أو اسم المجموعة") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newHandle,
                        onValueChange = { newHandle = it },
                        label = { Text("المعرف (@username)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !isGroupChat,
                            onClick = { isGroupChat = false },
                            label = { Text("محادثة فردية") }
                        )
                        FilterChip(
                            selected = isGroupChat,
                            onClick = { isGroupChat = true },
                            label = { Text("مجموعة نقاش جماعية") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            onCreateConversation(
                                newTitle,
                                newHandle.ifBlank { "pharb_member" },
                                isGroupChat
                            )
                            showNewChatDialog = false
                        }
                    }
                ) {
                    Text("بدء المحادثة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewChatDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

/**
 * PHARB WebRTC Voice / Video / Group Call Screen (Section 13)
 */
@Composable
fun PharbWebRtcCallScreen(
    callState: ActiveCallState,
    onToggleMute: () -> Unit,
    onToggleCamera: () -> Unit,
    onEndCall: () -> Unit
) {
    BackHandler { onEndCall() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PharbDeepNavy)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            color = Color.White.copy(alpha = 0.1f),
            shape = RoundedCornerShape(999.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Filled.Security, contentDescription = null, tint = PharbSoftBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = callState.connectionQuality,
                    style = MaterialTheme.typography.labelMedium,
                    color = PharbSoftBlue
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PharbAvatar(
                name = callState.participantName,
                avatarColorHex = 0xFF2563EB,
                size = 110.dp,
                isOnline = true
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = callState.participantName,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = when {
                    callState.isGroupCall -> "مكالمة جماعية نشطة • WebRTC SFU Mesh"
                    callState.isVideoCall -> "مكالمة فيديو عالية الدقة HD • متصلة"
                    else -> "مكالمة صوتية مشفرة • متصلة"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = PharbSoftBlue
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleMute,
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = if (callState.isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                    contentDescription = "كتم الميكروفون",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = onToggleCamera,
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = if (callState.isCameraOn) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
                    contentDescription = "تبديل الكاميرا",
                    tint = Color.White
                )
            }

            Button(
                onClick = onEndCall,
                colors = ButtonDefaults.buttonColors(containerColor = PharbLiveRed),
                shape = CircleShape,
                modifier = Modifier
                    .height(58.dp)
                    .testTag("end_call_button")
            ) {
                Icon(Icons.Filled.CallEnd, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إنهاء المكالمة", color = Color.White)
            }
        }
    }
}
