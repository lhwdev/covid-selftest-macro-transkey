plugins {
	kotlin("jvm")
	kotlin("plugin.serialization")
}

dependencies {
	implementation(project(":api-base"))
	
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.0")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")
	testImplementation(kotlin("test"))
}
