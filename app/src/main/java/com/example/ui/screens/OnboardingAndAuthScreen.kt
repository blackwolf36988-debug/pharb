package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.SecurityManager
import com.example.data.local.UserEntity
import com.example.ui.theme.PharbAvatar
import com.example.ui.theme.PharbDeepNavy
import com.example.ui.theme.PharbGeometricLogo
import com.example.ui.theme.PharbLogoVariant
import com.example.ui.theme.PharbRoyalBlue
import com.example.ui.theme.PharbSoftBlue
import com.example.ui.theme.PharbVerifiedBadge
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val pillText: String
)

/**
 * 01. PHARB SPLASH SCREEN (Section 2 & 21)
 * - Centered PHARB Geometric Logo with smooth spring/scale animation
 * - Below Logo: PHARB
 * - Subtitle: CONNECT • CREATE • SHARE
 * - Clean, futuristic Deep Navy & Royal Blue atmosphere with subtle Metallic Silver accents
 */
@Composable
fun PharbSplashScreen(
    isArabic: Boolean,
    onFinishSplash: () -> Unit,
    onOpenOnboarding: () -> Unit
) {
    BackHandler { onFinishSplash() }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2200)
        onFinishSplash()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(PharbDeepNavy, Color(0xFF071120), Color(0xFF000000))
                )
            )
            .clickable { onFinishSplash() }
            .padding(24.dp)
            .testTag("pharb_splash_screen")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PharbGeometricLogo(
                variant = PharbLogoVariant.SPLASH_LOGO,
                markSize = 104.dp,
                showTagline = false,
                isArabic = isArabic
            )
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = "PHARB",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = PharbRoyalBlue.copy(alpha = 0.28f),
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = "CONNECT • CREATE • SHARE",
                    style = MaterialTheme.typography.labelLarge,
                    color = PharbSoftBlue,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 7.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (isArabic) "تواصل • أبدع • شارك" else "Global Digital Social Platform",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFCBD5E1)
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onOpenOnboarding) {
                Text(
                    text = if (isArabic) "الجولة التعريفية" else "Onboarding",
                    color = PharbSoftBlue
                )
            }
            OutlinedButton(
                onClick = onFinishSplash,
                modifier = Modifier.testTag("splash_enter_app_button")
            ) {
                Text(
                    text = if (isArabic) "الدخول إلى PHARB" else "Enter PHARB",
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun PharbOnboardingScreen(
    isArabic: Boolean,
    onFinishOnboarding: () -> Unit,
    onOpenAuthPortal: () -> Unit
) {
    BackHandler { onFinishOnboarding() }

    val pages = remember(isArabic) {
        if (isArabic) {
            listOf(
                OnboardingPageData(
                    title = "تواصل مع العالم",
                    subtitle = "بيئة اجتماعية هادئة، سريعة، وآمنة تجمعك بمن يشاركونك الاهتمامات والأفكار حول العالم بدون ضجيج.",
                    pillText = "01 • تواصل عالمي آمن"
                ),
                OnboardingPageData(
                    title = "شارك أفكارك ومحتواك",
                    subtitle = "انشر النصوص، الصور، الفيديوهات القصيرة PHARB Shorts، القصص، والاستطلاعات بأدوات ذكاء اصطناعي تحترم خصوصيتك.",
                    pillText = "02 • أدوات إبداع متكاملة"
                ),
                OnboardingPageData(
                    title = "اكتشف ما يهمك",
                    subtitle = "استكشف المجتمعات المتخصصة، القنوات التعليمية والتجارية، والبث المباشر بنظام توصيات شفاف تحت سيطرتك.",
                    pillText = "03 • خوارزمية شفافة"
                ),
                OnboardingPageData(
                    title = "PHARB — عالمك الاجتماعي في مكان واحد",
                    subtitle = "Less Clutter — More Experience\nأقل ازدحامًا + أكثر وضوحًا + تجربة استخدام أفضل.",
                    pillText = "04 • تواصل. أبدع. اكتشف."
                )
            )
        } else {
            listOf(
                OnboardingPageData(
                    title = "Connect with the World",
                    subtitle = "A calm, fast, and secure social space connecting you with thinkers, creators, and communities worldwide.",
                    pillText = "01 • Global Connection"
                ),
                OnboardingPageData(
                    title = "Share Your Ideas & Content",
                    subtitle = "Publish posts, photos, PHARB Shorts, 24h Stories, polls, and voice notes with privacy-first AI tools.",
                    pillText = "02 • Creative Suite"
                ),
                OnboardingPageData(
                    title = "Discover What Matters",
                    subtitle = "Explore specialized communities, educational & business channels, and live streams with transparent recommendations.",
                    pillText = "03 • Transparent Discovery"
                ),
                OnboardingPageData(
                    title = "PHARB — Your Social World in One Place",
                    subtitle = "Less Clutter — More Experience.\nClean design, high performance, and total privacy control.",
                    pillText = "04 • Connect. Create. Discover."
                )
            )
        }
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(PharbDeepNavy, Color(0xFF0F172A), MaterialTheme.colorScheme.background)
                )
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PharbGeometricLogo(
                variant = PharbLogoVariant.SPLASH_LOGO,
                markSize = 42.dp,
                showTagline = true,
                isArabic = isArabic
            )
            TextButton(
                onClick = onFinishOnboarding,
                modifier = Modifier.testTag("onboarding_skip_button")
            ) {
                Text(if (isArabic) "تخطي إلى الرئيسية" else "Skip to Feed")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.img_onboarding_hero),
                    contentDescription = "PHARB Onboarding Hero",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, PharbDeepNavy.copy(alpha = 0.85f))
                            )
                        )
                )
                Text(
                    text = "PHARB • Connect. Create. Discover.",
                    style = MaterialTheme.typography.labelLarge,
                    color = PharbSoftBlue,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { pageIdx ->
            val item = pages[pageIdx]
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = PharbRoyalBlue.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            text = item.pillText,
                            style = MaterialTheme.typography.labelMedium,
                            color = PharbSoftBlue,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Page Indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pages.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(if (isSelected) 28.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (pagerState.currentPage < pages.lastIndex) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("onboarding_next_button")
                ) {
                    Text(if (isArabic) "التالي" else "Next")
                }
            }
            Button(
                onClick = onFinishOnboarding,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("onboarding_start_button")
            ) {
                Text(
                    text = if (isArabic) "ابدأ الآن" else "Start Now",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onOpenAuthPortal) {
            Text(if (isArabic) "تسجيل الدخول أو إنشاء حساب جديد" else "Sign In or Create Account")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PharbAuthPortalScreen(
    users: List<UserEntity>,
    generatedOtp: String,
    isArabic: Boolean,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String, String, String, String) -> Unit,
    onRequestOtp: (String) -> Unit,
    onVerifyOtp: (String) -> Unit,
    onQuickSwitchUser: (Long) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var authTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Register, 2: OTP & Recovery
    var loginIdentifier by remember { mutableStateOf("tarek_pharb") }
    var loginPassword by remember { mutableStateOf("Pharb@2026") }

    var regFullName by remember { mutableStateOf("") }
    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("+9665") }
    var regPassword by remember { mutableStateOf("") }
    var regAccountType by remember { mutableStateOf("PERSONAL") }

    var otpDestination by remember { mutableStateOf("+966501234567") }
    var otpInput by remember { mutableStateOf("") }

    val passwordStrength = remember(regPassword) {
        SecurityManager.evaluatePasswordStrength(regPassword)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
            }
            PharbGeometricLogo(variant = PharbLogoVariant.MAIN_LOGO, markSize = 34.dp, isArabic = isArabic)
            Spacer(modifier = Modifier.width(40.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Security Architecture Badge
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "نظام مصادقة PHARB المشفر (Zero Plaintext)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "تشفير PBKDF2-HMAC-SHA256 • جلسات JWT + Refresh Tokens • حماية ضد هجمات التخمين",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(selectedTabIndex = authTab) {
            Tab(
                selected = authTab == 0,
                onClick = { authTab = 0 },
                text = { Text("تسجيل الدخول") },
                modifier = Modifier.testTag("auth_tab_login")
            )
            Tab(
                selected = authTab == 1,
                onClick = { authTab = 1 },
                text = { Text("إنشاء حساب") },
                modifier = Modifier.testTag("auth_tab_register")
            )
            Tab(
                selected = authTab == 2,
                onClick = { authTab = 2 },
                text = { Text("رمز OTP / استعادة") },
                modifier = Modifier.testTag("auth_tab_otp")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (authTab) {
            0 -> {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "الدخول إلى حسابك في PHARB",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = loginIdentifier,
                            onValueChange = { loginIdentifier = it },
                            label = { Text("اسم المستخدم، البريد الإلكتروني، أو رقم الهاتف") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_identifier_input")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text("كلمة المرور") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input")
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onLogin(loginIdentifier, loginPassword) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_submit_button")
                        ) {
                            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تسجيل الدخول الآمن")
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { onQuickSwitchUser(1L) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("google_signin_button")
                        ) {
                            Icon(Icons.Filled.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("متابعة عبر الهوية الموحدة (Google Sign-In)")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { onQuickSwitchUser(2L) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("apple_signin_button")
                        ) {
                            Icon(Icons.Filled.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("متابعة عبر Apple ID (Apple Sign-In)")
                        }
                    }
                }
            }

            1 -> {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "إنشاء حساب جديد في PHARB",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = regFullName,
                            onValueChange = { regFullName = it },
                            label = { Text("الاسم الكامل أو اسم المؤسسة") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_name_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = regUsername,
                            onValueChange = { regUsername = it },
                            label = { Text("اسم المستخدم (@username)") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_username_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("البريد الإلكتروني") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_email_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = regPhone,
                            onValueChange = { regPhone = it },
                            label = { Text("رقم الهاتف (مع رمز الدولة)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("كلمة المرور (تشفير PBKDF2)") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_password_input")
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (passwordStrength.score / 5f).coerceIn(0.1f, 1f) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "قوة كلمة المرور: ${passwordStrength.labelAr}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("نوع الحساب:", style = MaterialTheme.typography.labelLarge)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                "PERSONAL" to "شخصي",
                                "CREATOR" to "صانع محتوى",
                                "EDUCATION" to "تعليمي (PHARB Education)",
                                "BUSINESS" to "تجاري (PHARB Business)"
                            ).forEach { (typeKey, label) ->
                                FilterChip(
                                    selected = regAccountType == typeKey,
                                    onClick = { regAccountType = typeKey },
                                    label = { Text(label) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                onRegister(regFullName, regUsername, regEmail, regPhone, regPassword, regAccountType)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("register_submit_button")
                        ) {
                            Text("إنشاء حسابي الآن")
                        }
                    }
                }
            }

            2 -> {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "التحقق عبر رمز OTP واستعادة الحساب",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = otpDestination,
                            onValueChange = { otpDestination = it },
                            label = { Text("رقم الهاتف أو البريد الإلكتروني") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { onRequestOtp(otpDestination) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("request_otp_button")
                        ) {
                            Icon(Icons.Filled.PhoneAndroid, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إرسال رمز التحقق (6 أرقام)")
                        }

                        if (generatedOtp.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "رمز OTP النشط للجلسة: $generatedOtp",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = { otpInput = it },
                            label = { Text("أدخل رمز OTP المكون من 6 أرقام") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("otp_code_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onVerifyOtp(otpInput) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("verify_otp_button")
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تأكيد الرمز والدخول")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "تبديل سريع بين الحسابات المعتمدة (صناع محتوى • تعليم • أعمال • إدارة):",
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.height(8.dp))

        users.forEach { account ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onQuickSwitchUser(account.id) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    PharbAvatar(name = account.fullName, avatarColorHex = account.avatarColorHex, size = 42.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(account.fullName, style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.width(6.dp))
                            PharbVerifiedBadge(isVerified = account.isVerified, accountType = account.accountType)
                        }
                        Text(
                            text = "@${account.username} • ${account.role}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "دخول",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
