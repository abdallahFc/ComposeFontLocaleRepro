package com.example.composefontlocalerepro

import android.os.Bundle
import android.os.Process
import android.util.Log
import android.util.TypedValue
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.FontRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.example.composefontlocalerepro.ui.theme.ComposeFontLocaleReproTheme

private const val LogTag = "FontLocaleRepro"
private const val SampleText = "Compose Font Test ABC 123 — اختبار الخط"
private const val MainFontDescription = "FontFamily(Font(R.font.locale_test_font))"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeFontLocaleReproTheme {
                ReproScreen(
                    onSwitchLanguage = ::switchLanguage,
                    onRecreateActivity = ::recreate,
                )
            }
        }
    }

    private fun switchLanguage(languageTag: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
    }
}

@Composable
private fun ReproScreen(
    onSwitchLanguage: (String) -> Unit,
    onRecreateActivity: () -> Unit,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val currentLocale = configuration.locales[0]
    val currentLocaleTag = currentLocale.toLanguageTag()
    val currentLocaleLabel = "${currentLocale.displayName} ($currentLocaleTag)"
    val currentLanguage = when (currentLocale.language) {
        "ar" -> "Arabic"
        "en" -> "English"
        else -> currentLocale.displayLanguage.ifBlank { currentLocale.language }
    }
    val isEnglish = currentLocale.language == "en"
    val isArabic = currentLocale.language == "ar"
    val currentPid = Process.myPid()
    val mainFontPath = resolveFontPath(R.font.locale_test_font)
    val explicitFontRes = if (currentLocale.language == "ar") {
        R.font.locale_test_font_ar_explicit
    } else {
        R.font.locale_test_font_en_explicit
    }
    val explicitFontPath = resolveFontPath(explicitFontRes)
    val explicitFontName = context.resources.getResourceName(explicitFontRes)

    SideEffect {
        Log.d(
            LogTag,
            "compose locale=$currentLocaleTag pid=$currentPid " +
                "main=FontFamily(Font(R.font.locale_test_font)) " +
                "mainResolvedPath=$mainFontPath " +
                "workaroundControl=FontFamily(Font($explicitFontName)) " +
                "workaroundResolvedPath=$explicitFontPath",
        )
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = Color(0xFFF7F7F3),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                HeaderPanel(
                    currentLanguage = currentLanguage,
                    currentLocaleTag = currentLocaleTag,
                )

                StatusPanel(
                    currentLanguage = currentLanguage,
                    currentLocale = currentLocaleLabel,
                    currentPid = currentPid.toString(),
                )

                ActionPanel(
                    isEnglish = isEnglish,
                    isArabic = isArabic,
                    onSwitchLanguage = onSwitchLanguage,
                    onRecreateActivity = onRecreateActivity,
                )

                SampleTextPanel(
                    eyebrow = "Main repro",
                    label = "Compose text using ResourceFont",
                    detail = "$MainFontDescription\nResolved resource: $mainFontPath",
                    fontFamily = FontFamily(Font(R.font.locale_test_font)),
                )

                StringResourcePanel()

                SampleTextPanel(
                    eyebrow = "Optional control",
                    label = "Workaround control",
                    detail = "Explicit resource id: $explicitFontName\nResolved resource: $explicitFontPath",
                    fontFamily = FontFamily(Font(explicitFontRes)),
                )

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun HeaderPanel(currentLanguage: String, currentLocaleTag: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Compose Font Locale Repro",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Current language: $currentLanguage ($currentLocaleTag)",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF315343),
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun StatusPanel(currentLanguage: String, currentLocale: String, currentPid: String) {
    Panel {
        LabelValue(label = "Current language", value = currentLanguage)
        HorizontalDivider()
        LabelValue(label = "Current locale", value = currentLocale)
        HorizontalDivider()
        LabelValue(label = "Current process id", value = currentPid)
    }
}

@Composable
private fun ActionPanel(
    isEnglish: Boolean,
    isArabic: Boolean,
    onSwitchLanguage: (String) -> Unit,
    onRecreateActivity: () -> Unit,
) {
    Panel {
        Text(
            text = "Language switch",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LanguageButton(
                label = "Switch to English",
                isSelected = isEnglish,
                onClick = { onSwitchLanguage("en") },
                modifier = Modifier.weight(1f),
            )
            LanguageButton(
                label = "Switch to Arabic",
                isSelected = isArabic,
                onClick = { onSwitchLanguage("ar") },
                modifier = Modifier.weight(1f),
            )
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onRecreateActivity,
        ) {
            Text(text = "Recreate Activity only")
        }
    }
}

@Composable
private fun LanguageButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isSelected) {
        Button(
            modifier = modifier,
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF315343),
                contentColor = Color.White,
            ),
        ) {
            Text(text = "$label: active")
        }
    } else {
        OutlinedButton(
            modifier = modifier,
            onClick = onClick,
        ) {
            Text(text = label)
        }
    }
}

@Composable
private fun SampleTextPanel(
    eyebrow: String,
    label: String,
    detail: String,
    fontFamily: FontFamily,
) {
    Panel {
        Text(
            text = eyebrow,
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF315343),
            fontWeight = FontWeight.SemiBold,
        )
        LabelValue(label = label, value = detail)
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF7F7F3))
                .padding(14.dp),
            text = SampleText,
            fontFamily = fontFamily,
            fontSize = 32.sp,
            lineHeight = 40.sp,
        )
    }
}

@Composable
private fun StringResourcePanel() {
    Panel {
        LabelValue(
            label = "stringResource text",
            value = stringResource(R.string.localized_resource_text),
        )
    }
}

@Composable
private fun LabelValue(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE1E1DA)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
private fun resolveFontPath(@FontRes fontRes: Int): String {
    val context = LocalContext.current
    val typedValue = TypedValue()
    context.resources.getValue(fontRes, typedValue, true)
    return typedValue.string?.toString() ?: context.resources.getResourceName(fontRes)
}
