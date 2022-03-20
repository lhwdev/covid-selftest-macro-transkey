plugins {
	kotlin("jvm")
	kotlin("plugin.serialization")
}

dependencies {
	implementation(projects.apiBase)
	
	implementation(libs.coroutinesCore)
	implementation(libs.serializationCore)
	implementation(libs.serializationJson)
	testImplementation(kotlin("test"))
}
