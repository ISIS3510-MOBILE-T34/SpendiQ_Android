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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isis3510.spendiq.R
import com.isis3510.spendiq.viewmodel.signup.SignUpViewModel
import com.isis3510.spendiq.views.theme.Purple40

@Composable
fun SignUpScreen(
    openAndPopUp: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val email = viewModel.email.collectAsState()
    val password = viewModel.password.collectAsState()
    val confirmPassword = viewModel.confirmPassword.collectAsState()
    val (checkedState, onStateChange) = remember { mutableStateOf(true) }

    Box(
        modifier = modifier
//            .requiredWidth(width = 393.dp)
//            .requiredHeight(height = 852.dp)
            .fillMaxSize()
            .clip(shape = RoundedCornerShape(7.dp))
            .background(color = Color.White)
    ) {
        Image(
            painter = painterResource(R.drawable.logoup),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 185.dp,
                    y = (-70).dp)
                .size(300.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.leftactionable),
            contentDescription = "Left Actionable",
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 16.dp,
                    y = 50.dp)
                .requiredSize(size = 24.dp)
                .clip(shape = RoundedCornerShape(7.dp))
        )
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create Free Account",
                color = Color.Black,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 48.sp,
                    fontFamily = FontFamily.SansSerif),
                modifier = Modifier
                    .offset(x = (-2).dp,
                        y = 38.dp)
                    .requiredWidth(width = 393.dp)
                    .requiredHeight(height = 112.dp)
            )

            Spacer(modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp))

            Spacer(modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp))

            Spacer(modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp))

            OutlinedTextField(
                singleLine = true,
                modifier = modifier
                    .fillMaxWidth()

                    .padding(16.dp, 4.dp)
                    .border(
                        BorderStroke(width = 2.dp, color = Purple40),
                        shape = RoundedCornerShape(50)
                    ),
                value = email.value,
                onValueChange = { viewModel.updateEmail(it) },
                placeholder = {  Text(text = "E-mail") },
                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Email") }
            )

            OutlinedTextField(
                singleLine = true,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp, 4.dp)
                    .border(
                        BorderStroke(width = 2.dp, color = Purple40),
                        shape = RoundedCornerShape(50)
                    ),
                value = password.value,
                onValueChange = { viewModel.updatePassword(it) },
                placeholder = { Text(text = "Password") },
                leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Email") },
                visualTransformation = PasswordVisualTransformation()
            )

            OutlinedTextField(
                singleLine = true,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp, 4.dp)
                    .border(
                        BorderStroke(width = 2.dp, color = Purple40),
                        shape = RoundedCornerShape(50)
                    ),
                value = confirmPassword.value,
                onValueChange = { viewModel.updateConfirmPassword(it) },
                placeholder = { Text(text = "Confirm Password") },
                leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Email") },
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = checkedState,
                        onValueChange = { onStateChange(!checkedState) },
                        role = Role.Checkbox
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = checkedState,
                    onCheckedChange = null // null recommended for accessibility with screenreaders
                )
                Text(
                    text = "Option selection",
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp))

            Button(
                onClick = { viewModel.onSignUpClick(openAndPopUp) },
                shape = RoundedCornerShape(7.dp),
                modifier = modifier
                    .requiredWidth(width = 280.dp)
                    .padding(16.dp, 0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff65558f)),
                ) {
                Text(
                    text = "Sign Up",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 1.43.em,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 20.sp,
                        letterSpacing = 0.1.sp),
                    modifier = Modifier
                        .wrapContentHeight(align = Alignment.CenterVertically)
                )
            }


        }
        Image(
            painter = painterResource(R.drawable.logodown),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-185).dp,
                    y = 500.dp)
                .size(300.dp)
        )

    }

}

@Preview
@Composable
private fun UserRegistrationPreview() {
    SignUpScreen({ _, _ -> })
}