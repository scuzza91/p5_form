package com.formulario.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para recibir datos de persona desde la API de Bondarea
 * Campos según documentación: idStage, custom_B26FNN8U, custom_B26FNN83, custom_B26FNHKS, custom_B26FNN87, custom_B26FNN8P
 */
public class PersonaApiDTO {
    
    @JsonProperty("idStage")
    private String idStage;
    
    @JsonProperty("custom_B26FNN8U")
    private String custom_B26FNN8U; // Nombre
    
    @JsonProperty("custom_B26FNN83")
    private String custom_B26FNN83; // Apellido
    
    @JsonProperty("custom_B26FNHKS")
    private String custom_B26FNHKS; // Documento (DNI)
    
    @JsonProperty("custom_B26FNN87")
    private String custom_B26FNN87; // Email
    
    @JsonProperty("custom_B26FNN8P")
    private String custom_B26FNN8P; // Campo adicional
    
    // Constructores
    public PersonaApiDTO() {}
    
    // Getters y Setters
    public String getIdStage() {
        return idStage;
    }
    
    public void setIdStage(String idStage) {
        this.idStage = idStage;
    }
    
    public String getCustom_B26FNN8U() {
        return custom_B26FNN8U;
    }
    
    public void setCustom_B26FNN8U(String custom_B26FNN8U) {
        this.custom_B26FNN8U = custom_B26FNN8U;
    }
    
    public String getCustom_B26FNN83() {
        return custom_B26FNN83;
    }
    
    public void setCustom_B26FNN83(String custom_B26FNN83) {
        this.custom_B26FNN83 = custom_B26FNN83;
    }
    
    public String getCustom_B26FNHKS() {
        return custom_B26FNHKS;
    }
    
    public void setCustom_B26FNHKS(String custom_B26FNHKS) {
        this.custom_B26FNHKS = custom_B26FNHKS;
    }
    
    public String getCustom_B26FNN87() {
        return custom_B26FNN87;
    }
    
    public void setCustom_B26FNN87(String custom_B26FNN87) {
        this.custom_B26FNN87 = custom_B26FNN87;
    }
    
    public String getCustom_B26FNN8P() {
        return custom_B26FNN8P;
    }
    
    public void setCustom_B26FNN8P(String custom_B26FNN8P) {
        this.custom_B26FNN8P = custom_B26FNN8P;
    }
    
    // Métodos de conveniencia para mapeo
    public String getNombre() {
        return custom_B26FNN8U;
    }
    
    public String getApellido() {
        return custom_B26FNN83;
    }
    
    public String getDocumento() {
        return custom_B26FNHKS;
    }
    
    public String getEmail() {
        return custom_B26FNN87;
    }
}

