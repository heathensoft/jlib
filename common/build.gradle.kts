plugins {
    id("io.github.heathensoft.jlib.conventions")
}

dependencies {

    api(files("../libs/joml/joml-1.10.4/joml-1.10.4.jar"))
    api(files("../libs/joml/joml-primitives-1.10.0/joml-primitives-1.10.0.jar"))
    api(files("../libs/json/json-simple-1.1.1.jar"))
    api(files("../libs/tinylog-2.5.0/tinylog-api-2.5.0.jar"))
    api(files("../libs/tinylog-2.5.0/tinylog-impl-2.5.0.jar"))
}


