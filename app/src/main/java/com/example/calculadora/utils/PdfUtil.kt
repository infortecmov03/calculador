package com.example.calculadora.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.calculadora.data.ItemDaLista
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object PdfUtil {

    // --- EXPORTAR PDF PROFISSIONAL ---
    @SuppressLint("NewApi")
    fun gerarPdf(
        context: Context,
        listaDeItens: List<ItemDaLista>,
        nomeLista: String,
        tituloRelatorio: String,
        autor: String,
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // Variáveis de Layout
        val margemEsquerda = 40f
        val larguraPagina = 595f
        val larguraUtil = larguraPagina - (margemEsquerda * 2)
        var yAtual = 60f

        // --- 1. CABEÇALHO ---
        // Título
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 24f
        paint.color = Color.BLACK
        canvas.drawText(tituloRelatorio, margemEsquerda, yAtual, paint)
        yAtual += 30f

        // Subtítulo
        paint.textSize = 14f
        paint.color = Color.DKGRAY
        canvas.drawText("Projeto/Lista: $nomeLista", margemEsquerda, yAtual, paint)
        yAtual += 40f

        // --- 2. CABEÇALHO DA TABELA ---
        paint.color = Color.rgb(41, 128, 185) // Azul Profissional
        canvas.drawRect(
            margemEsquerda,
            yAtual - 20f,
            margemEsquerda + larguraUtil,
            yAtual + 10f,
            paint
        )

        paint.color = Color.WHITE
        paint.textSize = 11f
        paint.typeface = Typeface.DEFAULT_BOLD

        // Colunas: Posição | Nome | Categoria | Valor
        canvas.drawText("POS", margemEsquerda + 5, yAtual, paint)
        canvas.drawText("ITEM / DESCRIÇÃO", margemEsquerda + 50, yAtual, paint)
        canvas.drawText("CATEGORIA", margemEsquerda + 320, yAtual, paint)
        canvas.drawText("VALOR (MZN)", margemEsquerda + 430, yAtual, paint)
        yAtual += 30f

        // --- 3. LISTAGEM DOS ITENS ---
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.BLACK
        paint.textSize = 12f

        listaDeItens.forEachIndexed { index, item ->
            // Zebra Striping (Linhas alternadas)
            if (index % 2 == 0) {
                paint.color = Color.rgb(245, 245, 245)
                canvas.drawRect(
                    margemEsquerda,
                    yAtual - 20f,
                    margemEsquerda + larguraUtil,
                    yAtual + 10f,
                    paint
                )
            }

            paint.color = Color.BLACK

            // Posição
            canvas.drawText("${index + 1}º", margemEsquerda + 5, yAtual, paint)

            // Nome (Limitando tamanho para não sobrepor)
            val nomeLimitado =
                if (item.nome.length > 35) item.nome.substring(0, 32) + "..." else item.nome
            canvas.drawText(nomeLimitado, margemEsquerda + 50, yAtual, paint)

            // Categoria (Tratamento seguro de nulos)
            val textoCategoria = item.categoria ?: ""
            val catLimitada = if (textoCategoria.length > 15) {
                textoCategoria.substring(0, 12) + "..."
            } else {
                textoCategoria
            }
            canvas.drawText(catLimitada, margemEsquerda + 320, yAtual, paint)

            // Valor (Alinhado e destacado se for Top 3)
            if (index < 3) paint.typeface = Typeface.DEFAULT_BOLD else paint.typeface =
                Typeface.DEFAULT
            canvas.drawText("MZN ${"%.2f".format(item.valor)}", margemEsquerda + 430, yAtual, paint)

            // Restaura fonte normal
            paint.typeface = Typeface.DEFAULT

            yAtual += 25f

            // Quebra de página simples
            if (yAtual > 780f) {
                canvas.drawText("... continua na próxima página ...", margemEsquerda, yAtual, paint)
                return@forEachIndexed
            }
        }

        // --- 4. BARRA DE TOTAIS (Verde) ---
        val total = listaDeItens.sumOf { it.valor }
        val menorValor = listaDeItens.minOfOrNull { it.valor } ?: 0.0
        val dezPorCento = total * 0.10

        yAtual += 20f

        // Fundo Verde
        paint.color = Color.rgb(39, 174, 96)
        canvas.drawRect(
            margemEsquerda,
            yAtual - 25f,
            margemEsquerda + larguraUtil,
            yAtual + 25f,
            paint
        )

        // Textos da Barra Verde
        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT_BOLD

        // Coluna 1: Total
        canvas.drawText("VALOR TOTAL", margemEsquerda + 10, yAtual - 5, paint)
        paint.textSize = 14f
        canvas.drawText("MZN ${"%.2f".format(total)}", margemEsquerda + 10, yAtual + 15, paint)

        // Coluna 2: Menor Valor
        paint.textSize = 10f
        canvas.drawText("MENOR VALOR", margemEsquerda + 200, yAtual - 5, paint)
        paint.textSize = 14f
        canvas.drawText(
            "MZN ${"%.2f".format(menorValor)}",
            margemEsquerda + 200,
            yAtual + 15,
            paint
        )

        // Coluna 3: 10%
        paint.textSize = 10f
        canvas.drawText("10% (MARGEM)", margemEsquerda + 380, yAtual - 5, paint)
        paint.textSize = 14f
        canvas.drawText(
            "MZN ${"%.2f".format(dezPorCento)}",
            margemEsquerda + 380,
            yAtual + 15,
            paint
        )


        // --- 5. RODAPÉ / ASSINATURA ---
        yAtual += 80f
        paint.color = Color.BLACK
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)

        val dataHora =
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm"))

        // CORREÇÃO DO AVISO 'ifBlank':
        val textoAssinatura =
            "Emitido por: ${autor.ifBlank { "___________________" }} aos $dataHora"

        // Linha da assinatura
        canvas.drawLine(margemEsquerda, yAtual, margemEsquerda + 300, yAtual, paint)
        yAtual += 15f

        // Texto da assinatura
        canvas.drawText(textoAssinatura, margemEsquerda, yAtual, paint)

        pdfDocument.finishPage(page)

        // --- SALVAR E COMPARTILHAR ---
        val nomeArquivo =
            "Relatorio_${nomeLista.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), nomeArquivo)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            Toast.makeText(context, "PDF criado!", Toast.LENGTH_SHORT).show()
            compartilharArquivo(context, file, "application/pdf")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erro PDF: ${e.message}", Toast.LENGTH_LONG).show()
            pdfDocument.close()
        }
    }

    // --- EXPORTAR EXCEL ESTILIZADO (CSV) ---
    @SuppressLint("NewApi")
    fun gerarExcel(context: Context, listaDeItens: List<ItemDaLista>, nomeLista: String) {
        val nomeArquivo =
            "Planilha_${nomeLista.replace(" ", "_")}_${System.currentTimeMillis()}.csv"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), nomeArquivo)

        try {
            val writer = FileWriter(file)
            writer.append("\uFEFF") // BOM para acentuação correta

            // Cabeçalho
            writer.append("RELATÓRIO DE CUSTOS;;;;\n")
            writer.append("LISTA:;${nomeLista.replace(";", ",")};;;\n\n")

            // Colunas
            writer.append("Ranking;Item;Categoria;Valor (MZN)\n")

            // Dados
            listaDeItens.forEachIndexed { index, item ->
                writer.append("${index + 1};")
                writer.append("${item.nome.replace(";", ",")};")
                writer.append("${(item.categoria ?: "").replace(";", ",")};")
                writer.append("${item.valor.toString().replace(".", ",")}\n")
            }

            // Totais calculados
            val total = listaDeItens.sumOf { it.valor }
            val menor = listaDeItens.minOfOrNull { it.valor } ?: 0.0
            val dezPorcento = total * 0.10

            writer.append("\n")
            writer.append(";;VALOR TOTAL;${total.toString().replace(".", ",")}\n")
            writer.append(";;MENOR VALOR;${menor.toString().replace(".", ",")}\n")
            writer.append(";;10% MARGEM;${dezPorcento.toString().replace(".", ",")}\n")

            // Rodapé
            val dataHora =
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            writer.append("\nEmitido em:;$dataHora;;")

            writer.flush()
            writer.close()

            Toast.makeText(context, "Excel gerado!", Toast.LENGTH_SHORT).show()
            compartilharArquivo(context, file, "text/csv")

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erro Excel: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun compartilharArquivo(context: Context, file: File, mimeType: String) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Compartilhar via"))
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Erro ao compartilhar: Verifique permissões.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}