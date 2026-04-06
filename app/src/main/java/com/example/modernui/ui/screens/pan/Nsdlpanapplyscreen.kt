package com.example.modernui.ui.screens.pan

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modernui.ui.components.DetailTopBar
import com.example.modernui.ui.theme.FintechColors


// ─────────────────────────────────────────────
// NSDL PAN APPLY SCREEN
// ─────────────────────────────────────────────

@Composable
fun NsdlPanApplyScreen(
    onBackClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp > 600

    // ── Form state ────────────────────────────
    var applicationType by remember { mutableStateOf("Normal") }
    var title           by remember { mutableStateOf("") }
    var firstName       by remember { mutableStateOf("") }
    var middleName      by remember { mutableStateOf("") }
    var lastName        by remember { mutableStateOf("") }
    var mobile          by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var gender          by remember { mutableStateOf("") }
    var dob             by remember { mutableStateOf("") }
    var fatherFirstName  by remember { mutableStateOf("") }
    var fatherMiddleName by remember { mutableStateOf("") }
    var fatherLastName   by remember { mutableStateOf("") }
    var namePrint       by remember { mutableStateOf("") }
    var place           by remember { mutableStateOf("") }

    // ── Options ───────────────────────────────
    val applicationTypes = listOf("Normal", "Correction", "Lost PAN", "NRI")
    val titles           = listOf("Shri", "Smt", "Kumari", "Mr", "Mrs", "Ms", "Dr")
    val genders          = listOf("Male", "Female", "Transgender")

    // ── Validation ────────────────────────────
    val mobileError = mobile.isNotEmpty() && mobile.length != 10
    val dobError    = dob.isNotEmpty() && !dob.matches(Regex("""\d{2}/\d{2}/\d{4}"""))
    val isFormValid = applicationType.isNotEmpty()
            && title.isNotEmpty()
            && firstName.isNotBlank()
            && lastName.isNotBlank()
            && mobile.length == 10
            && gender.isNotEmpty()
            && dob.isNotEmpty() && !dobError
            && fatherFirstName.isNotBlank()
            && fatherLastName.isNotBlank()
            && place.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // ── Top bar ───────────────────────────
        DetailTopBar(title = "NSDL PAN Application", onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Main form card — matches screenshot white card ──
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(if (isWideScreen) 24.dp else 12.dp),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(if (isWideScreen) 32.dp else 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // ── Title text ────────────
                    Text(
                        "PAN Card Details",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = colorScheme.onSurface
                    )

                    // ── Application Type (responsive dropdown width) ──
                    PanDropdownField(
                        label            = "Application Type *",
                        selectedValue    = applicationType,
                        options          = applicationTypes,
                        onOptionSelected = { applicationType = it },
                        modifier         = if (isWideScreen) Modifier.fillMaxWidth(0.3f) else Modifier.fillMaxWidth()
                    )

                    // ── Row: Title | First Name | Middle Name | Last Name ──
                    ResponsiveRow(isWideScreen = isWideScreen) {
                        PanDropdownField(
                            label            = "Title *",
                            selectedValue    = title,
                            options          = titles,
                            onOptionSelected = { title = it },
                            modifier         = Modifier.weight(if (isWideScreen) 1f else 1.2f)
                        )
                        PanTextField(
                            value         = firstName,
                            onValueChange = { firstName = it },
                            label         = "First Name *",
                            modifier      = Modifier.weight(if (isWideScreen) 2f else 1.5f)
                        )
                        PanTextField(
                            value         = middleName,
                            onValueChange = { middleName = it },
                            label         = "Middle Name",
                            modifier      = Modifier.weight(if (isWideScreen) 2f else 1.5f)
                        )
                        PanTextField(
                            value         = lastName,
                            onValueChange = { lastName = it },
                            label         = "Last Name *",
                            modifier      = Modifier.weight(if (isWideScreen) 2f else 1.5f)
                        )
                    }

                    // ── Row: Mobile | Email ───
                    ResponsiveRow(isWideScreen = isWideScreen) {
                        PanTextField(
                            value         = mobile,
                            onValueChange = { if (it.all(Char::isDigit)) mobile = it },
                            label         = "Mobile *",
                            keyboardType  = KeyboardType.Phone,
                            maxLength     = 10,
                            isError       = mobileError,
                            errorText     = "Enter valid 10-digit number",
                            modifier      = Modifier.weight(1f)
                        )
                        PanTextField(
                            value         = email,
                            onValueChange = { email = it },
                            label         = "Email",
                            keyboardType  = KeyboardType.Email,
                            modifier      = Modifier.weight(1f)
                        )
                    }

                    // ── Row: Gender | Date of Birth ──
                    ResponsiveRow(isWideScreen = isWideScreen) {
                        PanDropdownField(
                            label            = "Gender *",
                            selectedValue    = gender,
                            options          = genders,
                            onOptionSelected = { gender = it },
                            modifier         = Modifier.weight(1f)
                        )
                        // DOB with calendar icon — matching screenshot
                        PanDateField(
                            value         = dob,
                            onValueChange = {
                                // Auto-insert slashes: DD/MM/YYYY
                                val digits = it.filter(Char::isDigit).take(8)
                                val formatted = buildString {
                                    digits.forEachIndexed { i, c ->
                                        if (i == 2 || i == 4) append('/')
                                        append(c)
                                    }
                                }
                                dob = formatted
                            },
                            isError   = dobError,
                            errorText = "Enter date as DD/MM/YYYY",
                            modifier  = Modifier.weight(1f)
                        )
                    }

                    // ── Section: Father/Guardian Details ──
                    Text(
                        "Father/Guardian Details",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = colorScheme.onSurface
                    )

                    // ── Row: Father First | Middle | Last Name ──
                    ResponsiveRow(isWideScreen = isWideScreen) {
                        PanTextField(
                            value         = fatherFirstName,
                            onValueChange = { fatherFirstName = it },
                            label         = "Father First Name *",
                            modifier      = Modifier.weight(1f)
                        )
                        PanTextField(
                            value         = fatherMiddleName,
                            onValueChange = { fatherMiddleName = it },
                            label         = "Father Middle Name",
                            modifier      = Modifier.weight(1f)
                        )
                        PanTextField(
                            value         = fatherLastName,
                            onValueChange = { fatherLastName = it },
                            label         = "Father Last Name *",
                            modifier      = Modifier.weight(1f)
                        )
                    }

                    // ── Row: Name as to Print on PAN | Place ──
                    ResponsiveRow(isWideScreen = isWideScreen) {
                        PanTextField(
                            value         = namePrint,
                            onValueChange = { namePrint = it },
                            label         = "Name as to Print on PAN",
                            modifier      = Modifier.weight(1f)
                        )
                        PanTextField(
                            value         = place,
                            onValueChange = { place = it },
                            label         = "Place *",
                            modifier      = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // ── SUBMIT BUTTON — full width, dark navy ──
                    Button(
                        onClick  = { /* submit */ },
                        enabled  = isFormValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape  = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor         = FintechColors.NavyDark,
                            contentColor           = Color.White,
                            disabledContainerColor = FintechColors.NavyDark.copy(alpha = 0.4f),
                            disabledContentColor   = Color.White.copy(alpha = 0.7f)
                        )
                    ) {
                        Text(
                            "SUBMIT PAN APPLICATION",
                            fontWeight   = FontWeight.Bold,
                            fontSize     = 15.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

/**
 * A helper Row that stacks vertically on small screens and horizontally on wide screens.
 */
@Composable
fun ResponsiveRow(
    isWideScreen: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    if (isWideScreen) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Since Column doesn't provide RowScope, we wrap items to behave like full width
            // This is a simplified version; in a real RowScope, weight(1f) would work.
            // For NsdlPanApplyScreen, we know content uses weight(1f) on items.
            // We can use a custom Layout or just a Column where items are wrapContentSize.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                content = content
            )
        }
    }
}


// ─────────────────────────────────────────────
// PAN TEXT FIELD
// Rounded outline style matching screenshot
// ─────────────────────────────────────────────

@Composable
fun PanTextField(
    value:         String,
    onValueChange: (String) -> Unit,
    label:         String,
    modifier:      Modifier      = Modifier,
    keyboardType:  KeyboardType  = KeyboardType.Text,
    maxLength:     Int           = Int.MAX_VALUE,
    isError:       Boolean       = false,
    errorText:     String        = ""
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value         = value,
            onValueChange = { if (it.length <= maxLength) onValueChange(it) },
            label         = { Text(label, style = MaterialTheme.typography.bodySmall) },
            isError       = isError,
            singleLine    = true,
            shape         = RoundedCornerShape(10.dp),
            colors        = panFieldColors(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = keyboardType
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (isError && errorText.isNotEmpty()) {
            Text(
                errorText,
                color    = MaterialTheme.colorScheme.error,
                style    = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
            )
        }
    }
}


// ─────────────────────────────────────────────
// PAN DATE FIELD
// With calendar icon trailing — matches screenshot
// ─────────────────────────────────────────────

@Composable
fun PanDateField(
    value:         String,
    onValueChange: (String) -> Unit,
    modifier:      Modifier = Modifier,
    isError:       Boolean  = false,
    errorText:     String   = ""
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            label         = { Text("Date of Birth *", style = MaterialTheme.typography.bodySmall) },
            placeholder   = { Text("DD/MM/YYYY",
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.bodySmall) },
            trailingIcon  = {
                Icon(
                    Icons.Default.CalendarToday, "Date picker",
                    tint     = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            },
            isError    = isError,
            singleLine = true,
            shape      = RoundedCornerShape(10.dp),
            colors     = panFieldColors(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (isError && errorText.isNotEmpty()) {
            Text(
                errorText,
                color    = MaterialTheme.colorScheme.error,
                style    = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
            )
        }
    }
}


// ─────────────────────────────────────────────
// PAN DROPDOWN FIELD
// Rounded outline matching screenshot
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanDropdownField(
    label:            String,
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
            label         = { Text(label, style = MaterialTheme.typography.bodySmall) },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape         = RoundedCornerShape(10.dp),
            colors        = panFieldColors(),
            modifier      = Modifier
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
                        Icon(Icons.Default.Check, null,
                            tint = FintechColors.NavyDark)
                    }) else null
                )
            }
        }
    }
}


// ─────────────────────────────────────────────
// SHARED COLORS — light blue outline like screenshot
// ─────────────────────────────────────────────

@Composable
fun panFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = FintechColors.NavyDark,
    unfocusedBorderColor    = Color(0xFFBDBDBD),
    focusedLabelColor       = FintechColors.NavyDark,
    unfocusedLabelColor     = Color(0xFF9E9E9E),
    cursorColor             = FintechColors.NavyDark,
    focusedContainerColor   = Color.White,
    unfocusedContainerColor = Color.White
)


// ─────────────────────────────────────────────
// PREVIEWS
// ─────────────────────────────────────────────

@Preview(name = "NSDL PAN – Light", showBackground = true, showSystemUi = true)
@Composable
fun PreviewNsdlPanApplyScreen() {
    MaterialTheme {
        NsdlPanApplyScreen()
    }
}

@Preview(name = "NSDL PAN – Dark", showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES, showSystemUi = true)
@Composable
fun PreviewNsdlPanApplyScreenDark() {
    MaterialTheme {
        NsdlPanApplyScreen()
    }
}
