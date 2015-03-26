package com.segment.backo;

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Segment.io, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.util.concurrent.TimeUnit;

/**
 * Backo implements the "full jitter" backoff policy as described in this article <a
 * href="http://www.awsarchitectureblog.com/2015/03/backoff.html">Exponential Backoff And
 * Jitter </a>.
 * <p>
 * Create your own instance with the {@link Builder} class. Instances of backoff are stateless, so
 * you can create them once and share them across threads throughout your app.
 *
 * @see <a href="http://www.awsarchitectureblog.com/2015/03/backoff.html">Exponential Backoff And
 * Jitter
 * </a>
 */
public class Backo {
  private static final long DEFAULT_BASE = 100; // 100ms
  private static final int DEFAULT_FACTOR = 2;
  private static final double DEFAULT_JITTER = 0;
  private static final long DEFAULT_CAP = Long.MAX_VALUE;

  private final long base; // in milliseconds
  private final int factor;
  private final double jitter;
  private final long cap; // in milliseconds

  private Backo(long base, int factor, double jitter, long cap) {
    this.base = base;
    this.factor = factor;
    this.jitter = jitter;
    this.cap = cap;
  }

  /** Return a builder to construct instances of {@link Backo}. */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Sleeps for the duration returned by {@link #backoff}.
   *
   * @throws InterruptedException if any thread has interrupted the current thread. The
   * <i>interrupted status</i> of the current thread is
   * cleared when this exception is thrown.
   */
  public void sleep(int attempt) throws InterruptedException {
    Thread.sleep(backoff(attempt));
  }

  /** Return the duration (in milliseconds) for which you should backoff. */
  public long backoff(int attempt) {
    long duration = base * (long) Math.pow(factor, attempt);
    if (jitter != 0) {
      double random = Math.random();
      int deviation = (int) Math.floor(random * jitter * duration);
      if ((((int) Math.floor(random * 10)) & 1) == 0) {
        duration = duration - deviation;
      } else {
        duration = duration + deviation;
      }
    }
    if (duration < 0) {
      duration = Long.MAX_VALUE;
    }
    return Math.min(Math.max(duration, base), cap);
  }

  /** Fluent API to construct instances of {@link Backo}. */
  public static class Builder {
    private long base = DEFAULT_BASE;
    private int factor = DEFAULT_FACTOR;
    private double jitter = DEFAULT_JITTER;
    private long cap = DEFAULT_CAP;

    Builder() {
    }

    /** Set the initial backoff interval. Defaults to {@code 100ms}. */
    public Builder base(TimeUnit timeUnit, long duration) {
      this.base = timeUnit.toMillis(duration);
      return this;
    }

    /**
     * Set the backoff factor. Defaults to {@code 2}. Using a factor of {@code 2} will back off
     * linearly.
     */
    public Builder factor(int factor) {
      this.factor = factor;
      return this;
    }

    /** Set the backoff jitter. Defaults to {@code 0}, which represents no jitter. */
    public Builder jitter(int jitter) {
      this.jitter = jitter;
      return this;
    }

    /** Set the maximum backoff. Defaults to {@link Long#MAX_VALUE}. */
    public Builder cap(TimeUnit timeUnit, long duration) {
      this.cap = timeUnit.toMillis(duration);
      return this;
    }

    /** Build a {@link Backo} instance. */
    public Backo build() {
      if (cap < base) {
        throw new IllegalStateException("Initial backoff cannot be more than maximum.");
      }
      return new Backo(base, factor, jitter, cap);
    }
  }
}
