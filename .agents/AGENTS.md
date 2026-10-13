# KRONO - Contexto Global para Agentes Antigravity

- **Proyecto:** App Android multi-módulo en Kotlin con Jetpack Compose.
- **Fuentes de verdad obligatorias:**
  1. `CLAUDE.md`: Arquitectura, modularización y tokens del sistema de diseño Lumina Glass (innegociable).
  2. `docs/SPEC.md`: Requerimientos funcionales (`F-XX`) y criterios de aceptación (`CA-X`).
  3. `CONTRIBUTING.md`: Convenciones de Git y formato de commits.
- **Reglas críticas:**
  - Cero código duro de estilos (colores, fuentes o dp); todo usa `KronoTheme` y `core-ui`.
  - Cero textos quemados; todo copy va en `res/values/strings.xml` en español.
  - Ningún módulo `feature-*` importa a otro `feature-*`.
  - Los ViewModels jamás importan `androidx.compose.*`.
  - Rama base: `develop`. Todo trabajo se entrega vía Pull Request.