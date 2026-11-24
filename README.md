# Portafolio Virtual (Java + Spring Boot + MySQL)

Dos partes en una sola aplicación:

- **Sitio público** → http://localhost:8080
  Perfil con foto, 16 semanas (cada una con sus trabajos descargables), Proyectos, Habilidades y formulario de Contacto.
- **cPanel de administración** → http://localhost:8080/admin  (requiere login)
  Editar semanas, subir documentos PDF/Word/Excel a una semana (o dejarlos como borrador),
  agregar proyectos con imagen, leer los mensajes de contacto, cambiar la foto de inicio y los textos del perfil, y cambiar la contraseña.

## Acceso al cPanel
- Usuario: `adrianmix800@gmail.com`
- Contraseña inicial: `adrian999.net`

La contraseña se guarda cifrada (BCrypt) en la tabla `usuarios`. Cámbiala desde **Cuenta** dentro del panel.
El usuario inicial solo se crea si la tabla `usuarios` está vacía. Para otro correo/clave inicial usa
las variables `ADMIN_EMAIL` y `ADMIN_PASSWORD` antes del primer arranque.

## Requisitos
- Java 17+, Maven 3.8+, MySQL 8+ (o XAMPP/MariaDB) encendido

## Base de datos
Nombre: **portavirtual**. Se crea sola al iniciar. Usuario/clave de MySQL por defecto: `root` / (vacía).
Cámbialos con `DB_USER` y `DB_PASSWORD` o en `src/main/resources/application.properties`.
Si ya tienes la base creada, la app la usa tal cual y agrega lo que falte.

## Ejecutar
    mvn spring-boot:run

Los archivos subidos se guardan en la carpeta `uploads/`; los datos en MySQL.

## Publicar en Azure
Ver `DESPLIEGUE-AZURE.md` y el script `desplegar-azure.ps1`.
