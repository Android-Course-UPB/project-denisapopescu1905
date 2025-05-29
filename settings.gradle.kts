pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
        maven{
            url=uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication.create<BasicAuthentication>("basic")
            credentials {
                username = "mapbox"
                password = "sk.eyJ1IjoiZGVuaXNhcHBzcSIsImEiOiJjbHYweGVweHgwMTFmMndyN3c2b2YwNWFnIn0.9ukC1zFKOhO3h0IJobOMVA"
            }
        }
    }
}
dependencyResolutionManagement {
    //repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven{
            url=uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication.create<BasicAuthentication>("basic")
            credentials {
                username = "mapbox"
                password = "sk.eyJ1IjoiZGVuaXNhcHBzcSIsImEiOiJjbHYweGVweHgwMTFmMndyN3c2b2YwNWFnIn0.9ukC1zFKOhO3h0IJobOMVA"
            }
        }
    }
}

rootProject.name = "BLEScan"
include(":app")
