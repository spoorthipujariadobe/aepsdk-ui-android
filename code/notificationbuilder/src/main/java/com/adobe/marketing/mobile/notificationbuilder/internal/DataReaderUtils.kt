/*
  Copyright 2024 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.notificationbuilder.internal

import com.adobe.marketing.mobile.util.DataReader

internal object DataReaderUtils {
    /**
     * Returns the value of the key from the provided data map, or throws an exception if the key is not found.
     *
     * @param data the data [Map] to read from
     * @param key the [String] key to read from the data map
     * @return the `String` value of the key from the data map
     * @throws IllegalArgumentException if the key is not found in the data map
     */
    internal fun getNonNullOrThrow(
        data: Map<String, String>,
        key: String
    ): String {
        return DataReader.optString(data, key, null)
            ?: throw IllegalArgumentException("Required field \"$key\" not found.")
    }
}
