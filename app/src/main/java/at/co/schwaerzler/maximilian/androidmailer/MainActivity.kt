package at.co.schwaerzler.maximilian.androidmailer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.co.schwaerzler.maximilian.androidmailer.ui.theme.AndroidMailerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidMailerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen() {
    var email by remember {
        mutableStateOf("")
    }

    fun sendEmail(to: String) {
        val emailSender = AndroidMailer.Builder()
            .from("Max S. <maschwaerzler@a1.net>")
            .to(to)
            .smtpServer(BuildConfig.smtpServer)
            .smtpPort(BuildConfig.smtpPort.toInt())
            .useStartTLS(BuildConfig.useStartTLS.toBooleanStrict())
            .subject("Test Email")
            .body("This is a test")
            .build()

        emailSender.send(BuildConfig.username, BuildConfig.password)
    }

    Scaffold { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                email,
                { email = it },
                Modifier.fillMaxWidth(),
                placeholder = {
                    Text("jane.doe@example.com")
                },
                label = {
                    Text("To Email Address")
                }
            )

            Button(onClick = {
                sendEmail(email)
            }, Modifier.fillMaxWidth()) {
                Text("Send Email")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    AndroidMailerTheme {
        MainScreen()
    }
}