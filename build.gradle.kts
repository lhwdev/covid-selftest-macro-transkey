plugins {
	kotlin("jvm")
	id("kotlinx-serialization")
}

dependencies {
	implementation(project(":api-base"))
	
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.2.2")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
	testImplementation(kotlin("test"))
}
