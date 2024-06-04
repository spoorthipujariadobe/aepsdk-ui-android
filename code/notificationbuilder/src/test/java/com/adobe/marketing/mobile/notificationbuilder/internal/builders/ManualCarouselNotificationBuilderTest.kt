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
import android.graphics.Bitmap
import com.adobe.marketing.mobile.notificationbuilder.internal.PushTemplateImageUtils
import com.adobe.marketing.mobile.notificationbuilder.internal.PushTemplateImageUtils.getCachedImage
import com.adobe.marketing.mobile.notificationbuilder.internal.builders.ManualCarouselNotificationBuilder.downloadCarouselItems
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.ManualCarouselPushTemplate
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.provideMockedManualCarousalTemplate
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
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
        trackerActivityClass = mock(Activity::class.java).javaClass
        broadcastReceiverClass = mock(BroadcastReceiver::class.java).javaClass
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
    fun `test to verify the number of valid image urls available is exactly equals to the number of images downloaded`() {
        mockStatic(PushTemplateImageUtils::class.java)
        val bitmapMock = mock(Bitmap::class.java)
        var invocationCount = 0
        `when`(getCachedImage(anyString())).thenAnswer {
            invocationCount++
            if (invocationCount <= 3) bitmapMock else null
        }
        val imageList = downloadCarouselItems(pushTemplate.carouselItems)
        assertTrue(imageList.size == 3)
    }
}
