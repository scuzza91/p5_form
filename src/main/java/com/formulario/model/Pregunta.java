package com.formulario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name = "preguntas")
public class Pregunta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El enunciado de la pregunta es obligatorio")
    @Column(columnDefinition = "TEXT")
    private String enunciado;
    
    @NotNull(message = "El área de conocimiento es obligatoria")
    @Enumerated(EnumType.STRING)
    private AreaConocimiento areaConocimiento;
    
    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Opcion> opciones;
    
    @NotNull(message = "La opción correcta es obligatoria")
    private Integer opcionCorrecta; // Índice de la opción correcta (1-4)
    
    private boolean activa = true;
    
    @Column(name = "imagen_url", length = 500)
    private String imagenUrl; // URL o ruta de la imagen asociada (opcional)
    
    // Enum para las áreas de conocimiento
    public enum AreaConocimiento {
        LOGICA("Lógica"),
        MATEMATICA("Matemática"),
        CREATIVIDAD("Creatividad"),
        PROGRAMACION("Programación");
        
        private final String nombre;
        
        AreaConocimiento(String nombre) {
            this.nombre = nombre;
        }
        
        public String getNombre() {
            return nombre;
        }
        
        // Método para obtener la categoría laboral asociada
        public String getCategoriaLaboral() {
            switch (this) {
                case LOGICA:
                    return "Análisis";
                case MATEMATICA:
                    return "Cálculo";
                case CREATIVIDAD:
                    return "Innovación";
                case PROGRAMACION:
                    return "Desarrollo";
                default:
                    return "General";
            }
        }
        
        // Método para obtener el peso en diferentes tipos de posiciones
        public int getPesoParaPosicion(String tipoPosicion) {
            switch (tipoPosicion.toLowerCase()) {
                // Desarrollo de Software
                case "desarrollador":
                case "programador":
                case "software developer":
                case "full stack":
                case "fullstack":
                case "web developer":
                    return switch (this) {
                        case PROGRAMACION -> 40;
                        case LOGICA -> 30;
                        case MATEMATICA -> 20;
                        case CREATIVIDAD -> 10;
                    };
                    
                case "frontend":
                case "front end":
                case "react developer":
                case "javascript developer":
                    return switch (this) {
                        case PROGRAMACION -> 35;
                        case CREATIVIDAD -> 30;
                        case LOGICA -> 25;
                        case MATEMATICA -> 10;
                    };
                    
                case "backend":
                case "back end":
                case "java developer":
                case "database analyst":
                    return switch (this) {
                        case PROGRAMACION -> 35;
                        case LOGICA -> 30;
                        case MATEMATICA -> 25;
                        case CREATIVIDAD -> 10;
                    };
                
                // Análisis de Datos
                case "analista":
                case "data analyst":
                case "business analyst":
                case "bi analyst":
                case "business intelligence":
                    return switch (this) {
                        case MATEMATICA -> 35;
                        case LOGICA -> 30;
                        case PROGRAMACION -> 25;
                        case CREATIVIDAD -> 10;
                    };
                    
                case "data scientist":
                case "ml engineer":
                case "ai developer":
                case "machine learning":
                    return switch (this) {
                        case MATEMATICA -> 40;
                        case LOGICA -> 25;
                        case PROGRAMACION -> 25;
                        case CREATIVIDAD -> 10;
                    };
                    
                case "data engineer":
                case "analytics engineer":
                case "operador de datos":
                case "procesamiento de datos":
                    return switch (this) {
                        case MATEMATICA -> 35;
                        case PROGRAMACION -> 30;
                        case LOGICA -> 25;
                        case CREATIVIDAD -> 10;
                    };
                
                // Diseño y UX
                case "diseñador":
                case "ux/ui":
                case "maquetador":
                case "maquetador web":
                    return switch (this) {
                        case CREATIVIDAD -> 40;
                        case LOGICA -> 25;
                        case PROGRAMACION -> 20;
                        case MATEMATICA -> 15;
                    };
                
                // Gestión y Producto
                case "project manager":
                case "scrum master":
                case "product manager":
                case "product owner":
                    return switch (this) {
                        case LOGICA -> 30;
                        case CREATIVIDAD -> 30;
                        case MATEMATICA -> 20;
                        case PROGRAMACION -> 20;
                    };
                
                // DevOps e Infraestructura
                case "devops":
                case "sysadmin":
                case "sys admin":
                case "devops engineer":
                    return switch (this) {
                        case LOGICA -> 35;
                        case PROGRAMACION -> 30;
                        case MATEMATICA -> 20;
                        case CREATIVIDAD -> 15;
                    };
                
                // Seguridad
                case "qa":
                case "tester":
                case "ciberseguridad":
                case "analista de seguridad":
                case "incident responder":
                case "vulnerabilidades":
                    return switch (this) {
                        case LOGICA -> 40;
                        case CREATIVIDAD -> 25;
                        case PROGRAMACION -> 20;
                        case MATEMATICA -> 15;
                    };
                
                // Consultoría y Especialistas
                case "consultor":
                case "consultor sap":
                case "sap finance":
                    return switch (this) {
                        case LOGICA -> 35;
                        case MATEMATICA -> 30;
                        case PROGRAMACION -> 20;
                        case CREATIVIDAD -> 15;
                    };
                
                case "no code":
                case "automation specialist":
                case "ai implementation":
                    return switch (this) {
                        case LOGICA -> 35;
                        case CREATIVIDAD -> 30;
                        case PROGRAMACION -> 20;
                        case MATEMATICA -> 15;
                    };
                
                // Marketing y Negocios
                case "marketing":
                case "marketing technologist":
                case "marketing tecnologist":
                    return switch (this) {
                        case CREATIVIDAD -> 40;
                        case MATEMATICA -> 25;
                        case LOGICA -> 20;
                        case PROGRAMACION -> 15;
                    };
                
                // Base de Datos
                case "administrador de base de datos":
                case "desarrollador sql":
                case "dba":
                    return switch (this) {
                        case MATEMATICA -> 35;
                        case LOGICA -> 30;
                        case PROGRAMACION -> 25;
                        case CREATIVIDAD -> 10;
                    };
                
                // Arquitectura y Liderazgo
                case "arquitecto":
                case "tech lead":
                case "semi senior":
                case "senior":
                case "jefe":
                    return switch (this) {
                        case LOGICA -> 35;
                        case PROGRAMACION -> 30;
                        case MATEMATICA -> 20;
                        case CREATIVIDAD -> 15;
                    };
                
                // Junior/Trainee
                case "junior":
                case "trainee":
                case "frontend developer jr":
                    return switch (this) {
                        case PROGRAMACION -> 30;
                        case LOGICA -> 30;
                        case CREATIVIDAD -> 25;
                        case MATEMATICA -> 15;
                    };
                
                default:
                    return 25; // Peso equilibrado por defecto
            }
        }
        
        // Método para obtener descripción de la área
        public String getDescripcion() {
            switch (this) {
                case LOGICA:
                    return "Capacidad de razonamiento, resolución de problemas y pensamiento analítico";
                case MATEMATICA:
                    return "Habilidades matemáticas, estadísticas y de modelado";
                case CREATIVIDAD:
                    return "Pensamiento innovador, diseño y resolución creativa de problemas";
                case PROGRAMACION:
                    return "Conocimientos de programación, algoritmos y desarrollo de software";
                default:
                    return "Área de conocimiento general";
            }
        }
    }
    
    // Constructores
    public Pregunta() {}
    
    public Pregunta(String enunciado, AreaConocimiento areaConocimiento, Integer opcionCorrecta) {
        this.enunciado = enunciado;
        this.areaConocimiento = areaConocimiento;
        this.opcionCorrecta = opcionCorrecta;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEnunciado() {
        return enunciado;
    }
    
    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }
    
    public AreaConocimiento getAreaConocimiento() {
        return areaConocimiento;
    }
    
    public void setAreaConocimiento(AreaConocimiento areaConocimiento) {
        this.areaConocimiento = areaConocimiento;
    }
    
    public List<Opcion> getOpciones() {
        return opciones;
    }
    
    public void setOpciones(List<Opcion> opciones) {
        this.opciones = opciones;
    }
    
    public Integer getOpcionCorrecta() {
        return opcionCorrecta;
    }
    
    public void setOpcionCorrecta(Integer opcionCorrecta) {
        this.opcionCorrecta = opcionCorrecta;
    }
    
    public boolean isActiva() {
        return activa;
    }
    
    public void setActiva(boolean activa) {
        this.activa = activa;
    }
    
    public String getImagenUrl() {
        return imagenUrl;
    }
    
    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
} 