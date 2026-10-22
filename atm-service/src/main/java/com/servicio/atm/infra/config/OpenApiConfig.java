package com.servicio.atm.infra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de la documentacion OpenAPI (Swagger UI en /swagger-ui.html).
 */
@Configuration
public class OpenApiConfig {

    /**
     * Define la informacion general que se muestra en la documentacion de la API.
     *
     * @return descripcion base de la API
     */
    @Bean
    public OpenAPI atmOpenApi() {
        return new OpenAPI().info(new Info()
                .title("ATM Service API")
                .version("0.0.1")
                .description("Microservicio de cajero automatico: login, consulta de saldo, "
                        + "extraccion y deposito de efectivo."));
    }
}
