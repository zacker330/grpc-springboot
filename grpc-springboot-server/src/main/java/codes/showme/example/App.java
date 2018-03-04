package codes.showme.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class App {
    @Bean
    public GreeterService greeterService() {
        return new GreeterService();
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class,args);
    }

}
