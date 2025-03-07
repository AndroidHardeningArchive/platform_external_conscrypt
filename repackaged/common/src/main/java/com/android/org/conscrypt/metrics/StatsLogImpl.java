/* GENERATED SOURCE. DO NOT MODIFY. */
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
package com.android.org.conscrypt.metrics;

import com.android.org.conscrypt.Internal;
import com.android.org.conscrypt.Platform;
import com.android.org.conscrypt.ct.LogStore;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
 *   --javaClass StatsLog
 * @hide This class is not part of the Android public SDK API
 **/
@Internal
public final class StatsLogImpl implements StatsLog {
    /**
     * TlsHandshakeReported tls_handshake_reported
     * Usage: StatsLog.write(StatsLog.TLS_HANDSHAKE_REPORTED, boolean success, int protocol, int
     * cipher_suite, int handshake_duration_millis, int source, int[] uid);<br>
     */
    public static final int TLS_HANDSHAKE_REPORTED = 317;

    /**
     * CertificateTransparencyLogListStateChanged certificate_transparency_log_list_state_changed
     * Usage: StatsLog.write(StatsLog.CERTIFICATE_TRANSPARENCY_LOG_LIST_STATE_CHANGED, int status,
     * int loaded_compat_version, int min_compat_version_available, int major_version, int
     * minor_version);<br>
     */
    public static final int CERTIFICATE_TRANSPARENCY_LOG_LIST_STATE_CHANGED = 934;

    private static final ExecutorService e = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    // Ignore
                }
            });
            return thread;
        }
    });

    private static final StatsLog INSTANCE = new StatsLogImpl();
    private StatsLogImpl() {}
    public static StatsLog getInstance() {
        return INSTANCE;
    }

    @Override
    public void countTlsHandshake(
            boolean success, String protocol, String cipherSuite, long duration) {
        Protocol proto = Protocol.forName(protocol);
        CipherSuite suite = CipherSuite.forName(cipherSuite);

        write(TLS_HANDSHAKE_REPORTED, success, proto.getId(), suite.getId(), (int) duration,
                Platform.getStatsSource().ordinal(), Platform.getUids());
    }

    private static int logStoreStateToMetricsState(LogStore.State state) {
        /* These constants must match the atom LogListStatus
         * from frameworks/proto_logging/stats/atoms/conscrypt/conscrypt_extension_atoms.proto
         */
        final int METRIC_UNKNOWN = 0;
        final int METRIC_SUCCESS = 1;
        final int METRIC_NOT_FOUND = 2;
        final int METRIC_PARSING_FAILED = 3;
        final int METRIC_EXPIRED = 4;

        switch (state) {
            case UNINITIALIZED:
            case LOADED:
                return METRIC_UNKNOWN;
            case NOT_FOUND:
                return METRIC_NOT_FOUND;
            case MALFORMED:
                return METRIC_PARSING_FAILED;
            case COMPLIANT:
                return METRIC_SUCCESS;
            case NON_COMPLIANT:
                return METRIC_EXPIRED;
        }
        return METRIC_UNKNOWN;
    }

    @Override
    public void updateCTLogListStatusChanged(LogStore logStore) {
        int state = logStoreStateToMetricsState(logStore.getState());
        write(CERTIFICATE_TRANSPARENCY_LOG_LIST_STATE_CHANGED, state, logStore.getCompatVersion(),
                logStore.getMinCompatVersionAvailable(), logStore.getMajorVersion(),
                logStore.getMinorVersion());
    }

    private void write(int atomId, boolean success, int protocol, int cipherSuite, int duration,
            int source, int[] uids) {
        e.execute(new Runnable() {
            @Override
            public void run() {
                ReflexiveStatsEvent event = ReflexiveStatsEvent.buildEvent(
                        atomId, success, protocol, cipherSuite, duration, source, uids);

                ReflexiveStatsLog.write(event);
            }
        });
    }

    private void write(int atomId, int status, int loadedCompatVersion,
            int minCompatVersionAvailable, int majorVersion, int minorVersion) {
        e.execute(new Runnable() {
            @Override
            public void run() {
                ReflexiveStatsEvent.Builder builder = ReflexiveStatsEvent.newBuilder();
                builder.setAtomId(atomId);
                builder.writeInt(status);
                builder.writeInt(loadedCompatVersion);
                builder.writeInt(minCompatVersionAvailable);
                builder.writeInt(majorVersion);
                builder.writeInt(minorVersion);
                builder.usePooledBuffer();
                ReflexiveStatsLog.write(builder.build());
            }
        });
    }
}
