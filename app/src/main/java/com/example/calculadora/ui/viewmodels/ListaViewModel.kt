package com.example.calculadora.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.calculadora.data.ItemDaLista
import java.util.UUID

class ListaViewModel : ViewModel() {

    // Mapa de Listas: "Nome da Lista" -> "Lista de Itens"
    private var todasAsListas by mutableStateOf(mutableMapOf<String, List<ItemDaLista>>(
        "Lista Principal" to emptyList()
    ))

    // Nome da lista atualmente selecionada
    var nomeListaAtual by mutableStateOf("Lista Principal")

    // --- PROPRIEDADES PÚBLICAS (Usadas pela UI) ---

    // Retorna a lista atual para a tela exibir
    val listaAtual: List<ItemDaLista>
        get() = todasAsListas[nomeListaAtual] ?: emptyList()

    // Nomes de todas as listas disponíveis para o Menu Lateral
    val nomesDasListas: List<String>
        get() = todasAsListas.keys.toList()

    val valorTotal: Double
        get() = listaAtual.sumOf { it.valor }

    // --- GERENCIAMENTO DE LISTAS ---

    fun criarNovaLista(nome: String) {
        if (nome.isNotBlank() && !todasAsListas.containsKey(nome)) {
            todasAsListas = (todasAsListas + (nome to emptyList())).toMutableMap()
            nomeListaAtual = nome // Já muda para a nova lista
        }
    }

    fun mudarLista(nome: String) {
        if (todasAsListas.containsKey(nome)) {
            nomeListaAtual = nome
        }
    }

    // Função chamada pelo botão de excluir no menu
    fun excluirListaAtual() {
        val nome = nomeListaAtual
        if (todasAsListas.size > 1 && todasAsListas.containsKey(nome)) {
            val novoMapa = todasAsListas.toMutableMap()
            novoMapa.remove(nome)
            todasAsListas = novoMapa
            // Volta para a primeira lista disponível
            nomeListaAtual = todasAsListas.keys.first()
        }
    }

    // --- CRUD DE ITENS (Adicionar, Remover, Editar) ---

    fun adicionarItem(nome: String, valor: Double, categoria: String) {
        if (nome.isNotBlank() && valor >= 0) {
            val novoItem = ItemDaLista(id = UUID.randomUUID(), nome = nome, valor = valor, categoria = categoria)
            atualizarListaAtual(listaAtual + novoItem)
        }
    }

    // Atualizado para receber o INDEX (posição) da lista, como a tela envia
    fun removerItem(index: Int) {
        if (index in listaAtual.indices) {
            val listaMutavel = listaAtual.toMutableList()
            listaMutavel.removeAt(index)
            atualizarListaAtual(listaMutavel)
        }
    }

    // Atualizado para receber o INDEX e os novos valores
    fun editarItem(index: Int, novoNome: String, novoValor: Double) {
        if (index in listaAtual.indices) {
            val listaMutavel = listaAtual.toMutableList()
            val itemAntigo = listaMutavel[index]

            // Mantém o ID e Categoria, muda só nome e valor
            listaMutavel[index] = itemAntigo.copy(nome = novoNome, valor = novoValor)

            atualizarListaAtual(listaMutavel)
        }
    }

    // --- ORDENAÇÃO ---

    fun ordenarPorNome() {
        atualizarListaAtual(listaAtual.sortedBy { it.nome })
    }

    fun ordenarPorValor() {
        atualizarListaAtual(listaAtual.sortedByDescending { it.valor })
    }

    fun limparListaAtual() {
        atualizarListaAtual(emptyList())
    }


    // --- HELPER PRIVATE ---
    private fun atualizarListaAtual(novosItens: List<ItemDaLista>) {
        val novoMapa = todasAsListas.toMutableMap()
        novoMapa[nomeListaAtual] = novosItens
        todasAsListas = novoMapa
    }
}
