package sestra

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SestraApplication

fun main(args: Array<String>) {
    runApplication<SestraApplication>(*args)
}
