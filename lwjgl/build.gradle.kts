plugins {
    id("io.github.heathensoft.jlib.conventions")
}


repositories {
    //mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(files("../libs/lwjgl-3.3.1/lwjgl.jar"))
    implementation(files("../libs/lwjgl-3.3.1/lwjgl-glfw.jar"))
    implementation(files("../libs/lwjgl-3.3.1/lwjgl-opengl.jar"))
    implementation(files("../libs/lwjgl-3.3.1/lwjgl-stb.jar"))
    implementation(files("../libs/tinylog-2.5.0/tinylog-api-2.5.0.jar"))
    implementation(files("../libs/tinylog-2.5.0/tinylog-impl-2.5.0.jar"))
    implementation(files("../libs/joml/joml-1.10.4/joml-1.10.4.jar"))
    implementation(files("../libs/joml/joml-primitives-1.10.0/joml-primitives-1.10.0.jar"))
}
