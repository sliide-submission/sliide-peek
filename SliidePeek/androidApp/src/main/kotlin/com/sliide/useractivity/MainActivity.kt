package com.sliide.useractivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.sliide.useractivity.data.local.DatabaseDriverFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(databaseDriverFactory = DatabaseDriverFactory(applicationContext))
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(databaseDriverFactory = DatabaseDriverFactory(LocalContext.current))
}