# Script para verificar y solucionar problemas de conexión a PostgreSQL
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Verificación de PostgreSQL" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Paso 1: Verificar si PostgreSQL está instalado
Write-Host "1. Verificando instalación de PostgreSQL..." -ForegroundColor Yellow
$psqlPath = Get-Command psql -ErrorAction SilentlyContinue
if (-not $psqlPath) {
    Write-Host "   ❌ PostgreSQL no está en el PATH" -ForegroundColor Red
    Write-Host "   💡 Instala PostgreSQL desde: https://www.postgresql.org/download/windows/" -ForegroundColor Yellow
    Write-Host ""
    exit 1
} else {
    Write-Host "   ✅ PostgreSQL encontrado: $($psqlPath.Source)" -ForegroundColor Green
}

# Paso 2: Verificar si el servicio está corriendo
Write-Host ""
Write-Host "2. Verificando servicios de PostgreSQL..." -ForegroundColor Yellow
$postgresServices = Get-Service -Name postgresql* -ErrorAction SilentlyContinue
if ($postgresServices) {
    foreach ($service in $postgresServices) {
        if ($service.Status -eq 'Running') {
            Write-Host "   ✅ Servicio $($service.Name) está corriendo" -ForegroundColor Green
        } else {
            Write-Host "   ⚠️  Servicio $($service.Name) está detenido" -ForegroundColor Yellow
            Write-Host "   💡 Iniciando servicio..." -ForegroundColor Yellow
            try {
                Start-Service -Name $service.Name
                Write-Host "   ✅ Servicio iniciado correctamente" -ForegroundColor Green
            } catch {
                Write-Host "   ❌ Error al iniciar servicio: $_" -ForegroundColor Red
            }
        }
    }
} else {
    Write-Host "   ⚠️  No se encontraron servicios de PostgreSQL" -ForegroundColor Yellow
    Write-Host "   💡 PostgreSQL puede estar corriendo como proceso" -ForegroundColor Yellow
}

# Paso 3: Verificar conexión
Write-Host ""
Write-Host "3. Verificando conexión a PostgreSQL..." -ForegroundColor Yellow
Write-Host "   Ingresa las credenciales de PostgreSQL:" -ForegroundColor Cyan

$dbUser = Read-Host "   Usuario (default: postgres)"
if ([string]::IsNullOrWhiteSpace($dbUser)) {
    $dbUser = "postgres"
}

$dbPassword = Read-Host "   Contraseña" -AsSecureString
$dbPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($dbPassword)
)

# Intentar conexión
Write-Host ""
Write-Host "   Probando conexión..." -ForegroundColor Yellow

$env:PGPASSWORD = $dbPasswordPlain
$testConnection = psql -U $dbUser -h localhost -d postgres -c "SELECT version();" 2>&1
$env:PGPASSWORD = ""

if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✅ Conexión exitosa!" -ForegroundColor Green
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  Solución" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Actualiza el archivo: src/main/resources/application.properties" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Cambia la línea:" -ForegroundColor White
    Write-Host "  spring.datasource.password=\${DB_PASSWORD:Francisco.91}" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Por una de estas opciones:" -ForegroundColor White
    Write-Host ""
    Write-Host "Opción 1 - Contraseña directa:" -ForegroundColor Cyan
    Write-Host "  spring.datasource.password=TU_CONTRASEÑA_AQUI" -ForegroundColor Green
    Write-Host ""
    Write-Host "Opción 2 - Variable de entorno (más seguro):" -ForegroundColor Cyan
    Write-Host "  spring.datasource.password=\${DB_PASSWORD:TU_CONTRASEÑA_AQUI}" -ForegroundColor Green
    Write-Host ""
    Write-Host "Y luego ejecuta:" -ForegroundColor Yellow
    Write-Host "  `$env:DB_PASSWORD='TU_CONTRASEÑA'" -ForegroundColor Green
    Write-Host "  ./mvnw spring-boot:run" -ForegroundColor Green
    Write-Host ""
} else {
    Write-Host "   ❌ Error de conexión" -ForegroundColor Red
    Write-Host "   Detalles del error:" -ForegroundColor Yellow
    Write-Host $testConnection -ForegroundColor Red
    Write-Host ""
    Write-Host "Posibles causas:" -ForegroundColor Yellow
    Write-Host "  1. La contraseña es incorrecta" -ForegroundColor White
    Write-Host "  2. PostgreSQL no está corriendo" -ForegroundColor White
    Write-Host "  3. El usuario no existe" -ForegroundColor White
    Write-Host "  4. El puerto 5432 está bloqueado" -ForegroundColor White
    Write-Host ""
    Write-Host "Soluciones:" -ForegroundColor Yellow
    Write-Host "  1. Verifica la contraseña de PostgreSQL" -ForegroundColor White
    Write-Host "  2. Inicia el servicio de PostgreSQL" -ForegroundColor White
    Write-Host "  3. Usa pgAdmin para cambiar la contraseña si es necesario" -ForegroundColor White
    Write-Host ""
}

Write-Host "Presiona cualquier tecla para continuar..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

