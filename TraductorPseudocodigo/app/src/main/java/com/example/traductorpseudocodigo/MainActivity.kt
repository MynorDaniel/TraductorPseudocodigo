package com.example.traductorpseudocodigo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.traductorpseudocodigo.javafiles.Configuracion
import com.example.traductorpseudocodigo.javafiles.Instruccion
import com.example.traductorpseudocodigo.javafiles.Lexer
import com.example.traductorpseudocodigo.javafiles.Parser
import com.example.traductorpseudocodigo.javafiles.Programa
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
    var outputText by rememberSaveable { mutableStateOf("Presiona 'Analizar' para ejecutar el parser.") }
    var isRunning by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Analizador sintáctico de pseudocódigo")
        TextField(
            value = sourceText,
            onValueChange = { sourceText = it },
            modifier = Modifier.fillMaxWidth(),
            minLines = 6,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = {
                if (sourceText.isBlank()) {
                    outputText = "Pega una entrada antes de analizar."
                    return@Button
                }
                isRunning = true
                scope.launch(Dispatchers.Default) {
                    val result = runAnalysis(sourceText)
                    withContext(Dispatchers.Main) {
                        outputText = result
                        isRunning = false
                    }
                }
            },
            enabled = !isRunning
        ) {
            Text(text = if (isRunning) "Analizando..." else "Analizar")
        }
        Text(text = "Salida:")
        Text(text = outputText, modifier = Modifier.fillMaxWidth())
    }
}

private fun runAnalysis(source: String): String {
    return try {
        val lexer = Lexer(StringReader(source))
        val parser = Parser(lexer)
        val symbol = parser.parse()

        val builder = StringBuilder()
        val erroresLexer = lexer.errores
        for (error in erroresLexer) {
            builder.appendLine(error)
        }

        val syntaxErrors = parser.syntaxErrors
        if (syntaxErrors.isEmpty()) {
            val programa = symbol.value as? Programa
            if (programa == null) {
                builder.appendLine("Análisis sintáctico finalizado correctamente, pero sin programa.")
            } else {
                val instrucciones: List<Instruccion> = programa.instrucciones
                val configuraciones: List<Configuracion> = programa.configuraciones

                builder.appendLine("Análisis sintáctico finalizado correctamente.")
                for (instruccion in instrucciones) {
                    builder.appendLine(instruccion.toString())
                }
                for (configuracion in configuraciones) {
                    builder.appendLine(configuracion.toString())
                }
            }
        } else {
            builder.appendLine("Errores sintácticos encontrados:")
            for (error in syntaxErrors) {
                builder.appendLine(error)
            }
        }

        if (builder.isEmpty()) "Sin salida del analizador." else builder.toString().trimEnd()
    } catch (ex: Exception) {
        "Fallo al ejecutar el analizador: ${ex.message}"
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TraductorPseudocodigoTheme {
        ParserScreen()
    }
}