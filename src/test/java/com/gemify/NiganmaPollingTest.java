package com.gemify;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 临时测试类：每 1 秒并发请求 1000 次 http://10.42.147.84:5000/niganma
 * 运行 main 方法即可，Ctrl+C 停止。
 */
public class NiganmaPollingTest {

    private static final String TARGET_URL = "http://10.42.147.84:5000/niganma";
    private static final int REQUESTS_PER_SECOND = 1000;
    private static final int THREAD_POOL_SIZE = 200;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Object LOG_LOCK = new Object();

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        HttpClient client = HttpClient.newBuilder()
                .executor(executor)
                .build();

        AtomicLong totalCount = new AtomicLong(0);
        long batchNo = 0;

        System.out.printf("开始压测: %s (每秒 %d 次, 线程池 %d)%n",
                TARGET_URL, REQUESTS_PER_SECOND, THREAD_POOL_SIZE);

        while (true) {
            batchNo++;
            AtomicLong batchSuccess = new AtomicLong(0);
            AtomicLong batchFail = new AtomicLong(0);
            CountDownLatch latch = new CountDownLatch(REQUESTS_PER_SECOND);
            long batchStart = System.currentTimeMillis();

            for (int i = 0; i < REQUESTS_PER_SECOND; i++) {
                executor.submit(() -> {
                    long seq = totalCount.incrementAndGet();
                    String time = LocalDateTime.now().format(TIME_FORMAT);
                    try {
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(TARGET_URL))
                                .GET()
                                .build();
                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        int status = response.statusCode();
                        String body = response.body();
                        if (status >= 200 && status < 300) {
                            batchSuccess.incrementAndGet();
                        } else {
                            batchFail.incrementAndGet();
                        }
                        synchronized (LOG_LOCK) {
                            System.out.printf("[%s] #%d status=%d body=%s%n", time, seq, status, body);
                        }
                    } catch (Exception e) {
                        batchFail.incrementAndGet();
                        synchronized (LOG_LOCK) {
                            System.err.printf("[%s] #%d error: %s%n", time, seq, e.getMessage());
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            long elapsed = System.currentTimeMillis() - batchStart;
            String time = LocalDateTime.now().format(TIME_FORMAT);
            System.out.printf("[%s] batch #%d: %d requests in %dms, success=%d fail=%d, total=%d%n",
                    time, batchNo, REQUESTS_PER_SECOND, elapsed,
                    batchSuccess.get(), batchFail.get(), totalCount.get());

            long sleepMs = 1000 - elapsed;
            if (sleepMs > 0) {
                Thread.sleep(sleepMs);
            }
        }
    }
}
