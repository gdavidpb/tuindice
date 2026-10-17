import org.gradle.api.publish.PublishingExtension

// Publishes a shared contract module (academiccore, chatcontract) as Maven artifacts for the backend. Each module applies
// this script from its build file (see docs/shared-contracts.md). publishToMavenLocal needs no credentials; the Artifact
// Registry repository is declared only when ARTIFACT_REGISTRY_TOKEN is set, which the publishing workflow does.

val defaultContractsRepositoryUrl = "https://us-east1-maven.pkg.dev/tu-indice-usb/tuindice-libs"

apply(plugin = "maven-publish")

group = "com.gdavidpb.tuindice"
version = providers.gradleProperty("tuindice.contracts.version").get()

extensions.configure<PublishingExtension> {
	val artifactRegistryToken = System.getenv("ARTIFACT_REGISTRY_TOKEN")

	if (!artifactRegistryToken.isNullOrBlank()) {
		repositories {
			maven {
				name = "artifactRegistry"
				url = uri(
					providers.gradleProperty("tuindice.contracts.repositoryUrl")
						.orElse(defaultContractsRepositoryUrl)
						.get()
				)
				credentials {
					username = "oauth2accesstoken"
					password = artifactRegistryToken
				}
			}
		}
	}
}
