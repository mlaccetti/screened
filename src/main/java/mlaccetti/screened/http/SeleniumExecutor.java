package mlaccetti.screened.http;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.java.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationTemp;
import org.springframework.stereotype.Component;

import java.nio.file.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Log
public class SeleniumExecutor {
  @Autowired
  private ApplicationTemp appTemp;

  public void downloadResults() throws Exception {
    WebDriverManager.edgedriver().setup();

    final String downloadFilepath = appTemp.getDir().getAbsolutePath();
    final Map<String, Object> edgePrefs = new HashMap<>();
    edgePrefs.put("profile.default_content_settings.popups", 0);
    edgePrefs.put("download.default_directory", downloadFilepath);

    final EdgeOptions options = new EdgeOptions();
    options.setExperimentalOption("prefs", edgePrefs);
    options.addArguments("--headless", "--disable-gpu", "--disable-crash-reporter");

    final WebDriver driver = new EdgeDriver(options);
    final WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

    driver.get("https://covid-19.ontario.ca/school-screening/");

    final WebElement startScreeningButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Start school screening')]")));
    log.info("Clicking 'start school screening' button.");
    startScreeningButton.click();
    wait.until(ExpectedConditions.urlContains("/vaccinated"));

    final WebElement under11Button = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Yes')]")));
    log.info("Clicking 'yes' to under 11.");
    under11Button.click();
    wait.until(ExpectedConditions.urlContains("/travel"));

    final WebElement internationalTravelButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'No')]")));
    log.info("Clicking 'no' to international travel.");
    internationalTravelButton.click();
    wait.until(ExpectedConditions.urlContains("/symptoms"));

    final WebElement noSymptomsButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Continue')]")));
    log.info("Clicking 'continue' for symptoms.");
    noSymptomsButton.click();
    wait.until(ExpectedConditions.urlContains("/covid-positive"));

    final WebElement last5DaysPositive = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'No')]")));
    log.info("Clicking 'no' to last 5 days tested positive.");
    last5DaysPositive.click();
    wait.until(ExpectedConditions.urlContains("/household-isolation"));

    final WebElement currentlyIsolatingButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'No')]")));
    log.info("Clicking 'no' to should be currently isolating.");
    currentlyIsolatingButton.click();
    wait.until(ExpectedConditions.urlContains("/doctor-self-isolate"));

    final WebElement doctorIsolatingButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'No')]")));
    log.info("Clicking 'no' to being told to isolate.");
    doctorIsolatingButton.click();
    wait.until(ExpectedConditions.urlContains("/contact"));

    final WebElement closeContactButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'No')]")));
    log.info("Clicking 'no' to being a close contact.");
    closeContactButton.click();
    wait.until(ExpectedConditions.urlContains("/approved"));

    log.info("All buttons clicked, downloading results.");

    final WebElement downloadResultsLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[contains(text(),'Download result (PDF)')]")));
    log.info("Clicking download results link.");
    downloadResultsLink.click();

    watchForDownload();

    driver.close();
  }

  private void watchForDownload() throws Exception {
    final WatchService watchService = FileSystems.getDefault().newWatchService();
    final Path path = Paths.get(appTemp.getDir().getAbsolutePath());
    path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

    WatchKey key;
    watch: while ((key = watchService.take()) != null) {
      for (final WatchEvent<?> event : key.pollEvents()) {
        log.info("Event kind:" + event.kind() + ". File affected: " + event.context() + ".");
        final Path filePath = (Path)event.context();
        final String filename = filePath.toFile().getName();
        final String extension = Optional.of(filename)
          .filter(f -> f.contains("."))
          .map(f -> f.substring(filename.lastIndexOf(".") + 1))
          .orElse("");
        log.info("Extension: " + extension);
        if (extension.equals("pdf")) {
          log.info("File downloaded.");
          key.cancel();
          break watch;
        }
      }
      key.reset();
    }

    log.info("Stoppping watch.");
    watchService.close();
  }
}
