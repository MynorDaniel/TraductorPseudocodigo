package com.example.traductorpseudocodigo.diagrama

@Suppress("unused")
class DiagramaDeFlujo(
    val nodos: MutableList<Nodo> = mutableListOf(),
    val conexiones: MutableList<Conexion> = mutableListOf()
) {
    fun agregarNodo(nodo: Nodo) {
        nodos.add(nodo)
    }

    fun conectar(nodoInicial: Nodo, nodoFinal: Nodo, color: String) {
        val id = "c${conexiones.size + 1}"
        conexiones.add(Conexion(id = id, origen = nodoInicial, destino = nodoFinal, color = color))
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.appendLine("DiagramaDeFlujo")
        builder.appendLine("Nodos:")
        for (nodo in nodos) {
            builder.appendLine("- $nodo")
        }
        builder.appendLine("Conexiones:")
        for (conexion in conexiones) {
            builder.appendLine("- $conexion")
        }
        return builder.toString().trimEnd()
    }
}