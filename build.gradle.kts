import org.jetbrains.changelog.Changelog
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
  java
  // See bundled version: https://plugins.jetbrains.com/docs/intellij/using-kotlin.html#kotlin-standard-library
  kotlin("jvm") version "1.9.10"
  id("org.jetbrains.intellij") version "1.16.1"
  id("org.jetbrains.changelog") version "2.2.0"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

repositories {
  mavenLocal()
  mavenCentral()
}

dependencies {
  implementation("com.fasterxml.uuid:java-uuid-generator:4.1.0") {
    exclude(group = "org.slf4j", module = "slf4j-api")
  }
  implementation("com.jayway.jsonpath:json-path:2.8.0") {
    exclude(group = "org.slf4j", module = "slf4j-api")
  }
  implementation("com.auth0:java-jwt:4.3.0")
  val jacksonVersion = "2.15.0"
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jacksonVersion")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-properties:$jacksonVersion")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-toml:$jacksonVersion")
  implementation("com.github.tony19:named-regexp:0.2.8")
  implementation("org.apache.commons:commons-text:1.10.0")
  implementation("commons-codec:commons-codec:1.15")
  val textCaseConverterVersion = "1.0.2"
  implementation("dev.turingcomplete:text-case-converter:$textCaseConverterVersion")
  implementation("dev.turingcomplete:text-case-converter-kotlin-extension:$textCaseConverterVersion")
  implementation("com.github.vertical-blank:sql-formatter:2.0.3")
  implementation(kotlin("stdlib"))
  implementation("com.networknt:json-schema-validator:1.0.79") {
    exclude("org.apache.commons", "commons-lang3")
    exclude(group = "org.slf4j", module = "slf4j-api")
  }
  val zxing = "3.5.1"
  implementation("com.google.zxing:core:$zxing")
  implementation("com.google.zxing:javase:$zxing")
  implementation("com.aventrix.jnanoid:jnanoid:2.0.0")
  implementation("com.github.f4b6a3:ulid-creator:5.2.3")

  testImplementation("org.assertj:assertj-core:3.24.2")
  testImplementation("org.xmlunit:xmlunit-assertj:2.9.1")
  testImplementation("org.skyscreamer:jsonassert:1.5.0")
  val junitVersion = "5.10.0"
  testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
  testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

intellij {
  version.set(properties("platformVersion"))
  type.set(properties("platformType"))
  downloadSources.set(properties("platformDownloadSources").toBoolean())
  updateSinceUntilBuild.set(true)
  plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}

changelog {
  val projectVersion = project.version as String
  version.set(projectVersion)
  header.set("[$projectVersion] - ${org.jetbrains.changelog.date()}")
  groups.set(listOf("Added", "Changed", "Removed", "Fixed"))
}

tasks {
  patchPluginXml {
    version.set(properties("pluginVersion"))
    sinceBuild.set(properties("pluginSinceBuild"))
    untilBuild.set(properties("pluginUntilBuild"))
    changeNotes.set(provider { changelog.renderItem(changelog.get(project.version as String), Changelog.OutputType.HTML) })
  }

  runPluginVerifier {
    ideVersions.set(properties("pluginVerifierIdeVersions").split(",").map(String::trim).filter(String::isNotEmpty))
  }

  publishPlugin {
    dependsOn("patchChangelog")
    token.set(project.provider { properties("jetbrains.marketplace.token") })
    channels.set(listOf(properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()))
  }

  signPlugin {
    val jetbrainsDir = File(System.getProperty("user.home"), ".jetbrains")
    certificateChain.set(project.provider { File(jetbrainsDir, "plugin-sign-chain.crt").readText() })
    privateKey.set(project.provider { File(jetbrainsDir, "plugin-sign-private-key.pem").readText() })

    password.set(project.provider { properties("jetbrains.sign-plugin.password") })
  }

  withType<KotlinCompile> {
    kotlinOptions {
      freeCompilerArgs = listOf("-Xjsr305=strict")
      jvmTarget = "17"
    }
  }

  withType<Test> {
    useJUnitPlatform()
  }
}