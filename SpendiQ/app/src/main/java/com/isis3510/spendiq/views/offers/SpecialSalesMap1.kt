package com.isis3510.spendiq.view.offers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.isis3510.spendiq.model.data.Offer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialSalesMap1(modifier: Modifier = Modifier, offer: Offer, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .requiredHeight(height = 852.dp)
                .background(color = Color.White)
        ) {
            // Background Box
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape = RoundedCornerShape(7.dp))
                    .background(color = Color(0xfffcf5f3))
            )

            // Offer Image
            offer.shopImage?.let {
                Image(
                    painter = rememberImagePainter(it),
                    contentDescription = "Offer Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(shape = RoundedCornerShape(7.dp))
                        .border(
                            border = BorderStroke(1.dp, Color.Black),
                            shape = RoundedCornerShape(7.dp)
                        ),
                    contentScale = ContentScale.Crop
                )
            }

            // Title Text
            Text(
                text = "Special Sales in your Area",
                color = Color.Black,
                style = TextStyle(
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 16.dp, y = 78.dp)
            )

            // Place Name
            Text(
                text = offer.placeName ?: "",
                color = Color.Black,
                style = TextStyle(
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 96.dp, y = 162.dp)
                    .requiredWidth(width = 197.dp)
            )

            // Offer Description
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Sales\n")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("\n${offer.offerDescription}\n")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                    ) {
                        append("Recommended for: ${offer.recommendationReason}")
                    }
                },
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 27.dp, y = 636.dp)
                    .requiredWidth(width = 353.dp)
            )
        }
    }
}
