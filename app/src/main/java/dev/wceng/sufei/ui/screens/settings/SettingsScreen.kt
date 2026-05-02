package dev.wceng.sufei.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.wceng.sufei.BuildConfig
import dev.wceng.sufei.data.model.UserPreferences
import dev.wceng.sufei.ui.theme.SuFeiTheme

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val userPreferences by viewModel.userPreferences.collectAsState()

    SettingsContent(
        userPreferences = userPreferences,
        onFontSizeChange = viewModel::setFontSizeMultiplier,
        onLineHeightChange = viewModel::setLineHeightMultiplier,
        onDynamicColorToggle = viewModel::setUseDynamicColor,
        onFontFamilyChange = viewModel::setFontFamilyName
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    userPreferences: UserPreferences,
    onFontSizeChange: (Float) -> Unit,
    onLineHeightChange: (Float) -> Unit,
    onDynamicColorToggle: (Boolean) -> Unit,
    onFontFamilyChange: (String) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "设置", 
                        fontWeight = FontWeight.Bold
                    ) 
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 阅读偏好组
            SettingsSectionTitle(title = "阅读偏好", icon = Icons.Default.TextFormat)
            
            SettingsSliderItem(
                label = "字体大小",
                value = userPreferences.fontSizeMultiplier,
                onValueChange = onFontSizeChange,
                valueRange = 0.8f..1.5f
            )

            SettingsSliderItem(
                label = "行间距",
                value = userPreferences.lineHeightMultiplier,
                onValueChange = onLineHeightChange,
                valueRange = 1.0f..2.5f
            )

            // 外观组
            Spacer(modifier = Modifier.height(24.dp))
            SettingsSectionTitle(title = "外观定制", icon = Icons.Default.Palette)

            SettingsSwitchItem(
                label = "Material You 动态色彩",
                checked = userPreferences.useDynamicColor,
                onCheckedChange = onDynamicColorToggle
            )
            
            // 更多信息
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "素扉 v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun SettingsSliderItem(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun SettingsSwitchItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsContentPreview() {
    SuFeiTheme {
        SettingsContent(
            userPreferences = UserPreferences(),
            onFontSizeChange = {},
            onLineHeightChange = {},
            onDynamicColorToggle = {},
            onFontFamilyChange = {}
        )
    }
}
