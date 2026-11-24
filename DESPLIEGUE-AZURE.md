# Publicar en Azure

## Opción rápida (script)
1. Instala **Azure CLI**, **Java 17** y **Maven**.
2. Abre `desplegar-azure.ps1` y cambia `$NombreApp` y `$ServidorDb` por nombres únicos.
3. En PowerShell, dentro de esta carpeta: `.\desplegar-azure.ps1`
4. Abre `https://<NombreApp>.azurewebsites.net`

## Opción manual (Portal de Azure)
1. `mvn clean package -DskipTests` → genera `target/app.jar`.
2. Crea un **App Service**: Código · **Java 17** · **Java SE** · **Linux**.
3. Crea **Azure Database for MySQL – Flexible Server** y permite el acceso desde servicios de Azure.
4. En el App Service → Variables de entorno agrega:
   `WEBSITES_PORT=8080`, `DB_HOST` (servidor completo), `DB_PORT=3306`, `DB_USER`, `DB_PASSWORD`,
   `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `UPLOADS_DIR=/home/uploads`.
5. Sube el jar: `az webapp deploy --resource-group GRUPO --name APP --src-path target/app.jar --type jar`

## Si no abre
`az webapp log tail --resource-group GRUPO --name APP` muestra el error de Spring Boot.
