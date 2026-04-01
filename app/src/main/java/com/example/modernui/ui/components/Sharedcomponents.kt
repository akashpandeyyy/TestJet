package com.example.modernui.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modernui.ui.theme.FintechColors

// ─────────────────────────────────────────────
// TOP BAR — Home (hamburger)
// ─────────────────────────────────────────────

@Composable
fun HomeTopBar(
    title:       String,
    onMenuClick: () -> Unit,
    actions:     @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .background(FintechColors.NavyAlpha)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(
                Icons.Default.ArrowBack, // replace with Menu icon in your icons import
                contentDescription = "Menu",
                tint = Color.White
            )
        }
        Text(
            text       = title,
            color      = Color.White,
            fontSize   = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier
                .padding(start = 4.dp)
                .weight(1f)
        )
        actions()
    }
}

// ─────────────────────────────────────────────
// TOP BAR — Detail screens (back arrow)
// ─────────────────────────────────────────────

@Composable
fun DetailTopBar(
    title:       String,
    onBackClick: () -> Unit,
    actions:     @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .background(FintechColors.NavyAlpha)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Text(
            text       = title,
            color      = Color.White,
            fontSize   = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier
                .padding(start = 4.dp)
                .weight(1f)
        )
        actions()
    }
}

// ─────────────────────────────────────────────
// SECTION CARD — groups inputs with left accent
// ─────────────────────────────────────────────

@Composable
fun SectionCard(
    title:   String,
    icon:    ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(FintechColors.NavyDark, FintechColors.NavyLight)
                        ),
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment      = Alignment.CenterVertically,
                    horizontalArrangement  = Arrangement.spacedBy(8.dp),
                    modifier               = Modifier.padding(bottom = 12.dp)
                ) {
                    Surface(
                        shape    = CircleShape,
                        color    = FintechColors.NavyDark.copy(alpha = 0.1f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(icon, null, tint = FintechColors.NavyDark, modifier = Modifier.size(20.dp))
                        }
                    }
                    Text(
                        text       = title,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = FintechColors.NavyDark
                    )
                }
                content()
            }
        }
    }
}

// ─────────────────────────────────────────────
// OUTLINED TEXT FIELD — navy themed
// ─────────────────────────────────────────────

@Composable
fun NavyOutlinedField(
    value:         String,
    onValueChange: (String) -> Unit,
    label:         String,
    placeholder:   String,
    leadingIcon:   ImageVector,
    modifier:      Modifier      = Modifier,
    keyboardType:  KeyboardType  = KeyboardType.Text,
    maxLength:     Int           = Int.MAX_VALUE,
    isError:       Boolean       = false,
    errorMessage:  String        = "",
    trailingIcon:  (@Composable () -> Unit)? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value         = value,
            onValueChange = { if (it.length <= maxLength) onValueChange(it) },
            label         = { Text(label) },
            placeholder   = { Text(placeholder, color = MaterialTheme.colorScheme.outline) },
            leadingIcon   = { Icon(leadingIcon, null, tint = FintechColors.NavyDark) },
            trailingIcon  = trailingIcon,
            isError       = isError,
            singleLine    = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape         = RoundedCornerShape(12.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = FintechColors.NavyDark,
                focusedLabelColor       = FintechColors.NavyDark,
                focusedLeadingIconColor = FintechColors.NavyDark,
                cursorColor             = FintechColors.NavyDark,
                unfocusedBorderColor    = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (isError && errorMessage.isNotEmpty()) {
            Text(
                text     = errorMessage,
                color    = MaterialTheme.colorScheme.error,
                style    = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────
// DROPDOWN FIELD — navy themed
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavyDropdownField(
    label:            String,
    leadingIcon:      ImageVector,
    selectedValue:    String,
    options:          List<String>,
    onOptionSelected: (String) -> Unit,
    modifier:         Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded         = expanded,
        onExpandedChange = { expanded = it },
        modifier         = modifier
    ) {
        OutlinedTextField(
            value         = selectedValue,
            onValueChange = {},
            readOnly      = true,
            label         = { Text(label) },
            leadingIcon   = { Icon(leadingIcon, null, tint = FintechColors.NavyDark) },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape         = RoundedCornerShape(12.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = FintechColors.NavyDark,
                focusedLabelColor       = FintechColors.NavyDark,
                focusedLeadingIconColor = FintechColors.NavyDark,
                cursorColor             = FintechColors.NavyDark,
                unfocusedBorderColor    = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text    = { Text(option) },
                    onClick = { onOptionSelected(option); expanded = false },
                    leadingIcon = if (option == selectedValue) ({
                        Icon(Icons.Default.Check, null, tint = FintechColors.NavyDark)
                    }) else null
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// PRIMARY BUTTON — full-width navy
// ─────────────────────────────────────────────

@Composable
fun NavyPrimaryButton(
    text:     String,
    onClick:  () -> Unit,
    modifier: Modifier     = Modifier,
    enabled:  Boolean      = true,
    icon:     ImageVector? = null
) {
    Button(
        onClick  = onClick,
        enabled  = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape    = RoundedCornerShape(14.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor         = FintechColors.NavyDark,
            contentColor           = Color.White,
            disabledContainerColor = FintechColors.NavyDark.copy(alpha = 0.4f),
            disabledContentColor   = Color.White.copy(alpha = 0.6f)
        )
    ) {
        if (icon != null) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

// ─────────────────────────────────────────────
// NAVY HEADER CARD — gradient banner
// ─────────────────────────────────────────────

@Composable
fun NavyHeaderCard(
    icon:     ImageVector,
    title:    String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(FintechColors.NavyDark, FintechColors.NavyLight)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape    = CircleShape,
                    color    = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(icon, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
                Column {
                    Text(
                        title,
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        style      = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        subtitle,
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}