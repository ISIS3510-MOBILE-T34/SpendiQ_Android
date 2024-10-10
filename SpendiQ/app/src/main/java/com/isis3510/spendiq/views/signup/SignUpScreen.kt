package com.isis3510.spendiq.views.signup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.isis3510.spendiq.R

@Composable
fun UserRegistration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
//            .requiredWidth(width = 393.dp)
//            .requiredHeight(height = 852.dp)
            .fillMaxSize()
            .clip(shape = RoundedCornerShape(7.dp))
            .background(color = Color.White)
            .paint(painterResource(R.drawable.logoup),
                alignment = Alignment.TopEnd)
            .paint(painterResource(R.drawable.logodown),
                alignment = Alignment.BottomStart)
    ) {
        Image(
            painter = painterResource(id = R.drawable.leftactionable),
            contentDescription = "Left Actionable",
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 16.dp,
                    y = 30.dp)
                .requiredSize(size = 24.dp)
                .clip(shape = RoundedCornerShape(7.dp)))

    }

}

@Preview
@Composable
private fun UserRegistrationPreview() {
    UserRegistration(Modifier)
}