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
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.notificationbuilder.NotificationConstructionFailedException
import com.adobe.marketing.mobile.notificationbuilder.PushTemplateIntentConstants
import com.adobe.marketing.mobile.notificationbuilder.R
import com.adobe.marketing.mobile.notificationbuilder.internal.PushTemplateConstants
import com.adobe.marketing.mobile.notificationbuilder.internal.PushTemplateConstants.LOG_TAG
import com.adobe.marketing.mobile.notificationbuilder.internal.PushTemplateImageUtils
import com.adobe.marketing.mobile.notificationbuilder.internal.extensions.createNotificationChannelIfRequired
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.TimerPushTemplate
import com.adobe.marketing.mobile.services.Log

private const val TAG = "TimerNotificationBuilder"

/**
 * Object responsible for constructing a [NotificationCompat.Builder] object containing a timer push template notification.
 */
internal object TimerNotificationBuilder {

    data class TimerContent(
        val title: String,
        val body: String?,
        val expandedBody: String?,
        val imageUrl: String?
    )

    /**
     * Constructs a notification for the timer push template
     *
     * @param context the context
     * @param template the timer push template
     * @param trackerActivityClass the tracker
     * @param broadcastReceiverClass the broadcast receiver
     * @return the notification builder
     */
    @Throws(NotificationConstructionFailedException::class)
    fun construct(
        context: Context,
        template: TimerPushTemplate,
        trackerActivityClass: Class<out Activity>?,
        broadcastReceiverClass: Class<out BroadcastReceiver>?
    ): NotificationCompat.Builder {

        Log.trace(LOG_TAG, TAG, "Building a timer template push notification.")

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // create the notification channel if needed
        val channelIdToUse = notificationManager.createNotificationChannelIfRequired(context, template)

        // check if the template is expired
        val isExpired = template.isExpired()
        val (smallLayout, expandedLayout) = initializeLayouts(context, isExpired)

        // create the notification builder with the common settings applied
        val notificationBuilder = AEPPushNotificationBuilder.construct(
            context,
            template,
            channelIdToUse,
            trackerActivityClass,
            smallLayout,
            expandedLayout,
            R.id.basic_expanded_layout
        )

        // create the timer content according to the expiry time
        val timerContent = if (isExpired) {
            TimerContent(template.alternateTitle, template.alternateBody, template.alternateExpandedBody, template.alternateImage)
        } else {
            TimerContent(template.title, template.body, template.expandedBodyText, template.imageUrl)
        }

        // add text to collapsed layout
        smallLayout.setTextViewText(R.id.notification_title, timerContent.title)
        smallLayout.setTextViewText(R.id.notification_body, timerContent.body)

        // add text to expanded layout
        expandedLayout.setTextViewText(R.id.notification_title, timerContent.title)
        expandedLayout.setTextViewText(R.id.notification_body_expanded, timerContent.expandedBody)

        // download the image for expanded layout
        val downloadedImageCount = PushTemplateImageUtils.cacheImages(listOf(timerContent.imageUrl))
        if (downloadedImageCount == 0) {
            Log.trace(LOG_TAG, TAG, "Timer push template unable to download the image for url : " + timerContent.imageUrl)
            expandedLayout.setViewVisibility(R.id.expanded_template_image, View.GONE)
        } else {
            expandedLayout.setImageViewBitmap(R.id.expanded_template_image, PushTemplateImageUtils.getCachedImage(timerContent.imageUrl))
        }

        if (!isExpired) {
            // set the timer clock
            setTimerClock(smallLayout, expandedLayout, template)

            // create the intent for the timer expiry
            val intent = createIntent(template)
            broadcastReceiverClass?.let {
                intent.setClass(context, broadcastReceiverClass)
            }

            // create the pending intent for the timer expiry
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            // set the alarm manager to trigger the intent at the expiry time
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val triggerTime = System.currentTimeMillis() + (template.remainingTimeInSeconds * 1000)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
        return notificationBuilder
    }

    /**
     * Initializes the layouts for the notification
     *
     * @param context the context
     * @param isExpired whether the template is expired
     * @return the collapsed and expanded layouts
     */
    private fun initializeLayouts(context: Context, isExpired: Boolean): Pair<RemoteViews, RemoteViews> {
        val packageName = context.packageName
        val smallLayoutRes = if (isExpired) R.layout.push_template_collapsed else R.layout.push_template_timer_collapsed
        val expandedLayoutRes = if (isExpired) R.layout.push_template_expanded else R.layout.push_template_timer_expanded

        val smallLayout = RemoteViews(packageName, smallLayoutRes)
        val expandedLayout = RemoteViews(packageName, expandedLayoutRes)

        return smallLayout to expandedLayout
    }

    /**
     * Sets the timer on the notification
     *
     * @param smallLayout the collapsed layout
     * @param expandedLayout the expanded layout
     * @param template the timer push template
     */
    private fun setTimerClock(smallLayout: RemoteViews, expandedLayout: RemoteViews, template: TimerPushTemplate) {
        val remainingTime = SystemClock.elapsedRealtime() + (template.remainingTimeInSeconds * 1000)

        smallLayout.setChronometer(R.id.timer_text, remainingTime, null, true)
        expandedLayout.setChronometer(R.id.timer_text, remainingTime, null, true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            smallLayout.setChronometerCountDown(R.id.timer_text, true)
            expandedLayout.setChronometerCountDown(R.id.timer_text, true)
        }
    }

    /**
     * Creates an intent for the timer expiry
     *
     * @param template the timer push template
     * @return the intent for the timer expiry
     */
    private fun createIntent(template: TimerPushTemplate): Intent {
        val intent = AEPPushNotificationBuilder.createIntent(PushTemplateIntentConstants.IntentActions.TIMER_EXPIRED, template)
        intent.putExtra(PushTemplateConstants.PushPayloadKeys.TEMPLATE_TYPE, template.templateType?.value)
        intent.putExtra(PushTemplateConstants.TimerKeys.ALTERNATE_TITLE, template.alternateTitle)
        intent.putExtra(PushTemplateConstants.TimerKeys.ALTERNATE_BODY, template.alternateBody)
        intent.putExtra(PushTemplateConstants.TimerKeys.ALTERNATE_EXPANDED_BODY, template.alternateExpandedBody)
        intent.putExtra(PushTemplateConstants.TimerKeys.ALTERNATE_IMAGE, template.alternateImage)
        intent.putExtra(PushTemplateConstants.TimerKeys.TIMER_COLOR, template.timerColor)
        return intent
    }
}
