@echo off
echo ========================================
echo   Configuracion de Base de Datos
echo   Sistema Piso Cinco
echo ========================================
echo.

REM Verificar si PostgreSQL está instalado
psql --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: PostgreSQL no está instalado o no está en el PATH
    echo Por favor instale PostgreSQL desde: https://www.postgresql.org/download/windows/
    echo.
    pause
    exit /b 1
)

echo PostgreSQL detectado correctamente
echo.

REM Solicitar credenciales
set /p DB_USER=Usuario de PostgreSQL (default: postgres): 
if "%DB_USER%"=="" set DB_USER=postgres

set /p DB_PASSWORD=Contraseña de PostgreSQL: 
if "%DB_PASSWORD%"=="" (
    echo ERROR: Debe ingresar la contraseña de PostgreSQL
    pause
    exit /b 1
)

echo.
echo Configurando base de datos...

REM Crear base de datos si no existe
echo Creando base de datos p5_form_dev...
psql -U %DB_USER% -c "CREATE DATABASE p5_form_dev;" 2>nul
if errorlevel 1 (
    echo La base de datos ya existe o hubo un error. Continuando...
) else (
    echo Base de datos creada correctamente.
)

echo.
echo Ejecutando script de instalación completa...
echo.

REM Ejecutar script de instalación
psql -U %DB_USER% -d p5_form_dev -f database\INSTALACION_COMPLETA.sql

if errorlevel 1 (
    echo.
    echo ERROR: Hubo un problema ejecutando el script SQL
    echo Verifique las credenciales y que PostgreSQL esté ejecutándose
    echo.
    pause
    exit /b 1
) else (
    echo.
    echo ========================================
    echo   Base de datos configurada correctamente
    echo ========================================
    echo.
    echo Tablas creadas:
    echo - usuarios
    echo - personas  
    echo - examenes
    echo - configuracion_sistema
    echo - provincias
    echo - localidades
    echo - posiciones_laborales
    echo - roles_profesionales
    echo.
    echo Usuario administrador creado:
    echo - Username: admin
    echo - Password: admin123
    echo.
    echo La aplicación está lista para ejecutarse.
    echo.
)

pause 