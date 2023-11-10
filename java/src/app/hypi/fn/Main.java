package app.hypi.fn;

import java.util.Map;

public class Main {
  public Map<String, Object> invoke(Map<String, Object> input) {
    System.out.printf("ENV: %s", input.get("env"));
    System.out.printf("ARGS: %s", input.get("args"));
    System.out.flush();
    return input;
  }
}
