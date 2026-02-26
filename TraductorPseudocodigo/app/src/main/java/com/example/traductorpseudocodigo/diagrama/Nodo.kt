package com.example.traductorpseudocodigo.diagrama

data class Nodo(
    val indice: Int,
    val texto: String,
    val x: Float,
    val y: Float,
    val ancho: Float,
    val alto: Float,
    val color: String,
    val colorTexto: String,
    val figura: Figura,
    val letra: Letra,
    val tamLetra: Float
)
