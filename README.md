# CanonMultiCamPro (v3.0.0 — Dark Studio Pro)

تطبيق أندرويد احترافي للتحكم المتزامن في كاميرات **Canon Cinema EOS** و**EOS R** (حتى 4 كاميرات في نفس الجلسة) عبر الشبكة والـ USB، مصمم بهوية **Dark Studio Pro** ومبني وفق **Clean Architecture** و**Jetpack Compose**.

---

## المميزات الرئيسية والهوية البصرية (Dark Studio Pro)

* **واجهة Dark Studio Pro:** لوحة تحكم إخراجية داكنة (`#0A0C10` Studio Obsidian) مريحة للعين في مواقع التصوير والبلاتوه، مع إشارات Tally حمراء (`#E50914`) ومؤشرات تركيز وربط (`#00E5FF`) وشعار فيكتور مدمج.
* **Canon CCAPI (حتى v1.4.0f):** التحكم الكامل في التصوير الفوتوغرافي والفيديو، تسلسل الغالق اليدوي (`full_press` / `release`)، ضبط التعريض (`TV`, `AV`, `ISO`, `WB`)، والتركيز باللمس (`Touch AF`) وفق أبعاد البث الحي الفعلية.
* **Canon XC Control Protocol (`/-wvhttp-01-/`):** دعم وضع السينما لكاميرا **EOS R5 C** عبر نقاط النهاية الرسمية (`open.cgi`، `control.cgi`، `close.cgi`) مع مصادقة **HTTP Digest (RFC 2617)** شاملةً لـ Query String وإغلاق الجلسة بأمان عبر `NonCancellable`.
* **Wi-Fi PTP/IP & USB PTP (`0x04A9`):** دعم الاتصال المباشر عبر الشبكة اللاسلكية أو كابل USB OTG.
* **مزامنة جماعية (Master Sync):** زر موحد لتشغيل/إيقاف التسجيل (`SYNC REC ALL`) أو التقاط الصور (`SYNC SHUTTER`) على الكاميرات المحددة في نفس اللحظة.

---

## هيكل المشروع (Project Structure)

```text
CanonMultiCamPro-v3.0.0/
├── .github/workflows/build_apk.yml       # مسار GitHub Actions لبناء APK تلقائياً
├── github_workflow_backup/build_apk.yml  # نسخة احتياطية في حال الرفع عبر المتصفح
├── config/detekt/detekt.yml              # إعدادات فحص جودة الكود الثابت (Detekt)
├── gradle/wrapper/gradle-wrapper.properties
├── gradlew & gradlew.bat                 # سكربتات Gradle Wrapper ذاتية التجهيز
├── PUSH_TO_GITHUB.bat & push_to_github.sh # رفع تلقائي بضغطة واحدة مع تضمين الملفات المخفية
├── build.gradle.kts & settings.gradle.kts
└── app/
    ├── build.gradle.kts
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/com/canon/multicampro/
        │   │   ├── MainActivity.kt
        │   │   ├── domain/ (model & repository)
        │   │   ├── data/   (network DigestAuth, CCAPI, XC Protocol, repository)
        │   │   └── ui/     (theme, StudioLogo, MultiCamViewModel, CameraStudioScreen)
        │   └── res/ (drawable vector logo, mipmap adaptive icons, values, xml)
        └── test/java/com/canon/multicampro/DigestAuthAndProtocolTest.kt
```

---

## طريقة الرفع إلى GitHub وبناء ملف APK تلقائياً (بدون أخطاء)

### الطريقة الأولى: الرفع التلقائي عبر Git (الأسرع والأضمن)
1. فك الضغط عن الملف المضغوط `CanonMultiCamPro-v3.0.0-github-ready.zip`.
2. أنشئ مستودعاً جديداً فارغاً (New Repository) على حسابك في GitHub وانسخ رابطه.
3. شغّل ملف **`PUSH_TO_GITHUB.bat`** (على ويندوز) أو **`./push_to_github.sh`** (على ماك/لينكس) وألصق رابط المستودع؛ سيقوم برفع كافة الملفات بما فيها مجلد `.github` المخفي وملفات `gradlew`.
4. افتح تبويب **Actions** في المستودع على GitHub، وستجد ملف **`app-debug.apk`** جاهزاً للتحميل تحت قسم **Artifacts -> CanonMultiCamPro-v3.0.0-debug-apk**.

### الطريقة الثانية: الرفع عبر المتصفح (Drag & Drop)
> **تنبيه مهم:** متصفحات الويب تتجاهل المجلدات التي تبدأ بنقطة مثل `.github` عند السحب والإفلات.
1. اسحب محتويات المجلد إلى المستودع على GitHub.
2. اضغط **Add file -> Create new file** في GitHub، واكتب في اسم الملف: `.github/workflows/build_apk.yml`، ثم انسخ محتوى الملف الموجود داخل `github_workflow_backup/build_apk.yml` والصقه واحفظ التغييرات.
