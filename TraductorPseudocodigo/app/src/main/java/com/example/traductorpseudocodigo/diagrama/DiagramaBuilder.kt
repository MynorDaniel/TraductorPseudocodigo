package com.example.traductorpseudocodigo.diagrama

import com.example.traductorpseudocodigo.javafiles.*

class DiagramaBuilder {

    private var contadorNodos = 0
    private var contadorSI = 0
    private var contadorMientras = 0
    private var contadorBloque = 0

    private val separacionVertical = 90f

    fun build(programa: Programa): DiagramaDeFlujo {
        contadorNodos = 0
        contadorSI = 0
        contadorMientras = 0
        contadorBloque = 0

        val diagrama = DiagramaDeFlujo()
        val nodosLineales = mutableListOf<Nodo>()

        agregarInstrucciones(
            programa.instrucciones,
            programa.configuraciones,
            diagrama,
            nodosLineales
        )

        // Conectar secuencialmente
        for (i in 0 until nodosLineales.size - 1) {
            diagrama.conectar(nodosLineales[i], nodosLineales[i + 1], "DEFAULT")
        }

        return diagrama
    }

    private fun agregarInstrucciones(
        instrucciones: List<Instruccion>,
        configuraciones: List<Configuracion>,
        diagrama: DiagramaDeFlujo,
        nodos: MutableList<Nodo>
    ) {
        for (instr in instrucciones) {

            when (instr.tipo) {

                TipoInstruccion.SI -> {
                    contadorSI++
                    val style = resolverEstilo(configuraciones, "SI", contadorSI)

                    val nodo = crearNodo(
                        "SI (${instr.argumento})",
                        Figura.ROMBO,
                        style
                    )

                    diagrama.agregarNodo(nodo)
                    nodos.add(nodo)

                    instr.bloque?.let {
                        agregarInstrucciones(it, configuraciones, diagrama, nodos)
                    }
                }

                TipoInstruccion.MIENTRAS -> {
                    contadorMientras++
                    val style = resolverEstilo(configuraciones, "MIENTRAS", contadorMientras)

                    val nodo = crearNodo(
                        "MIENTRAS (${instr.argumento})",
                        Figura.ROMBO,
                        style
                    )

                    diagrama.agregarNodo(nodo)
                    nodos.add(nodo)

                    instr.bloque?.let {
                        agregarInstrucciones(it, configuraciones, diagrama, nodos)
                    }
                }

                TipoInstruccion.ASIGNACION,
                TipoInstruccion.MOSTRAR,
                TipoInstruccion.LEER -> {

                    contadorBloque++
                    val style = resolverEstilo(configuraciones, "BLOQUE", contadorBloque)

                    val texto = construirTexto(instr)

                    val figura = when (instr.tipo) {
                        TipoInstruccion.ASIGNACION -> Figura.RECTANGULO
                        TipoInstruccion.MOSTRAR,
                        TipoInstruccion.LEER -> Figura.PARALELOGRAMO
                        else -> Figura.RECTANGULO
                    }

                    val nodo = crearNodo(texto, figura, style)

                    diagrama.agregarNodo(nodo)
                    nodos.add(nodo)
                }

                else -> {}
            }
        }
    }

    private fun construirTexto(instr: Instruccion): String {
        return when (instr.tipo) {
            TipoInstruccion.ASIGNACION -> {
                if (instr.expresion != null)
                    "ASIGNACION ${instr.argumento} = ${instr.expresion}"
                else
                    "ASIGNACION ${instr.argumento}"
            }
            TipoInstruccion.MOSTRAR -> "MOSTRAR ${instr.argumento}"
            TipoInstruccion.LEER -> "LEER ${instr.argumento}"
            else -> instr.argumento ?: ""
        }
    }

    private fun resolverEstilo(
        configuraciones: List<Configuracion>,
        categoria: String,
        indiceContexto: Int
    ): NodoStyle {

        var color = "DEFAULT"
        var colorTexto = "DEFAULT"
        var figura = Figura.RECTANGULO
        var letra = Letra.ARIAL
        var tamLetra = 14f

        for (config in configuraciones) {

            val tipo = config.tipo ?: continue
            val indice = config.indice?.toIntOrNull() ?: continue

            if (indice != indiceContexto) continue

            val coincideCategoria = tipo.endsWith("_$categoria")
            val esDefault = tipo == "DEFAULT"

            if (!coincideCategoria && !esDefault) continue

            val valor = config.valor?.toString() ?: continue

            when {
                tipo.startsWith("COLOR_TEXTO") -> colorTexto = valor
                tipo.startsWith("COLOR_") -> color = valor
                tipo.startsWith("FIGURA_") -> {
                    figura = try { Figura.valueOf(valor) } catch (e: Exception) { figura }
                }
                tipo.startsWith("LETRA_SIZE_") -> {
                    tamLetra = evaluarExpresion(valor) ?: tamLetra
                }
                tipo.startsWith("LETRA_") -> {
                    letra = try { Letra.valueOf(valor) } catch (e: Exception) { letra }
                }
            }
        }

        return NodoStyle(color, colorTexto, figura, letra, tamLetra)
    }

    private fun evaluarExpresion(expr: String): Float? {
        return try {
            val tokens = transformarExpresion(expr.replace(",", "."))
            if (tokens.isEmpty()) return null

            val output = mutableListOf<String>()
            val operators = ArrayDeque<String>()

            for (token in tokens) {
                when {
                    token.isNumber() -> output.add(token)

                    token == "(" -> operators.addLast(token)

                    token == ")" -> {
                        while (operators.isNotEmpty() && operators.last() != "(") {
                            output.add(operators.removeLast())
                        }
                        if (operators.isEmpty()) return null
                        operators.removeLast()
                    }

                    token.isOperator() -> {
                        while (
                            operators.isNotEmpty() &&
                            precedence(operators.last()) >= precedence(token)
                        ) {
                            output.add(operators.removeLast())
                        }
                        operators.addLast(token)
                    }

                    else -> return null
                }
            }

            while (operators.isNotEmpty()) {
                output.add(operators.removeLast())
            }

            // Evaluar notación postfija
            val stack = ArrayDeque<Float>()
            for (token in output) {
                when {
                    token.isNumber() -> stack.addLast(token.toFloat())

                    token.isOperator() -> {
                        val right = stack.removeLastOrNull() ?: return null
                        val left = stack.removeLastOrNull() ?: return null
                        val result = when (token) {
                            "+" -> left + right
                            "-" -> left - right
                            "*" -> left * right
                            "/" -> left / right
                            else -> return null
                        }
                        stack.addLast(result)
                    }
                }
            }

            stack.singleOrNull()

        } catch (e: Exception) {
            null
        }
    }

    private fun transformarExpresion(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0

        while (i < expr.length) {
            val ch = expr[i]

            if (ch.isWhitespace()) {
                i++
                continue
            }

            if (ch.isDigit() || ch == '.') {
                val start = i
                i++
                while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                    i++
                }
                tokens.add(expr.substring(start, i))
                continue
            }

            if (ch in listOf('+', '-', '*', '/', '(', ')')) {
                tokens.add(ch.toString())
                i++
                continue
            }

            return emptyList()
        }

        return tokens
    }

    private fun String.isOperator(): Boolean =
        this == "+" || this == "-" || this == "*" || this == "/"

    private fun String.isNumber(): Boolean =
        this.toFloatOrNull() != null

    private fun precedence(op: String): Int =
        when (op) {
            "*", "/" -> 2
            "+", "-" -> 1
            else -> 0
        }

    private fun crearNodo(
        texto: String,
        figuraBase: Figura,
        style: NodoStyle
    ): Nodo {

        contadorNodos++

        return Nodo(
            indice = contadorNodos,
            texto = texto,
            x = 0f,
            y = contadorNodos * separacionVertical,
            ancho = 240f,
            alto = 70f,
            color = style.color,
            colorTexto = style.colorTexto,
            figura = style.figura ?: figuraBase,
            letra = style.letra,
            tamLetra = style.tamLetra
        )
    }

    private data class NodoStyle(
        val color: String,
        val colorTexto: String,
        val figura: Figura?,
        val letra: Letra,
        val tamLetra: Float
    )
}