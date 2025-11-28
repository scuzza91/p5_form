# Script mejorado para probar el endpoint
Write-Host "=== Prueba del Endpoint de API ===" -ForegroundColor Cyan
Write-Host ""

# Verificar que la aplicacion este corriendo
$url = "http://localhost:8083/api/persona/crear"
Write-Host "Verificando conexion a: $url" -ForegroundColor Yellow

try {
    $test = Invoke-WebRequest -Uri "http://localhost:8083" -Method Get -TimeoutSec 2 -ErrorAction Stop
    Write-Host "Aplicacion esta corriendo" -ForegroundColor Green
} catch {
    Write-Host "ERROR: La aplicacion NO esta corriendo en localhost:8083" -ForegroundColor Red
    Write-Host "Inicia la aplicacion con: ./mvnw spring-boot:run" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Token de API de Bondarea
$apiToken = "41855ad220d5c0f4fb39ea6b2ed8d56e"

# Datos de prueba
$datos = @{
    idStage = "B26F5HRR"
    custom_B26FNN8U = "Juan"
    custom_B26FNN83 = "Perez"
    custom_B26FNHKS = "12345678"
    custom_B26FNN87 = "juan.perez.test.$(Get-Date -Format 'yyyyMMddHHmmss')@example.com"
    custom_B26FNN8P = "valor_adicional"
}

Write-Host "Enviando datos:" -ForegroundColor Yellow
$datos | ConvertTo-Json | Write-Host
Write-Host ""

try {
    $json = $datos | ConvertTo-Json
    
    $headers = @{
        "Content-Type" = "application/json"
        "X-API-Token" = $apiToken
    }
    
    Write-Host "Realizando peticion..." -ForegroundColor Yellow
    $respuesta = Invoke-RestMethod -Uri $url -Method Post -Headers $headers -Body $json
    
    Write-Host "EXITO!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Respuesta:" -ForegroundColor Cyan
    $respuesta | ConvertTo-Json | Write-Host
    
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
        Write-Host "Detalles del error:" -ForegroundColor Yellow
        Write-Host $_.ErrorDetails.Message -ForegroundColor Red
    }
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "Codigo HTTP: $statusCode" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Presiona cualquier tecla para continuar..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

