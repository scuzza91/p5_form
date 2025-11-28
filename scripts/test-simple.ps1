# Script simple para probar el endpoint de API (PowerShell)
# Uso: .\scripts\test-simple.ps1

Write-Host "=== Prueba del Endpoint de API ===" -ForegroundColor Cyan
Write-Host ""

# URL del endpoint
$url = "http://localhost:8083/api/persona/crear"

# Token de API de Bondarea (del email)
$apiToken = "41855ad220d5c0f4fb39ea6b2ed8d56e"

# Datos de prueba (formato Bondarea)
$datos = @{
    idStage = "B26F5HRR"
    custom_B26FNN8U = "Juan"
    custom_B26FNN83 = "Perez"
    custom_B26FNHKS = "12345678"
    custom_B26FNN87 = "juan.perez.test.$(Get-Date -Format 'yyyyMMddHHmmss')@example.com"
    custom_B26FNN8P = "valor_adicional"
}

Write-Host "Enviando datos a: $url" -ForegroundColor Yellow
Write-Host "Datos: " -ForegroundColor Yellow
$datos | ConvertTo-Json | Write-Host
Write-Host ""

try {
    # Convertir a JSON
    $json = $datos | ConvertTo-Json
    
    # Preparar headers
    $headers = @{
        "Content-Type" = "application/json"
    }
    
    # Agregar token si esta configurado
    if ($apiToken -and $apiToken -ne "") {
        $headers["X-API-Token"] = $apiToken
        Write-Host "Usando token de autenticacion" -ForegroundColor Yellow
    }
    
    # Realizar la peticion
    $respuesta = Invoke-RestMethod -Uri $url -Method Post -Headers $headers -Body $json
    
    Write-Host "EXITO!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Respuesta:" -ForegroundColor Cyan
    $respuesta | ConvertTo-Json | Write-Host
    
    # Mostrar URL del examen
    if ($respuesta.examenId) {
        Write-Host ""
        Write-Host "Accede al examen en:" -ForegroundColor Green
        Write-Host "http://localhost:8083/examen/$($respuesta.examenId)" -ForegroundColor Cyan
    }
    
} catch {
    Write-Host "ERROR!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Mensaje: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.ErrorDetails.Message) {
        Write-Host "Detalles: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Presiona cualquier tecla para continuar..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
