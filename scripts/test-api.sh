#!/bin/bash

# Script para probar el endpoint de creación de persona desde API
# Uso: ./test-api.sh

API_URL="http://localhost:8083/api/persona/crear"

echo "=== Prueba del Endpoint de API ==="
echo "URL: $API_URL"
echo ""

# Datos de prueba
JSON_DATA='{
  "nombre": "Juan",
  "apellido": "Pérez",
  "Documento": "12345678",
  "email": "juan.perez.test@example.com"
}'

echo "Enviando datos:"
echo "$JSON_DATA" | jq .
echo ""

# Realizar la petición
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d "$JSON_DATA")

# Separar respuesta y código HTTP
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

echo "=== Respuesta ==="
echo "Código HTTP: $HTTP_CODE"
echo "Body:"
echo "$BODY" | jq . 2>/dev/null || echo "$BODY"
echo ""

# Verificar resultado
if [ "$HTTP_CODE" -eq 200 ]; then
    echo "✅ Éxito! Persona y examen creados."
    EXAMEN_ID=$(echo "$BODY" | jq -r '.examenId' 2>/dev/null)
    if [ "$EXAMEN_ID" != "null" ] && [ -n "$EXAMEN_ID" ]; then
        echo "🔗 Puedes acceder al examen en: http://localhost:8083/examen/$EXAMEN_ID"
    fi
else
    echo "❌ Error en la petición"
fi

