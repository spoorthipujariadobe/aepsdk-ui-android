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

package com.adobe.marketing.mobile.notificationbuilder.internal.templates

import com.adobe.marketing.mobile.notificationbuilder.internal.PushTemplateConstants.TimerKeys
import com.adobe.marketing.mobile.notificationbuilder.internal.util.NotificationData

/**
 * Class for parsing the data required to display a Timer notification.
 */
internal class TimerPushTemplate(data: NotificationData) : AEPPushTemplate(data) {

    internal var alternateTitle: String
    internal var alternateBody: String?
    internal var alternateExpandedBody: String?
    internal var alternateImage: String?
    internal var timerColor: String?
    internal var expiryTime: Long
    private val currentTimeInSeconds: Long
        get() = System.currentTimeMillis() / 1000

    val remainingTimeInSeconds: Long
        get() = expiryTime - currentTimeInSeconds

    /**
     * Initialize the TimerPushTemplate with the provided [NotificationData].
     */
    init {
        alternateTitle = data.getRequiredString(TimerKeys.ALTERNATE_TITLE)
        expiryTime = extractExpiryTime(data) ?: throw IllegalArgumentException("Timer expiry time not found in the data. Skipping to display timer notification.")
        alternateBody = data.getString(TimerKeys.ALTERNATE_BODY)
        alternateExpandedBody = data.getString(TimerKeys.ALTERNATE_EXPANDED_BODY)
        alternateImage = data.getString(TimerKeys.ALTERNATE_IMAGE)
        timerColor = data.getString(TimerKeys.TIMER_COLOR)
    }

    /**
     * Returns true if the timer has expired, false otherwise.
     */
    internal fun isExpired(): Boolean {
        return expiryTime.let { currentTimeInSeconds > it }
    }

    /**
     * Returns the timestamp when the timer will expire.
     *
     * @param source the notification data source
     * @return the expiry time in seconds
     */
    private fun extractExpiryTime(source: NotificationData): Long? {
        // extract duration and endTimestamp from the source data
        val duration = source.getString(TimerKeys.TIMER_DURATION)?.toLongOrNull()
        val endTimestamp = source.getString(TimerKeys.TIMER_END_TIME)?.toLongOrNull()

        // If duration is provided, calculate the expiry time based on the current time.
        // If endTimestamp is provided, use it as the expiry time.
        // duration takes precedence over endTimestamp, if both are provided.
        return when {
            duration != null -> currentTimeInSeconds + duration
            endTimestamp != null -> endTimestamp
            else -> null
        }
    }
}
