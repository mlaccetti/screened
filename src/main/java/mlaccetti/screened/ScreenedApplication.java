package mlaccetti.screened;

import lombok.extern.java.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationTemp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
public class ScreenedApplication {
	public static void main(String[] args) {
		SpringApplication.run(ScreenedApplication.class, args);
	}

	@Configuration
  @Log
	public static class AppConfig {
	  public AppConfig() {
	    log.info("Created AppConfig");
    }

    @Bean
	  public ApplicationTemp appTemp() {
	    return new ApplicationTemp();
    }
  }
}
