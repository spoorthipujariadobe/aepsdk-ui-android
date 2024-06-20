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

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class PendingIntentUtilsTest {

    private lateinit var mockContext: Context
    private lateinit var scheduledIntent: Intent
    private lateinit var broadcastReceiverClass: Class<out BroadcastReceiver>

    @Before
    fun setup() {
        mockContext = mockk<Context>(relaxed = true)
        scheduledIntent = Intent()
        broadcastReceiverClass = BroadcastReceiver::class.java
    }

    @After
    fun teardown() {
        // reset the mock
        unmockkAll()
    }

    @Test
    @Config(sdk = [31])
    fun `scheduleNotification schedules exact alarm when exact alarms are allowed`() {
        val triggerAtSeconds = 1000L
        val mockAlarmManager = mockk<AlarmManager>(relaxed = true)
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager
        every { mockAlarmManager.canScheduleExactAlarms() } returns true

        PendingIntentUtils.scheduleNotification(
            mockContext,
            scheduledIntent,
            broadcastReceiverClass,
            triggerAtSeconds
        )

        val pendingIntentCapture = slot<PendingIntent>()
        verify(exactly = 1) {
            mockAlarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtSeconds * 1000,
                capture(pendingIntentCapture)
            )
        }
        verifyScheduledPendingIntent(pendingIntentCapture.captured)
    }

    @Test
    @Config(sdk = [31])
    fun `scheduleNotification schedules inexact alarm when exact alarms are not allowed`() {
        val triggerAtSeconds = 1000L
        val mockAlarmManager = mockk<AlarmManager>(relaxed = true)
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager
        every { mockAlarmManager.canScheduleExactAlarms() } returns false

        PendingIntentUtils.scheduleNotification(
            mockContext,
            scheduledIntent,
            broadcastReceiverClass,
            triggerAtSeconds
        )

        val pendingIntentCapture = slot<PendingIntent>()
        verify(exactly = 1) {
            mockAlarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtSeconds * 1000,
                capture(pendingIntentCapture)
            )
        }

        verifyScheduledPendingIntent(pendingIntentCapture.captured)
    }

    @Test
    @Config(sdk = [30])
    fun `scheduleNotification schedules exact alarm when version is less than 31`() {
        val triggerAtSeconds = 1000L
        val mockAlarmManager = mockk<AlarmManager>(relaxed = true)
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager

        PendingIntentUtils.scheduleNotification(
            mockContext,
            scheduledIntent,
            broadcastReceiverClass,
            triggerAtSeconds
        )

        val pendingIntentCapture = slot<PendingIntent>()
        verify(exactly = 1) {
            mockAlarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtSeconds * 1000,
                capture(pendingIntentCapture)
            )
        }
        verifyScheduledPendingIntent(pendingIntentCapture.captured)
    }

    @Test
    @Config(sdk = [22])
    fun `scheduleNotification schedules exact alarm when version is less than 23`() {
        val triggerAtSeconds = 1000L
        val mockAlarmManager = mockk<AlarmManager>(relaxed = true)
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager

        PendingIntentUtils.scheduleNotification(
            mockContext,
            scheduledIntent,
            broadcastReceiverClass,
            triggerAtSeconds
        )

        val pendingIntentCapture = slot<PendingIntent>()
        verify(exactly = 1) {
            mockAlarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAtSeconds * 1000,
                capture(pendingIntentCapture)
            )
        }
        verifyScheduledPendingIntent(pendingIntentCapture.captured)
    }

    @Test
    @Config(sdk = [31])
    fun `scheduleNotification schedules exact alarm when broadcastReceiverClass is null`() {
        val triggerAtSeconds = 1000L
        val mockAlarmManager = mockk<AlarmManager>(relaxed = true)
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager
        every { mockAlarmManager.canScheduleExactAlarms() } returns true

        PendingIntentUtils.scheduleNotification(
            mockContext,
            scheduledIntent,
            null,
            triggerAtSeconds
        )

        val pendingIntentCapture = slot<PendingIntent>()
        verify(exactly = 1) {
            mockAlarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtSeconds * 1000,
                capture(pendingIntentCapture)
            )
        }
        val pendingIntent = pendingIntentCapture.captured
        assertNotNull(pendingIntent)
        val shadowPendingIntent = Shadows.shadowOf(pendingIntent)
        assertTrue(shadowPendingIntent.isBroadcastIntent)
        assertEquals(mockContext, shadowPendingIntent.savedContext)
        assertEquals(
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            shadowPendingIntent.flags
        )

        val intent = shadowPendingIntent.savedIntent
        assertEquals(null, intent.component?.className)
        assertEquals(scheduledIntent, intent)
    }

    @Test
    @Config(sdk = [22])
    fun `scheduleNotification does not schedule alarm when AlarmManager is null`() {
        val triggerAtSeconds = 1000L
        val mockAlarmManager = mockk<AlarmManager>(relaxed = true)
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns null

        PendingIntentUtils.scheduleNotification(
            mockContext,
            scheduledIntent,
            broadcastReceiverClass,
            triggerAtSeconds
        )

        verify(exactly = 0) {
            mockAlarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAtSeconds * 1000,
                any()
            )
        }
    }

    @Test
    @Config(sdk = [30])
    fun `isExactAlarmsAllowed returns true when SDK version is less than S`() {
        val context = RuntimeEnvironment.getApplication()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        val result = PendingIntentUtils.isExactAlarmsAllowed(alarmManager)

        assertTrue(result)
    }

    @Test
    @Config(sdk = [31])
    fun `isExactAlarmsAllowed returns true when canScheduleExactAlarms is true`() {
        val alarmManager = mockk<AlarmManager>()
        every { alarmManager.canScheduleExactAlarms() } returns true

        val result = PendingIntentUtils.isExactAlarmsAllowed(alarmManager)

        assertTrue(result)
    }

    @Test
    @Config(sdk = [31])
    fun `isExactAlarmsAllowed returns false when canScheduleExactAlarms is false`() {
        val alarmManager = mockk<AlarmManager>()
        every { alarmManager.canScheduleExactAlarms() } returns false

        val result = PendingIntentUtils.isExactAlarmsAllowed(alarmManager)

        assertFalse(result)
    }

    @Test
    @Config(sdk = [31])
    fun `isExactAlarmsAllowed returns false when alarmManager is null and SDK version is S or higher`() {
        val result = PendingIntentUtils.isExactAlarmsAllowed(null)

        assertFalse(result)
    }

    private fun verifyScheduledPendingIntent(pendingIntent: PendingIntent) {
        assertNotNull(pendingIntent)
        val shadowPendingIntent = Shadows.shadowOf(pendingIntent)
        assertTrue(shadowPendingIntent.isBroadcastIntent)
        assertEquals(mockContext, shadowPendingIntent.savedContext)
        assertEquals(
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            shadowPendingIntent.flags
        )

        val intent = shadowPendingIntent.savedIntent
        assertEquals(broadcastReceiverClass.name, intent.component?.className)
        assertEquals(scheduledIntent, intent)
    }
}
