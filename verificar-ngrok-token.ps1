# Script para verificar y configurar correctamente el authtoken de ngrok
Write-Host "=== Verificación de Authtoken de ngrok ===" -ForegroundColor Cyan
Write-Host ""

Write-Host "El authtoken que ingresaste no es válido." -ForegroundColor Yellow
Write-Host "Los authtokens de ngrok tienen un formato específico." -ForegroundColor Yellow
Write-Host ""

Write-Host "PASO 1: Obtener el authtoken correcto" -ForegroundColor Green
Write-Host ""
Write-Host "1. Ve a tu dashboard de ngrok:" -ForegroundColor White
Write-Host "   https://dashboard.ngrok.com/get-started/your-authtoken" -ForegroundColor Cyan
Write-Host ""
Write-Host "2. Busca la sección 'Your Authtoken'" -ForegroundColor White
Write-Host ""
Write-Host "3. El authtoken debería verse así:" -ForegroundColor White
Write-Host "   - Muy largo (más de 40 caracteres)" -ForegroundColor Gray
Write-Host "   - Con formato: 2abc123def456ghi789jkl012mno345pqr678..." -ForegroundColor Gray
Write-Host "   - NO es un hash MD5 corto" -ForegroundColor Gray
Write-Host ""
Write-Host "4. Copia TODO el authtoken (haz clic en 'Copy' si hay un botón)" -ForegroundColor White
Write-Host ""

$authtoken = Read-Host "Pega tu authtoken completo aquí"

if ([string]::IsNullOrWhiteSpace($authtoken)) {
    Write-Host "❌ No se ingresó un authtoken" -ForegroundColor Red
    exit 1
}

# Verificar formato básico
if ($authtoken.Length -lt 30) {
    Write-Host ""
    Write-Host "⚠️  ADVERTENCIA: El authtoken parece muy corto" -ForegroundColor Yellow
    Write-Host "Los authtokens de ngrok suelen tener más de 40 caracteres" -ForegroundColor Yellow
    Write-Host ""
    $continuar = Read-Host "¿Deseas continuar de todos modos? (S/N)"
    if ($continuar -ne "S" -and $continuar -ne "s") {
        Write-Host "Operación cancelada" -ForegroundColor Yellow
        exit 0
    }
}

Write-Host ""
Write-Host "Configurando ngrok con el authtoken..." -ForegroundColor Yellow

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
    Write-Host ""
    Write-Host "Posibles causas:" -ForegroundColor Yellow
    Write-Host "1. El authtoken no es válido" -ForegroundColor White
    Write-Host "2. El authtoken ya expiró" -ForegroundColor White
    Write-Host "3. Copiaste el token incorrecto" -ForegroundColor White
    Write-Host ""
    Write-Host "Solución:" -ForegroundColor Yellow
    Write-Host "1. Ve a: https://dashboard.ngrok.com/get-started/your-authtoken" -ForegroundColor White
    Write-Host "2. Genera un nuevo authtoken si es necesario" -ForegroundColor White
    Write-Host "3. Asegúrate de copiar TODO el token (sin espacios)" -ForegroundColor White
}

