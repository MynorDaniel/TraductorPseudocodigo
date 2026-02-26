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
public class Programa {

    private List<Instruccion> instrucciones;
    private List<Configuracion> configuraciones;

    public Programa(List<Instruccion> instrucciones, List<Configuracion> configuraciones) {
        this.instrucciones = instrucciones;
        this.configuraciones = configuraciones;
    }

    public List<Instruccion> getInstrucciones() {
        return instrucciones;
    }

    public List<Configuracion> getConfiguraciones() {
        return configuraciones;
    }
}
