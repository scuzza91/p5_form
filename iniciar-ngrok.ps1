# Script para iniciar ngrok y mostrar la URL
Write-Host "=== Iniciando ngrok ===" -ForegroundColor Cyan
Write-Host ""

# Encontrar ngrok
$ngrokPath = (Get-Command ngrok -ErrorAction SilentlyContinue).Source

if (-not $ngrokPath) {
    Write-Host "❌ ngrok no está instalado" -ForegroundColor Red
    Write-Host ""
    Write-Host "Descarga ngrok desde: https://ngrok.com/download" -ForegroundColor Yellow
    Write-Host "O instálalo con: winget install ngrok" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ ngrok encontrado en: $ngrokPath" -ForegroundColor Green
Write-Host ""
Write-Host "Iniciando túnel en el puerto 8083..." -ForegroundColor Yellow
Write-Host ""
Write-Host "⚠️  IMPORTANTE: Deja esta ventana abierta mientras uses ngrok" -ForegroundColor Yellow
Write-Host ""
Write-Host "Cuando veas la URL, cópiala y úsala en Bondarea:" -ForegroundColor Cyan
Write-Host "   https://XXXXX.ngrok.io/api/persona/crear" -ForegroundColor White
Write-Host ""

# Iniciar ngrok
& $ngrokPath http 8083

