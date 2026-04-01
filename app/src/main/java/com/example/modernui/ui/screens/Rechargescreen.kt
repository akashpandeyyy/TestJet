package com.example.modernui.ui.screens

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.modernui.ui.components.*
import com.example.modernui.ui.theme.FintechColors


private data class RechargePlan(
    val amount:   String,
    val validity: String,
    val benefits: String
)

private val popularPlans = listOf(
    RechargePlan("179",  "28 days", "2GB/day · Unlimited calls ·100 SMS/day"),
    RechargePlan("299",  "28 days", "3GB/day · Unlimited calls · 100 SMS/day"),
    RechargePlan("479",  "56 days", "2GB/day · Unlimited calls · 100 SMS/day"),
    RechargePlan("666",  "84 days", "2GB/day · Unlimited calls · 100 SMS/day"),
    RechargePlan("999",  "84 days", "3GB/day · Unlimited calls · 100 SMS/day"),
)

private val allStates = listOf(
    "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
    "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand",
    "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur",
    "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab",
    "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura",
    "Uttar Pradesh", "Uttarakhand", "West Bengal",
    "Delhi", "Jammu & Kashmir", "Ladakh"
)

private val mobileOperators = listOf("Jio", "Airtel", "Vi (Vodafone Idea)", "BSNL", "MTNL")
private val dthOperators    = listOf("Tata Play", "Dish TV", "Airtel DTH", "Sun Direct", "D2H")


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun RechargeScreen(
    onBackClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    // ── Tab state (Mobile / DTH) ──────────────
    var selectedTab by remember { mutableIntStateOf(0) }

    // ── Form state ────────────────────────────
    var subscriberNumber  by remember { mutableStateOf("") }
    var selectedOperator  by remember { mutableStateOf("") }
    var selectedState     by remember { mutableStateOf("") }
    var amount            by remember { mutableStateOf("") }
    var selectedPlanIndex by remember { mutableIntStateOf(-1) }

    // Reset operator when tab changes
    LaunchedEffect(selectedTab) {
        selectedOperator  = ""
        selectedState     = ""
        selectedPlanIndex = -1
        amount            = ""
    }

    val operators   = if (selectedTab == 0) mobileOperators else dthOperators
    val maxNumLen   = if (selectedTab == 0) 10 else 12
    val numError    = subscriberNumber.isNotEmpty() && subscriberNumber.length != maxNumLen
    val isFormValid = subscriberNumber.length == maxNumLen
            && selectedOperator.isNotEmpty()
            && selectedState.isNotEmpty()
            && amount.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        // ── Top bar ───────────────────────────
        DetailTopBar(title = "Recharge", onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Type selector card ────────────
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
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Select Recharge Type",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                        ) {
                            listOf("Mobile" to Icons.Default.PhoneAndroid,
                                "DTH"    to Icons.Default.Tv
                            ).forEachIndexed { index, (label, icon) ->
                                val isSelected = selectedTab == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Color.White else Color.Transparent)
                                        .clickable { selectedTab = index }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment     = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            icon, null,
                                            tint     = if (isSelected) FintechColors.NavyDark else Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            label,
                                            color      = if (isSelected) FintechColors.NavyDark else Color.White,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            style      = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Subscriber details ────────────
            SectionCard(
                title = if (selectedTab == 0) "Mobile Details" else "DTH Details",
                icon  = if (selectedTab == 0) Icons.Default.PhoneAndroid else Icons.Default.Tv
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    NavyOutlinedField(
                        value         = subscriberNumber,
                        onValueChange = { if (it.all(Char::isDigit)) subscriberNumber = it },
                        label         = if (selectedTab == 0) "Mobile Number *" else "Subscriber ID *",
                        placeholder   = if (selectedTab == 0) "10-digit mobile number" else "Enter subscriber ID",
                        leadingIcon   = if (selectedTab == 0) Icons.Default.Phone else Icons.Default.ConfirmationNumber,
                        keyboardType  = KeyboardType.Number,
                        maxLength     = maxNumLen,
                        isError       = numError,
                        errorMessage  = if (selectedTab == 0)
                            "Enter a valid 10-digit mobile number"
                        else
                            "Enter a valid subscriber ID",
                        trailingIcon  = if (subscriberNumber.length == maxNumLen) ({
                            Icon(Icons.Default.CheckCircle, null,
                                tint = FintechColors.SuccessGreen)
                        }) else null
                    )

                    // Operator + State side by side
//                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                        NavyDropdownField(
//                            label            = "Operator *",
//                            leadingIcon      = Icons.Default.Business,
//                            selectedValue    = selectedOperator,
//                            options          = operators,
//                            onOptionSelected = { selectedOperator = it },
//                            modifier         = Modifier.weight(1f)
//                        )
//                        NavyDropdownField(
//                            label            = "State *",
//                            leadingIcon      = Icons.Default.LocationOn,
//                            selectedValue    = selectedState,
//                            options          = allStates,
//                            onOptionSelected = { selectedState = it },
//                            modifier         = Modifier.weight(1f)
//                        )
//                    }
//                }
//            }


                    // new one
                    BoxWithConstraints {
                        val isCompact = maxWidth < 400.dp

                        if (isCompact) {
                            // Small screens → Stack vertically
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                                NavyDropdownField(
                                    label            = "Operator *",
                                    leadingIcon      = Icons.Default.Business,
                                    selectedValue    = selectedOperator,
                                    options          = operators,
                                    onOptionSelected = { selectedOperator = it },
                                    modifier         = Modifier.fillMaxWidth()
                                )

                                NavyDropdownField(
                                    label            = "State *",
                                    leadingIcon      = Icons.Default.LocationOn,
                                    selectedValue    = selectedState,
                                    options          = allStates,
                                    onOptionSelected = { selectedState = it },
                                    modifier         = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            // Large screens → Side by side (your original UI)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                                NavyDropdownField(
                                    label            = "Operator *",
                                    leadingIcon      = Icons.Default.Business,
                                    selectedValue    = selectedOperator,
                                    options          = operators,
                                    onOptionSelected = { selectedOperator = it },
                                    modifier         = Modifier.weight(1f)
                                )

                                NavyDropdownField(
                                    label            = "State *",
                                    leadingIcon      = Icons.Default.LocationOn,
                                    selectedValue    = selectedState,
                                    options          = allStates,
                                    onOptionSelected = { selectedState = it },
                                    modifier         = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // ── Operator confirmation strip ───
            if (selectedOperator.isNotEmpty()) {
                Surface(
                    shape    = RoundedCornerShape(12.dp),
                    color    = FintechColors.NavyDark.copy(alpha = 0.06f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape    = CircleShape,
                            color    = FintechColors.NavyDark.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(Icons.Default.Business, null,
                                    tint     = FintechColors.NavyDark,
                                    modifier = Modifier.size(22.dp))
                            }
                        }
                        Column {
                            Text(
                                selectedOperator,
                                fontWeight = FontWeight.Bold,
                                color      = FintechColors.NavyDark,
                                style      = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                selectedState.ifEmpty { "Select state" },
                                color = colorScheme.outline,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { selectedOperator = ""; selectedState = "" }) {
                            Text("Change", color = FintechColors.NavyDark,
                                style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // ── Amount & Plan picker ──────────
            SectionCard(title = "Amount & Plan", icon = Icons.Default.CurrencyRupee) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    NavyOutlinedField(
                        value         = amount,
                        onValueChange = {
                            amount            = it
                            selectedPlanIndex = -1          // deselect plan on manual entry
                        },
                        label       = "Amount (₹) *",
                        placeholder = "Enter or pick a plan below",
                        leadingIcon = Icons.Default.CurrencyRupee,
                        keyboardType = KeyboardType.Decimal
                    )

                    Text(
                        "Popular Plans",
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color      = FintechColors.NavyDark
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        popularPlans.forEachIndexed { index, plan ->
                            val isSelected = selectedPlanIndex == index
                            Surface(
                                shape    = RoundedCornerShape(12.dp),
                                color    = if (isSelected)
                                    FintechColors.NavyDark.copy(alpha = 0.08f)
                                else
                                    colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.dp,
                                        color = if (isSelected) FintechColors.NavyDark else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        selectedPlanIndex = index
                                        amount            = plan.amount
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) FintechColors.NavyDark
                                        else FintechColors.NavyDark.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            "₹${plan.amount}",
                                            modifier   = Modifier.padding(
                                                horizontal = 10.dp, vertical = 6.dp
                                            ),
                                            color      = if (isSelected) Color.White
                                            else FintechColors.NavyDark,
                                            fontWeight = FontWeight.Bold,
                                            style      = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            plan.benefits,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colorScheme.onSurface
                                        )
                                        Text(
                                            "Validity: ${plan.validity}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colorScheme.outline
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.CheckCircle, null,
                                            tint     = FintechColors.NavyDark,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Summary strip (shows when ready) ─
            if (isFormValid) {
                Surface(
                    shape    = RoundedCornerShape(12.dp),
                    color    = FintechColors.NavyDark.copy(alpha = 0.06f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Recharging",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.outline)
                            Text(subscriberNumber,
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Operator",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.outline)
                            Text(
                                selectedOperator.split(" ").first(),
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Amount",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.outline)
                            Text(
                                "₹$amount",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color      = FintechColors.NavyDark
                            )
                        }
                    }
                }
            }

            // ── Submit ────────────────────────
            NavyPrimaryButton(
                text    = "Proceed to Pay",
                onClick = { /* trigger payment */ },
                enabled = isFormValid,
                icon    = Icons.Default.Payment
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}


// ─────────────────────────────────────────────
// PREVIEWS
// ─────────────────────────────────────────────

@Preview(name = "Recharge – Light", showBackground = true)
@Preview(name = "Recharge – Dark",  showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewRechargeScreen() {
    MaterialTheme { RechargeScreen() }
}
