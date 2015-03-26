package com.segment.backo.sample;

import com.segment.backo.Backo;
import java.util.concurrent.TimeUnit;

public class Sample {
  private Sample() {
    // No instances.
  }

  public static void main(String... args) throws Exception {
    Backo backo = Backo.builder().base(TimeUnit.MILLISECONDS, 1).jitter(1).factor(2).build();

    for (int i = 0; i < 30; i++) {
      System.out.println(i + ":\tBack off for " + backo.backoff(i) + " ms");
    }
  }
}
