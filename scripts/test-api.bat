@echo off
REM Script para probar el endpoint de creación de persona desde API (Windows)
REM Uso: test-api.bat

set API_URL=http://localhost:8083/api/persona/crear

echo === Prueba del Endpoint de API ===
echo URL: %API_URL%
echo.

REM Datos de prueba
set JSON_DATA={"nombre": "Juan", "apellido": "Pérez", "Documento": "12345678", "email": "juan.perez.test@example.com"}

echo Enviando datos:
echo %JSON_DATA%
echo.

REM Realizar la petición usando PowerShell
powershell -Command "Invoke-RestMethod -Uri '%API_URL%' -Method Post -ContentType 'application/json' -Body '%JSON_DATA%' | ConvertTo-Json"

echo.
echo === Prueba completada ===
pause

