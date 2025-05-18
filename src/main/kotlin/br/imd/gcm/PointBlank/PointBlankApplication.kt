package br.imd.gcm.PointBlank

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class PointBlankApplication

fun main(args: Array<String>) {
	runApplication<PointBlankApplication>(*args)
}
