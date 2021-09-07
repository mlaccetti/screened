package mlaccetti.screened;

import lombok.extern.java.Log;
import mlaccetti.screened.email.EmailResults;
import mlaccetti.screened.http.SeleniumExecutor;
import mlaccetti.screened.util.Cleanup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationTemp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Log
public class ScreenedApplication implements CommandLineRunner {
  private static final String OS = System.getProperty("os.name").toLowerCase();

  @Autowired
  private SeleniumExecutor executor;

  @Autowired
  private EmailResults emailer;

  @Autowired
  private Cleanup cleanup;

	public static void main(String[] args) {
    if (isUnix()) {
      System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
    }

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

  @Override
  public void run(String... args) throws Exception {
	  log.info("screened running - cleaning up old PDFs");
    cleanup.clean();

    log.info("Old PDFs cleaned, downloading new one.");
    executor.downloadResults();

    log.info("PDF downloaded, sending email.");
    emailer.send();
  }

  private static boolean isUnix() {
    return (OS.indexOf("nix") >= 0
            || OS.indexOf("nux") >= 0
            || OS.indexOf("aix") > 0);
}
}
