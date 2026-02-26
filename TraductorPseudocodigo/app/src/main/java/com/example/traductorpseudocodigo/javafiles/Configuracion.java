/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.traductorpseudocodigo.javafiles;

/**
 *
 * @author mynordma
 */
public class Configuracion {

    private String tipo;      // COLOR_SI, FIGURA_BLOQUE, etc.
    private Object valor;     // HFF0000 o 12,45,1
    private String indice;    // expresión del índice

    public Configuracion(String tipo, Object valor, String indice) {
        this.tipo = tipo;
        this.valor = valor;
        this.indice = indice;
    }
    
    @Override
    public String toString(){
        return "Config -> " + tipo + " - valor: " + valor + " indice: " + indice;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Object getValor() {
        return valor;
    }

    public void setValor(Object valor) {
        this.valor = valor;
    }

    public String getIndice() {
        return indice;
    }

    public void setIndice(String indice) {
        this.indice = indice;
    }

}