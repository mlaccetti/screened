package mlaccetti.screened.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationTemp;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

@Component
public class Cleanup {
  @Autowired
  private ApplicationTemp applicationTemp;

  public void clean() {
    FileSystemUtils.deleteRecursively(applicationTemp.getDir());
    applicationTemp.getDir().mkdirs();
  }
}
