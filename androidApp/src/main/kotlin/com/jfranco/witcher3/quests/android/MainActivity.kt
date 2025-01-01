package com.jfranco.witcher3.quests.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jfranco.witcher3.quests.android.theme.AppTheme

@ExperimentalFoundationApi
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme(
                useDarkTheme = false
            ) {
                Surface {
                    Scaffold(
                        topBar = {
                            Surface(shadowElevation = 5.dp) {
                                CenterAlignedTopAppBar(title = { Text("The Witcher 3 Quests") })
                            }
                        }
                    ) { paddingValues ->
                        Box(
                            Modifier
                                .padding(paddingValues)
                                .fillMaxSize()
                        ) {
                            LoadDiContainer()
                        }
                    }
                }
            }
        }
    }
}

