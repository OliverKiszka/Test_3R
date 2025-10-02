package pl.kurs.test3r;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import pl.kurs.test3r.config.ImportProperties;

@SpringBootApplication
@EntityScan(basePackages = "pl.kurs.test3r.models")
@EnableAsync
@EnableConfigurationProperties(ImportProperties.class)
public class Test3rApplication {

    public static void main(String[] args) {
        SpringApplication.run(Test3rApplication.class, args);
    }

}
