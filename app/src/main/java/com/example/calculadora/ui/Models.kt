package com.example.calculadora.ui

import java.util.UUID

// Representa um único item na sua lista
data class ItemDaLista(
    val id: String = UUID.randomUUID().toString(),
    val nome: String,
    val valor: Double,
    val categoria: String? = "Sem categoria"
)
