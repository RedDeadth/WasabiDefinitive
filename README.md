# GuardBox64 (WasabiDefinitive) 🔒

Una aplicación nativa de Android moderna construida con **Kotlin**, **Jetpack Compose** y **Firebase** para la gestión remota e inteligente de casilleros (smart lockers) institucionales o universitarios.

## 🚀 Características Principales
*   **Gestión en Tiempo Real**: Visualización inmediata del estado de ocupación y apertura de los casilleros gracias a Firebase Realtime Database.
*   **Autenticación Segura**: Integración de inicio de sesión por correo tradicional y acceso rápido vía **Google Sign-In**.
*   **Sistema de Reservas**: Asignación de tiempo (1h, 2h, 5h, 12h, etc.) calculando precio transparente al instante.
*   **Control de Acceso Compartido**: Capacidad de compartir llave digital temporizada con otros correos electrónicos autorizados en forma segura.
*   **Cuenta Regresiva y Finalización Segura**: Proceso paso a paso para retirar pertenencias sin cortar tiempo abruptamente.

---

## 📸 Demostración Visual de la Interfaz

| 1. Autenticación (Login) | 2. Lista de Casilleros |
| :---: | :---: |
| <img src="docs/LOGIN.jpg" width="250" /> | <img src="docs/LIST.jpg" width="250" /><br><img src="docs/LIST_WITH_OWN.jpg" width="250" /> |

| 3. Detalle de Casillero | 4. Tarificación y Reserva |
| :---: | :---: |
| <img src="docs/DETAIL.jpg" width="250" /> | <img src="docs/TIME.jpg" width="250" /> |

| 5. Modos Extendidos | 6. Finalización Segura |
| :---: | :---: |
| <img src="docs/OWN.jpg" width="250" /> | <img src="docs/TIMEREND.jpg" width="250" /> |

---

## 🛠 Entorno Técnico y Arquitectura
- **Kotlin 1.9** + SDK de Android 34
- **Jetpack Compose**: Diseño declarativo completo, sin soporte heredado de layouts XML (Excepto el manifest base).
- **MVVM**: Separation of Concerns en Modelo, Vista e Interacciones del Usuario (ViewModel de estado y lógica de negocio).
- **Firebase Authentication**: Credenciales federadas (Google) e Identity Providers.
- **Firebase Realtime Database**: Websockets nativos asíncronos para el motor de bases de datos NoSQL y reactividad.

## 📦 Instalación y Despliegue (Local)

1. Clonar este repositorio.
2. Crear un nuevo proyecto en **Firebase Console**.
3. Activar los servicios: Authentication (Activando Correo y Google) y Realtime Database (En Test Mode para desarrollo).
4. Generar la firma `SHA-1` de Android Studio y registrar la App en Firebase.
5. Descargar el archivo `google-services.json` y colocarlo dentro de la ruta `app/google-services.json`.
6. Compilar el proyecto usando Android Studio o mediante el Gradle local:
   ```bash
   ./gradlew assembleDebug
   ```
7. El archivo final `.apk` se ubicará en `app/build/outputs/apk/debug/`.

---

## 📂 Estructura de Carpetas Clave

```text
WasabiDefinitive/
├── app/
│   ├── src/main/java/com/example/guardbox64/
│   │   ├── model/         # Clases de datos (Locker, Repository)
│   │   ├── navigator/     # Control de rutas (NavGraph)
│   │   ├── ui/
│   │   │   ├── screens/   # Vistas de Jetpack Compose (Login, Details)
│   │   │   └── viewmodel/ # Manejo de estados y lógica Firebase
│   │   └── utils/         # Utilidades secundarias
│   ├── google-services.json.example # Archivo base de variables de entorno
│   └── build.gradle.kts   # Configuración modular
├── docs/                  # Documentación y recursos gráficos
└── build.gradle.kts       # Configuración raíz de dependencias
```
