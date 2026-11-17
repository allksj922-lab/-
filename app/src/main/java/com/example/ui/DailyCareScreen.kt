package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.MedicationSlot
import com.example.ui.theme.DeepCharcoalText
import com.example.ui.theme.DinnerIndigo
import com.example.ui.theme.DinnerIndigoBg
import com.example.ui.theme.EmeraldDoneBorder
import com.example.ui.theme.EmeraldDoneContainer
import com.example.ui.theme.EmeraldDoneLightBg
import com.example.ui.theme.EmeraldDoneText
import com.example.ui.theme.LunchOrange
import com.example.ui.theme.LunchOrangeBg
import com.example.ui.theme.MorningAmber
import com.example.ui.theme.MorningAmberBg
import com.example.ui.theme.OceanBlueBorder
import com.example.ui.theme.OceanBluePrimary
import com.example.ui.theme.WarmIvoryBackground
import com.example.ui.theme.WarmIvoryCard
import com.example.ui.theme.WarmIvorySubtle
import com.example.ui.theme.WarmSlateText
import com.example.ui.theme.WarmStoneBorder
import com.example.ui.theme.WaterCardBorder
import com.example.ui.theme.WaterCardContainer
import com.example.ui.theme.WaterCupEmptyFill
import com.example.ui.theme.WaterCupEmptyOutline
import com.example.ui.theme.WaterFillBottom
import com.example.ui.theme.WaterFillTop

private const val DAILY_WATER_GOAL_CUPS = 8

@Composable
fun DailyCareScreen(
    uiState: DailyCareUiState,
    onToggleMedication: (MedicationSlot) -> Unit,
    onAddWaterCup: () -> Unit,
    onRemoveWaterCup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvoryBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            TodayDateHeaderCard(
                formattedDate = uiState.formattedDate,
                dayOfWeek = uiState.dayOfWeek,
                completedCount = uiState.record.completedMedicationCount,
                allDone = uiState.record.allMedicationsTaken
            )

            MedicationSection(
                morningTaken = uiState.record.morningTaken,
                lunchTaken = uiState.record.lunchTaken,
                dinnerTaken = uiState.record.dinnerTaken,
                onToggleMedication = { slot ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleMedication(slot)
                }
            )

            WaterIntakeSection(
                waterCups = uiState.record.waterCups,
                onAddWaterCup = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAddWaterCup()
                },
                onRemoveWaterCup = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onRemoveWaterCup()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TodayDateHeaderCard(
    formattedDate: String,
    dayOfWeek: String,
    completedCount: Int,
    allDone: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("today_date_card"),
        shape = RoundedCornerShape(24.dp),
        color = WarmIvoryCard,
        border = BorderStroke(2.dp, WarmStoneBorder),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CalendarToday,
                    contentDescription = null,
                    tint = EmeraldDoneContainer,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = stringResource(R.string.today_label),
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmSlateText
                )
            }

            Text(
                text = "$formattedDate $dayOfWeek",
                fontSize = 28.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Bold,
                color = DeepCharcoalText,
                modifier = Modifier.testTag("today_date_text")
            )

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (allDone) EmeraldDoneLightBg else WarmIvorySubtle,
                border = BorderStroke(
                    width = 1.5.dp,
                    color = if (allDone) EmeraldDoneContainer else WarmStoneBorder
                )
            ) {
                Text(
                    text = if (allDone) {
                        stringResource(R.string.med_all_done_message)
                    } else {
                        stringResource(R.string.med_progress_format, completedCount)
                    },
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (allDone) EmeraldDoneText else DeepCharcoalText,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun MedicationSection(
    morningTaken: Boolean,
    lunchTaken: Boolean,
    dinnerTaken: Boolean,
    onToggleMedication: (MedicationSlot) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = stringResource(R.string.med_section_title),
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Bold,
            color = DeepCharcoalText,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        MedicationCheckButton(
            label = stringResource(R.string.med_morning),
            icon = Icons.Rounded.WbTwilight,
            iconDescription = stringResource(R.string.cd_med_morning_icon),
            accentColor = MorningAmber,
            accentBgColor = MorningAmberBg,
            isTaken = morningTaken,
            testTag = "med_button_morning",
            onClick = { onToggleMedication(MedicationSlot.MORNING) }
        )

        MedicationCheckButton(
            label = stringResource(R.string.med_lunch),
            icon = Icons.Rounded.LightMode,
            iconDescription = stringResource(R.string.cd_med_lunch_icon),
            accentColor = LunchOrange,
            accentBgColor = LunchOrangeBg,
            isTaken = lunchTaken,
            testTag = "med_button_lunch",
            onClick = { onToggleMedication(MedicationSlot.LUNCH) }
        )

        MedicationCheckButton(
            label = stringResource(R.string.med_dinner),
            icon = Icons.Rounded.NightsStay,
            iconDescription = stringResource(R.string.cd_med_dinner_icon),
            accentColor = DinnerIndigo,
            accentBgColor = DinnerIndigoBg,
            isTaken = dinnerTaken,
            testTag = "med_button_dinner",
            onClick = { onToggleMedication(MedicationSlot.DINNER) }
        )
    }
}

@Composable
private fun MedicationCheckButton(
    label: String,
    icon: ImageVector,
    iconDescription: String,
    accentColor: Color,
    accentBgColor: Color,
    isTaken: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isTaken) EmeraldDoneContainer else WarmIvoryCard,
        animationSpec = tween(durationMillis = 220),
        label = "med_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isTaken) EmeraldDoneBorder else WarmStoneBorder,
        animationSpec = tween(durationMillis = 220),
        label = "med_border"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isTaken) Color.White else DeepCharcoalText,
        animationSpec = tween(durationMillis = 220),
        label = "med_text"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(22.dp),
        color = containerColor,
        border = BorderStroke(2.5.dp, borderColor),
        shadowElevation = if (isTaken) 4.dp else 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(
                            color = if (isTaken) Color.White.copy(alpha = 0.2f) else accentBgColor,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = iconDescription,
                        tint = if (isTaken) Color.White else accentColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = label,
                    fontSize = 27.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (isTaken) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .background(
                            color = Color.White.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    AnimatedVisibility(
                        visible = true,
                        enter = scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) + fadeIn()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = stringResource(R.string.cd_med_done_icon),
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.med_status_done),
                        fontSize = 23.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = WarmIvorySubtle,
                    border = BorderStroke(1.5.dp, WarmStoneBorder)
                ) {
                    Text(
                        text = stringResource(R.string.med_status_pending),
                        fontSize = 22.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WarmSlateText,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WaterIntakeSection(
    waterCups: Int,
    onAddWaterCup: () -> Unit,
    onRemoveWaterCup: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("water_section_card"),
        shape = RoundedCornerShape(26.dp),
        color = WaterCardContainer,
        border = BorderStroke(2.5.dp, WaterCardBorder),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WaterDrop,
                        contentDescription = stringResource(R.string.cd_water_drop_icon),
                        tint = OceanBluePrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = stringResource(R.string.water_section_title),
                        fontSize = 24.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepCharcoalText
                    )
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = waterCups.toString(),
                        fontSize = 44.sp,
                        lineHeight = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = OceanBluePrimary,
                        modifier = Modifier.testTag("water_count_number")
                    )
                    Text(
                        text = stringResource(R.string.water_count_unit),
                        fontSize = 26.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepCharcoalText,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            WaterCupsGrid(waterCups = waterCups)

            if (waterCups >= DAILY_WATER_GOAL_CUPS) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = EmeraldDoneLightBg,
                    border = BorderStroke(1.5.dp, EmeraldDoneContainer)
                ) {
                    Text(
                        text = if (waterCups > DAILY_WATER_GOAL_CUPS) {
                            stringResource(
                                R.string.water_extra_cups_format,
                                waterCups - DAILY_WATER_GOAL_CUPS
                            )
                        } else {
                            stringResource(R.string.water_goal_done_message)
                        },
                        fontSize = 22.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDoneText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            Button(
                onClick = onAddWaterCup,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 84.dp)
                    .testTag("add_water_button"),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OceanBluePrimary,
                    contentColor = Color.White
                ),
                border = BorderStroke(2.dp, OceanBlueBorder),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.WaterDrop,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.water_add_button),
                    fontSize = 23.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            if (waterCups > 0) {
                OutlinedButton(
                    onClick = onRemoveWaterCup,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 58.dp)
                        .testTag("remove_water_button"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, WaterCardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = WarmIvoryCard,
                        contentColor = WarmSlateText
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = stringResource(R.string.cd_water_undo_icon),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.water_undo_button),
                        fontSize = 22.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun WaterCupsGrid(waterCups: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        for (rowIndex in 0 until 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (colIndex in 0 until 4) {
                    val cupNumber = rowIndex * 4 + colIndex + 1
                    val isFilled = cupNumber <= waterCups
                    val cupDesc = if (isFilled) {
                        stringResource(R.string.cd_water_cup_filled, cupNumber)
                    } else {
                        stringResource(R.string.cd_water_cup_empty, cupNumber)
                    }
                    WaterCupIcon(
                        isFilled = isFilled,
                        description = cupDesc,
                        modifier = Modifier
                            .size(width = 56.dp, height = 64.dp)
                            .testTag("water_cup_$cupNumber")
                    )
                }
            }
        }
    }
}

@Composable
private fun WaterCupIcon(
    isFilled: Boolean,
    description: String,
    modifier: Modifier = Modifier
) {
    val fillFraction by animateFloatAsState(
        targetValue = if (isFilled) 0.82f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "water_cup_fill"
    )

    Canvas(
        modifier = modifier.semantics {
            contentDescription = description
        }
    ) {
        val w = size.width
        val h = size.height
        val strokeWidth = w * 0.08f
        val topLeftX = w * 0.14f
        val topRightX = w * 0.86f
        val bottomLeftX = w * 0.24f
        val bottomRightX = w * 0.76f
        val topY = h * 0.12f
        val bottomY = h * 0.90f

        val cupPath = Path().apply {
            moveTo(topLeftX, topY)
            lineTo(topRightX, topY)
            lineTo(bottomRightX, bottomY)
            lineTo(bottomLeftX, bottomY)
            close()
        }

        // Cup interior background
        drawPath(
            path = cupPath,
            color = WaterCupEmptyFill
        )

        // Animated water fill clipped to the cup interior
        if (fillFraction > 0.01f) {
            clipPath(cupPath) {
                val waterHeight = (bottomY - topY) * fillFraction
                val waterTopY = bottomY - waterHeight
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(WaterFillTop, WaterFillBottom),
                        startY = waterTopY,
                        endY = bottomY
                    ),
                    topLeft = Offset(0f, waterTopY),
                    size = Size(w, waterHeight + strokeWidth)
                )
                // Bright meniscus surface highlight
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.45f),
                    topLeft = Offset(w * 0.20f, waterTopY + h * 0.02f),
                    size = Size(w * 0.60f, h * 0.05f),
                    cornerRadius = CornerRadius(h * 0.03f, h * 0.03f)
                )
            }
        }

        // Bold high-contrast cup outline
        drawPath(
            path = cupPath,
            color = if (isFilled) OceanBluePrimary else WaterCupEmptyOutline,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
