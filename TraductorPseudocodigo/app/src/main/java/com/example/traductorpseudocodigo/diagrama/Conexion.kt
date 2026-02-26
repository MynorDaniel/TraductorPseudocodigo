package com.example.traductorpseudocodigo.diagrama

data class Conexion(
    val id: String,
    val origen: Nodo,
    val destino: Nodo,
    val color: String
)
