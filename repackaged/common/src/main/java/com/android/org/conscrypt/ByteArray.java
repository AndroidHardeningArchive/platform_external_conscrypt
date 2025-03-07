/* GENERATED SOURCE. DO NOT MODIFY. */
/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.org.conscrypt;

import java.util.Arrays;

/**
 * Byte array wrapper for hashtable use. Implements equals() and hashCode().
 * @hide This class is not part of the Android public SDK API
 */
@Internal
public final class ByteArray {
    private final byte[] bytes;
    private final int hashCode;

    public ByteArray(byte[] bytes) {
        this.bytes = bytes;
        this.hashCode = Arrays.hashCode(bytes);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ByteArray)) {
            return false;
        }
        ByteArray lhs = (ByteArray) o;
        if (hashCode != lhs.hashCode) {
            return false;
        }
        return Arrays.equals(bytes, lhs.bytes);
    }
}
