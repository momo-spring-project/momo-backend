package com.example.momo.global.utils;

import org.springframework.dao.OptimisticLockingFailureException;

import java.util.function.Supplier;

public class RetryUtil {

	public static <T> T retry(Supplier<T> task, int maxAttempts) {
		int attempts = 0;
		while (true) {
			try {
				return task.get();
			} catch (OptimisticLockingFailureException e) {
				attempts++;
				if (attempts >= maxAttempts) {
					throw e;
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new RuntimeException("Interrupted while waiting for retry", ie);
				}
			}
		}
	}
}
