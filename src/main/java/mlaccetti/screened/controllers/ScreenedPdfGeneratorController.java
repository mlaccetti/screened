package mlaccetti.screened.controllers;

import lombok.extern.java.Log;
import mlaccetti.screened.email.EmailResults;
import mlaccetti.screened.http.SeleniumExecutor;
import mlaccetti.screened.util.Cleanup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Log
public class ScreenedPdfGeneratorController {
  @Autowired
  private SeleniumExecutor executor;

  @Autowired
  private EmailResults emailer;

  @Autowired
  private Cleanup cleanup;

  @RequestMapping("/generate")
  public ResponseEntity<String> generate() throws Exception {
    log.info("Generating PDF.");

    cleanup.clean();
    executor.downloadResults();
    emailer.send();

    return ResponseEntity.ok()
      .body("Email sent.");
  }
}
