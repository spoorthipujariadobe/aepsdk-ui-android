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

package com.adobe.marketing.mobile.notificationbuilder.internal.extensions

import android.content.Intent

/**
 * Returns the value of the key from the [Intent], or throws an exception if the key is not found.
 *
 * @param key the [String] key to read from the intent
 * @return the `String` value of the key from the intent
 * @throws IllegalArgumentException if the key is not found in intent
 */
internal fun Intent.getNonNullOrThrow(key: String): String {
    return this.getStringExtra(key)
        ?: throw IllegalArgumentException("Required field \"$key\" not found.")
}
