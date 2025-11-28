# 🔧 Solución: Error de Autenticación de ngrok

## ❌ Error
```
ERROR: authentication failed: Usage of ngrok requires a verified account and authtoken.
```

## ✅ Solución

ngrok requiere una cuenta gratuita y un authtoken para funcionar.

---

## 📋 Pasos para Configurar ngrok

### Paso 1: Crear cuenta en ngrok

1. Ve a: https://dashboard.ngrok.com/signup
2. Crea una cuenta gratuita (o inicia sesión si ya tienes una)
3. Es completamente **gratis** y solo toma 1 minuto

### Paso 2: Obtener tu authtoken

1. Después de crear la cuenta, ve a:
   ```
   https://dashboard.ngrok.com/get-started/your-authtoken
   ```
2. Verás tu **authtoken** en la sección "Your Authtoken"
3. **IMPORTANTE:** El authtoken debe ser:
   - Muy largo (más de 40 caracteres)
   - Formato: `2abc123def456ghi789jkl012mno345pqr678...`
   - NO es un hash MD5 corto
4. **Copia TODO el authtoken** (usa el botón "Copy" si está disponible)
5. Asegúrate de copiarlo completo, sin espacios al inicio o final

### Paso 3: Configurar ngrok en tu computadora

**Opción A: Usando el script automático**

```powershell
.\configurar-ngrok.ps1
```

Sigue las instrucciones y pega tu authtoken cuando te lo pida.

**Opción B: Manualmente**

```powershell
ngrok config add-authtoken TU_AUTHTOKEN_AQUI
```

(Reemplaza `TU_AUTHTOKEN_AQUI` con el authtoken que copiaste)

### Paso 4: Verificar que funcione

```powershell
ngrok http 8083
```

Deberías ver algo como:

```
Session Status                online
Account                       tu-email@example.com
Forwarding                    https://abc123.ngrok.io -> http://localhost:8083
```

**¡Listo!** Ya puedes usar la URL `https://abc123.ngrok.io` en Bondarea.

---

## 🎯 Resumen Rápido

1. ✅ Crear cuenta en: https://dashboard.ngrok.com/signup
2. ✅ Obtener authtoken en: https://dashboard.ngrok.com/get-started/your-authtoken
3. ✅ Configurar: `ngrok config add-authtoken TU_TOKEN`
4. ✅ Iniciar: `ngrok http 8083`
5. ✅ Copiar la URL HTTPS y usarla en Bondarea

---

## 💡 Notas Importantes

- **Es gratis:** La cuenta básica de ngrok es completamente gratuita
- **Solo una vez:** Solo necesitas configurar el authtoken una vez
- **URL temporal:** La URL de ngrok cambia cada vez que lo reinicias (a menos que tengas cuenta paga)
- **Mantén ngrok corriendo:** Deja la ventana de ngrok abierta mientras pruebes la integración

---

## ❓ ¿Problemas?

### Error: "The authtoken you specified does not look like a proper ngrok authtoken"

**Causa:** El token que copiaste no es el authtoken correcto.

**Solución:**
1. Ve a: https://dashboard.ngrok.com/get-started/your-authtoken
2. Busca la sección **"Your Authtoken"** (no otros tokens o hashes)
3. El authtoken debe ser:
   - Muy largo (más de 40 caracteres)
   - NO es un hash MD5 corto como `41855ad220d5c0f4fb39ea6b2ed8d56e`
   - Formato: `2abc123def456ghi789jkl012mno345pqr678...`
4. Usa el botón **"Copy"** si está disponible
5. Asegúrate de copiar TODO el token, sin espacios

### El authtoken no funciona

- Verifica que lo copiaste completo (sin espacios)
- Asegúrate de estar en la página correcta de ngrok
- Intenta generar un nuevo authtoken
- Verifica que el token tenga más de 40 caracteres

### No puedo crear cuenta

- Usa un email válido
- Verifica tu email después de registrarte
- Si ya tienes cuenta, solo inicia sesión

### ngrok sigue dando error

- Cierra y vuelve a abrir la terminal
- Verifica que el authtoken esté configurado: `ngrok config check`
- Reinstala ngrok si es necesario

