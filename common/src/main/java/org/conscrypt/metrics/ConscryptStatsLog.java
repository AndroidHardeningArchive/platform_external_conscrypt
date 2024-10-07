/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.conscrypt.metrics;

import org.conscrypt.Internal;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.lang.Thread.UncaughtExceptionHandler;

/**
 * Reimplement with reflection calls the logging class,
 * generated by frameworks/statsd.
 * <p>
 * In case atom is changed, generate new wrapper with stats-log-api-gen
 * tool as shown below and add corresponding methods to ReflexiveStatsEvent's
 * newEvent() method.
 * <p>
 * $ stats-log-api-gen \
 *   --java "common/src/main/java/org/conscrypt/metrics/ConscryptStatsLog.java" \
 *   --module conscrypt \
 *   --javaPackage org.conscrypt.metrics \
 *   --javaClass ConscryptStatsLog
 **/
@Internal
public final class ConscryptStatsLog {
    public static final int TLS_HANDSHAKE_REPORTED = 317;

    private static final ExecutorService e =
        Executors.newSingleThreadExecutor(
                        new ThreadFactory() {
                            @Override
                            public Thread newThread(Runnable r) {
                                Thread thread = new Thread(r);
                                thread.setUncaughtExceptionHandler(
                                    new UncaughtExceptionHandler() {
                                        @Override
                                        public void uncaughtException(Thread t, Throwable e) {
                                           // Ignore
                                        }
                                    });
                                return thread;
                            }
                        });

    private ConscryptStatsLog() {}

    public static void write(int atomId, boolean success, int protocol, int cipherSuite,
            int duration, Source source, int[] uids) {
        e.execute(new Runnable() {
            @Override
            public void run() {
                ReflexiveStatsEvent event = ReflexiveStatsEvent.buildEvent(
                        atomId, success, protocol, cipherSuite, duration,
                        source.ordinal(), uids);

                ReflexiveStatsLog.write(event);
            }
        });
    }
}
