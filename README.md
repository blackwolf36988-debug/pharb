# PHARB Social Network 🌐

**Connect. Create. Discover. — تواصل. أبدع. اكتشف.**

منصة تواصل اجتماعي عالمية حديثة وأصلية مبنية على مبدأ **Less Clutter — More Experience** (أقل ازدحامًا + أكثر وضوحًا + تجربة استخدام أفضل)، تجمع في تجربة واحدة متكاملة:
**Social Network + PHARB Shorts + 24h Stories + PHARB Messenger + WebRTC Calls + Communities + Channels + PHARB Live + Education + Business + Responsible AI + Admin Panel.**

---

## 1. فكرة ورؤية PHARB
صُممت منصة **PHARB** لتكون بيئة تواصل اجتماعي سريعة، مريحة للعين، آمنة، وتدعم اللغة العربية (RTL) والإنجليزية (LTR) من اللحظة الأولى، مع خوارزمية توصيات شفافة يمنح فيها المستخدم السيطرة الكاملة على اهتماماته وخصوصيته.

## 2. المعمارية التقنية (Architecture)
```text
Android Client (Kotlin + Jetpack Compose + Room Offline-First Cache)
   ↓ HTTPS / TLS 1.3 + WebSocket / WebRTC
API Gateway & Rate Limiter
   ↓
Modular Backend Services (FastAPI / REST + OpenAPI 3.1)
   ├── PostgreSQL 16 (Relational Core: 24 Tables in /database/schema.sql)
   ├── Redis 7 (Session Store, Rate Limiting, Feed Cache)
   └── Object Storage + CDN (Adaptive Bitrate Video & Media Delivery)
```

## 3. التقنيات المستخدمة (Technologies)
- **تطبيق Android**: Kotlin, Jetpack Compose (Material 3), MVVM Architecture, Coroutines & StateFlow, Room Database (KSP), DataStore Preferences, Retrofit & OkHttp.
- **الهوية البصرية والخطوط**: نظام ألوان `Deep Navy (#0B192C)` + `Royal Blue (#2563EB)` + `Soft Blue (#38BDF8)` + `Graphite Dark` + `AMOLED Black` مع خطوط `Cairo` و `Plus Jakarta Sans` المحلية.
- **الأمان والتشفير**: `PBKDF2WithHmacSHA256` لتشفير كلمات المرور مع `Salt` عشوائي، توقيع `HMAC-SHA256 JWT` للجلسات، حماية ضد `XSS / SQLi`، ونظام تحديد معدل الطلبات `Rate Limiting`.
- **الذكاء الاصطناعي المسؤول (`PharbAiEngine`)**: اقتراح الوسوم `#Hashtags`، تلخيص المنشورات، الترجمة الفورية بين العربية والإنجليزية، تحسين الصياغة، فحص المحتوى ضد `Spam`، وتفسير شفاف لأسباب التوصيات.

## 4. هيكل المشروع (Project Structure)
```text
PHARB/
├── app/                        # تطبيق Android الكامل (Kotlin + Jetpack Compose + Room + Tests)
├── backend/main.py             # خدمة الـ Backend المرجعية (FastAPI REST + WebSocket)
├── database/schema.sql         # مخطط قاعدة البيانات الإنتاجية PostgreSQL (24 جدولاً)
├── api/openapi.yaml            # توثيق الـ API الرسمي بمعيار OpenAPI 3.1
├── admin-panel/index.html      # لوحة الإدارة المركزية للويب
├── deployment/docker-compose.yml # إعداد التشغيل السحابي (Docker + PostgreSQL + Redis)
├── docs/ARCHITECTURE_AND_SECURITY.md # دليل الأمان، المعمارية، والنشر على Google Play
└── README.md                   # الدليل الشامل للمشروع
```

## 5. التشغيل والاختبار (Installation, Setup & Testing)
1. **تشغيل واختبار تطبيق Android**:
   - بناء التطبيق: `./gradlew :app:assembleDebug`
   - تشغيل اختبارات الوحدة و Robolectric: `gradle :app:testDebugUnitTest`
2. **تشغيل قاعدة البيانات والـ Backend سحابيًا**:
   - `docker compose -f deployment/docker-compose.yml up -d`
