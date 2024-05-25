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

import androidx.core.app.NotificationCompat

enum class NotificationVisibility(val value: Int, val string: String) {
    VISIBILITY_PRIVATE(NotificationCompat.VISIBILITY_PRIVATE, "VISIBILITY_PRIVATE"),
    VISIBILITY_PUBLIC(NotificationCompat.VISIBILITY_PUBLIC, "VISIBILITY_PUBLIC"),
    VISIBILITY_SECRET(NotificationCompat.VISIBILITY_SECRET, "VISIBILITY_SECRET");

    companion object {

        private val mapString = NotificationVisibility.values().associateBy{it.string}
        private val mapValue = NotificationVisibility.values().associateBy{it.value}

        @JvmStatic
        fun fromString(visibilityString: String?): NotificationVisibility {
            return visibilityString?.let { mapString[it] } ?: VISIBILITY_PRIVATE
        }

        @JvmStatic
        fun fromValue(visibilityValue: Int?): NotificationVisibility {
            return visibilityValue?.let { mapValue[it] } ?: VISIBILITY_PRIVATE
        }
    }

    override fun toString(): String = string
}
