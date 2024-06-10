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
import com.adobe.marketing.mobile.notificationbuilder.PushTemplateConstants
import com.adobe.marketing.mobile.notificationbuilder.internal.PushTemplateImageUtils
import com.adobe.marketing.mobile.notificationbuilder.internal.PushTemplateImageUtils.cacheImages
import com.adobe.marketing.mobile.notificationbuilder.internal.PushTemplateImageUtils.getCachedImage
import com.adobe.marketing.mobile.notificationbuilder.internal.builders.ManualCarouselNotificationBuilder.getCarouselIndices
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.ManualCarouselPushTemplate
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.provideMockedManualCarousalTemplate
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    @Test
    fun `test downloadCarouselItems return non empty list for it retrieve the image bitmap from cache successfully`() {
        every { getCachedImage(any()) } answers { mockkClass(Bitmap::class) }
        val imagesList =
            ManualCarouselNotificationBuilder.downloadCarouselItems(pushTemplate.carouselItems)
        assertFalse(imagesList.isEmpty())
    }

    @Test
    fun `test getCarouselIndices with left click intent action`() {
        pushTemplate.intentAction = PushTemplateConstants.IntentActions.MANUAL_CAROUSEL_LEFT_CLICKED
        val imageUris = listOf("image1", "image2", "image3")
        val result = getCarouselIndices(pushTemplate, imageUris)
        assertEquals(Triple(1, 2, 0), result)
    }

    @Test
    fun `test getCarouselIndices with filmstrip left click intent action`() {
        pushTemplate.intentAction = PushTemplateConstants.IntentActions.FILMSTRIP_LEFT_CLICKED
        val imageUris = listOf("image1", "image2", "image3")
        val result = getCarouselIndices(pushTemplate, imageUris)
        assertEquals(Triple(1, 2, 0), result)
    }

    @Test
    fun `test getCarouselIndices with no intent action and filmstrip layout`() {
        pushTemplate.intentAction = PushTemplateConstants.DefaultValues.FILMSTRIP_CAROUSEL_MODE
        val imageUris = listOf("image1", "image2", "image3")
        val result = getCarouselIndices(pushTemplate, imageUris)
        assertEquals(
            Triple(
                PushTemplateConstants.DefaultValues.FILMSTRIP_CAROUSEL_CENTER_INDEX - 1,
                PushTemplateConstants.DefaultValues.FILMSTRIP_CAROUSEL_CENTER_INDEX,
                PushTemplateConstants.DefaultValues.FILMSTRIP_CAROUSEL_CENTER_INDEX + 1
            ),
            result
        )
    }
}
