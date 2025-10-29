package org.rag4j.nomnom;

import com.embabel.agent.config.annotation.EnableAgents;
import com.embabel.agent.config.annotation.LoggingThemes;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main application class for the NomNom food ordering service.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"org.rag4j"})
@EnableAgents(loggingTheme = LoggingThemes.STAR_WARS)
public class NomNomApplication {

    public static void main(String[] args) {
        SpringApplication.run(NomNomApplication.class, args);
    }
}