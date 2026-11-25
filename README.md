# Evaluacion — DSY1105

Aplicación Android desarrollada en Kotlin con Jetpack Compose para la **Evaluación Parcial 2** de la asignatura **Desarrollo de Aplicaciones Móviles (DSY1105)**.

## Descripción general
El proyecto representa un prototipo académico para la **gestión móvil y seguimiento de solicitudes de soporte informático**. La aplicación permite registrar usuarios de prueba, crear solicitudes, consultar un listado persistente, asociar fotografías de evidencia y utilizar un lector de códigos QR.

## Funcionalidades implementadas
- Registro de usuario con validaciones e íconos.
- Pantalla resumen posterior al registro.
- Creación de solicitudes con:
  - título,
  - descripción,
  - categoría,
  - código local tipo `SOL-0001`,
  - estado inicial `Recibida`.
- Persistencia local con **Room** para usuarios y solicitudes.
- Migraciones de base de datos `1→2` y `2→3`.
- Selección de imágenes con **Android Photo Picker**.
- Almacenamiento de evidencias en el almacenamiento privado de la aplicación.
- Visualización de fotografías en el listado de solicitudes.
- Lector QR con **CameraX** y **ML Kit**.
- Animaciones en Compose:
  - contador animado de solicitudes,
  - descripción expandible/colapsable.

## Arquitectura
El proyecto utiliza **MVVM**:

- **UI / View**: pantallas Compose (`RegistroScreen`, `ResumenScreen`, `CrearSolicitudScreen`, `ListaSolicitudesScreen`, `QrScannerScreen`).
- **ViewModel**: `UsuarioViewModel`, `SolicitudViewModel`, `QrViewModel`.
- **Model / Data**: entidades Room, DAO, base de datos, `QrRepository` y `EvidenciaStorage`.
- **Navigation**: `AppNavigation`.

## Tecnologías utilizadas
- Kotlin
- Android Studio
- Jetpack Compose + Material 3
- Room + KSP
- StateFlow y LiveData
- CameraX
- ML Kit Barcode Scanning
- Android Photo Picker
- Coroutines

## Estructura principal
```text
com.example.evaluacion
├── model
│   ├── data.local
│   │   ├── UsuarioEntity.kt
│   │   ├── UsuarioDao.kt
│   │   ├── UsuarioDatabase.kt
│   │   ├── SolicitudEntity.kt
│   │   └── SolicitudDao.kt
│   ├── repository
│   │   └── QrRepository.kt
│   ├── SolicitudError.kt
│   ├── SolicitudUiState.kt
│   ├── UsuarioErrores.kt
│   └── UsuarioUiState.kt
├── navigation
│   └── AppNavigation.kt
├── ui
│   ├── screen
│   │   ├── CrearSolicitudScreen.kt
│   │   ├── ListaSolicitudesScreen.kt
│   │   ├── RegistroScreen.kt
│   │   └── ResumenScreen.kt
│   ├── theme
│   └── view
│       └── QrScannerScreen.kt
├── utils
│   ├── CameraPermisosHelper.kt
│   ├── EvidenciaStorage.kt
│   └── QrScanner.kt
└── viewmodel
    ├── QrViewModel.kt
    ├── SolicitudViewModel.kt
    └── UsuarioViewModel.kt
```

## Ejecución del proyecto
1. Abrir el proyecto en Android Studio.
2. Sincronizar Gradle.
3. Ejecutar la app en un emulador o dispositivo Android.
4. Para probar QR, conceder permiso de cámara.
5. Para probar Photo Picker, seleccionar una imagen JPG o PNG.

## Consideraciones
- La base de datos local se llama `usuario_database`.
- Las imágenes seleccionadas se copian al almacenamiento privado de la app y se almacena su ruta en la columna `evidenciaPath`.
- Las nuevas contraseñas de usuarios no se almacenan en texto real; el registro es de carácter académico y no constituye autenticación productiva.

## Estado del proyecto
Esta entrega corresponde a un **avance funcional de la Evaluación Parcial 2**. No incluye todavía perfiles por rol, sincronización remota, asignación de técnicos ni gestión avanzada de estados.

## Autor
- Guillermo Iván Miranda Gomez
- Equipo 11
- Asignatura: Desarrollo de aplicaciones móviles
