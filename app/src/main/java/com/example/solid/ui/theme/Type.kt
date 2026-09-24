package com.example.solid.ui.theme
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.solid.R

val TicketingFontFamily = FontFamily(
    Font(R.font.ticketing_regular, FontWeight.Normal)
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = TicketingFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(fontFamily = TicketingFontFamily, fontWeight = FontWeight.Normal, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = TicketingFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    titleSmall = TextStyle(fontFamily = TicketingFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodyMedium = TextStyle(fontFamily = TicketingFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = TicketingFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelMedium = TextStyle(fontFamily = TicketingFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = TicketingFontFamily, fontWeight = FontWeight.Normal, fontSize = 11.sp),
    labelLarge = TextStyle(fontFamily = TicketingFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    headlineSmall = TextStyle(fontFamily = TicketingFontFamily, fontWeight = FontWeight.Normal, fontSize = 24.sp)
)