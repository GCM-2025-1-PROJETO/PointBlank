package br.imd.gcm.PointBlank.configs

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig(
    @Value("\${info.app.version}") private val appVersion: String
) {
    @Bean
    fun api(): OpenAPI {
        return OpenAPI().info(
            Info()
                .title("PointBlank API")
                .description("API para gerenciamento de contas")
                .version(appVersion)
        )
    }
}
