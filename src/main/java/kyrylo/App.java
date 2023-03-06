package kyrylo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class App {

    public static void main(String[] args) throws Exception {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(App.class);
        XesEventIngester.ingestLog("/Users/kyrylo/Downloads/12707639/sepsis.xes");
        builder.headless(false);

        ConfigurableApplicationContext context = builder.run(args);
    }

}
