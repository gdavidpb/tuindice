# Contratos compartidos

`academiccore` y `chatcontract` son contratos que la app y el backend comparten. La app los usa como módulos del
proyecto KMP; el backend (`tuindice-api`) los consume como artefactos Maven de la JVM.

## Qué se publica

| Módulo | Qué contiene | Artefacto para el backend |
|---|---|---|
| `academiccore` | Motores académicos puros que comparten la app y el backend: escala oficial de notas, validez de la memoria del asistente y el resto de los motores del dominio. | `com.gdavidpb.tuindice:academiccore-jvm` |
| `chatcontract` | Contrato del asistente académico: eventos, partes del mensaje, componentes, intenciones y contexto de pantalla. | `com.gdavidpb.tuindice:chatcontract-jvm` |

Gradle publica también las variantes Android y Apple de cada módulo. El backend solo consume las de la JVM.

## Versionado

- La versión está en la propiedad `tuindice.contracts.version` de `gradle.properties`, con SemVer.
- Un lanzamiento se publica con el tag `contracts-vX.Y.Z`, que debe coincidir con esa propiedad. El workflow se niega
  a publicar si no coinciden.
- No se vuelve a publicar una versión ya publicada: para cambiar un contrato, sube `tuindice.contracts.version` antes.
- Un cambio incompatible (nombres, firmas, `@SerialName`, valores que el backend persiste) sube la versión mayor.

## Publicación

- Workflow: `.github/workflows/publish-shared-contracts.yml`. Se ejecuta con un tag `contracts-vX.Y.Z` o a mano con
  `workflow_dispatch`; en ese caso publica la versión que declara `gradle.properties` del commit elegido.
- Repositorio: Artifact Registry `tuindice-libs`, región `us-east1`, proyecto `tu-indice-usb`:
  `https://us-east1-maven.pkg.dev/tu-indice-usb/tuindice-libs`. Se puede cambiar con
  `-Ptuindice.contracts.repositoryUrl=<url>`.
- Autenticación: Workload Identity Federation con la cuenta de servicio del secreto
  `GCP_CONTRACTS_PUBLISHER_SERVICE_ACCOUNT`. El token se obtiene con `gcloud` y vive en la variable
  `ARTIFACT_REGISTRY_TOKEN` solo durante el paso de publicación.
- Sin `ARTIFACT_REGISTRY_TOKEN` el repositorio remoto no se declara, así que `publishToMavenLocal` funciona sin
  credenciales.
- Los módulos aplican `gradle/shared-contract-publishing.gradle.kts`, que define el grupo, la versión y el repositorio.

## Permisos IAM

| Quién | Rol | Sobre |
|---|---|---|
| Cuenta de servicio de publicación del CI de la app (`GCP_CONTRACTS_PUBLISHER_SERVICE_ACCOUNT`) | `roles/artifactregistry.writer` | Repositorio `tuindice-libs` |
| CI y deployer del backend (`tuindice-api`) | `roles/artifactregistry.reader` | Repositorio `tuindice-libs` |

## Probar en local

```sh
./gradlew :academiccore:publishToMavenLocal :chatcontract:publishToMavenLocal
```

Los artefactos quedan en `~/.m2/repository/com/gdavidpb/tuindice/`. El backend los consume con `mavenLocal()`, declarado
solo cuando se compila con `-PuseLocalContracts`, para probar un cambio de contrato antes de publicarlo.
