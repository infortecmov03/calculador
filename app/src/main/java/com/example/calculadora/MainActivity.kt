package com.example.calculadora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculadora.ui.screens.MainScreen
import com.example.calculadora.ui.theme.CalculadoraTheme
import com.example.calculadora.ui.viewmodels.ListaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculadoraTheme {
                val viewModel: ListaViewModel = viewModel()
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
