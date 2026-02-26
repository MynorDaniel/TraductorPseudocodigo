/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.traductorpseudocodigo.javafiles;

import java.util.List;

/**
 *
 * @author mynordma
 */
public class Instruccion {

    private TipoInstruccion tipo;

    // Para asignaciones: variable
    // Para SI/MIENTRAS: condición
    // Para MOSTRAR/LEER: valor
    private String argumento;

    // Para asignaciones: expresión
    // En otros casos puede ser null
    private String expresion;

    // Para SI y MIENTRAS
    private List<Instruccion> bloque;

    public Instruccion(TipoInstruccion tipo, String argumento, String expresion) {
        this.tipo = tipo;
        this.argumento = argumento;
        this.expresion = expresion;
    }

    public TipoInstruccion getTipo() {
        return tipo;
    }

    public String getArgumento() {
        return argumento;
    }

    public String getExpresion() {
        return expresion;
    }

    public List<Instruccion> getBloque() {
        return bloque;
    }

    public void setBloque(List<Instruccion> bloque) {
        this.bloque = bloque;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(tipo).append(" -> ").append(argumento);

        if (expresion != null) {
            sb.append(" = ").append(expresion);
        }

        if (bloque != null) {
            sb.append("\n  BLOQUE:\n");
            for (Instruccion i : bloque) {
                sb.append("    ").append(i.toString()).append("\n");
            }
        }

        return sb.toString();
    }
}