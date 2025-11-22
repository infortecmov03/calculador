package com.example.calculadora.data

import java.util.UUID

data class ItemDaLista(
    val id: UUID = UUID.randomUUID(), // <--- ESTA LINHA É OBRIGATÓRIA PARA O 'key = { it.id }' FUNCIONAR
    val nome: String,
    val valor: Double,
    val categoria: String? = "Geral"
)
