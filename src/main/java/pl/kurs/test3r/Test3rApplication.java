package pl.kurs.test3r;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "pl.kurs.test3r.models")
public class Test3rApplication {

    public static void main(String[] args) {
        SpringApplication.run(Test3rApplication.class, args);
    }

}
