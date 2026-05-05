package dev.elide.maven.plugin

import dev.elide.maven.plugin.kotlin.ElideKotlinLifecycleParticipant

/** @author Lauri Heino <datafox> */
open class ElideLifecycleParticipant : ElideKotlinLifecycleParticipant() {
    init {
        artifact = "elide-maven-plugin"
    }
}
