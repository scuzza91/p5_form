# Script para probar el endpoint en el SERVIDOR desde tu máquina local
# Uso: .\probar-servidor.ps1 -ServidorIP "tu-ip-servidor"
# O: .\probar-servidor.ps1 -ServidorIP "tu-dominio.com"

param(
    [Parameter(Mandatory=$false)]
    [string]$ServidorIP = "",
    
    [Parameter(Mandatory=$false)]
    [string]$Puerto = "8083",
    
    [Parameter(Mandatory=$false)]
    [string]$ApiToken = "41855ad220d5c0f4fb39ea6b2ed8d56e"
)

Write-Host "=== Prueba del Endpoint en el SERVIDOR ===" -ForegroundColor Cyan
Write-Host ""

# Si no se proporciona IP, pedirla
if ([string]::IsNullOrEmpty($ServidorIP)) {
    Write-Host "Necesitas la IP o dominio de tu servidor" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Para obtener la IP del servidor, ejecuta en el servidor:" -ForegroundColor Yellow
    Write-Host "  curl ifconfig.me" -ForegroundColor Cyan
    Write-Host "  o" -ForegroundColor White
    Write-Host "  hostname -I" -ForegroundColor Cyan
    Write-Host ""
    $ServidorIP = Read-Host "Ingresa la IP o dominio del servidor"
}

# Construir URL
$url = "http://${ServidorIP}:${Puerto}/api/persona/crear"

Write-Host "URL del endpoint: $url" -ForegroundColor Yellow
Write-Host ""

# Verificar que el servidor esté accesible
Write-Host "Verificando conexion al servidor..." -ForegroundColor Yellow
try {
    $test = Invoke-WebRequest -Uri "http://${ServidorIP}:${Puerto}" -Method Get -TimeoutSec 5 -ErrorAction Stop
    Write-Host "Servidor accesible ✓" -ForegroundColor Green
} catch {
    Write-Host "ADVERTENCIA: No se pudo conectar al servidor" -ForegroundColor Red
    Write-Host "Verifica:" -ForegroundColor Yellow
    Write-Host "  - Que el servidor esté corriendo" -ForegroundColor White
    Write-Host "  - Que el puerto $Puerto esté abierto en el firewall" -ForegroundColor White
    Write-Host "  - Que la IP sea correcta" -ForegroundColor White
    Write-Host ""
    $continuar = Read-Host "¿Continuar de todos modos? (S/N)"
    if ($continuar -ne "S" -and $continuar -ne "s") {
        exit 1
    }
}

Write-Host ""

# Datos de prueba (formato Bondarea)
$timestamp = Get-Date -Format 'yyyyMMddHHmmss'
$datos = @{
    idStage = "B26F5HRR"
    custom_B26FNN8U = "Juan"
    custom_B26FNN83 = "Perez"
    custom_B26FNHKS = "12345678"
    custom_B26FNN87 = "juan.perez.test.$timestamp@example.com"
    custom_B26FNN8P = "valor_adicional"
}

Write-Host "Datos de prueba:" -ForegroundColor Yellow
$datos | ConvertTo-Json | Write-Host
Write-Host ""

# Preparar headers
$headers = @{
    "Content-Type" = "application/json"
    "X-API-Token" = $ApiToken
}

Write-Host "Token de API: $ApiToken" -ForegroundColor Yellow
Write-Host ""

# Realizar la petición
Write-Host "Enviando peticion al servidor..." -ForegroundColor Yellow
Write-Host ""

try {
    $json = $datos | ConvertTo-Json
    
    $respuesta = Invoke-RestMethod -Uri $url -Method Post -Headers $headers -Body $json
    
    Write-Host "✅ EXITO!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Respuesta del servidor:" -ForegroundColor Cyan
    $respuesta | ConvertTo-Json | Write-Host
    
    if ($respuesta.examenId) {
        Write-Host ""
        Write-Host "🎉 Persona y examen creados exitosamente!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Accede al examen en:" -ForegroundColor Yellow
        Write-Host "http://${ServidorIP}:${Puerto}/examen/$($respuesta.examenId)" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Persona ID: $($respuesta.personaId)" -ForegroundColor White
        Write-Host "Examen ID: $($respuesta.examenId)" -ForegroundColor White
    }
    
} catch {
    Write-Host "❌ ERROR!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Mensaje: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.ErrorDetails.Message) {
        Write-Host ""
        Write-Host "Detalles del error:" -ForegroundColor Yellow
        Write-Host $_.ErrorDetails.Message -ForegroundColor Red
    }
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host ""
        Write-Host "Código HTTP: $statusCode" -ForegroundColor Yellow
        
        if ($statusCode -eq 401) {
            Write-Host ""
            Write-Host "⚠️ Token de API inválido" -ForegroundColor Red
            Write-Host "Verifica que el token esté configurado correctamente en el servidor" -ForegroundColor Yellow
            Write-Host "Accede a: http://${ServidorIP}:${Puerto}/configuracion" -ForegroundColor Cyan
        }
        
        if ($statusCode -eq 403) {
            Write-Host ""
            Write-Host "⚠️ Inscripciones cerradas" -ForegroundColor Red
            Write-Host "Las inscripciones están cerradas actualmente" -ForegroundColor Yellow
        }
    }
}

Write-Host ""
Write-Host "Presiona cualquier tecla para continuar..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

