/*
 * Copyright (C) 2015 The Android Open Source Project
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

package org.conscrypt.ct;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

import junit.framework.TestCase;

import org.conscrypt.OpenSSLKey;
import org.conscrypt.metrics.StatsLog;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;

public class LogStoreImplTest extends TestCase {
    static class FakeStatsLog implements StatsLog {
        public ArrayList<LogStore.State> states = new ArrayList<LogStore.State>();

        @Override
        public void countTlsHandshake(
                boolean success, String protocol, String cipherSuite, long duration) {}
        @Override
        public void updateCTLogListStatusChanged(LogStore logStore) {
            states.add(logStore.getState());
        }
    }

    Policy alwaysCompliantStorePolicy = new Policy() {
        @Override
        public boolean isLogStoreCompliant(LogStore store) {
            return true;
        }
        @Override
        public PolicyCompliance doesResultConformToPolicy(
                VerificationResult result, X509Certificate leaf) {
            return PolicyCompliance.COMPLY;
        }
    };

    Policy neverCompliantStorePolicy = new Policy() {
        @Override
        public boolean isLogStoreCompliant(LogStore store) {
            return false;
        }
        @Override
        public PolicyCompliance doesResultConformToPolicy(
                VerificationResult result, X509Certificate leaf) {
            return PolicyCompliance.COMPLY;
        }
    };

    public void test_loadValidLogList() throws Exception {
        // clang-format off
        String content = "" +
"{" +
"  \"version\": \"1.1\"," +
"  \"log_list_timestamp\": 1704070861000," +
"  \"operators\": [" +
"    {" +
"      \"name\": \"Operator 1\"," +
"      \"email\": [\"ct@operator1.com\"]," +
"      \"logs\": [" +
"        {" +
"          \"description\": \"Operator 1 'Test2024' log\"," +
"          \"log_id\": \"7s3QZNXbGs7FXLedtM0TojKHRny87N7DUUhZRnEftZs=\"," +
"          \"key\": \"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEHblsqctplMVc5ramA7vSuNxUQxcomQwGAVAdnWTAWUYr3MgDHQW0LagJ95lB7QT75Ve6JgT2EVLOFGU7L3YrwA==\"," +
"          \"url\": \"https://operator1.example.com/logs/test2024/\"," +
"          \"mmd\": 86400," +
"          \"state\": {" +
"            \"usable\": {" +
"              \"timestamp\": 1667328840000" +
"            }" +
"          }," +
"          \"temporal_interval\": {" +
"            \"start_inclusive\": 1704070861000," +
"            \"end_exclusive\": 1735693261000" +
"          }" +
"        }," +
"        {" +
"          \"description\": \"Operator 1 'Test2025' log\"," +
"          \"log_id\": \"TnWjJ1yaEMM4W2zU3z9S6x3w4I4bjWnAsfpksWKaOd8=\"," +
"          \"key\": \"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEIIKh+WdoqOTblJji4WiH5AltIDUzODyvFKrXCBjw/Rab0/98J4LUh7dOJEY7+66+yCNSICuqRAX+VPnV8R1Fmg==\"," +
"          \"url\": \"https://operator1.example.com/logs/test2025/\"," +
"          \"mmd\": 86400," +
"          \"state\": {" +
"            \"usable\": {" +
"              \"timestamp\": 1700960461000" +
"            }" +
"          }," +
"          \"temporal_interval\": {" +
"            \"start_inclusive\": 1735693261000," +
"            \"end_exclusive\": 1751331661000" +
"          }" +
"        }" +
"      ]" +
"    }," +
"    {" +
"      \"name\": \"Operator 2\"," +
"      \"email\": [\"ct@operator2.com\"]," +
"      \"logs\": [" +
"        {" +
"          \"description\": \"Operator 2 'Test2024' Log\"," +
"          \"log_id\": \"2ra/az+1tiKfm8K7XGvocJFxbLtRhIU0vaQ9MEjX+6s=\"," +
"          \"key\": \"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEd7Gbe4/mizX+OpIpLayKjVGKJfyTttegiyk3cR0zyswz6ii5H+Ksw6ld3Ze+9p6UJd02gdHrXSnDK0TxW8oVSA==\"," +
"          \"url\": \"https://operator2.example.com/logs/test2024/\"," +
"          \"mmd\": 86400," +
"          \"state\": {" +
"            \"usable\": {" +
"              \"timestamp\": 1669770061000" +
"            }" +
"          }," +
"          \"temporal_interval\": {" +
"            \"start_inclusive\": 1704070861000," +
"            \"end_exclusive\": 1735693261000" +
"          }" +
"        }" +
"      ]" +
"    }" +
"  ]" +
"}";
        // clang-format on

        FakeStatsLog metrics = new FakeStatsLog();
        File logList = writeFile(content);
        LogStore store = new LogStoreImpl(logList.toPath(), metrics);
        store.setPolicy(alwaysCompliantStorePolicy);

        assertNull("A null logId should return null", store.getKnownLog(null));

        byte[] pem = ("-----BEGIN PUBLIC KEY-----\n"
                + "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEHblsqctplMVc5ramA7vSuNxUQxcomQwGAVAdnWTAWUYr"
                + "3MgDHQW0LagJ95lB7QT75Ve6JgT2EVLOFGU7L3YrwA=="
                + "\n-----END PUBLIC KEY-----\n")
                             .getBytes(US_ASCII);
        ByteArrayInputStream is = new ByteArrayInputStream(pem);

        LogInfo log1 =
                new LogInfo.Builder()
                        .setPublicKey(OpenSSLKey.fromPublicKeyPemInputStream(is).getPublicKey())
                        .setDescription("Operator 1 'Test2024' log")
                        .setUrl("https://operator1.example.com/logs/test2024/")
                        .setState(LogInfo.STATE_USABLE, 1667328840000L)
                        .setOperator("Operator 1")
                        .build();
        byte[] log1Id = Base64.getDecoder().decode("7s3QZNXbGs7FXLedtM0TojKHRny87N7DUUhZRnEftZs=");
        assertEquals("An existing logId should be returned", log1, store.getKnownLog(log1Id));
        assertEquals("One metric update should be emitted", metrics.states.size(), 1);
        assertEquals("The metric update for log list state should be compliant",
                metrics.states.get(0), LogStore.State.COMPLIANT);
    }

    public void test_loadMalformedLogList() throws Exception {
        FakeStatsLog metrics = new FakeStatsLog();
        String content = "}}";
        File logList = writeFile(content);
        LogStore store = new LogStoreImpl(logList.toPath(), metrics);
        store.setPolicy(alwaysCompliantStorePolicy);

        assertEquals(
                "The log state should be malformed", store.getState(), LogStore.State.MALFORMED);
        assertEquals("One metric update should be emitted", metrics.states.size(), 1);
        assertEquals("The metric update for log list state should be malformed",
                metrics.states.get(0), LogStore.State.MALFORMED);
    }

    public void test_loadMissingLogList() throws Exception {
        FakeStatsLog metrics = new FakeStatsLog();
        File logList = new File("does_not_exist");
        LogStore store = new LogStoreImpl(logList.toPath(), metrics);
        store.setPolicy(alwaysCompliantStorePolicy);

        assertEquals(
                "The log state should be not found", store.getState(), LogStore.State.NOT_FOUND);
        assertEquals("One metric update should be emitted", metrics.states.size(), 1);
        assertEquals("The metric update for log list state should be not found",
                metrics.states.get(0), LogStore.State.NOT_FOUND);
    }

    private File writeFile(String content) throws IOException {
        File file = File.createTempFile("test", null);
        file.deleteOnExit();
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(content);
        }
        return file;
    }
}
