package com.example.calculadora.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculadora.ui.viewmodels.ListaViewModel
import com.example.calculadora.utils.PdfUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculadoraDeListaScreen(viewModel: ListaViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val keyboardController = LocalSoftwareKeyboardController.current

    // --- ESTADOS DE CONTROLE ---
    var showExportDialog by remember { mutableStateOf(false) }
    var showDeleteListDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Estados para Edição
    var itemParaEditarIndexReal by remember { mutableIntStateOf(-1) }
    var editNome by remember { mutableStateOf("") }
    var editValor by remember { mutableStateOf("") }

    // Estados de Input Principal
    var nomeItem by remember { mutableStateOf("") }
    var valorItem by remember { mutableStateOf("") }
    // Categoria mantida internamente como "Geral" para compatibilidade
    val categoriaSelecionada = "Geral"

    // Estado para Nova Lista
    var novaListaNome by remember { mutableStateOf("") }

    // Estados para Assinatura/Personalização do PDF
    var nomeAssinatura by remember { mutableStateOf("") }
    var tituloRelatorio by remember { mutableStateOf("Relatório de Custos") }

    // --- ORDENAÇÃO AUTOMÁTICA PARA VISUALIZAÇÃO ---
    // A lista visual é sempre ordenada pelo maior valor
    val listaVisualOrdenada = viewModel.listaAtual.mapIndexed { index, item ->
        Pair(index, item)
    }.sortedByDescending { it.second.valor }

    // --- MENU LATERAL (DRAWER) ---
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            "Gestão de Listas",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Organize seus projetos",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                HorizontalDivider()

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(viewModel.nomesDasListas.size) { index ->
                        val nomeLista = viewModel.nomesDasListas[index]
                        NavigationDrawerItem(
                            label = { Text(nomeLista, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            selected = nomeLista == viewModel.nomeListaAtual,
                            onClick = {
                                viewModel.mudarLista(nomeLista)
                                scope.launch { drawerState.close() }
                            },
                            icon = { Icon(Icons.AutoMirrored.Filled.List, null) },
                            badge = {
                                if (nomeLista == viewModel.nomeListaAtual && viewModel.nomesDasListas.size > 1) {
                                    IconButton(onClick = { showDeleteListDialog = true }) {
                                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }

                HorizontalDivider()

                // Área de Criar Nova Lista
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Nova Lista", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = novaListaNome,
                            onValueChange = { novaListaNome = it },
                            placeholder = { Text("Ex: Material Obra") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledIconButton(
                            onClick = {
                                if (novaListaNome.isNotBlank()) {
                                    viewModel.criarNovaLista(novaListaNome)
                                    novaListaNome = ""
                                    Toast.makeText(context, "Lista criada com sucesso!", Toast.LENGTH_SHORT).show()
                                    scope.launch { drawerState.close() }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, "Criar")
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showExportDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp),
                    icon = { Icon(Icons.Default.Share, null) },
                    text = { Text("EXPORTAR", fontWeight = FontWeight.Bold) }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                // --- CABEÇALHO DA TELA ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.primary)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = viewModel.nomeListaAtual.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "MZN ${"%.2f".format(viewModel.valorTotal)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF27ae60) // Verde Dinheiro
                        )
                    }
                }

                // --- CARD DE ADIÇÃO DE ITEM ---
                Card(
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = nomeItem,
                                onValueChange = { nomeItem = it },
                                label = { Text("Descrição do Item") },
                                modifier = Modifier.weight(1.8f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Next
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = valorItem,
                                onValueChange = { valorItem = it },
                                label = { Text("Valor") },
                                modifier = Modifier.weight(1.2f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    val valorDouble = valorItem.replace(',', '.').toDoubleOrNull()
                                    if (nomeItem.isNotBlank() && valorDouble != null) {
                                        viewModel.adicionarItem(nomeItem, valorDouble, categoriaSelecionada)
                                        nomeItem = ""; valorItem = ""; keyboardController?.hide()
                                    }
                                })
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val valorDouble = valorItem.replace(',', '.').toDoubleOrNull()
                                if (nomeItem.isNotBlank() && valorDouble != null) {
                                    viewModel.adicionarItem(nomeItem, valorDouble, categoriaSelecionada)
                                    nomeItem = ""; valorItem = ""; keyboardController?.hide()
                                } else {
                                    Toast.makeText(context, "Preencha nome e valor", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ADICIONAR ITEM", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // --- TÍTULO DA LISTA (Separador) ---
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Itens Ordenados (Maior Valor)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // --- LISTA DE ITENS (Visualização Ordenada) ---
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(listaVisualOrdenada) { indexVisual, itemPair ->
                        val indexOriginal = itemPair.first
                        val item = itemPair.second
                        val ranking = indexVisual + 1

                        // Cores Profissionais para Badges (Ouro, Prata, Bronze)
                        val (badgeColor, badgeTextColor) = when (ranking) {
                            1 -> Color(0xFFD4AF37) to Color.White
                            2 -> Color(0xFFC0C0C0) to Color.White
                            3 -> Color(0xFFCD7F32) to Color.White
                            else -> Color(0xFFE0E0E0) to Color.Black
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    itemParaEditarIndexReal = indexOriginal
                                    editNome = item.nome
                                    editValor = item.valor.toString()
                                    showEditDialog = true
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(1.dp),
                            // Borda sutil apenas para itens fora do Top 3
                            border = if(ranking <= 3) null else BorderStroke(0.5.dp, Color.LightGray)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // BADGE DE RANKING (Profissional, sem Emojis)
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(badgeColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$ranking",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = badgeTextColor
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                // NOME DO ITEM
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.nome,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // VALOR E AÇÃO
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "MZN ${"%.2f".format(item.valor)}",
                                        fontWeight = FontWeight.Bold,
                                        color = if(ranking <= 3) Color(0xFF27ae60) else MaterialTheme.colorScheme.onSurface
                                    )

                                    // Botão de Deletar Discreto
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Remover",
                                        tint = Color.LightGray,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .padding(top = 4.dp)
                                            .clickable { viewModel.removerItem(indexOriginal) }
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(10.dp)) }
                }
            }
        }
    }

    // --- DIÁLOGO DE EDIÇÃO ---
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar Item") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editNome,
                        onValueChange = { editNome = it },
                        label = { Text("Nome do Item") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editValor,
                        onValueChange = { editValor = it },
                        label = { Text("Valor (MZN)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val novoValor = editValor.replace(',', '.').toDoubleOrNull()
                    if (editNome.isNotBlank() && novoValor != null && itemParaEditarIndexReal != -1) {
                        viewModel.editarItem(itemParaEditarIndexReal, editNome, novoValor)
                        showEditDialog = false
                        Toast.makeText(context, "Item atualizado!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Valores inválidos", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Salvar Alterações") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // --- DIÁLOGO DE EXPORTAÇÃO (Corrigido para não sobrepor) ---
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            icon = { Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Exportar Relatório", fontWeight = FontWeight.Bold) },
            text = {
                // Scroll para garantir visualização em telas pequenas
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "Personalize os dados antes de gerar o arquivo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = tituloRelatorio,
                        onValueChange = { tituloRelatorio = it },
                        label = { Text("Título do Documento") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nomeAssinatura,
                        onValueChange = { nomeAssinatura = it },
                        label = { Text("Nome do Responsável") },
                        placeholder = { Text("Ex: Eng. João") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botões de Ação
                    Button(
                        onClick = {
                            val listaOrdenada = viewModel.listaAtual.sortedByDescending { it.valor }
                            PdfUtil.gerarPdf(
                                context = context,
                                listaDeItens = listaOrdenada,
                                nomeLista = viewModel.nomeListaAtual,
                                tituloRelatorio = tituloRelatorio,
                                autor = nomeAssinatura
                            )
                            showExportDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("GERAR PDF ASSINADO")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            val listaOrdenada = viewModel.listaAtual.sortedByDescending { it.valor }
                            PdfUtil.gerarExcel(
                                context = context,
                                listaDeItens = listaOrdenada,
                                nomeLista = viewModel.nomeListaAtual
                            )
                            showExportDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.List, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("EXPORTAR EXCEL (CSV)")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showExportDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar", color = Color.Red)
                }
            }
        )
    }

    // Diálogo de Exclusão de Lista
    if (showDeleteListDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteListDialog = false },
            title = { Text("Excluir Lista?") },
            text = { Text("Tem certeza que deseja apagar '${viewModel.nomeListaAtual}'? Isso não pode ser desfeito.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.excluirListaAtual()
                        showDeleteListDialog = false
                        scope.launch { drawerState.open() }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Sim, Excluir") }
            },
            dismissButton = { TextButton(onClick = { showDeleteListDialog = false }) { Text("Cancelar") } }
        )
    }
}
