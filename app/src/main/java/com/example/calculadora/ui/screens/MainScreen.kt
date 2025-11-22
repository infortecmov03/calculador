package com.example.calculadora.ui.screens

// Importe a sua tela de calculadora
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculadora.ui.viewmodels.ListaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class TipoTela {
    SIMPLES,
    LISTA_COMPRAS,
    SUPER_SUITE
}

@Composable
fun MainScreen(viewModel: ListaViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var telaAtual by remember { mutableStateOf(TipoTela.SIMPLES) }
    var mostrarConfiguracoes by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Toast.makeText(context, "Arraste da esquerda ou clique no > para o menu", Toast.LENGTH_LONG).show()
    }

    if (telaAtual == TipoTela.SUPER_SUITE) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Em breve: Super Suite")
        }
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { telaAtual = TipoTela.SIMPLES },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Home, "Voltar")
            }
        }
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF2c3e50), Color(0xFF000000))
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column {
                        Text(
                            "Menu Principal",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Ferramentas Matemáticas",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                HorizontalDivider()

                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Calculate, null) },
                        label = { Text("Calculadora Simples") },
                        selected = telaAtual == TipoTela.SIMPLES,
                        onClick = {
                            telaAtual = TipoTela.SIMPLES; scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.ShoppingCart, null) },
                        label = { Text("Calculador em Lista ") },
                        selected = telaAtual == TipoTela.LISTA_COMPRAS,
                        onClick = {
                            telaAtual = TipoTela.LISTA_COMPRAS; scope.launch { drawerState.close() }

                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Apps, null) },
                        label = { Text("Mais Calculadoras...") },
                        badge = { Badge { Text("Pro") } },
                        selected = false,
                        onClick = {
                            telaAtual = TipoTela.SUPER_SUITE; scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }

                HorizontalDivider()
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Sobre") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close(); mostrarConfiguracoes = true } },
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (telaAtual) {
                    TipoTela.SIMPLES -> CalculadoraSimplesScreen()
                    TipoTela.LISTA_COMPRAS -> CalculadoraDeListaScreen(viewModel)
                    TipoTela.SUPER_SUITE -> {}
                    else -> {}
                }
            }

            BotaoMenuAnimado(onClick = { scope.launch { drawerState.open() } })
        }

        if (mostrarConfiguracoes) {
            AppConfigDialog(onDismiss = { mostrarConfiguracoes = false })
        }
    }
}

@Composable
fun BotaoMenuAnimado(onClick: () -> Unit) {
    var devePulsar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60000)
            devePulsar = true
            delay(2000)
            devePulsar = false
        }
    }

    val escala by animateFloatAsState(if (devePulsar) 1.3f else 1.0f, label = "escala")
    val alpha by animateFloatAsState(if (devePulsar) 1f else 0.3f, label = "alpha")

    Box(
        modifier = Modifier
            .padding(top = 40.dp, start = 16.dp)
            .size(40.dp)
            .alpha(alpha)
            .scale(escala)
            .background(Color.Black.copy(alpha = 0.2f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.List,
            contentDescription = "Menu",
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun AppConfigDialog(onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Info, null) },
        title = { Text("Sobre o Aplicativo") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ItemConfiguracao(Icons.Default.CheckCircle, "Versão", "v3.0.0 (Voice Pro)")
                HorizontalDivider()
                ItemConfiguracao(
                    Icons.Default.Person,
                    "Dev",
                    "Fernando J. Antonio",
                    onClick = { uriHandler.openUri("https://infortecmov.netlify.app") }
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}

@Composable
private fun ItemConfiguracao(
    icon: ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null, onClick = { onClick?.invoke() })
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(titulo, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitulo, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
