# Script para subir el proyecto al servidor usando SCP
# Uso: .\subir-servidor.ps1

param(
    [Parameter(Mandatory=$true)]
    [string]$Servidor,
    
    [Parameter(Mandatory=$true)]
    [string]$Usuario,
    
    [Parameter(Mandatory=$false)]
    [string]$ClavePem = "",
    
    [Parameter(Mandatory=$false)]
    [string]$RutaRemota = "~/p5_form"
)

Write-Host "=== Subir Proyecto al Servidor ===" -ForegroundColor Cyan
Write-Host ""

# Verificar que existe scp (requiere OpenSSH en Windows)
$scpPath = Get-Command scp -ErrorAction SilentlyContinue

if (-not $scpPath) {
    Write-Host "ERROR: scp no esta disponible" -ForegroundColor Red
    Write-Host ""
    Write-Host "Instala OpenSSH en Windows:" -ForegroundColor Yellow
    Write-Host "  Add-WindowsCapability -Online -Name OpenSSH.Client~~~~0.0.1.0" -ForegroundColor White
    Write-Host ""
    Write-Host "O usa Git para subir el codigo:" -ForegroundColor Yellow
    Write-Host "  git clone <url-repositorio>" -ForegroundColor White
    exit 1
}

Write-Host "Servidor: $Servidor" -ForegroundColor Yellow
Write-Host "Usuario: $Usuario" -ForegroundColor Yellow
Write-Host "Ruta remota: $RutaRemota" -ForegroundColor Yellow
Write-Host ""

# Archivos a excluir
$excluir = @(
    ".git",
    "target",
    ".idea",
    "*.iml",
    ".env",
    "node_modules"
)

Write-Host "Subiendo archivos..." -ForegroundColor Yellow

if ($ClavePem -and $ClavePem -ne "") {
    # Usar clave PEM
    $comando = "scp -i `"$ClavePem`" -r . $Usuario@${Servidor}:$RutaRemota"
} else {
    # Sin clave PEM (usar autenticación por contraseña)
    $comando = "scp -r . $Usuario@${Servidor}:$RutaRemota"
}

Write-Host "Ejecutando: $comando" -ForegroundColor Gray
Write-Host ""

Invoke-Expression $comando

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "EXITO! Codigo subido al servidor" -ForegroundColor Green
    Write-Host ""
    Write-Host "Proximos pasos en el servidor:" -ForegroundColor Cyan
    Write-Host "  1. cd $RutaRemota" -ForegroundColor White
    Write-Host "  2. cp .env.example .env" -ForegroundColor White
    Write-Host "  3. nano .env  # Editar con tus valores" -ForegroundColor White
    Write-Host "  4. docker compose up -d --build" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "ERROR al subir archivos" -ForegroundColor Red
    Write-Host "Verifica:" -ForegroundColor Yellow
    Write-Host "  - Que tengas acceso SSH al servidor" -ForegroundColor White
    Write-Host "  - Que la ruta remota exista o sea accesible" -ForegroundColor White
    Write-Host "  - Que tengas permisos de escritura" -ForegroundColor White
}

