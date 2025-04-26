package eina.unizar.frontend_movil.ui.screens

import MainMenuScreen
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import eina.unizar.frontend_movil.ui.functions.SharedPrefsUtil
import eina.unizar.frontend_movil.ui.models.Friend
import eina.unizar.frontend_movil.ui.models.Message
import eina.unizar.frontend_movil.ui.theme.*
import eina.unizar.frontend_movil.ui.viewmodel.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinPrivateRoomScreen(navController: NavController) {
    var roomCode by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UNIRSE A SALA", color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = PurpleBackground
                )
            )
        },
        containerColor = PurpleBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardGray.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextField(
                        value = roomCode,
                        onValueChange = { roomCode = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Código de sala", color = TextWhite.copy(0.5f)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = TextWhite.copy(0.5f),
                            unfocusedIndicatorColor = TextWhite.copy(0.3f),
                            cursorColor = TextWhite
                        ),
                        textStyle = LocalTextStyle.current.copy(color = TextWhite),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            autoCorrect = false
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (roomCode.length == 6) {
                                navController.navigate("waiting-room/$roomCode")
                            } else {
                                Toast.makeText(context, "Código inválido", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenMessage.copy(alpha = 0.8f)
                        )
                    ) {
                        Text("UNIRSE A LA SALA", color = TextWhite)
                    }
                }
            }
        }
    }
}