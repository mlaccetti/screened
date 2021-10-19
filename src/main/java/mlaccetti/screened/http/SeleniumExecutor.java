package mlaccetti.screened.http;

import lombok.extern.java.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationTemp;
import org.springframework.stereotype.Component;

import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Log
public class SeleniumExecutor {
  @Autowired
  private ApplicationTemp appTemp;

  public void downloadResults() throws Exception {
    final String downloadFilepath = appTemp.getDir().getAbsolutePath();
    final Map<String, Object> chromePrefs = new HashMap<>();
    chromePrefs.put("profile.default_content_settings.popups", 0);
    chromePrefs.put("download.default_directory", downloadFilepath);

    final ChromeOptions options = new ChromeOptions();
    options.setExperimentalOption("prefs", chromePrefs);
    options.addArguments("--headless", "--disable-gpu", "--disable-crash-reporter");

    final WebDriver driver = new ChromeDriver(options);
    final WebDriverWait wait = new WebDriverWait(driver, 10);

    driver.get("https://covid-19.ontario.ca/school-screening/");

    final WebElement startScreeningButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()=\"Start school screening\"]")));
    log.info("Clicking 'start screening' button.");
    startScreeningButton.click();

    final WebElement guardianRadioButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@id='guardian']")));
    log.info("Selecting 'guardian' input.");
    guardianRadioButton.click();

    final WebElement guardianContinueButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Continue')]")));
    log.info("Clicking 'guardian' continue button.");
    guardianContinueButton.click();
    wait.until(ExpectedConditions.urlContains("/vaccinated"));

    final WebElement vaccinatedOrPositive = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'No')]")));
    log.info("Clicking 'Fully vaccinated or tested positive' no button.");
    vaccinatedOrPositive.click();
    wait.until(ExpectedConditions.urlContains("/travel-unvaccinated"));

    final WebElement travelledOutsideOfCanadaButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'No')]")));
    log.info("Clicking 'no' to travelling outside of Canada.");
    travelledOutsideOfCanadaButton.click();
    wait.until(ExpectedConditions.urlContains("/doctor-self-isolate"));

    final WebElement currentlyIsolatingButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'No')]")));
    log.info("Clicking 'no' to should be currently isolating.");
    currentlyIsolatingButton.click();
    wait.until(ExpectedConditions.urlContains("/contact"));

    final WebElement closeContactButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'No')]")));
    log.info("Clicking 'no' to being in close contact with Covid button.");
    closeContactButton.click();
    wait.until(ExpectedConditions.urlContains("/covid-alert"));

    final WebElement covidAlertButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'No')]")));
    log.info("Clicking 'no' to having a covid alert notification button.");
    covidAlertButton.click();
    wait.until(ExpectedConditions.urlContains("/covid-alert"));

    final WebElement noneOfTheAboveInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@id='none_of_the_above']")));
    log.info("Clicking 'none of the above' input.");
    noneOfTheAboveInput.click();

    final WebElement noneOfTheAboveContinue = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Continue')]")));
    log.info("Clicking 'none of the above' continue button.");
    noneOfTheAboveContinue.click();
    wait.until(ExpectedConditions.urlContains("/covid-test"));

    final WebElement isolatingButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'No')]")));
    log.info("Clicking 'no' to living with folks currently experiencing symptoms.");
    isolatingButton.click();
    wait.until(ExpectedConditions.urlContains("/rapid-test"));

    final WebElement testedPositiveButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'No')]")));
    log.info("Clicking 'no' to tested positive in last ten days.");
    testedPositiveButton.click();
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
