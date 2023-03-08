package kyrylo;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class App {
    public static void main(String[] args) throws Exception {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(App.class);
        XesEventIngester.ingestLog(args[0]);
        builder.headless(false);

        ConfigurableApplicationContext context = builder.run(args);
    }
}
