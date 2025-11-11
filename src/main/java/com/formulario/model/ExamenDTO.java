package com.formulario.model;

import java.util.Map;

public class ExamenDTO {
    private Long examenId;
    private Map<Long, Integer> respuestas; // preguntaId -> respuestaSeleccionada (1-4)
    
    public ExamenDTO() {}
    
    public ExamenDTO(Long examenId, Map<Long, Integer> respuestas) {
        this.examenId = examenId;
        this.respuestas = respuestas;
    }
    
    // Getters y Setters
    public Long getExamenId() {
        return examenId;
    }
    
    public void setExamenId(Long examenId) {
        this.examenId = examenId;
    }
    
    public Map<Long, Integer> getRespuestas() {
        return respuestas;
    }
    
    public void setRespuestas(Map<Long, Integer> respuestas) {
        this.respuestas = respuestas;
    }
} 