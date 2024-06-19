/*
  Copyright 2024 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License")
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.notificationbuilder.internal

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.adobe.marketing.mobile.notificationbuilder.NotificationBuilder
import com.adobe.marketing.mobile.notificationbuilder.NotificationConstructionFailedException
import com.adobe.marketing.mobile.notificationbuilder.PushTemplateConstants
import com.adobe.marketing.mobile.notificationbuilder.internal.builders.DummyActivity
import com.adobe.marketing.mobile.notificationbuilder.internal.builders.DummyBroadcastReceiver
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.MockAEPPushTemplateDataProvider
import com.adobe.marketing.mobile.services.AppContextService
import com.adobe.marketing.mobile.services.ServiceProvider
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class NotificationBuilderTests {

    @Mock
    private lateinit var application: Application
    private lateinit var trackerActivityClass: Class<out Activity>
    private lateinit var broadcastReceiverClass: Class<out BroadcastReceiver>
    private lateinit var mockNotificationManagerCompat: NotificationManagerCompat

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        application = RuntimeEnvironment.getApplication()
        ServiceProvider.getInstance().appContextService.setApplication(application)
        trackerActivityClass = DummyActivity::class.java
        broadcastReceiverClass = DummyBroadcastReceiver::class.java
        mockkObject(PendingIntentUtils)
        mockkStatic(NotificationManagerCompat::class)
        mockNotificationManagerCompat = mockkClass(NotificationManagerCompat::class)
        every { NotificationManagerCompat.from(any()) } returns mockNotificationManagerCompat
        every { mockNotificationManagerCompat.cancel(any()) } returns Unit
    }

    @After
    fun cleanup() {
        clearAllMocks()
    }

    @Test
    fun `NotificationBuilder version values matches the expected version`() {
        val version = NotificationBuilder.version()

        assertEquals("3.0.0", version)
    }

    @Test
    fun `constructNotificationBuilder given a map with required data should return a NotificationCompat Builder`() {
        val mapData = MockAEPPushTemplateDataProvider.getMockedDataMapWithRequiredData()
        val notificationBuilder = NotificationBuilder.constructNotificationBuilder(
            mapData,
            trackerActivityClass,
            broadcastReceiverClass
        )

        assertNotNull(notificationBuilder)
    }

    @Test(expected = NotificationConstructionFailedException::class)
    fun `constructNotificationBuilder given a map with required data but no context is available should throw an exception`() {
        setNullContext()
        val mapData = MockAEPPushTemplateDataProvider.getMockedDataMapWithRequiredData()
        val notificationBuilder = NotificationBuilder.constructNotificationBuilder(
            mapData,
            trackerActivityClass,
            broadcastReceiverClass
        )

        assertNull(notificationBuilder)
    }

    @Test(expected = NotificationConstructionFailedException::class)
    fun `constructNotificationBuilder given a map with empty data should thrown an exception`() {
        val mapData = emptyMap<String, String>()
        val notificationBuilder = NotificationBuilder.constructNotificationBuilder(
            mapData,
            trackerActivityClass,
            broadcastReceiverClass
        )

        assertNull(notificationBuilder)
    }

    @Test
    fun `constructNotificationBuilder given an intent with required data should return a NotificationCompat Builder`() {
        val bundle = MockAEPPushTemplateDataProvider.getMockedBundleWithRequiredData()
        val intent = Intent()
        intent.action = "mockAction"
        intent.putExtras(bundle)
        val notificationBuilder = NotificationBuilder.constructNotificationBuilder(
            intent,
            trackerActivityClass,
            broadcastReceiverClass
        )

        assertNotNull(notificationBuilder)
    }

    @Test(expected = NotificationConstructionFailedException::class)
    fun `constructNotificationBuilder given an intent with required data but no context is available should throw an exception`() {
        setNullContext()
        val bundle = MockAEPPushTemplateDataProvider.getMockedBundleWithRequiredData()
        val intent = Intent()
        intent.action = "mockAction"
        intent.putExtras(bundle)
        val notificationBuilder = NotificationBuilder.constructNotificationBuilder(
            intent,
            trackerActivityClass,
            broadcastReceiverClass
        )

        assertNull(notificationBuilder)
    }

    @Test(expected = NotificationConstructionFailedException::class)
    fun `constructNotificationBuilder given an intent with empty data should thrown an exception`() {
        val intent = Intent()
        intent.action = "mockAction"
        val notificationBuilder = NotificationBuilder.constructNotificationBuilder(
            intent,
            trackerActivityClass,
            broadcastReceiverClass
        )

        assertNull(notificationBuilder)
    }

    @Test
    fun `handleRemindIntent given a valid intent should schedule a remind notification`() {
        val bundle = MockAEPPushTemplateDataProvider.getMockedAEPBundleWithAllKeys()
        val remindIntent = Intent()
        remindIntent.putExtras(bundle)
        remindIntent.action = PushTemplateConstants.IntentActions.REMIND_LATER_CLICKED
        NotificationBuilder.handleRemindIntent(
            remindIntent,
            broadcastReceiverClass
        )

        verify(exactly = 1) { PendingIntentUtils.scheduleNotification(any(), any(), any(), any()) }
        verify(exactly = 1) { mockNotificationManagerCompat.cancel(any()) }
    }

    @Test
    fun `handleRemindIntent given a valid intent with no broadcast receiver should not schedule a remind notification`() {
        val bundle = MockAEPPushTemplateDataProvider.getMockedAEPBundleWithAllKeys()
        val remindIntent = Intent()
        remindIntent.putExtras(bundle)
        remindIntent.action = PushTemplateConstants.IntentActions.REMIND_LATER_CLICKED
        NotificationBuilder.handleRemindIntent(
            remindIntent,
            null
        )

        verify(exactly = 0) { PendingIntentUtils.scheduleNotification(any(), any(), any(), any()) }
        verify(exactly = 1) { mockNotificationManagerCompat.cancel(any()) }
    }

    @Test
    fun `handleRemindIntent given a valid intent with no broadcast receiver and tag should not schedule a remind notification and should not cancel the current notification`() {
        val bundle = MockAEPPushTemplateDataProvider.getMockedAEPBundleWithAllKeys()
        bundle.remove(PushTemplateConstants.PushPayloadKeys.TAG)
        val remindIntent = Intent()
        remindIntent.putExtras(bundle)
        remindIntent.action = PushTemplateConstants.IntentActions.REMIND_LATER_CLICKED
        NotificationBuilder.handleRemindIntent(
            remindIntent,
            null
        )

        verify(exactly = 0) { PendingIntentUtils.scheduleNotification(any(), any(), any(), any()) }
        verify(exactly = 0) { mockNotificationManagerCompat.cancel(any()) }
    }

    @Test
    fun `handleRemindIntent given a valid intent with a remind later timestamp should schedule a remind notification`() {
        val bundle = MockAEPPushTemplateDataProvider.getMockedAEPBundleWithAllKeys()
        bundle.remove(PushTemplateConstants.PushPayloadKeys.REMIND_LATER_DURATION)
        val remindIntent = Intent()
        remindIntent.putExtras(bundle)
        remindIntent.action = PushTemplateConstants.IntentActions.REMIND_LATER_CLICKED
        NotificationBuilder.handleRemindIntent(
            remindIntent,
            broadcastReceiverClass
        )

        verify(exactly = 1) { PendingIntentUtils.scheduleNotification(any(), any(), any(), any()) }
        verify(exactly = 1) { mockNotificationManagerCompat.cancel(any()) }
    }

    @Test
    fun `handleRemindIntent given a valid intent with a remind later timestamp and no tag should schedule a remind notification but should not cancel the current notification`() {
        val bundle = MockAEPPushTemplateDataProvider.getMockedAEPBundleWithAllKeys()
        bundle.remove(PushTemplateConstants.PushPayloadKeys.REMIND_LATER_DURATION)
        bundle.remove(PushTemplateConstants.PushPayloadKeys.TAG)
        val remindIntent = Intent()
        remindIntent.putExtras(bundle)
        remindIntent.action = PushTemplateConstants.IntentActions.REMIND_LATER_CLICKED
        NotificationBuilder.handleRemindIntent(
            remindIntent,
            broadcastReceiverClass
        )

        verify(exactly = 1) { PendingIntentUtils.scheduleNotification(any(), any(), any(), any()) }
        verify(exactly = 0) { mockNotificationManagerCompat.cancel(any()) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `handleRemindIntent given an intent with no remind later timestamp or duration should throw an exception`() {
        val bundle = MockAEPPushTemplateDataProvider.getMockedAEPBundleWithAllKeys()
        bundle.remove(PushTemplateConstants.PushPayloadKeys.REMIND_LATER_DURATION)
        bundle.remove(PushTemplateConstants.PushPayloadKeys.REMIND_LATER_TIMESTAMP)
        val remindIntent = Intent()
        remindIntent.putExtras(bundle)
        remindIntent.action = PushTemplateConstants.IntentActions.REMIND_LATER_CLICKED
        NotificationBuilder.handleRemindIntent(
            remindIntent,
            broadcastReceiverClass
        )

        verify(exactly = 0) { PendingIntentUtils.scheduleNotification(any(), any(), any(), any()) }
        verify(exactly = 1) { mockNotificationManagerCompat.cancel(any()) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `handleRemindIntent given an intent with no remind later timestamp, duration, and tag should throw an exception but should not cancel the current notification`() {
        val bundle = MockAEPPushTemplateDataProvider.getMockedAEPBundleWithAllKeys()
        bundle.remove(PushTemplateConstants.PushPayloadKeys.REMIND_LATER_DURATION)
        bundle.remove(PushTemplateConstants.PushPayloadKeys.REMIND_LATER_TIMESTAMP)
        bundle.remove(PushTemplateConstants.PushPayloadKeys.TAG)
        val remindIntent = Intent()
        remindIntent.putExtras(bundle)
        remindIntent.action = PushTemplateConstants.IntentActions.REMIND_LATER_CLICKED
        NotificationBuilder.handleRemindIntent(
            remindIntent,
            broadcastReceiverClass
        )

        verify(exactly = 0) { PendingIntentUtils.scheduleNotification(any(), any(), any(), any()) }
        verify(exactly = 0) { mockNotificationManagerCompat.cancel(any()) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `handleRemindIntent given an intent with an invalid remind later timestamp and duration should throw an exception`() {
        val bundle = MockAEPPushTemplateDataProvider.getMockedAEPBundleWithAllKeys()
        bundle.putString(PushTemplateConstants.PushPayloadKeys.REMIND_LATER_DURATION, "duration")
        bundle.putString(PushTemplateConstants.PushPayloadKeys.REMIND_LATER_TIMESTAMP, "timestamp")
        val remindIntent = Intent()
        remindIntent.putExtras(bundle)
        remindIntent.action = PushTemplateConstants.IntentActions.REMIND_LATER_CLICKED
        NotificationBuilder.handleRemindIntent(
            remindIntent,
            broadcastReceiverClass
        )

        verify(exactly = 0) { PendingIntentUtils.scheduleNotification(any(), any(), any(), any()) }
        verify(exactly = 0) { mockNotificationManagerCompat.cancel(any()) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `handleRemindIntent given a valid intent with a remind later timestamp before the current date should throw an exception`() {
        val bundle = MockAEPPushTemplateDataProvider.getMockedAEPBundleWithAllKeys()
        bundle.remove(PushTemplateConstants.PushPayloadKeys.REMIND_LATER_DURATION)
        bundle.putString(PushTemplateConstants.PushPayloadKeys.REMIND_LATER_TIMESTAMP, "1234567890")
        val remindIntent = Intent()
        remindIntent.putExtras(bundle)
        remindIntent.action = PushTemplateConstants.IntentActions.REMIND_LATER_CLICKED
        NotificationBuilder.handleRemindIntent(
            remindIntent,
            broadcastReceiverClass
        )

        verify(exactly = 0) { PendingIntentUtils.scheduleNotification(any(), any(), any(), any()) }
        verify(exactly = 0) { mockNotificationManagerCompat.cancel(any()) }
    }

    @Test(expected = NotificationConstructionFailedException::class)
    fun `handleRemindIntent given a valid intent but no context is available should throw an exception`() {
        setNullContext()
        val bundle = MockAEPPushTemplateDataProvider.getMockedAEPBundleWithAllKeys()
        val remindIntent = Intent()
        remindIntent.putExtras(bundle)
        remindIntent.action = PushTemplateConstants.IntentActions.REMIND_LATER_CLICKED
        NotificationBuilder.handleRemindIntent(
            remindIntent,
            broadcastReceiverClass
        )

        verify(exactly = 0) { PendingIntentUtils.scheduleNotification(any(), any(), any(), any()) }
        verify(exactly = 0) { mockNotificationManagerCompat.cancel(any()) }
    }

    @Test(expected = NotificationConstructionFailedException::class)
    fun `handleRemindIntent given an intent with no bundle should throw an exception`() {
        val remindIntent = Intent()
        remindIntent.action = PushTemplateConstants.IntentActions.REMIND_LATER_CLICKED
        NotificationBuilder.handleRemindIntent(
            remindIntent,
            broadcastReceiverClass
        )

        verify(exactly = 0) { PendingIntentUtils.scheduleNotification(any(), any(), any(), any()) }
        verify(exactly = 0) { mockNotificationManagerCompat.cancel(any()) }
    }

    private fun setNullContext() {
        val mockAppContextService = mockk<AppContextService>()
        val mockServiceProvider = mockkClass(ServiceProvider::class)
        mockkStatic(ServiceProvider::getInstance)
        every { ServiceProvider.getInstance() } returns mockServiceProvider
        every { mockServiceProvider.appContextService } returns mockAppContextService
        every { mockAppContextService.applicationContext } returns null
    }
}
