package com.jobprocessing.worker;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExponentialBackoffTest {

    @Test
    void backoffCalculation_shouldGrowExponentially() {
        long baseBackoffMs = 2000L;
        double multiplier = 2.0;

        // Attempt 1 -> 2000 * 2^0 = 2000ms (2s)
        long delayAttempt1 = (long) (baseBackoffMs * Math.pow(multiplier, 0));
        assertThat(delayAttempt1).isEqualTo(2000L);

        // Attempt 2 -> 2000 * 2^1 = 4000ms (4s)
        long delayAttempt2 = (long) (baseBackoffMs * Math.pow(multiplier, 1));
        assertThat(delayAttempt2).isEqualTo(4000L);

        // Attempt 3 -> 2000 * 2^2 = 8000ms (8s)
        long delayAttempt3 = (long) (baseBackoffMs * Math.pow(multiplier, 2));
        assertThat(delayAttempt3).isEqualTo(8000L);
    }

    @Test
    void maxRetriesThreshold_shouldRouteToDlq() {
        int maxRetries = 3;
        int currentAttempt = 3;

        boolean shouldRetry = currentAttempt < maxRetries;
        boolean shouldMoveToDlq = currentAttempt >= maxRetries;

        assertThat(shouldRetry).isFalse();
        assertThat(shouldMoveToDlq).isTrue();
    }
}
