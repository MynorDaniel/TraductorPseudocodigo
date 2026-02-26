package com.example.traductorpseudocodigo.reportes

import com.example.traductorpseudocodigo.javafiles.Instruccion
import com.example.traductorpseudocodigo.javafiles.TipoInstruccion


data class Reportes(
    val operadores: List<OperadorOcurrencia>,
    val estructuras: List<EstructuraControl>
)

data class OperadorOcurrencia(
    val operador: String,
    val linea: Int,
    val columna: Int,
    val ocurrencia: String
)

data class EstructuraControl(
    val objeto: String,
    val linea: Int,
    val condicion: String
)

fun construirReportes(instrucciones: List<Instruccion>): Reportes {
    val operadores = mutableListOf<OperadorOcurrencia>()
    val estructuras = mutableListOf<EstructuraControl>()
    val lineaCounter = LineaCounter()

    recolectar(instrucciones, lineaCounter, operadores, estructuras)

    return Reportes(
        operadores = operadores,
        estructuras = estructuras
    )
}

private fun recolectar(
    instrucciones: List<Instruccion>,
    lineaCounter: LineaCounter,
    operadores: MutableList<OperadorOcurrencia>,
    estructuras: MutableList<EstructuraControl>
) {
    for (inst in instrucciones) {
        val linea = lineaCounter.next()

        when (inst.tipo) {
            TipoInstruccion.SI -> {
                val condicion = inst.argumento ?: ""
                estructuras.add(EstructuraControl("SI", linea, condicion))
                scanOperadores(condicion, linea, operadores)
                inst.bloque?.let { recolectar(it, lineaCounter, operadores, estructuras) }
            }

            TipoInstruccion.MIENTRAS -> {
                val condicion = inst.argumento ?: ""
                estructuras.add(EstructuraControl("MIENTRAS", linea, condicion))
                scanOperadores(condicion, linea, operadores)
                inst.bloque?.let { recolectar(it, lineaCounter, operadores, estructuras) }
            }

            TipoInstruccion.ASIGNACION -> {
                inst.expresion?.let { scanOperadores(it, linea, operadores) }
            }

            else -> {
            }
        }
    }
}

private fun scanOperadores(
    texto: String,
    linea: Int,
    operadores: MutableList<OperadorOcurrencia>
) {
    for (i in texto.indices) {
        when (texto[i]) {
            '+' -> operadores.add(OperadorOcurrencia("Suma", linea, i + 1, texto.trim()))
            '-' -> operadores.add(OperadorOcurrencia("Resta", linea, i + 1, texto.trim()))
            '*' -> operadores.add(OperadorOcurrencia("Multiplicacion", linea, i + 1, texto.trim()))
            '/' -> operadores.add(OperadorOcurrencia("Division", linea, i + 1, texto.trim()))
        }
    }
}

private class LineaCounter {
    private var value = 0

    fun next(): Int {
        value += 1
        return value
    }
}

