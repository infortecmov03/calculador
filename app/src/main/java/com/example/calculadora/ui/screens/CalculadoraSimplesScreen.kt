package com.example.calculadora.ui.screens

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

// --- PALETA DE CORES PREMIUM ---
object CalcColors {
    val Background = Color(0xFF121212)
    val VisorBackground = Color(0xFF252525)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB0B0B0)
    val TextExplanation = Color(0xFF909090)

    // Botões
    val ButtonNumBg = Color(0xFF2D2D2D)
    val ButtonOpBgStart = Color(0xFF4A4A4A)
    val ButtonOpBgEnd = Color(0xFF383838)
    val ButtonActionBg = Color(0xFF555555)

    // Destaques
    val AccentGreenStart = Color(0xFF4CAF50)
    val AccentGreenEnd = Color(0xFF2E7D32)
    val ControlPanelBg = Color(0xFF333333)
}

@Composable
fun CalculadoraSimplesScreen() {
    val context = LocalContext.current

    // --- ESTADOS DA CALCULADORA ---
    var num1 by rememberSaveable { mutableStateOf("") }
    var operador by rememberSaveable { mutableStateOf("") }
    var num2 by rememberSaveable { mutableStateOf("") }
    var num2IsPorcentagem by rememberSaveable { mutableStateOf(false) }

    var resultadoPreview by rememberSaveable { mutableStateOf("") }
    var explicacaoTexto by rememberSaveable { mutableStateOf("Olá, estudante! Digite um número para começarmos a aula.") }

    // --- ESTADOS DE VOZ (TTS) AVANÇADOS ---
    var isTtsEnabled by rememberSaveable { mutableStateOf(false) }
    var showTtsControls by rememberSaveable { mutableStateOf(false) }

    var speechRate by rememberSaveable { mutableFloatStateOf(1.0f) }
    var isSpeaking by remember { mutableStateOf(false) }

    // Controle de Vozes
    var availableVoices by remember { mutableStateOf<List<Voice>>(emptyList()) }
    var currentVoiceIndex by rememberSaveable { mutableIntStateOf(0) }
    var currentVoiceName by remember { mutableStateOf("Padrão") }

    var tts: TextToSpeech? by remember { mutableStateOf(null) }

    // --- INICIALIZAÇÃO DO TTS COM OTIMIZAÇÃO OFFLINE ---
    DisposableEffect(Unit) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // 1. Tenta configurar Português do Brasil
                val result = tts?.setLanguage(Locale("pt", "BR"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = Locale.getDefault()
                }

                // 2. Carregar e FILTRAR vozes para priorizar OFFLINE (Baixa Latência)
                try {
                    val allVoices = tts?.voices?.toList() ?: emptyList()
                    // Filtra apenas vozes em português
                    val ptVoices = allVoices.filter { it.locale.language == "pt" }

                    // Ordena: vozes que NÃO precisam de rede aparecem primeiro
                    val sortedVoices = ptVoices.sortedBy { if(it.isNetworkConnectionRequired) 1 else 0 }

                    if (sortedVoices.isNotEmpty()) {
                        availableVoices = sortedVoices
                        // Se a voz salva for válida, usa ela. Senão, pega a primeira (que é offline)
                        if (currentVoiceIndex < sortedVoices.size) {
                            tts?.voice = sortedVoices[currentVoiceIndex]
                            val tipo = if(sortedVoices[currentVoiceIndex].isNetworkConnectionRequired) " (Online)" else " (Offline)"
                            currentVoiceName = "Voz ${currentVoiceIndex + 1}$tipo"
                        }
                    }
                } catch (e: Exception) {
                    // Falha silenciosa, mantém padrão
                }

                // 3. Configura listener para animar o ícone
                tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) { isSpeaking = true }
                    override fun onDone(utteranceId: String?) { isSpeaking = false }
                    @Deprecated("Deprecated in Java", ReplaceWith("isSpeaking = false"))
                    override fun onError(utteranceId: String?) { isSpeaking = false }
                    override fun onStop(utteranceId: String?, interrupted: Boolean) { isSpeaking = false }
                })
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    // Função otimizada para falar sem delay
    fun falarTexto(texto: String, forcar: Boolean = false) {
        if (isTtsEnabled || forcar) {
            tts?.setSpeechRate(speechRate)
            // QUEUE_FLUSH limpa a fila anterior e fala IMEDIATAMENTE
            tts?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "calculo_tts")
        }
    }

    @SuppressLint("DefaultLocale")
    fun formatarParaFala(valor: Double): String {
        if (valor.isNaN()) return "erro matemático"
        if (valor.isInfinite()) return "infinito"
        val inteiro = valor.toInt()
        // Troca ponto por vírgula na fala para soar natural em PT-BR
        return if (valor == inteiro.toDouble()) "$inteiro" else String.format("%.2f", valor).replace(".", " vírgula ")
    }

    // Debounce reduzido para leitura rápida
    LaunchedEffect(explicacaoTexto, isTtsEnabled) {
        if (isTtsEnabled && explicacaoTexto.isNotEmpty() &&
            !explicacaoTexto.startsWith("Olá") && !explicacaoTexto.startsWith("Tudo")) {
            tts?.stop()
            isSpeaking = false
            delay(600) // Pequeno delay natural para não atropelar a digitação
            falarTexto(explicacaoTexto)
        }
    }

    // Monitora mudança de voz no painel
    LaunchedEffect(currentVoiceIndex, speechRate) {
        if (availableVoices.isNotEmpty() && currentVoiceIndex < availableVoices.size) {
            try {
                val selectedVoice = availableVoices[currentVoiceIndex]
                tts?.voice = selectedVoice
                val tipo = if(selectedVoice.isNetworkConnectionRequired) " (Online)" else " (Offline)"
                currentVoiceName = "Voz ${currentVoiceIndex + 1}$tipo"
            } catch (e: Exception) { }
        }
        tts?.setSpeechRate(speechRate)
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val symbols = DecimalFormatSymbols(Locale.US)
    val formatador = DecimalFormat("#,###.########", symbols)

    fun formatarVisor(valor: Double): String {
        return try {
            if (valor.isNaN()) "Erro"
            else if (valor.isInfinite()) "Infinito"
            else if (valor == 0.0) "0"
            else if (valor % 1 == 0.0) formatador.format(valor).replace(",", " ")
            else formatador.format(valor)
        } catch (e: Exception) { "Erro" }
    }

    // --- LÓGICA "PROFESSOR" DETALHADA ---
    fun recalcular() {
        if (num1.isEmpty()) {
            resultadoPreview = ""
            explicacaoTexto = "Aguardando você inserir os dados."
            return
        }
        val v1 = num1.toDoubleOrNull() ?: 0.0

        if (operador.isEmpty()) {
            resultadoPreview = formatarVisor(v1)
            explicacaoTexto = "Certo, o primeiro número é ${formatarParaFala(v1)}. Qual operação faremos agora?"
            return
        }

        val v2 = num2.toDoubleOrNull() ?: 0.0
        var valorFinal = 0.0

        if (num2IsPorcentagem) {
            val valorDaPorcentagem = v1 * (v2 / 100.0)
            when (operador) {
                "+" -> {
                    valorFinal = v1 + valorDaPorcentagem
                    explicacaoTexto = "Vamos analisar: Primeiro, calculamos ${formatarParaFala(v2)}% de ${formatarParaFala(v1)}, que resulta em ${formatarParaFala(valorDaPorcentagem)}. " +
                            "Como é uma soma, adicionamos isso ao valor inicial. Portanto, ${formatarParaFala(v1)} mais ${formatarParaFala(valorDaPorcentagem)} resulta em ${formatarParaFala(valorFinal)}."
                }
                "-" -> {
                    valorFinal = v1 - valorDaPorcentagem
                    explicacaoTexto = "Preste atenção no desconto: ${formatarParaFala(v2)}% de ${formatarParaFala(v1)} equivale a ${formatarParaFala(valorDaPorcentagem)}. " +
                            "Subtraindo esse valor do original, ficamos com ${formatarParaFala(v1)} menos ${formatarParaFala(valorDaPorcentagem)}, que dá ${formatarParaFala(valorFinal)}."
                }
                "×" -> {
                    valorFinal = v1 * (v2 / 100.0)
                    explicacaoTexto = "Multiplicação percentual: Estamos buscando quanto vale ${formatarParaFala(v2)}% do total de ${formatarParaFala(v1)}. " +
                            "O cálculo direto nos mostra que essa fatia corresponde a ${formatarParaFala(valorFinal)}."
                }
                "÷" -> {
                    if (v2 == 0.0) { valorFinal = 0.0; explicacaoTexto = "Não é possível dividir por zero porcento, pois isso gera uma indefinição matemática." }
                    else {
                        valorFinal = v1 / (v2 / 100.0)
                        explicacaoTexto = "Cálculo reverso: Se ${formatarParaFala(v1)} corresponde a apenas ${formatarParaFala(v2)}% de um total maior, " +
                                "então para descobrir esse total (100%), dividimos um pelo outro. O valor integral original seria ${formatarParaFala(valorFinal)}."
                    }
                }
            }
        } else {
            when (operador) {
                "+" -> {
                    valorFinal = v1 + v2
                    explicacaoTexto = "Operação de Adição: Juntando a quantia de ${formatarParaFala(v1)} com mais ${formatarParaFala(v2)}, " +
                            "acumulamos um total de ${formatarParaFala(valorFinal)}."
                }
                "-" -> {
                    valorFinal = v1 - v2
                    explicacaoTexto = "Operação de Subtração: Você começou com ${formatarParaFala(v1)}. Ao retirar ${formatarParaFala(v2)}, " +
                            "a diferença que resta é ${formatarParaFala(valorFinal)}."
                }
                "×" -> {
                    valorFinal = v1 * v2
                    explicacaoTexto = "Operação de Multiplicação: Se repetirmos o número ${formatarParaFala(v1)}, ${formatarParaFala(v2)} vezes, " +
                            "o produto final será ${formatarParaFala(valorFinal)}."
                }
                "÷" -> {
                    if (v2 == 0.0) {
                        resultadoPreview = "Erro"
                        explicacaoTexto = "Regra Fundamental: Na matemática, a divisão por zero é indefinida. Tente dividir por outro número."
                        return
                    }
                    valorFinal = v1 / v2
                    explicacaoTexto = "Operação de Divisão: Distribuindo ${formatarParaFala(v1)} em ${formatarParaFala(v2)} partes iguais, " +
                            "cada parte ficará com ${formatarParaFala(valorFinal)}."
                }
            }
        }
        resultadoPreview = formatarVisor(valorFinal)
    }

    LaunchedEffect(num1, operador, num2, num2IsPorcentagem) { recalcular() }

    fun onInput(botao: String) {
        when (botao) {
            "AC" -> {
                num1 = ""; operador = ""; num2 = ""; num2IsPorcentagem = false
                tts?.stop(); isSpeaking = false
                explicacaoTexto = "A memória foi limpa. Tudo pronto para um novo cálculo."
                falarTexto("Memória limpa.")
            }
            "⌫" -> {
                when {
                    num2IsPorcentagem -> num2IsPorcentagem = false
                    num2.isNotEmpty() -> num2 = num2.dropLast(1)
                    operador.isNotEmpty() -> operador = ""
                    num1.isNotEmpty() -> num1 = num1.dropLast(1)
                }
            }
            "%" -> { if (operador.isNotEmpty()) num2IsPorcentagem = !num2IsPorcentagem }
            "+", "-", "×", "÷" -> {
                if (num1.isNotEmpty()) {
                    if (num2.isNotEmpty()) {
                        num1 = resultadoPreview.replace(" ", "").replace(",", "")
                        num2 = ""; num2IsPorcentagem = false
                    }
                    operador = botao
                    if (num1 == "-" && botao != "-") num1 = "0"
                } else if (botao == "-") num1 = "-"
            }
            "=" -> {
                if (num2.isNotEmpty()) {
                    falarTexto("O resultado final é ${formatarVisor(resultadoPreview.replace(" ", "").toDoubleOrNull() ?: 0.0)}. $explicacaoTexto", true)
                    num1 = resultadoPreview.replace(" ", "").replace(",", "")
                    operador = ""; num2 = ""; num2IsPorcentagem = false
                }
            }
            "." -> {
                if (operador.isEmpty()) {
                    if (!num1.contains(".")) num1 += if (num1.isEmpty() || num1 == "-") "0." else "."
                } else {
                    if (!num2.contains(".")) num2 += if (num2.isEmpty() || num2 == "-") "0." else "."
                }
            }
            "±" -> {
                if (operador.isEmpty()) {
                    num1 = if (num1.startsWith("-")) num1.substring(1) else "-$num1"
                } else {
                    num2 = if (num2.startsWith("-")) num2.substring(1) else "-$num2"
                }
            }
            else -> {
                if (operador.isEmpty()) { if (num1.length < 15) num1 += botao }
                else { if (num2.length < 15) num2 += botao }
            }
        }
    }

    // --- UI PRINCIPAL ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CalcColors.Background)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (isLandscape) {
                // --- PAISAGEM ---
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .weight(1.4f)
                            .fillMaxHeight()
                            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 8.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center
                    ) {
                        VisorCalculadora(
                            num1, operador, num2, num2IsPorcentagem, resultadoPreview, explicacaoTexto,
                            isLandscape = true, isTtsEnabled, isSpeaking, showTtsControls,
                            onToggleControls = { showTtsControls = !showTtsControls },
                            onToggleTts = { isTtsEnabled = !isTtsEnabled; if(isTtsEnabled) falarTexto("Modo de voz ativado.") else tts?.stop() }
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(start = 8.dp, top = 16.dp, bottom = 16.dp, end = 16.dp)
                            .verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center
                    ) {
                        TecladoCalculadora(::onInput, num1, operador, num2, isLandscape = true)
                    }
                }
            } else {
                // --- RETRATO ---
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Box(modifier = Modifier.weight(0.8f)) { // Visor ocupa um pouco menos agora
                        VisorCalculadora(
                            num1, operador, num2, num2IsPorcentagem, resultadoPreview, explicacaoTexto,
                            isLandscape = false, isTtsEnabled, isSpeaking, showTtsControls,
                            onToggleControls = { showTtsControls = !showTtsControls },
                            onToggleTts = { isTtsEnabled = !isTtsEnabled; if(isTtsEnabled) falarTexto("Modo de voz ativado.") else tts?.stop() }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.wrapContentHeight()) {
                        TecladoCalculadora(::onInput, num1, operador, num2, isLandscape = false)
                    }
                }
            }
        }

        // --- OVERLAY DO CONTROLE DE VOZ ---
        AnimatedVisibility(
            visible = showTtsControls,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            ControleDeVozPanel(
                isVisible = showTtsControls,
                isTtsEnabled = isTtsEnabled,
                isSpeaking = isSpeaking,
                speechRate = speechRate,
                currentVoiceName = currentVoiceName,
                onRateChange = { speechRate = it },
                onPlayPause = {
                    if (isSpeaking) tts?.stop()
                    else falarTexto(explicacaoTexto, true)
                },
                onNextVoice = {
                    if (availableVoices.isNotEmpty()) {
                        currentVoiceIndex = (currentVoiceIndex + 1) % availableVoices.size
                        falarTexto("Testando nova voz.")
                    }
                },
                onClose = { showTtsControls = false },
                onEnableTts = { isTtsEnabled = true; falarTexto("Voz ativada.") },
                onDisableTts = {
                    isTtsEnabled = false
                    tts?.stop()
                    isSpeaking = false
                }
            )
        }
    }
}

// --- COMPONENTES VISUAIS ---

@SuppressLint("DefaultLocale")
@Composable
fun ControleDeVozPanel(
    isVisible: Boolean,
    isTtsEnabled: Boolean,
    isSpeaking: Boolean,
    speechRate: Float,
    currentVoiceName: String,
    onRateChange: (Float) -> Unit,
    onPlayPause: () -> Unit,
    onNextVoice: () -> Unit,
    onClose: () -> Unit,
    onEnableTts: () -> Unit,
    onDisableTts: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = CalcColors.ControlPanelBg),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Configurações de Voz", color = Color.White, fontWeight = FontWeight.Bold)
                IconButton(onClick = onClose) {
                    Icon(Icons.Rounded.Close, null, tint = Color.Gray)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha = 0.3f))

            if (!isTtsEnabled) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("O som está desativado.", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onEnableTts,
                        colors = ButtonDefaults.buttonColors(containerColor = CalcColors.AccentGreenStart)
                    ) {
                        Text("ATIVAR SOM AGORA", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play/Pause
                    ControlButton(
                        icon = if (isSpeaking) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        label = if (isSpeaking) "Pausar" else "Ouvir",
                        onClick = onPlayPause
                    )

                    // Velocidade
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Velocidade: ${String.format("%.1fx", speechRate)}", color = Color.LightGray, fontSize = 10.sp)
                        Row {
                            IconButton(onClick = { if (speechRate > 0.5f) onRateChange(speechRate - 0.25f) }) {
                                Icon(Icons.Rounded.Remove, null, tint = Color.White)
                            }
                            IconButton(onClick = { if (speechRate < 2.0f) onRateChange(speechRate + 0.25f) }) {
                                Icon(Icons.Rounded.Add, null, tint = Color.White)
                            }
                        }
                    }

                    // Trocar Voz
                    ControlButton(
                        icon = Icons.Rounded.RecordVoiceOver,
                        label = currentVoiceName.take(10), // Limita tamanho do nome
                        onClick = onNextVoice
                    )
                }

                // --- BOTÃO DE DESLIGAR A VOZ ---
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray.copy(alpha = 0.2f))

                Button(
                    onClick = onDisableTts,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCF6679)) // Vermelho suave
                ) {
                    Icon(Icons.Rounded.VolumeOff, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Desativar Voz", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ControlButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(CalcColors.ButtonActionBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.White)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = Color.LightGray, fontSize = 10.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun VisorCalculadora(
    num1: String, operador: String, num2: String, isPorc: Boolean,
    resultado: String, explicacao: String,
    isLandscape: Boolean, isTtsEnabled: Boolean, isSpeaking: Boolean, showControls: Boolean,
    onToggleControls: () -> Unit,
    onToggleTts: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSpeaking) 1.2f else 1.0f,
        animationSpec = infiniteRepeatable(animation = tween(500), repeatMode = RepeatMode.Reverse),
        label = "som"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!isLandscape) Modifier.fillMaxHeight() else Modifier.wrapContentHeight())
            .clip(RoundedCornerShape(28.dp))
            .background(CalcColors.VisorBackground)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(28.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Cabeçalho
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "EXPLICAÇÃO DO PROFESSOR",
                    color = CalcColors.AccentGreenStart,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = explicacao,
                    color = CalcColors.TextExplanation,
                    fontSize = if (isLandscape) 13.sp else 14.sp,
                    lineHeight = 20.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }

            // Botão de Som
            Box(modifier = Modifier.padding(start = 12.dp)) {
                Box(
                    modifier = Modifier
                        .scale(if (isSpeaking) scale else 1f)
                        .clip(CircleShape)
                        .background(if (isTtsEnabled) CalcColors.AccentGreenStart.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { onToggleControls() }
                        .padding(10.dp)
                ) {
                    Icon(
                        imageVector = if (isTtsEnabled) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
                        contentDescription = "Voz",
                        tint = if (isTtsEnabled) CalcColors.AccentGreenStart else CalcColors.TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        if (isLandscape) Spacer(modifier = Modifier.height(12.dp))

        // Área Numérica
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            val textoPorcentagem = if (isPorc) "%" else ""
            val textoEquacao = if (operador.isEmpty()) num1 else "$num1 $operador $num2$textoPorcentagem"

            AutoSizeText(
                text = textoEquacao.ifEmpty { "0" },
                color = if (resultado.isNotEmpty() && operador.isNotEmpty()) CalcColors.TextSecondary else CalcColors.TextPrimary,
                maxFontSize = if (isLandscape) 40.sp else 55.sp,
                minFontSize = 24.sp
            )

            if (operador.isNotEmpty() && (num2.isNotEmpty() || isPorc)) {
                AutoSizeText(
                    text = "= $resultado",
                    color = CalcColors.TextPrimary,
                    maxFontSize = if (isLandscape) 55.sp else 70.sp,
                    minFontSize = 30.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AutoSizeText(
    text: String, color: Color, maxFontSize: TextUnit, minFontSize: TextUnit,
    fontWeight: FontWeight = FontWeight.Light
) {
    val tamanho = text.length
    val fontSize = when {
        tamanho > 20 -> minFontSize
        tamanho > 15 -> maxFontSize * 0.6f
        tamanho > 10 -> maxFontSize * 0.8f
        else -> maxFontSize
    }

    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        fontFamily = FontFamily.Monospace,
        textAlign = TextAlign.End,
        lineHeight = fontSize,
        softWrap = false,
        maxLines = 1
    )
}

@Composable
fun TecladoCalculadora(
    onInput: (String) -> Unit, n1: String, op: String, n2: String, isLandscape: Boolean
) {
    val botoes = listOf(
        listOf("AC", "⌫", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("±", "0", ".", "=")
    )
    val haptic = LocalHapticFeedback.current
    // AJUSTE DO TAMANHO DO TECLADO: Reduzi o espaçamento para compactar
    val espacamento = if (isLandscape) 8.dp else 10.dp

    Column(verticalArrangement = Arrangement.spacedBy(espacamento)) {
        botoes.forEach { linha ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(espacamento)
            ) {
                linha.forEach { botao ->
                    val isDestaque = botao == "="
                    val isOperador = "+-×÷".contains(botao)
                    val isAcao = "AC⌫%±".contains(botao)
                    val isSelecionado = op == botao && n2.isEmpty()

                    val (bgBrush, contentColor) = when {
                        isDestaque -> Pair(Brush.verticalGradient(listOf(CalcColors.AccentGreenStart, CalcColors.AccentGreenEnd)), Color.White)
                        isSelecionado -> Pair(SolidColor(Color.White), Color.Black)
                        isOperador -> Pair(Brush.verticalGradient(listOf(CalcColors.ButtonOpBgStart, CalcColors.ButtonOpBgEnd)), Color.White)
                        isAcao -> Pair(SolidColor(CalcColors.ButtonActionBg), Color.White)
                        else -> Pair(SolidColor(CalcColors.ButtonNumBg), Color.White)
                    }

                    BotaoCalculadora(
                        texto = botao,
                        backgroundBrush = bgBrush,
                        corTexto = contentColor,
                        peso = 1f,
                        isLandscape = isLandscape,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onInput(botao)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.BotaoCalculadora(
    texto: String, backgroundBrush: Brush, corTexto: Color, peso: Float,
    isLandscape: Boolean, onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.90f else 1f, label = "scale")
    val shape = if (isLandscape) RoundedCornerShape(16.dp) else CircleShape

    val modifierBase = Modifier
        .weight(peso)
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clip(shape)
        .background(backgroundBrush)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = { isPressed = true; tryAwaitRelease(); isPressed = false; onClick() }
            )
        }

    // AJUSTE DO TAMANHO DO TECLADO:
    // Em modo retrato, removemos o aspectRatio(1f) que forçava quadrados perfeitos e gigantes.
    // Agora usamos um valor fixo de altura ou aspectRatio(1.25f) para botões mais "achatados".
    val modifierFinal = if (isLandscape) {
        modifierBase.height(50.dp)
    } else {
        modifierBase.aspectRatio(1.3f) // Maior que 1.0f deixa o botão mais "gordo" horizontalmente e menor verticalmente
    }

    Box(modifier = modifierFinal, contentAlignment = Alignment.Center) {
        Text(
            text = texto,
            color = corTexto,
            fontSize = if (isLandscape) 20.sp else 26.sp, // Fonte levemente reduzida
            fontWeight = FontWeight.SemiBold
        )
    }
}
