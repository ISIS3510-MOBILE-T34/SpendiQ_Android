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
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.*
import com.google.type.LatLng
import com.isis3510.spendiq.model.data.Offer
import com.isis3510.spendiq.R

@Composable
fun SpecialSalesDetail(offer: Offer) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        // Background Box for overall UI
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape = RoundedCornerShape(7.dp))
                .background(color = Color(0xfffcf5f3))
        )

        // Offer Image at the top
        offer.shopImage?.let {
            Image(
                painter = rememberImagePainter(it),
                contentDescription = "Offer Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(shape = RoundedCornerShape(7.dp))
                    .border(
                        border = BorderStroke(1.dp, Color.Black),
                        shape = RoundedCornerShape(7.dp)
                    ),
                contentScale = ContentScale.Crop
            )
        }

        // Content and Details
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Special Sales in your Area",
                color = Color.Black,
                style = TextStyle(
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Offer Details Section
            offer.placeName?.let {
                Text(
                    text = it,
                    color = Color.Black,
                    style = TextStyle(
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Google Map to show the location of the offer
            if (offer.latitude != null && offer.longitude != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Offer Location",
                    color = Color.Black,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(vertical = 8.dp),
                    cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(
                            com.google.android.gms.maps.model.LatLng(
                                offer.latitude,
                                offer.longitude
                            ),
                            15f
                        )
                    }
                ) {
                    Marker(
                        state = MarkerState(
                            position = com.google.android.gms.maps.model.LatLng(
                                offer.latitude,
                                offer.longitude
                            )
                        ),
                        title = offer.placeName,
                        snippet = "Special Offer Location"
                    )
                }
            }
        }
    }
}
