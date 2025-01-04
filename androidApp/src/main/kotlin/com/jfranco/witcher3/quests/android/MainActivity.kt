package com.jfranco.witcher3.quests.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import com.jfranco.witcher3.quests.android.ui.screens.QuestsScreen
import com.jfranco.witcher3.quests.android.ui.theme.AppTheme

@ExperimentalFoundationApi
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme(
                useDarkTheme = false
            ) {
                QuestsScreen()
            }
        }
    }
}


