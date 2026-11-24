# =====================================================================
#  Despliega el Portafolio Virtual en Azure (App Service Linux + MySQL)
#  Requisitos: Azure CLI (az), Java 17 y Maven instalados.
#  Uso (PowerShell, dentro de la carpeta del proyecto):
#      .\desplegar-azure.ps1
# =====================================================================

# ---------- CAMBIA ESTOS NOMBRES (solo minúsculas, números y guiones) ----------
$Grupo      = "rg-portafolio"          # grupo de recursos
$Ubicacion  = "chilecentral"           # si MySQL no está disponible ahí, usa: brazilsouth o eastus2
$NombreApp  = "adrian-portafolio-web"  # será  https://<NombreApp>.azurewebsites.net  (debe ser único en todo Azure)
$ServidorDb = "adrian-portafolio-db"   # será  <ServidorDb>.mysql.database.azure.com  (debe ser único)
$UsuarioDb  = "adminportafolio"
$Plan       = "plan-portafolio"
$Sku        = "B1"                     # plan de App Service (B1 = básico)
# --------------------------------------------------------------------------------

function Leer-Clave($texto) {
    $s = Read-Host $texto -AsSecureString
    [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($s))
}

$ErrorActionPreference = "Stop"
Write-Host "`n== 1/6 Iniciar sesión en Azure ==" -ForegroundColor Cyan
az login | Out-Null

Write-Host "`nContraseñas (no se muestran al escribir):" -ForegroundColor Yellow
$ClaveDb    = Leer-Clave "Contraseña para MySQL (mín. 8, con mayúscula, minúscula y número)"
$ClaveAdmin = Leer-Clave "Contraseña para entrar al cPanel de tu portafolio"

Write-Host "`n== 2/6 Compilando el proyecto (.jar) ==" -ForegroundColor Cyan
mvn clean package -DskipTests
if (-not (Test-Path "target\app.jar")) { throw "No se generó target\app.jar" }

Write-Host "`n== 3/6 Creando grupo de recursos ==" -ForegroundColor Cyan
az group create --name $Grupo --location $Ubicacion | Out-Null

Write-Host "`n== 4/6 Creando base de datos MySQL (tarda unos minutos) ==" -ForegroundColor Cyan
az mysql flexible-server create `
    --resource-group $Grupo --name $ServidorDb --location $Ubicacion `
    --admin-user $UsuarioDb --admin-password $ClaveDb `
    --sku-name Standard_B1ms --tier Burstable --storage-size 20 `
    --public-access 0.0.0.0 --yes | Out-Null

Write-Host "`n== 5/6 Creando la aplicación web (Linux + Java 17) ==" -ForegroundColor Cyan
az appservice plan create --name $Plan --resource-group $Grupo --sku $Sku --is-linux | Out-Null
az webapp create --resource-group $Grupo --plan $Plan --name $NombreApp --runtime "JAVA:17-java17" | Out-Null

az webapp config appsettings set --resource-group $Grupo --name $NombreApp --settings `
    WEBSITES_PORT=8080 `
    DB_HOST="$ServidorDb.mysql.database.azure.com" `
    DB_PORT=3306 `
    DB_USER=$UsuarioDb `
    DB_PASSWORD=$ClaveDb `
    ADMIN_EMAIL="adrianmix800@gmail.com" `
    ADMIN_PASSWORD=$ClaveAdmin `
    UPLOADS_DIR="/home/uploads" | Out-Null

Write-Host "`n== 6/6 Subiendo el .jar ==" -ForegroundColor Cyan
az webapp deploy --resource-group $Grupo --name $NombreApp --src-path "target\app.jar" --type jar

Write-Host "`nListo. Espera 1-2 minutos y abre:" -ForegroundColor Green
Write-Host "  Sitio:  https://$NombreApp.azurewebsites.net"
Write-Host "  cPanel: https://$NombreApp.azurewebsites.net/admin"
Write-Host "  Usuario: adrianmix800@gmail.com   (la contraseña es la que escribiste arriba)"
Write-Host "`nSi no abre, mira el error con:  az webapp log tail --resource-group $Grupo --name $NombreApp"
