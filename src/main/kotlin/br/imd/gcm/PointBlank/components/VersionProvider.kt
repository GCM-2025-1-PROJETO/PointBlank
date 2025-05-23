package br.imd.gcm.PointBlank.components

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class VersionProvider {

    @Value("\${info.app.version}")
    lateinit var version: String
}
