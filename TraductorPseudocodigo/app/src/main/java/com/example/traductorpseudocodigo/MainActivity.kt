package com.example.traductorpseudocodigo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.traductorpseudocodigo.diagrama.*
import com.example.traductorpseudocodigo.javafiles.*
import com.example.traductorpseudocodigo.reportes.Reportes
import com.example.traductorpseudocodigo.reportes.construirReportes
import com.example.traductorpseudocodigo.ui.theme.TraductorPseudocodigoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.StringReader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TraductorPseudocodigoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ParserScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ParserScreen(modifier: Modifier = Modifier) {

    val scope = rememberCoroutineScope()

    var sourceText by rememberSaveable { mutableStateOf("") }
    var outputText by rememberSaveable { mutableStateOf("") }
    var isRunning by remember { mutableStateOf(false) }
    var diagrama by remember { mutableStateOf<DiagramaDeFlujo?>(null) }
    var reportes by remember { mutableStateOf<Reportes?>(null) }

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text("Analizador sintáctico de pseudocódigo")

        TextField(
            value = sourceText,
            onValueChange = { sourceText = it },
            modifier = Modifier.fillMaxWidth(),
            minLines = 6,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
        )

        Button(
            onClick = {
                if (sourceText.isBlank()) return@Button

                isRunning = true
                scope.launch(Dispatchers.Default) {

                    val result = runAnalysis(sourceText)

                    withContext(Dispatchers.Main) {
                        outputText = result.output
                        diagrama = result.diagrama
                        reportes = result.reportes
                        isRunning = false
                    }
                }
            },
            enabled = !isRunning
        ) {
            Text(if (isRunning) "Analizando..." else "Analizar")
        }

        Text("Salida:")
        Text(outputText)

        Spacer(modifier = Modifier.height(20.dp))

        diagrama?.let {
            DibujarDiagrama(it)
        }

        reportes?.let {
            ReportesView(it)
        }
    }
}

private data class AnalysisResult(
    val output: String,
    val diagrama: DiagramaDeFlujo?,
    val reportes: Reportes?
)

private fun runAnalysis(source: String): AnalysisResult {

    return try {

        val lexer = Lexer(StringReader(source))
        val parser = Parser(lexer)
        val symbol = parser.parse()

        val syntaxErrors = parser.syntaxErrors

        if (syntaxErrors.isEmpty()) {

            val programa = symbol.value as? Programa

            if (programa != null) {

                val dBuilder = DiagramaBuilder()
                val diagrama = dBuilder.build(programa)
                val reportes = construirReportes(programa.instrucciones)

                AnalysisResult("Análisis sintáctico correcto.", diagrama, reportes)

            } else {
                AnalysisResult("Programa nulo.", null, null)
            }

        } else {

            val errores = buildString {
                appendLine("Errores lexicos:")
                lexer.errores.forEach { appendLine(it) }
                appendLine("Errores sintácticos:")
                syntaxErrors.forEach { appendLine(it) }

            }

            AnalysisResult(errores, null, null)
        }

    } catch (ex: Exception) {
        AnalysisResult("Error: ${ex.message}", null, null)
    }
}

@Composable
fun DibujarDiagrama(diagrama: DiagramaDeFlujo) {

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(1500.dp)
            .background(Color.White)
    ) {

        diagrama.conexiones.forEach { conexion ->
            dibujarConexion(conexion)
        }

        diagrama.nodos.forEach { nodo ->
            dibujarNodo(nodo)
        }
    }
}

private fun DrawScope.dibujarNodo(nodo: Nodo) {

    var color = nodo.color.toComposeColor()
    val rectTopLeft = Offset(nodo.x, nodo.y)
    val size = Size(nodo.ancho, nodo.alto)

    when (nodo.figura) {

        Figura.RECTANGULO -> {
            drawRect(color, rectTopLeft, size)
        }

        Figura.CIRCULO -> {
            drawOval(color, rectTopLeft, size)
        }

        Figura.ROMBO -> {
            val path = Path().apply {
                moveTo(nodo.x + nodo.ancho / 2, nodo.y)
                lineTo(nodo.x + nodo.ancho, nodo.y + nodo.alto / 2)
                lineTo(nodo.x + nodo.ancho / 2, nodo.y + nodo.alto)
                lineTo(nodo.x, nodo.y + nodo.alto / 2)
                close()
            }
            drawPath(path, color)
        }

        else -> {}
    }

    drawIntoCanvas { canvas ->

        val nativeCanvas = canvas.nativeCanvas

        val paint = androidx.compose.ui.graphics.Paint().apply {
            color = nodo.colorTexto.toComposeColor()
        }
        val frameworkPaint = paint.asFrameworkPaint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = nodo.tamLetra
            isAntiAlias = true
        }

        nativeCanvas.drawText(
            nodo.texto,
            nodo.x + nodo.ancho / 2,
            nodo.y + nodo.alto / 2,
            frameworkPaint
        )
    }
}

private fun DrawScope.dibujarConexion(conexion: Conexion) {

    val origen = conexion.origen
    val destino = conexion.destino

    val start = Offset(
        origen.x + origen.ancho / 2,
        origen.y + origen.alto
    )

    val end = Offset(
        destino.x + destino.ancho / 2,
        destino.y
    )

    drawLine(
        color = Color.Black,
        start = start,
        end = end,
        strokeWidth = 4f
    )

    val arrowSize = 12f

    drawLine(Color.Black, end, Offset(end.x - arrowSize, end.y - arrowSize), 4f)
    drawLine(Color.Black, end, Offset(end.x + arrowSize, end.y - arrowSize), 4f)
}

@Composable
private fun ReportesView(reportes: Reportes) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Reporte de ocurrencias de operadores matemáticos")
        Text("Operador | Línea | Columna | Ocurrencia")
        reportes.operadores.forEach { op ->
            Text("${op.operador} | ${op.linea} | ${op.columna} | ${op.ocurrencia}")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Reporte de estructuras de control")
        Text("Objeto | Línea | Condición")
        reportes.estructuras.forEach { est ->
            Text("${est.objeto} | ${est.linea} | ${est.condicion}")
        }
    }
}

fun String.toComposeColor(): Color {
    return when {
        this == "DEFAULT" -> Color.LightGray
        this.startsWith("H") -> Color(android.graphics.Color.parseColor("#${this.drop(1)}"))
        else -> Color.Gray
    }
}