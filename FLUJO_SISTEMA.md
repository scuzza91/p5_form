# 📊 Flujos del Sistema - Evaluación de Candidatos

Diagramas para entender el proceso completo. Compatible con **Mermaid** (GitHub, VS Code, Cursor).

---

## 1. Vista general: entradas y módulos

```mermaid
flowchart TB
    subgraph ENTRADAS["🔗 Entradas al sistema"]
        BOND["Bondarea\n(Webhook POST)"]
        WEB["Formulario web\n(Paso 1 + Paso 2)"]
    end

    subgraph APP["Aplicación p5_form"]
        API["/api/persona/crear"]
        FORM["Formulario\n/paso1, /paso2"]
        EXAM["Examen\n/examen/{token}"]
        RES["Resultado\n/resultado/{id}"]
        REC["Recomendaciones"]
        ADMIN["Panel Admin\n/dashboard, /login"]
    end

    subgraph BD["Base de datos"]
        PG[(PostgreSQL)]
    end

    BOND -->|"idCaso + X-API-Token"| API
    WEB --> FORM
    API -->|Crea Persona + Examen| PG
    FORM -->|Crea Persona + Examen| PG
    API -->|Devuelve examenUrl| BOND
    FORM --> EXAM
    EXAM -->|Guarda respuestas| PG
    EXAM --> RES
    RES --> REC
    ADMIN --> PG
```

---

## 2. Flujo Bondarea → Examen (integración externa)

```mermaid
sequenceDiagram
    participant Bondarea as Bondarea
    participant API as POST /api/persona/crear
    participant Config as ConfiguracionService
    participant BondareaSvc as BondareaService
    participant FormSvc as FormularioService
    participant DB as PostgreSQL

    Bondarea->>+API: POST idCaso + X-API-Token
    API->>Config: validarApiToken(token)
    Config-->>API: OK / 401

    alt Token inválido
        API-->>Bondarea: 401 Token inválido
    end

    API->>+BondareaSvc: obtenerSolicitudFinanciamiento(idStage, idCaso)
    BondareaSvc->>Bondarea: GET /api/v2/monitoring/{idStage}/{idCaso}
    Bondarea-->>BondareaSvc: JSON (custom_B26FNN8U, etc.)
    BondareaSvc-->>-API: datos persona

    API->>API: mapearPersonaDesdeBondarea()
    Note over API: nombre, apellido, email, CUIL, etc.

    alt Email ya existe
        API->>FormSvc: buscarPersonaPorEmail()
        FormSvc->>DB: SELECT persona
        alt Ya tiene examen
            API-->>Bondarea: 409 Conflicto
        else No tiene examen
            API->>FormSvc: guardarExamen(personaExistente)
        end
    else Persona nueva
        API->>FormSvc: guardarPersona() + guardarExamen()
        FormSvc->>DB: INSERT persona, INSERT examen
    end

    API->>API: ExamenTokenUtil.generarToken(examenId)
    API-->>Bondarea: 200 JSON { examenId, personaId, examenUrl }
```

---

## 3. Flujo del candidato: hacer el examen

```mermaid
flowchart LR
    subgraph ACCESO["Acceso al examen"]
        A1["Recibe link\n/examen/{token}"]
        A2["Valida token\nExamenTokenUtil"]
        A3["Carga examen\n+ persona"]
    end

    subgraph EXAMEN["Realización"]
        B1["Genera 32 preguntas\n8 por área"]
        B2["Lógica · Matemática\nCreatividad · Programación"]
        B3["Candidato responde\ncada pregunta"]
        B4["Guarda RespuestaExamen\npor cada una"]
    end

    subgraph FIN["Finalización"]
        C1["finalizarExamen()"]
        C2["calcularPuntuaciones()\n% por área"]
        C3["fechaFin, totalPreguntas\nrespuestasCorrectas"]
        C4["Aprobado si\npromedio ≥ 70"]
    end

    subgraph RESULTADO["Resultado"]
        D1["/resultado/{personaId}"]
        D2["Ver puntajes\n+ PDF"]
        D3["Recomendaciones\nposiciones laborales"]
    end

    A1 --> A2 --> A3 --> B1 --> B2 --> B3 --> B4 --> C1 --> C2 --> C3 --> C4 --> D1 --> D2 --> D3
```

---

## 4. Cálculo de puntuaciones y aprobación

```mermaid
flowchart TB
    subgraph ENTRADA["Al finalizar examen"]
        R["Lista de RespuestaExamen\n(examen, pregunta, opción)"]
    end

    subgraph AREAS["Áreas de conocimiento"]
        L[Lógica]
        M[Matemática]
        C[Creatividad]
        P[Programación]
    end

    subgraph CALCULO["Por cada área"]
        N["Total preguntas área"]
        OK["Respuestas correctas"]
        PCT["% = (correctas / total) × 100"]
    end

    subgraph EXAMEN_ENTITY["Examen (BD)"]
        E1["logica: Integer"]
        E2["matematica: Integer"]
        E3["creatividad: Integer"]
        E4["programacion: Integer"]
        E5["totalPreguntas"]
        E6["respuestasCorrectas"]
    end

    subgraph APROBACION["Aprobación"]
        PROM["promedio = (L+M+C+P) / 4"]
        UMBRAL["¿ promedio ≥ 70 ?"]
        SI["Aprobado ✅"]
        NO["Reprobado ❌"]
    end

    R --> L & M & C & P
    L & M & C & P --> N --> OK --> PCT
    PCT --> E1 & E2 & E3 & E4
    R --> E5 & E6
    E1 & E2 & E3 & E4 --> PROM --> UMBRAL
    UMBRAL --> SI
    UMBRAL --> NO
```

---

## 5. Recomendaciones de posiciones laborales

```mermaid
flowchart TB
    subgraph ENTRADA["Entrada"]
        EX["Examen completado\n(puntuaciones por área)"]
    end

    subgraph POSICIONES["Posiciones laborales (BD)"]
        P1["Posición 1\npesos: L, M, C, P"]
        P2["Posición 2\n..."]
        P3["Posición N\n..."]
    end

    subgraph CALCULO["Por cada posición"]
        F["calcularCompatibilidad(examen)"]
        W["Usa pesos por área\nvs puntajes del examen"]
        SCORE["Compatibilidad 0–100%"]
    end

    subgraph SALIDA["Resultado"]
        FILTRO["Solo compatibilidad > 0"]
        ORDEN["Ordenar por compatibilidad DESC"]
        LISTA["Lista RecomendacionDTO\npara el candidato"]
    end

    EX --> P1 & P2 & P3
    P1 & P2 & P3 --> F --> W --> SCORE
    SCORE --> FILTRO --> ORDEN --> LISTA
```

---

## 6. Panel administrativo (resumen)

```mermaid
flowchart TB
    subgraph ACCESO["Acceso"]
        L["/login"]
        A["Spring Security\nBCrypt"]
        D["/dashboard"]
    end

    subgraph GESTION["Gestión"]
        I["/inscripciones\nLista candidatos"]
        CONF["/configuracion\nToken Bondarea, inscripciones abiertas"]
        PREG["/admin/preguntas\nPreguntas del examen"]
        ROL["/admin/roles\nRoles profesionales"]
        EST["/admin/recomendaciones-estudios"]
    end

    subgraph REPORTES["Reportes"]
        PDF["PDF resultado\npor persona"]
        EXCEL["Exportar Excel"]
    end

    L --> A --> D
    D --> I & CONF & PREG & ROL & EST
    I --> PDF & EXCEL
```

---

## 7. Flujo completo en una página (simplificado)

```mermaid
flowchart TB
    Start([Inicio]) --> Origen{Origen del candidato?}

    Origen -->|Bondarea| Webhook["Bondarea POST /api/persona/crear"]
    Origen -->|Web| Form["Formulario /paso1 + /paso2"]

    Webhook --> ValidarToken{Token OK?}
    ValidarToken -->|No| E401[401]
    ValidarToken -->|Sí| GetBondarea["Obtener datos Bondarea API"]
    GetBondarea --> Mapear["Mapear a Persona"]
    Form --> Mapear

    Mapear --> Existe{¿Email existe?}
    Existe -->|Sí| TieneExam{¿Tiene examen?}
    TieneExam -->|Sí| Conflict[409 Conflicto]
    TieneExam -->|No| CrearExam1["Crear Examen"]
    Existe -->|No| CrearPersona["Crear Persona"]
    CrearPersona --> CrearExam2["Crear Examen"]
    CrearExam1 --> Token
    CrearExam2 --> Token["Generar token examen\nExamenTokenUtil"]

    Token --> Respuesta["Responder: examenUrl\n+ examenId, personaId"]

    Respuesta --> Candidato["Candidato abre /examen/{token}"]
    Candidato --> Preguntas["32 preguntas\n(8 por área)"]
    Preguntas --> Responder["Responde y guarda\nRespuestaExamen"]
    Responder --> Finalizar["Finalizar examen"]
    Finalizar --> Puntuar["calcularPuntuaciones()\n% por área"]
    Puntuar --> Aprobado{¿Promedio ≥ 70?}
    Aprobado -->|Sí| OK["Aprobado ✅"]
    Aprobado -->|No| NO["Reprobado ❌"]
    OK --> Resultado["/resultado/{personaId}"]
    NO --> Resultado
    Resultado --> Recomendaciones["Recomendaciones\nposiciones laborales"]
    Recomendaciones --> PDF["Descargar PDF"]
    PDF --> End([Fin])
```

---

## Cómo ver los diagramas

- **GitHub:** al subir este `.md`, los bloques ```mermaid se renderizan automáticamente.
- **VS Code / Cursor:** instalar extensión "Mermaid" o "Markdown Preview Mermaid Support" y abrir la vista previa del Markdown.
- **Online:** copiar el contenido de un bloque `mermaid` en [mermaid.live](https://mermaid.live) para editar y exportar a PNG/SVG.

---

**Versión:** 1.0 · **Proyecto:** p5_form
