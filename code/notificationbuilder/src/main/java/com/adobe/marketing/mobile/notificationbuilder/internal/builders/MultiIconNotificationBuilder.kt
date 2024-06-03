package com.adobe.marketing.mobile.notificationbuilder.internal.builders

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.notificationbuilder.NotificationConstructionFailedException
import com.adobe.marketing.mobile.notificationbuilder.R
import com.adobe.marketing.mobile.notificationbuilder.internal.PushTemplateConstants
import com.adobe.marketing.mobile.notificationbuilder.internal.PushTemplateImageUtils
import com.adobe.marketing.mobile.notificationbuilder.internal.extensions.createNotificationChannelIfRequired
import com.adobe.marketing.mobile.notificationbuilder.internal.extensions.setBundledImage
import com.adobe.marketing.mobile.notificationbuilder.internal.extensions.setRemoteImage
import com.adobe.marketing.mobile.notificationbuilder.internal.extensions.setRemoteViewClickAction
import com.adobe.marketing.mobile.notificationbuilder.internal.extensions.setRemoteViewImage
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.MultiIconPushTemplate
import com.adobe.marketing.mobile.services.Log

internal object MultiIconNotificationBuilder {
    const val SELF_TAG = "MultiIconNotificationBuilder"

    fun construct(
        context: Context,
        pushTemplate: MultiIconPushTemplate,
        trackerActivityClass: Class<out Activity>?
    ): NotificationCompat.Builder {

        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Building an icon template push notification."
        )

        val packageName = context.packageName
        val notificationLayout = RemoteViews(packageName, R.layout.push_template_multi_icon)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelIdToUse: String = notificationManager.createNotificationChannelIfRequired(
            context,
            pushTemplate
        )

        populateIconsForMultiIconTemplate(
            context,
            trackerActivityClass,
            notificationLayout,
            pushTemplate,
            pushTemplate.templateItemList,
            packageName
        )

        notificationLayout.setRemoteViewClickAction(
            context,
            trackerActivityClass,
            R.id.five_icon_close_button,
            null,
            null,
            pushTemplate.tag,
            false
        )


        return AEPPushNotificationBuilder.construct(
            context,
            pushTemplate,
            channelIdToUse,
            trackerActivityClass,
            notificationLayout,
            notificationLayout,
            R.id.carousel_container_layout
        )

    }

    private fun populateIconsForMultiIconTemplate(
        context: Context,
        trackerActivityClass: Class<out Activity>?,
        notificationLayout: RemoteViews,
        pushTemplate: MultiIconPushTemplate,
        items: MutableList<MultiIconPushTemplate.MultiIconTemplateItem>,
        packageName: String?
    ){
        var validImagesAddedCount = 0
        for (item in items) {
            val imageUri: String = item.iconUrl
            val iconItem = RemoteViews(packageName, R.layout.multi_icon_template_item)
            if(iconItem.setRemoteViewImage(imageUri, R.id.icon_item_image_view)) {
                validImagesAddedCount++
            }

            trackerActivityClass?.let {
                val interactionUri = item.actionUri ?: pushTemplate.actionUri
                iconItem.setRemoteViewClickAction(
                    context,
                    trackerActivityClass,
                    R.id.icon_item_image_view,
                    interactionUri,
                    null,
                    pushTemplate.tag,
                    pushTemplate.isNotificationSticky ?: false
                )
            }
            notificationLayout.addView(R.id.icons_layout_linear, iconItem)
        }
        if (validImagesAddedCount < 3) {
            throw NotificationConstructionFailedException("Valid icons are less then 3, cannot build a notification.")
        }
    }
}