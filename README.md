# Evaluación — Gestión de solicitudes de soporte técnico

**Proyecto Android | Desarrollo de Aplicaciones Móviles (DSY1105)**

Aplicación móvil académica desarrollada con **Kotlin y Jetpack Compose** para registrar, gestionar y dar seguimiento a solicitudes de soporte informático. Integra persistencia local mediante Room y operaciones remotas contra una API REST construida con Spring Boot y MongoDB Atlas.

## Funcionalidades

La aplicación contempla tres perfiles en el flujo remoto autenticado:

| Perfil | Operaciones principales |
| --- | --- |
| **CLIENTE** | Registro e inicio de sesión, creación y consulta de sus solicitudes, edición y eliminación de solicitudes propias, selección de categoría (**Hardware, Software o Redes**), captura de evidencia fotográfica y visualización del estado. |
| **ADMIN** | Supervisión de solicitudes, consulta de categorías y fotografías, modificación de prioridades y asignación de solicitudes a usuarios con rol técnico. |
| **TECNICO** | Consulta de solicitudes asignadas, visualización de fotografías, actualización de estado y registro/consulta del historial de avances de trabajo. |

Otras funciones presentes en el proyecto:

- Lector de códigos **QR** con CameraX y ML Kit.
- Gestión de usuarios y solicitudes **locales** con Room.
- Selección de fotografías locales mediante Android Photo Picker y almacenamiento privado de la aplicación.
- Notificaciones **locales** al registrar solicitudes; en el flujo remoto se disparan después de una respuesta satisfactoria de la API.
- Compartir información de solicitudes mediante un `Intent` de Android, con intento de abrir WhatsApp Business y alternativa con el selector de aplicaciones. La acción abre la interfaz de compartir: **no envía automáticamente mensajes**.
- Navegación entre pantallas mediante Navigation Compose y gestión de estado mediante ViewModels y StateFlow.

> **Importante:** las solicitudes locales de Room y las remotas de MongoDB son flujos de almacenamiento distintos. Los permisos del servidor se controlan mediante JWT y roles; el rol de un usuario almacenado solo en el dispositivo no otorga acceso a las rutas protegidas de la API.

## Tecnologías

- **Lenguaje:** Kotlin.
- **Interfaz:** Jetpack Compose y Material 3.
- **Arquitectura:** MVVM (pantallas, ViewModels, modelos y capa de datos).
- **Persistencia local:** Room **2.8.4**, con KSP y migraciones `1→2`, `2→3` y `3→4`.
- **Conectividad:** Retrofit **3.0.0** y converter-gson.
- **Cámara y QR:** CameraX, ML Kit Barcode Scanning y ZXing.
- **Operaciones asíncronas:** Kotlin Coroutines y StateFlow.
- **Configuración Android:** `minSdk 24`, `compileSdk 37`, `targetSdk 37`.

## Arquitectura y estructura del código

```text
app/src/main/java/
├── com/example/evaluacion/
│   ├── MainActivity.kt
│   ├── navigation/AppNavigation.kt
│   ├── model/
│   │   ├── data/local/           # Room: entidades, DAO y base de datos
│   │   └── data/remote/          # Retrofit, DTO, servicios API y sesión
│   ├── ui/
│   │   ├── screen/               # Registro, login y pantallas por rol
│   │   └── view/                 # Lector QR
│   ├── viewmodel/                # Lógica y estado de solicitudes/usuarios
│   └── utils/                    # Evidencias, QR y utilidades
└── com/example/integraciones/    # Notificaciones locales y compartir
```

**Flujo remoto:** `Pantalla Compose → ViewModel → Retrofit → API Spring Boot → MongoDB Atlas`.

**Flujo local:** `Pantalla Compose → ViewModel → DAO Room → usuario_database`.

El archivo `EvidenciaStorage.kt` incluye funciones para convertir fotografías a JPEG comprimido en Base64 y volver a decodificarlas para mostrarlas en las tarjetas de solicitudes. La cámara remota utiliza una captura de vista previa; por ello la resolución puede ser limitada.

## Requisitos y ejecución

1. Instalar Android Studio con los componentes Android SDK correspondientes al proyecto (SDK 37).
2. Clonar este repositorio y abrir su carpeta raíz en Android Studio.
3. Sincronizar Gradle y seleccionar un emulador Android con cámara disponible.
4. Ejecutar el backend del repositorio [solicitudes-api](https://github.com/Guillermo223101/solicitudes-api), con sus variables de entorno configuradas.
5. Comprobar que el backend esté escuchando en **`http://localhost:8080`** en el computador.
6. Ejecutar la aplicación desde **Run** y conceder los permisos de cámara, notificaciones y red local cuando corresponda.

La dirección del servidor está configurada en:

```kotlin
// app/src/main/java/com/example/evaluacion/model/data/remote/RetrofitClient.kt
private const val BASE_URL = "http://10.0.2.2:8080/"
```

`10.0.2.2` permite que el **emulador Android** acceda al `localhost` del computador. Para un dispositivo físico se requiere una dirección accesible desde ese dispositivo y adaptar la configuración de red de forma segura.

La configuración HTTP para desarrollo local se encuentra bajo `app/src/debug/`. **No se considera una configuración de producción**. En determinados emuladores/API recientes puede ser necesario autorizar acceso a la red local.

### Compilar

Desde PowerShell, en la raíz del proyecto:

```powershell
.\gradlew.bat :app:assembleDebug
```

También es posible usar **Build → Make Project** en Android Studio. No es necesario generar un APK firmado para ejecutar las pruebas funcionales de esta evaluación; el trabajo de empaquetado exclusivo del examen se mantiene separado.

## Persistencia y autenticación

**Modo local (Room):** mantiene usuarios y solicitudes en `usuario_database`. Se incluyen migraciones de esquema para conservar los registros al actualizar la aplicación. La fotografía local se almacena en el espacio privado de la aplicación y se registra su ruta.

**Modo remoto (MongoDB):** registro e inicio de sesión mediante la API. El servidor emite un token JWT y valida el rol para las operaciones de CLIENTE, ADMIN y TECNICO. El registro público crea cuentas de tipo CLIENTE; las cuentas administrativas y técnicas deben estar autorizadas en el backend.

Las solicitudes remotas pueden incluir:

```json
{
  "titulo": "Equipo sin conexión",
  "descripcion": "No se conecta a la red",
  "categoria": "Redes",
  "evidenciaBase64": null
}
```

La evidencia es opcional. Cuando se adjunta una fotografía, Android envía un texto Base64 y el backend comprueba su tamaño y formato. **No deben registrarse fotografías privadas o credenciales reales como datos de demostración.**

## Pruebas y verificación

Se comprobaron funcionalmente los tres perfiles en emulador Android y la persistencia de categorías, fotografías y avances técnicos en MongoDB Atlas. En el backend integrado se ejecutaron **41 pruebas automatizadas exitosas**, con cero fallos y errores.

Para una verificación manual:

1. Iniciar sesión como CLIENTE remoto, crear una solicitud con categoría y fotografía y comprobar su aparición en el listado y MongoDB.
2. Iniciar sesión como ADMIN, localizar la solicitud, consultar la fotografía, modificar la prioridad y asignarla a un técnico.
3. Iniciar sesión como TECNICO, comprobar la asignación y fotografía, actualizar el estado y registrar un comentario de avance.
4. Cerrar y reabrir el historial de avances para comprobar su recuperación desde el backend.
5. Comprobar que los registros anteriores sin fotografía o categoría continúen visibles.

**Nota sobre pruebas instrumentadas:** `app/src/androidTest/java/com/example/evaluacion/RetrofitConexionTest.kt` conserva pruebas del CRUD genérico inicial de la API. Las rutas genéricas `/api/solicitudes/**` se bloquearon posteriormente para exigir las rutas protegidas por rol. Estas pruebas requieren actualización para utilizar registro/login, JWT y endpoints actuales; no deben interpretarse como evidencia de pruebas automatizadas exitosas de la versión final.

No se ha acreditado un porcentaje de cobertura de pruebas de Android. Las 41 pruebas indicadas corresponden al **backend**, no a la aplicación móvil.

## Seguridad y límites del prototipo

- No incluir contraseñas, URI de MongoDB Atlas, tokens JWT ni archivos de firma en el repositorio.
- El almacenamiento Room y las notificaciones son **locales**; no equivalen a sincronización entre dispositivos ni a notificaciones push.
- El uso de `Intent` para compartir hacia WhatsApp Business **no constituye consumo de su API oficial**.
- La conexión HTTP a `10.0.2.2` corresponde a desarrollo en emulador; una publicación requeriría HTTPS y revisión de seguridad adicional.
- El envío de fotografía utiliza Base64 dentro de JSON y está orientado a evidencias pequeñas del prototipo.

## Repositorios y trabajo colaborativo

- **Aplicación Android:** [EvaluacionDesarrolloAppMovil](https://github.com/Guillermo223101/EvaluacionDesarrolloAppMovil)
- **API REST:** [solicitudes-api](https://github.com/Guillermo223101/solicitudes-api)
- **Integración Android revisada y aprobada:** [Pull Request #1](https://github.com/Guillermo223101/EvaluacionDesarrolloAppMovil/pull/1)
- **Integración backend revisada y aprobada:** [Pull Request #1](https://github.com/Guillermo223101/solicitudes-api/pull/1)

**Equipo:** Guillermo Miranda y Matías (`Matito15`). La implementación incluye componentes desarrollados de manera colaborativa, revisión entre integrantes y fusión de ramas mediante Pull Requests.

**Alcance:** entrega académica de desarrollo de aplicaciones móviles. Los componentes preparados exclusivamente para el examen de APK se mantienen fuera de esta versión.
