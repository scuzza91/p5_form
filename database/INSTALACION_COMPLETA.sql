-- ========================================
-- SCRIPT DE INSTALACIÓN COMPLETA - SISTEMA PISO CINCO
-- ========================================
-- Este script ejecuta todos los scripts necesarios para configurar
-- completamente la base de datos del sistema de evaluación
-- 
-- Fecha: Diciembre 2024
-- Versión: 1.0.0
-- ========================================

-- Configurar encoding y formato
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

-- Crear la base de datos si no existe
-- (Este comando debe ejecutarse desde psql como superusuario)
-- CREATE DATABASE p5_form_dev;

-- Conectar a la base de datos
\c p5_form_dev;

-- ========================================
-- 1. CREAR TABLAS PRINCIPALES
-- ========================================

-- Crear tabla de usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    rol VARCHAR(20) DEFAULT 'USUARIO',
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear tabla de provincias
CREATE TABLE IF NOT EXISTS provincias (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    codigo VARCHAR(10)
);

-- Crear tabla de localidades
CREATE TABLE IF NOT EXISTS localidades (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    provincia_id INTEGER REFERENCES provincias(id),
    codigo_postal VARCHAR(10)
);

-- Crear tabla de personas
CREATE TABLE IF NOT EXISTS personas (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    telefono VARCHAR(20),
    fecha_nacimiento DATE,
    direccion TEXT,
    provincia_id INTEGER REFERENCES provincias(id),
    localidad_id INTEGER REFERENCES localidades(id),
    cuil VARCHAR(15) UNIQUE,
    genero VARCHAR(20),
    conocimientos_programacion VARCHAR(50),
    internet_hogar BOOLEAN,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear tabla de posiciones laborales
CREATE TABLE IF NOT EXISTS posiciones_laborales (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    area VARCHAR(100),
    nivel_experiencia VARCHAR(50),
    salario_minimo DECIMAL(10,2),
    salario_maximo DECIMAL(10,2),
    requisitos TEXT,
    beneficios TEXT,
    activa BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear tabla de roles profesionales
CREATE TABLE IF NOT EXISTS roles_profesionales (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    area_tecnica VARCHAR(100),
    nivel_experiencia VARCHAR(50),
    habilidades_requeridas TEXT,
    responsabilidades TEXT,
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear tabla de exámenes
CREATE TABLE IF NOT EXISTS examenes (
    id SERIAL PRIMARY KEY,
    persona_id INTEGER UNIQUE REFERENCES personas(id),
    programacion_basica INTEGER CHECK (programacion_basica >= 0 AND programacion_basica <= 100),
    estructuras_datos INTEGER CHECK (estructuras_datos >= 0 AND estructuras_datos <= 100),
    algoritmos INTEGER CHECK (algoritmos >= 0 AND algoritmos <= 100),
    base_datos INTEGER CHECK (base_datos >= 0 AND base_datos <= 100),
    comentarios TEXT,
    fecha_examen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    aprobado BOOLEAN GENERATED ALWAYS AS (
        (programacion_basica + estructuras_datos + algoritmos + base_datos) / 4.0 >= 70
    ) STORED
);

-- Crear tabla de configuración del sistema
CREATE TABLE IF NOT EXISTS configuracion_sistema (
    id SERIAL PRIMARY KEY,
    clave VARCHAR(100) UNIQUE NOT NULL,
    valor TEXT,
    descripcion TEXT,
    tipo VARCHAR(50),
    editable BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- 2. INSERTAR DATOS INICIALES
-- ========================================

-- Insertar usuario administrador por defecto
-- (La contraseña debe ser hasheada con BCrypt en la aplicación)
INSERT INTO usuarios (username, password, email, rol) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'admin@piso5.com', 'ADMIN')
ON CONFLICT (username) DO NOTHING;

-- Insertar configuraciones del sistema
INSERT INTO configuracion_sistema (clave, valor, descripcion, tipo) VALUES
('criterio_aprobacion', '70', 'Puntuación mínima para aprobar el examen', 'NUMERICO'),
('max_puntuacion_por_area', '100', 'Puntuación máxima por área de evaluación', 'NUMERICO'),
('areas_evaluacion', 'programacion_basica,estructuras_datos,algoritmos,base_datos', 'Áreas de evaluación del examen', 'TEXTO'),
('sistema_activo', 'true', 'Estado del sistema de evaluación', 'BOOLEANO'),
('fecha_inicio_evaluaciones', '2024-01-01', 'Fecha de inicio de las evaluaciones', 'FECHA'),
('fecha_fin_evaluaciones', '2024-12-31', 'Fecha de fin de las evaluaciones', 'FECHA')
ON CONFLICT (clave) DO NOTHING;

-- ========================================
-- 3. CREAR ÍNDICES PARA OPTIMIZACIÓN
-- ========================================

-- Índices para búsquedas frecuentes
CREATE INDEX IF NOT EXISTS idx_personas_email ON personas(email);
CREATE INDEX IF NOT EXISTS idx_personas_cuil ON personas(cuil);
CREATE INDEX IF NOT EXISTS idx_examenes_persona ON examenes(persona_id);
CREATE INDEX IF NOT EXISTS idx_examenes_fecha ON examenes(fecha_examen);
CREATE INDEX IF NOT EXISTS idx_usuarios_username ON usuarios(username);
CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email);
CREATE INDEX IF NOT EXISTS idx_localidades_provincia ON localidades(provincia_id);
CREATE INDEX IF NOT EXISTS idx_posiciones_activa ON posiciones_laborales(activa);
CREATE INDEX IF NOT EXISTS idx_roles_activo ON roles_profesionales(activo);

-- ========================================
-- 4. CREAR VISTAS ÚTILES
-- ========================================

-- Vista para estadísticas generales
CREATE OR REPLACE VIEW estadisticas_generales AS
SELECT 
    COUNT(DISTINCT p.id) as total_candidatos,
    COUNT(DISTINCT e.id) as total_evaluaciones,
    COUNT(DISTINCT CASE WHEN e.aprobado = true THEN e.id END) as total_aprobados,
    COUNT(DISTINCT CASE WHEN e.aprobado = false THEN e.id END) as total_reprobados,
    ROUND(AVG(e.programacion_basica), 2) as promedio_programacion,
    ROUND(AVG(e.estructuras_datos), 2) as promedio_estructuras,
    ROUND(AVG(e.algoritmos), 2) as promedio_algoritmos,
    ROUND(AVG(e.base_datos), 2) as promedio_base_datos
FROM personas p
LEFT JOIN examenes e ON p.id = e.persona_id;

-- Vista para candidatos con resultados
CREATE OR REPLACE VIEW candidatos_con_resultados AS
SELECT 
    p.id,
    p.nombre,
    p.apellido,
    p.email,
    p.cuil,
    e.programacion_basica,
    e.estructuras_datos,
    e.algoritmos,
    e.base_datos,
    ROUND((e.programacion_basica + e.estructuras_datos + e.algoritmos + e.base_datos) / 4.0, 2) as promedio,
    e.aprobado,
    e.fecha_examen,
    p.fecha_registro
FROM personas p
LEFT JOIN examenes e ON p.id = e.persona_id
ORDER BY e.fecha_examen DESC;

-- ========================================
-- 5. CREAR FUNCIONES ÚTILES
-- ========================================

-- Función para calcular promedio de un examen
CREATE OR REPLACE FUNCTION calcular_promedio_examen(
    p_programacion_basica INTEGER,
    p_estructuras_datos INTEGER,
    p_algoritmos INTEGER,
    p_base_datos INTEGER
) RETURNS DECIMAL AS $$
BEGIN
    RETURN (p_programacion_basica + p_estructuras_datos + p_algoritmos + p_base_datos) / 4.0;
END;
$$ LANGUAGE plpgsql;

-- Función para verificar si un examen está aprobado
CREATE OR REPLACE FUNCTION verificar_aprobacion(
    p_promedio DECIMAL,
    p_criterio INTEGER DEFAULT 70
) RETURNS BOOLEAN AS $$
BEGIN
    RETURN p_promedio >= p_criterio;
END;
$$ LANGUAGE plpgsql;

-- ========================================
-- 6. CREAR TRIGGERS PARA AUDITORÍA
-- ========================================

-- Función para actualizar fecha de modificación
CREATE OR REPLACE FUNCTION actualizar_fecha_modificacion()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_actualizacion = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para configuracion_sistema
CREATE TRIGGER trigger_actualizar_configuracion
    BEFORE UPDATE ON configuracion_sistema
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_fecha_modificacion();

-- ========================================
-- 7. CONFIGURAR PERMISOS
-- ========================================

-- Otorgar permisos al usuario de la aplicación (si existe)
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO p5_form_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO p5_form_user;
-- GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO p5_form_user;

-- ========================================
-- 8. VERIFICACIONES FINALES
-- ========================================

-- Verificar que las tablas se crearon correctamente
DO $$
DECLARE
    tabla_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO tabla_count 
    FROM information_schema.tables 
    WHERE table_schema = 'public' 
    AND table_name IN ('usuarios', 'personas', 'examenes', 'configuracion_sistema');
    
    IF tabla_count = 4 THEN
        RAISE NOTICE '✅ Todas las tablas principales se crearon correctamente';
    ELSE
        RAISE NOTICE '⚠️ Algunas tablas no se crearon correctamente. Verificar errores.';
    END IF;
END $$;

-- Mostrar estadísticas de la instalación
SELECT 
    'Instalación completada' as estado,
    COUNT(*) as total_tablas,
    CURRENT_TIMESTAMP as fecha_instalacion
FROM information_schema.tables 
WHERE table_schema = 'public';

-- ========================================
-- FIN DEL SCRIPT DE INSTALACIÓN
-- ========================================
-- 
-- Para ejecutar este script:
-- psql -U postgres -d p5_form_dev -f INSTALACION_COMPLETA.sql
-- 
-- O desde pgAdmin: Ejecutar este archivo completo
-- ======================================== 