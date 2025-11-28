# Script para configurar ngrok con authtoken
Write-Host "=== Configuración de ngrok ===" -ForegroundColor Cyan
Write-Host ""

Write-Host "ngrok requiere una cuenta gratuita y un authtoken." -ForegroundColor Yellow
Write-Host ""

# Paso 1: Obtener authtoken
Write-Host "PASO 1: Obtener tu authtoken" -ForegroundColor Green
Write-Host ""
Write-Host "1. Ve a: https://dashboard.ngrok.com/signup" -ForegroundColor White
Write-Host "2. Crea una cuenta gratuita (o inicia sesión si ya tienes una)" -ForegroundColor White
Write-Host "3. Ve a: https://dashboard.ngrok.com/get-started/your-authtoken" -ForegroundColor White
Write-Host "4. Copia tu authtoken" -ForegroundColor White
Write-Host ""

$authtoken = Read-Host "Pega tu authtoken aquí"

if ([string]::IsNullOrWhiteSpace($authtoken)) {
    Write-Host "❌ No se ingresó un authtoken" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Configurando ngrok..." -ForegroundColor Yellow

# Encontrar ngrok
$ngrokPath = (Get-Command ngrok -ErrorAction SilentlyContinue).Source

if (-not $ngrokPath) {
    Write-Host "❌ ngrok no está instalado" -ForegroundColor Red
    exit 1
}

# Configurar authtoken
& $ngrokPath config add-authtoken $authtoken

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ ngrok configurado correctamente!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Ahora puedes iniciar ngrok con:" -ForegroundColor Cyan
    Write-Host "   ngrok http 8083" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "❌ Error al configurar ngrok" -ForegroundColor Red
    Write-Host "Verifica que el authtoken sea correcto" -ForegroundColor Yellow
}

