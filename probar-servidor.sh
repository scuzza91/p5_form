#!/bin/bash

# Script para probar el endpoint desde el SERVIDOR
# Uso: ./probar-servidor.sh

API_URL="http://localhost:8083/api/persona/crear"
API_TOKEN="41855ad220d5c0f4fb39ea6b2ed8d56e"

echo "=== Prueba del Endpoint en el SERVIDOR ==="
echo ""
echo "URL: $API_URL"
echo ""

# Generar timestamp único
TIMESTAMP=$(date +%Y%m%d%H%M%S)

# Datos de prueba (formato Bondarea)
JSON_DATA=$(cat <<EOF
{
  "idStage": "B26F5HRR",
  "custom_B26FNN8U": "Juan",
  "custom_B26FNN83": "Perez",
  "custom_B26FNHKS": "12345678",
  "custom_B26FNN87": "juan.perez.test.${TIMESTAMP}@example.com",
  "custom_B26FNN8P": "valor_adicional"
}
EOF
)

echo "Datos de prueba:"
echo "$JSON_DATA" | jq . 2>/dev/null || echo "$JSON_DATA"
echo ""

# Realizar la petición
echo "Enviando petición POST..."
echo ""

RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -H "X-API-Token: $API_TOKEN" \
  -d "$JSON_DATA")

# Separar respuesta y código HTTP
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

echo "=== Respuesta ==="
echo "Código HTTP: $HTTP_CODE"
echo ""
echo "Body:"
echo "$BODY" | jq . 2>/dev/null || echo "$BODY"
echo ""

# Verificar resultado
if [ "$HTTP_CODE" -eq 200 ]; then
    echo "✅ ÉXITO! Persona y examen creados."
    EXAMEN_ID=$(echo "$BODY" | jq -r '.examenId' 2>/dev/null)
    PERSONA_ID=$(echo "$BODY" | jq -r '.personaId' 2>/dev/null)
    if [ "$EXAMEN_ID" != "null" ] && [ -n "$EXAMEN_ID" ]; then
        echo ""
        echo "🔗 Puedes acceder al examen en:"
        echo "   http://localhost:8083/examen/$EXAMEN_ID"
        echo ""
        echo "Persona ID: $PERSONA_ID"
        echo "Examen ID: $EXAMEN_ID"
    fi
elif [ "$HTTP_CODE" -eq 401 ]; then
    echo "❌ ERROR 401: Token de API inválido o no configurado"
    echo ""
    echo "Solución:"
    echo "  1. Accede a: http://localhost:8083/configuracion"
    echo "  2. Ingresa el token: $API_TOKEN"
    echo "  3. Guarda la configuración"
elif [ "$HTTP_CODE" -eq 403 ]; then
    echo "❌ ERROR 403: Inscripciones cerradas"
    echo ""
    echo "Solución:"
    echo "  1. Accede a: http://localhost:8083/configuracion"
    echo "  2. Activa las inscripciones"
    echo "  3. Guarda la configuración"
else
    echo "❌ Error en la petición (Código: $HTTP_CODE)"
fi

