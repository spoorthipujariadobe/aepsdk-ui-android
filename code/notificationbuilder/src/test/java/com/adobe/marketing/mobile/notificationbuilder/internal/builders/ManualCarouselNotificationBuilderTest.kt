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

package com.adobe.marketing.mobile.notificationbuilder.internal.builders

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import com.adobe.marketing.mobile.notificationbuilder.internal.PushTemplateImageUtils
import com.adobe.marketing.mobile.notificationbuilder.internal.PushTemplateImageUtils.cacheImages
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.ManualCarouselPushTemplate
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.provideMockedManualCarousalTemplate
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class ManualCarouselNotificationBuilderTest {

    private lateinit var context: Context
    private lateinit var pushTemplate: ManualCarouselPushTemplate
    private lateinit var trackerActivityClass: Class<out Activity>
    private lateinit var broadcastReceiverClass: Class<out BroadcastReceiver>

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        pushTemplate = provideMockedManualCarousalTemplate(false)
        trackerActivityClass = mockkClass(Activity::class, relaxed = true).javaClass
        broadcastReceiverClass = mockkClass(BroadcastReceiver::class, relaxed = true).javaClass
        mockkObject(PushTemplateImageUtils)
        mockkObject(BasicNotificationBuilder)
    }

    @Test
    fun `construct returns NotificationCompat Builder for valid inputs`() {
        val result = ManualCarouselNotificationBuilder.construct(
            context,
            pushTemplate,
            trackerActivityClass,
            broadcastReceiverClass
        )
        assertNotNull(result)
    }

    @Test
    fun `construct returns BasicNotificationBuilder if download image count is less than 3`() {
        every { cacheImages(any()) } answers { 2 }
        ManualCarouselNotificationBuilder.construct(
            context,
            pushTemplate,
            trackerActivityClass,
            broadcastReceiverClass
        )
        verify {
            BasicNotificationBuilder.fallbackToBasicNotification(
                context,
                trackerActivityClass,
                broadcastReceiverClass,
                pushTemplate.data
            )
        }
    }

    @Test
    fun `construct returns AEPPushNotificationBuilder if download image count is greater than equal to 3`() {
        every { cacheImages(any()) } answers { 3 }
        ManualCarouselNotificationBuilder.construct(
            context,
            pushTemplate,
            trackerActivityClass,
            broadcastReceiverClass
        )
        verify(exactly = 0) {
            BasicNotificationBuilder.fallbackToBasicNotification(
                context,
                trackerActivityClass,
                broadcastReceiverClass,
                pushTemplate.data
            )
        }
    }
}
