# TiendaStore

App móvil en Kotlin/Compose + backend Spring Boot.

<<<<<<< HEAD
## Arquitectura
- App: MVVM, Retrofit, Compose, StateFlow. Productos y auth contra backend; API externa (FakeStore) en Home.
- Backend: Spring Boot 3, JPA/H2, Security + JWT, entidades Product y User.
=======
Tecnologías y Recursos Implementados
-------------------
- Lenguaje: Kotlin
- Interfaz de Usuario: Jetpack Compose 
- Arquitectura: MVVM (Model-View-ViewModel)
- Base de Datos: Room (sobre SQLite) para persistencia local.
- Gestión de Estado: StateFlow para comunicación reactiva entre UI y ViewModels.

Arquitectura (MVVM)
-------------------
- `data/`: Room (AppDatabase, DAOs, Entities) + helper de imágenes.
- `domain/validation`: validaciones desacopladas (`AuthValidator`, `ProductValidator`).
- `viewmodel/`: estado con `StateFlow` (Auth, Product, Cart).
- `ui/`: pantallas Compose, navegación por estado (enum `Screen`) y componentes.
>>>>>>> e2a807cfb0e72d451956d94bdd2b34d6b1c48494

## Endpoints backend
- Auth: `POST /api/auth/register`, `POST /api/auth/login`
- Productos: `GET /api/products`, `GET /api/products/{id}` (público), `POST|PUT|DELETE /api/products/{id}` (requiere JWT).
- Swagger: `/swagger-ui.html`
- H2: `/h2-console` (JDBC `jdbc:h2:mem:tiendastore`)

## API externa
- FakeStore `https://fakestoreapi.com/products?limit=4`, visible en Home como “Novedades”.

## Ejecución backend
```bash
cd tiendastore-service
./gradlew bootRun
```

## Ejecución app
- BASE_URL en `BuildConfig` (emulador: `http://10.0.2.2:8080/`).
- Permitir HTTP claro via `network_security_config`.
- Correr desde Android Studio (emulador o dispositivo en la misma red).

## APK firmado
- `app/build.gradle.kts` incluye `signingConfig` release (ajusta ruta y contraseñas de tu .jks).
- Generar: Android Studio > Build > Generate Signed Bundle / APK.

## Credenciales seed
- admin@local / admin123 (admin)
- user@local / user12345 (usuario)

## Pruebas
```bash
cd TiendaStore
./gradlew test
```

## Flujos
- Login/Registro contra backend (JWT).
- CRUD productos (lista pública; crear/editar/eliminar con token).
- Carrito local; checkout limpia carrito.
- Novedades desde API externa en Home.
