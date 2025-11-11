@echo off
echo ========================================
echo   Formulario de Inscripcion en 2 Pasos
echo ========================================
echo.
echo Verificando requisitos...
echo.

REM Verificar si Java está instalado
java --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java no está instalado o no está en el PATH
    echo Por favor instale Java 17 o superior
    pause
    exit /b 1
)

REM Agregar PostgreSQL al PATH si no está
set PATH=%PATH%;C:\Program Files\PostgreSQL\16\bin

REM Verificar si PostgreSQL está ejecutándose
echo Verificando conexión a PostgreSQL...
psql --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: PostgreSQL no está instalado o no está en el PATH
    echo Por favor instale PostgreSQL desde: https://www.postgresql.org/download/windows/
    echo Después ejecute configurar_postgresql.bat para configurar la base de datos
    echo.
    pause
    exit /b 1
) else (
    echo PostgreSQL detectado correctamente
    echo.
)

REM Intentar usar Maven wrapper si existe
if exist "mvnw.cmd" (
    echo Usando Maven Wrapper...
    call mvnw.cmd spring-boot:run
) else (
    echo ERROR: No se encontró Maven Wrapper
    echo Por favor instale Maven o use el wrapper
    pause
    exit /b 1
)

echo.
echo La aplicacion se ha detenido.
pause 