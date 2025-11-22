package com.example.calculadora.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculadora.ui.viewmodels.ListaViewModel
import kotlinx.coroutines.launch

// --- CLASSES RENOMEADAS PARA SEGURANÇA (EVITA CONFLITOS) ---

private data class TelaCalc_CategoriaMenu(
    val nome: String,
    val icone: ImageVector,
    val itens: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaCalculadoraDeLista(viewModel: ListaViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Controle do Modal de Configurações e Categorias
    var mostrarConfiguracoes by remember { mutableStateOf(false) }
    var categoriaExpandida by remember { mutableStateOf<String?>(null) }

    // --- LISTA COMPLETA DE CALCULADORAS RESTAURADA ---
    val todasCategorias = remember {
        listOf(
            TelaCalc_CategoriaMenu(
                "Geral", Icons.Default.Calculate,
                listOf("Simples", "Científica", "Gráfica", "Programável")
            ),
            TelaCalc_CategoriaMenu(
                "Financeiras", Icons.Default.AttachMoney,
                listOf("Juros", "Investimentos", "Empréstimos", "Finanças Pessoais", "Hipoteca", "Taxas", "ROI", "Aposentadoria")
            ),
            TelaCalc_CategoriaMenu(
                "Matemáticas", Icons.Default.Functions,
                listOf("Álgebra", "Cálculo", "Estatística", "Matrizes", "Equações", "Frações", "Números Complexos")
            ),
            TelaCalc_CategoriaMenu(
                "Geometria", Icons.Default.Hexagon,
                listOf("Área", "Volume", "Trigonometria", "Geometria Analítica", "Pitágoras")
            ),
            TelaCalc_CategoriaMenu(
                "Especializadas", Icons.Default.Stars,
                listOf("Conversão", "Unidades", "Idade", "IMC", "Calorias", "Gravidez", "Datas")
            ),
            TelaCalc_CategoriaMenu(
                "Engenharia", Icons.Default.Construction,
                listOf("Civil", "Eletricidade", "Estruturas", "Materiais", "Orçamentos")
            ),
            TelaCalc_CategoriaMenu(
                "Profissionais", Icons.Default.BusinessCenter,
                listOf("Comercial", "Impostos", "Folha de Pagamento", "Vendas", "Markup")
            ),
            TelaCalc_CategoriaMenu(
                "Desenvolvedores", Icons.Default.Code,
                listOf("Hexadecimal/Binária", "Base Numérica", "Bitwise", "Tempo de Dev", "Algoritmos")
            ),
            TelaCalc_CategoriaMenu(
                "Criativas", Icons.Default.Palette,
                listOf("Cores RGB/HEX", "Design", "Fotografia", "Música", "Tempo Musical")
            ),
            TelaCalc_CategoriaMenu(
                "Educação", Icons.Default.School,
                listOf("Para Crianças", "Aprendizado", "Interativa")
            ),
            TelaCalc_CategoriaMenu(
                "Utilitárias", Icons.Default.Build,
                listOf("Gorjeta", "Divisão de Contas", "Consumo", "Velocidade", "Distância")
            ),
            TelaCalc_CategoriaMenu(
                "Ideias Originais", Icons.Default.Lightbulb,
                listOf("Inteligente (IA)", "Códigos", "Expressões", "Multifuncional", "Temas")
            )
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                // Cabeçalho do Menu
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column {
                        Text("Calculadora", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Xikotela Multi-Tools", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Divider()

                // Lista Rolável de Itens
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Item Fixo: Nova Lista
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        label = { Text("Nova Lista de Compras") },
                        selected = false,
                        onClick = { scope.launch { drawerState.close() } },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        "Tipos de Calculadora",
                        modifier = Modifier.padding(start = 28.dp, top = 8.dp, bottom = 8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Geração Dinâmica das Categorias
                    todasCategorias.forEach { categoria ->
                        val isExpanded = categoriaExpandida == categoria.nome

                        NavigationDrawerItem(
                            icon = { Icon(categoria.icone, contentDescription = null) },
                            label = { Text(categoria.nome) },
                            selected = isExpanded,
                            badge = {
                                Icon(
                                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                categoriaExpandida = if (isExpanded) null else categoria.nome
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        // Sub-itens (Expandidos)
                        if (isExpanded) {
                            categoria.itens.forEach { tipoCalculadora ->
                                NavigationDrawerItem(
                                    label = { Text(tipoCalculadora, fontSize = 14.sp) },
                                    selected = false,
                                    onClick = {
                                        // Aqui você implementará a navegação futura
                                        scope.launch { drawerState.close() }
                                    },
                                    modifier = Modifier
                                        .padding(start = 32.dp, end = 12.dp)
                                        .height(40.dp)
                                )
                            }
                        }
                    }
                }

                Divider()

                // Configurações (Rodapé)
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Configurações & Sobre") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            mostrarConfiguracoes = true
                        }
                    },
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Calculadora Xikotela") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { if (drawerState.isClosed) drawerState.open() else drawerState.close() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Abrir Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.limparListaAtual() }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Limpar")
                        }
                    }
                )
            }
        ) { innerPadding ->
            // Conteúdo Principal da Tela
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Selecione uma calculadora no menu",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    "Use o ícone ☰ no topo esquerdo",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        if (mostrarConfiguracoes) {
            TelaCalc_ModalConfiguracoes(onDismiss = { mostrarConfiguracoes = false })
        }
    }
}

// --- COMPONENTES AUXILIARES (RENOMEADOS PARA SEGURANÇA) ---

@Composable
private fun TelaCalc_ModalConfiguracoes(onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Info, contentDescription = null) },
        title = { Text(text = "Sobre o App") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TelaCalc_ItemConfiguracao(Icons.Default.CheckCircle, "Versão", "v2.0.0 (Super Suite)")
                Divider()
                TelaCalc_ItemConfiguracao(
                    Icons.Default.Person,
                    "Desenvolvedor",
                    "Fernando J. Antonio",
                    onClick = { uriHandler.openUri("https://infortecmov.netlify.app") }
                )
                TelaCalc_ItemConfiguracao(
                    Icons.Default.Build,
                    "Código Fonte",
                    "GitHub",
                    onClick = { uriHandler.openUri("https://github.com/infortecmov03") }
                )
                TelaCalc_ItemConfiguracao(
                    Icons.Default.Share,
                    "Web App",
                    "Versão Online",
                    onClick = { uriHandler.openUri("https://calculadoraxikotela.netlify.app") }
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}

@Composable
private fun TelaCalc_ItemConfiguracao(icon: ImageVector, titulo: String, subtitulo: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null, onClick = { onClick?.invoke() })
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = titulo, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = subtitulo, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
