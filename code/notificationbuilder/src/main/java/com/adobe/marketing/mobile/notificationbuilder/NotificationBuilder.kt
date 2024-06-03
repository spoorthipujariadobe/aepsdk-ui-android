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

package com.adobe.marketing.mobile.notificationbuilder

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.adobe.marketing.mobile.notificationbuilder.NotificationBuilder.constructNotificationBuilder
import com.adobe.marketing.mobile.notificationbuilder.PushTemplateConstants.LOG_TAG
import com.adobe.marketing.mobile.notificationbuilder.internal.PushTemplateType
import com.adobe.marketing.mobile.notificationbuilder.internal.builders.AutoCarouselNotificationBuilder
import com.adobe.marketing.mobile.notificationbuilder.internal.builders.BasicNotificationBuilder
import com.adobe.marketing.mobile.notificationbuilder.internal.builders.InputBoxNotificationBuilder
import com.adobe.marketing.mobile.notificationbuilder.internal.builders.LegacyNotificationBuilder
import com.adobe.marketing.mobile.notificationbuilder.internal.builders.ManualCarouselNotificationBuilder
import com.adobe.marketing.mobile.notificationbuilder.internal.builders.ProductCatalogNotificationBuilder
import com.adobe.marketing.mobile.notificationbuilder.internal.builders.ProductRatingNotificationBuilder
import com.adobe.marketing.mobile.notificationbuilder.internal.builders.TimerNotificationBuilder
import com.adobe.marketing.mobile.notificationbuilder.internal.builders.ZeroBezelNotificationBuilder
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.AEPPushTemplate
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.AutoCarouselPushTemplate
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.BasicPushTemplate
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.CarouselPushTemplate
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.InputBoxPushTemplate
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.ManualCarouselPushTemplate
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.ProductCatalogPushTemplate
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.ProductRatingPushTemplate
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.TimerPushTemplate
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.ZeroBezelPushTemplate
import com.adobe.marketing.mobile.notificationbuilder.internal.util.IntentData
import com.adobe.marketing.mobile.notificationbuilder.internal.util.MapData
import com.adobe.marketing.mobile.notificationbuilder.internal.util.NotificationData
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider
import java.util.Calendar

/**
 * Public facing object to construct a [NotificationCompat.Builder] object for the specified [PushTemplateType].
 * The [constructNotificationBuilder] methods will build the appropriate notification based on the provided
 * [AEPPushTemplate] or [Intent].
 */
object NotificationBuilder {
    private const val SELF_TAG = "NotificationBuilder"
    private const val VERSION = "3.0.0"

    @JvmStatic
    fun version(): String {
        return VERSION
    }

    /**
     * Constructs a [NotificationCompat.Builder] object from the provided [messageData]
     *
     * @param messageData [Map] containing the data needed for the notification construction
     * @param trackerActivityClass [Class] of the [Activity] to be launched when the notification is clicked
     * @param broadcastReceiverClass [Class] of the [BroadcastReceiver] to be used for handling notification actions
     * @return [NotificationCompat.Builder] object
     * @throws [NotificationConstructionFailedException] if the notification construction fails due to missing data
     * @throws [IllegalArgumentException] if the provided message data has invalid data
     */
    @Throws(NotificationConstructionFailedException::class, IllegalArgumentException::class)
    @JvmStatic
    fun constructNotificationBuilder(
        messageData: Map<String, String>,
        trackerActivityClass: Class<out Activity>?,
        broadcastReceiverClass: Class<out BroadcastReceiver>?
    ): NotificationCompat.Builder {
        val context = ServiceProvider.getInstance().appContextService.applicationContext
            ?: throw NotificationConstructionFailedException("Application context is null, cannot build a notification.")
        if (messageData.isEmpty()) {
            throw NotificationConstructionFailedException("Message data is empty, cannot build a notification.")
        }
        val notificationData = MapData(messageData)
        return getNotificationBuilder(context, notificationData, trackerActivityClass, broadcastReceiverClass)
    }

    /**
     * Constructs a [NotificationCompat.Builder] object from the provided [intent]
     *
     * @param intent [Intent] containing the data needed for the notification construction
     * @param trackerActivityClass [Class] of the [Activity] to be launched when the notification is clicked
     * @param broadcastReceiverClass [Class] of the [BroadcastReceiver] to be used for handling notification actions
     * @return [NotificationCompat.Builder] object
     * @throws [NotificationConstructionFailedException] if the notification construction fails due to missing data
     * @throws [IllegalArgumentException] if the provided message data has invalid data
     */
    @Throws(NotificationConstructionFailedException::class, IllegalArgumentException::class)
    @JvmStatic
    fun constructNotificationBuilder(
        intent: Intent,
        trackerActivityClass: Class<out Activity>?,
        broadcastReceiverClass: Class<out BroadcastReceiver>?
    ): NotificationCompat.Builder {
        val context = ServiceProvider.getInstance().appContextService.applicationContext
            ?: throw NotificationConstructionFailedException("Application context is null, cannot build a notification.")
        val extras = intent.extras ?: throw NotificationConstructionFailedException("Intent extras are null, cannot re-build the notification.")
        val intentData = IntentData(extras, intent.action)
        return getNotificationBuilder(context, intentData, trackerActivityClass, broadcastReceiverClass)
    }

    /**
     * Handles the remind later intent by scheduling a [PendingIntent] to the [broadcastReceiverClass]
     * which will be fire at a time specified in the [remindLaterIntent].
     *
     * Once the PendingIntent is fired, the [broadcastReceiverClass] is responsible for
     * reconstructing the notification and displaying it .
     *
     * @param remindLaterIntent [Intent] containing the data needed to schedule and recreate the notification
     * @param broadcastReceiverClass [Class] of the [BroadcastReceiver] that will be fired when the [PendingIntent] resolves at a later time
     */
    @Throws(NotificationConstructionFailedException::class)
    @JvmStatic
    fun handleRemindIntent(
        remindLaterIntent: Intent,
        broadcastReceiverClass: Class<out BroadcastReceiver>?
    ) {
        val context = ServiceProvider.getInstance().appContextService.applicationContext
            ?: throw NotificationConstructionFailedException("Application context is null, cannot schedule notification for later.")
        if (broadcastReceiverClass == null) {
            Log.trace(
                LOG_TAG,
                SELF_TAG,
                "Broadcast receiver class is null, will not schedule a notification from the received" +
                    " intent with action %s",
                remindLaterIntent.action
            )
            return
        }
        // get basic notification values from the intent extras
        val intentExtras = remindLaterIntent.extras
        if (intentExtras == null) {
            Log.trace(
                LOG_TAG,
                SELF_TAG,
                "Intent extras are null, will not schedule a notification from the received" +
                    " intent with action %s",
                remindLaterIntent.action
            )
            return
        }

        // set the calender time to the remind timestamp to allow the notification to be displayed
        // at the later time
        val remindLaterTimestamp =
            intentExtras.getString(PushTemplateConstants.PushPayloadKeys.REMIND_LATER_TIMESTAMP)?.toLongOrNull() ?: 0
        val remindLaterDuration =
            intentExtras.getString(PushTemplateConstants.PushPayloadKeys.REMIND_LATER_DURATION)?.toLongOrNull() ?: 0

        if (remindLaterTimestamp <= 0 && remindLaterDuration <= 0) {
            Log.trace(
                LOG_TAG,
                SELF_TAG,
                "Remind later timestamp or duration is less than or equal to current timestamp" +
                    "will not schedule a notification from the received intent with action %s",
                remindLaterIntent.action
            )
            return
        }
        val calendar: Calendar = Calendar.getInstance()
        val notificationManager = NotificationManagerCompat.from(context)

        // get the tag from the intent extras
        val tag = intentExtras.getString(PushTemplateConstants.PushPayloadKeys.TAG)

        // calculate difference in fire date from the current date if timestamp is provided
        val secondsUntilFireDate: Long = if (remindLaterDuration > 0) remindLaterDuration
        else remindLaterTimestamp - calendar.timeInMillis / 1000

        // if fire date is greater than 0 then we want to schedule a reminder notification.
        if (secondsUntilFireDate <= 0) {
            Log.trace(
                LOG_TAG,
                SELF_TAG,
                "Remind later date is before the current date. Will not reschedule the" +
                    " notification.",
                secondsUntilFireDate
            )
            // cancel the displayed notification
            notificationManager.cancel(tag.hashCode())
            return
        }
        Log.trace(
            LOG_TAG,
            SELF_TAG,
            "Remind later pressed, will reschedule the notification to be displayed %d" +
                " seconds from now",
            secondsUntilFireDate
        )
        calendar.add(Calendar.SECOND, secondsUntilFireDate.toInt())

        // schedule a pending intent to be broadcast at the specified timestamp
        val scheduledIntent = Intent(PushTemplateConstants.IntentActions.SCHEDULED_NOTIFICATION_BROADCAST)
        broadcastReceiverClass.let {
            scheduledIntent.setClass(context.applicationContext, broadcastReceiverClass)
        }
        scheduledIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        scheduledIntent.putExtras(intentExtras)

        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            scheduledIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                Log.trace(
                    LOG_TAG,
                    SELF_TAG,
                    "Exact alarms are permitted, scheduling an exact alarm for the current notification."
                )
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
                )
            } else {
                // schedule an inexact alarm for the current notification
                Log.trace(
                    LOG_TAG,
                    SELF_TAG,
                    "Exact alarms are not permitted, scheduling an inexact alarm for the current notification."
                )
                alarmManager[AlarmManager.RTC_WAKEUP, calendar.timeInMillis] =
                    pendingIntent
            }
            // cancel the displayed notification
            notificationManager.cancel(tag.hashCode())
        }
    }

    private fun getNotificationBuilder(
        context: Context,
        notificationData: NotificationData,
        trackerActivityClass: Class<out Activity>?,
        broadcastReceiverClass: Class<out BroadcastReceiver>?
    ): NotificationCompat.Builder {

        val pushTemplateType =
            PushTemplateType.fromString(notificationData.getString(PushTemplateConstants.PushPayloadKeys.TEMPLATE_TYPE))

        when (pushTemplateType) {
            PushTemplateType.BASIC -> {
                val basicPushTemplate = BasicPushTemplate(notificationData)
                return BasicNotificationBuilder.construct(
                    context,
                    basicPushTemplate,
                    trackerActivityClass,
                    broadcastReceiverClass
                )
            }

            PushTemplateType.CAROUSEL -> {
                val carouselPushTemplate =
                    CarouselPushTemplate(notificationData)

                when (carouselPushTemplate) {
                    is AutoCarouselPushTemplate -> {
                        return AutoCarouselNotificationBuilder.construct(
                            context,
                            carouselPushTemplate,
                            trackerActivityClass,
                            broadcastReceiverClass
                        )
                    }

                    is ManualCarouselPushTemplate -> {
                        return ManualCarouselNotificationBuilder.construct(
                            context,
                            carouselPushTemplate,
                            trackerActivityClass,
                            broadcastReceiverClass
                        )
                    }

                    else -> {
                        Log.trace(
                            LOG_TAG,
                            SELF_TAG,
                            "Unknown carousel push template type, creating a legacy style notification."
                        )
                        return LegacyNotificationBuilder.construct(
                            context,
                            BasicPushTemplate(notificationData),
                            trackerActivityClass
                        )
                    }
                }
            }

            PushTemplateType.ZERO_BEZEL -> {
                val zeroBezelPushTemplate = ZeroBezelPushTemplate(notificationData)
                return ZeroBezelNotificationBuilder.construct(
                    context,
                    zeroBezelPushTemplate,
                    trackerActivityClass
                )
            }

            PushTemplateType.INPUT_BOX -> {
                return InputBoxNotificationBuilder.construct(
                    context,
                    InputBoxPushTemplate(notificationData),
                    trackerActivityClass,
                    broadcastReceiverClass
                )
            }

            PushTemplateType.PRODUCT_CATALOG -> {
                return ProductCatalogNotificationBuilder.construct(
                    context,
                    ProductCatalogPushTemplate(notificationData),
                    trackerActivityClass,
                    broadcastReceiverClass
                )
            }

            PushTemplateType.PRODUCT_RATING -> {
                return ProductRatingNotificationBuilder.construct(
                    context,
                    ProductRatingPushTemplate(notificationData),
                    trackerActivityClass,
                    broadcastReceiverClass
                )
            }

            PushTemplateType.TIMER -> {
                return TimerNotificationBuilder.construct(
                    context,
                    TimerPushTemplate(notificationData),
                    trackerActivityClass,
                    broadcastReceiverClass
                )
            }

            PushTemplateType.UNKNOWN -> {
                return LegacyNotificationBuilder.construct(
                    context,
                    BasicPushTemplate(notificationData),
                    trackerActivityClass
                )
            }
        }
    }
}
