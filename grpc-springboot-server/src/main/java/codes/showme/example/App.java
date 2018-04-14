package codes.showme.example;

import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnablePrometheusEndpoint
public class App {
    @Bean
    public GreeterService greeterService() {
        return new GreeterService();
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class,args);
    }

}
