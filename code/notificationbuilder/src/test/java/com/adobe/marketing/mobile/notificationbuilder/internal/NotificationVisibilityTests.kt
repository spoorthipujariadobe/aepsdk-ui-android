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
import org.junit.Assert.assertEquals
import org.junit.Test
class NotificationVisibilityTests {

    @Test
    fun testEnumValues() {
        assertEquals(NotificationCompat.VISIBILITY_PRIVATE, NotificationVisibility.VISIBILITY_PRIVATE.value)
        assertEquals("VISIBILITY_PRIVATE", NotificationVisibility.VISIBILITY_PRIVATE.string)

        assertEquals(NotificationCompat.VISIBILITY_PUBLIC, NotificationVisibility.VISIBILITY_PUBLIC.value)
        assertEquals("VISIBILITY_PUBLIC", NotificationVisibility.VISIBILITY_PUBLIC.string)

        assertEquals(NotificationCompat.VISIBILITY_SECRET, NotificationVisibility.VISIBILITY_SECRET.value)
        assertEquals("VISIBILITY_SECRET", NotificationVisibility.VISIBILITY_SECRET.string)
    }

    @Test
    fun testFromString() {
        assertEquals(NotificationVisibility.VISIBILITY_PRIVATE, NotificationVisibility.fromString("VISIBILITY_PRIVATE"))
        assertEquals(NotificationVisibility.VISIBILITY_PUBLIC, NotificationVisibility.fromString("VISIBILITY_PUBLIC"))
        assertEquals(NotificationVisibility.VISIBILITY_SECRET, NotificationVisibility.fromString("VISIBILITY_SECRET"))
        // verify invalid inputs
        assertEquals(NotificationVisibility.VISIBILITY_PRIVATE, NotificationVisibility.fromString("INVALID"))
        assertEquals(NotificationVisibility.VISIBILITY_PRIVATE, NotificationVisibility.fromString(""))
        assertEquals(NotificationVisibility.VISIBILITY_PRIVATE, NotificationVisibility.fromString(null))
    }

    @Test
    fun testFromValue() {
        assertEquals(NotificationVisibility.VISIBILITY_PRIVATE, NotificationVisibility.fromValue(NotificationCompat.VISIBILITY_PRIVATE))
        assertEquals(NotificationVisibility.VISIBILITY_PUBLIC, NotificationVisibility.fromValue(NotificationCompat.VISIBILITY_PUBLIC))
        assertEquals(NotificationVisibility.VISIBILITY_SECRET, NotificationVisibility.fromValue(NotificationCompat.VISIBILITY_SECRET))
        // verify invalid inputs
        assertEquals(NotificationVisibility.VISIBILITY_PRIVATE, NotificationVisibility.fromValue(999))
        assertEquals(NotificationVisibility.VISIBILITY_PRIVATE, NotificationVisibility.fromValue(null))
        assertEquals(NotificationVisibility.VISIBILITY_PRIVATE, NotificationVisibility.fromValue(-999))
    }

    @Test
    fun testToString() {
        assertEquals("VISIBILITY_PRIVATE", NotificationVisibility.VISIBILITY_PRIVATE.toString())
        assertEquals("VISIBILITY_PUBLIC", NotificationVisibility.VISIBILITY_PUBLIC.toString())
        assertEquals("VISIBILITY_SECRET", NotificationVisibility.VISIBILITY_SECRET.toString())
    }
}
