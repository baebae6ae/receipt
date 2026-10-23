package com.baebae.receipt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.baebae.receipt.ui.navigation.ReceiptApp
import com.baebae.receipt.ui.theme.ReceiptTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReceiptTheme {
                ReceiptApp()
            }
        }
    }
}
