package com.segment.backo;

import java.util.concurrent.TimeUnit;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BackoTest {
  @Test public void backoff() {
    Backo backo = new Backo.Builder().build();

    assertThat(backo.backoff(0)).isEqualTo(100);
    assertThat(backo.backoff(1)).isEqualTo(200);
    assertThat(backo.backoff(2)).isEqualTo(400);
    assertThat(backo.backoff(3)).isEqualTo(800);

    assertThat(backo.backoff(0)).isEqualTo(100);
    assertThat(backo.backoff(1)).isEqualTo(200);
  }

  @Test public void jittersBetweenBaseAndMin() {
    Backo backo = Backo.builder()
        .base(TimeUnit.MILLISECONDS, 100)
        .jitter(1)
        .factor(2)
        .cap(TimeUnit.MILLISECONDS, 10000)
        .build();

    for (int i = 0; i < 100; i++) {
      assertThat(backo.backoff(i)).isGreaterThanOrEqualTo(100).isLessThanOrEqualTo(10000);
    }
  }
}
