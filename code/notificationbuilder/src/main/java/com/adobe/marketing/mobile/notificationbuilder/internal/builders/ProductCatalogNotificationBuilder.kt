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
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.notificationbuilder.NotificationConstructionFailedException
import com.adobe.marketing.mobile.notificationbuilder.PushTemplateIntentConstants
import com.adobe.marketing.mobile.notificationbuilder.R
import com.adobe.marketing.mobile.notificationbuilder.internal.PendingIntentUtils
import com.adobe.marketing.mobile.notificationbuilder.internal.PushTemplateConstants
import com.adobe.marketing.mobile.notificationbuilder.internal.PushTemplateConstants.LOG_TAG
import com.adobe.marketing.mobile.notificationbuilder.internal.PushTemplateImageUtils
import com.adobe.marketing.mobile.notificationbuilder.internal.extensions.createNotificationChannelIfRequired
import com.adobe.marketing.mobile.notificationbuilder.internal.extensions.setElementColor
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.ProductCatalogPushTemplate
import com.adobe.marketing.mobile.services.Log

/**
 * Object responsible for constructing a [NotificationCompat.Builder] object containing a product catalog template notification.
 */
internal object ProductCatalogNotificationBuilder {
    private const val SELF_TAG = "ProductCatalogNotificationBuilder"
    private val intentActionList = listOf(
        PushTemplateIntentConstants.IntentActions.CATALOG_THUMBNAIL_1_CLICKED,
        PushTemplateIntentConstants.IntentActions.CATALOG_THUMBNAIL_2_CLICKED,
        PushTemplateIntentConstants.IntentActions.CATALOG_THUMBNAIL_3_CLICKED
    )

    @Throws(NotificationConstructionFailedException::class)
    fun construct(
        context: Context,
        pushTemplate: ProductCatalogPushTemplate,
        trackerActivityClass: Class<out Activity>?,
        broadcastReceiverClass: Class<out BroadcastReceiver>?
    ): NotificationCompat.Builder {
        Log.trace(
            LOG_TAG,
            SELF_TAG,
            "Building a product catalog push notification."
        )
        val packageName = context.packageName
        val smallLayout = RemoteViews(packageName, R.layout.push_template_collapsed)
        val expandedLayout = if (pushTemplate.displayLayout == "vertical") {
            RemoteViews(packageName, R.layout.push_tempate_vertical_catalog)
        } else {
            RemoteViews(packageName, R.layout.push_template_horizontal_catalog)
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelIdToUse: String = notificationManager.createNotificationChannelIfRequired(context, pushTemplate)

        // create the notification builder with the common settings applied
        val notificationBuilder = AEPPushNotificationBuilder.construct(
            context,
            pushTemplate,
            channelIdToUse,
            trackerActivityClass,
            smallLayout,
            expandedLayout,
            R.id.catalog_container_layout
        )

        val catalogItems = pushTemplate.catalogItems
        // downloads all the catalog item images then sets the currently selected product
        // within the main product catalog image view
        val downloadedImageCount = populateCenterImage(
            context,
            trackerActivityClass,
            expandedLayout,
            pushTemplate,
            catalogItems
        )
        if (downloadedImageCount != catalogItems.size) {
            Log.error(
                LOG_TAG,
                SELF_TAG,
                "Failed to download all images for the product catalog notification."
            )
            throw NotificationConstructionFailedException("Failed to download all images for the product catalog notification.")
        }

        // set the product title, description, and price
        expandedLayout.setTextViewText(
            R.id.product_title,
            catalogItems[pushTemplate.currentIndex].title
        )
        expandedLayout.setTextViewText(
            R.id.product_description,
            catalogItems[pushTemplate.currentIndex].body
        )
        expandedLayout.setTextViewText(
            R.id.product_price,
            catalogItems[pushTemplate.currentIndex].price
        )

        // setup the CTA button
        setupCtaButton(context, trackerActivityClass, expandedLayout, pushTemplate)

        // populate product thumbnails
        populateThumbnails(
            context,
            broadcastReceiverClass,
            channelIdToUse,
            expandedLayout,
            catalogItems,
            pushTemplate
        )

        return notificationBuilder
    }

    /**
     * Sets the product catalog center image and its click action.
     *
     * @param context the application [Context]
     * @param trackerActivityClass the [Class] of the activity to set in the created pending intent for tracking purposes
     * @param expandedLayout the [RemoteViews] object containing the expanded layout of the push template notification
     * @param pushTemplate the [ProductCatalogPushTemplate] object containing the product catalog push template data
     * @param catalogItems the list of [ProductCatalogPushTemplate.CatalogItem] objects containing the product catalog items
     * @return the number of images downloaded
     */
    private fun populateCenterImage(
        context: Context,
        trackerActivityClass: Class<out Activity>?,
        expandedLayout: RemoteViews,
        pushTemplate: ProductCatalogPushTemplate,
        catalogItems: List<ProductCatalogPushTemplate.CatalogItem>
    ): Int {
        Log.trace(
            LOG_TAG,
            SELF_TAG,
            "Populating center image for product catalog notification."
        )

        val catalogImageUris = catalogItems.map { it.img }
        val downloadedImageCount = PushTemplateImageUtils.cacheImages(catalogImageUris)
        if (downloadedImageCount == catalogImageUris.size) {
            val pushImage =
                PushTemplateImageUtils.getCachedImage(catalogItems[pushTemplate.currentIndex].img)
            expandedLayout.setImageViewBitmap(R.id.product_image, pushImage)
            expandedLayout.setOnClickPendingIntent(
                R.id.product_image,
                PendingIntentUtils.createPendingIntent(
                    context,
                    trackerActivityClass,
                    catalogItems[pushTemplate.currentIndex].uri,
                    PushTemplateConstants.CatalogActionIds.PRODUCT_IMAGE_CLICKED,
                    pushTemplate.tag,
                    pushTemplate.isNotificationSticky ?: false
                )
            )
        }
        return downloadedImageCount
    }

    /**
     * Sets the product catalog thumbnails and their click actions.
     *
     * @param context the application [Context]
     * @param broadcastReceiverClass the [Class] of the broadcast receiver to set in the created pending intent
     * @param channelIdToUse [String] containing the notification channel ID
     * @param expandedLayout the [RemoteViews] object containing the expanded layout of the push template notification
     * @param catalogItems the list of [ProductCatalogPushTemplate.CatalogItem] objects containing the product catalog items
     * @param pushTemplate the [ProductCatalogPushTemplate] object containing the product catalog push template data
     */
    private fun populateThumbnails(
        context: Context,
        broadcastReceiverClass: Class<out BroadcastReceiver>?,
        channelIdToUse: String,
        expandedLayout: RemoteViews,
        catalogItems: List<ProductCatalogPushTemplate.CatalogItem>,
        pushTemplate: ProductCatalogPushTemplate
    ) {
        Log.trace(
            LOG_TAG,
            SELF_TAG,
            "Populating product catalog thumbnails."
        )

        val thumbIds = listOf(
            R.id.product_thumbnail_1,
            R.id.product_thumbnail_2,
            R.id.product_thumbnail_3
        )
        for (index in catalogItems.indices) {
            val thumbImage = PushTemplateImageUtils.getCachedImage(catalogItems[index].img)

            if (thumbImage == null) {
                Log.trace(
                    LOG_TAG,
                    SELF_TAG,
                    "No image found for catalog item thumbnail."
                )
            } else {
                expandedLayout.setImageViewBitmap(thumbIds[index], thumbImage)
            }

            // create a pending intent for the thumbnail
            val pendingIntentThumbnailInteraction = createThumbnailInteractionPendingIntent(
                context,
                broadcastReceiverClass,
                channelIdToUse,
                pushTemplate,
                index
            )
            expandedLayout.setOnClickPendingIntent(
                thumbIds[index],
                pendingIntentThumbnailInteraction
            )
        }
    }

    /**
     * Sets the CTA button for the product catalog notification.
     *
     * @param context the application [Context]
     * @param trackerActivityClass the [Class] of the activity to set in the created pending intent for tracking purposes
     * @param expandedLayout the [RemoteViews] object containing the expanded layout of the push template notification
     * @param pushTemplate the [ProductCatalogPushTemplate] object containing the product catalog push template data
     */
    private fun setupCtaButton(
        context: Context,
        trackerActivityClass: Class<out Activity>?,
        expandedLayout: RemoteViews,
        pushTemplate: ProductCatalogPushTemplate
    ) {
        // apply the color to the cta button
        expandedLayout.setCtaButtonColor(R.id.cta_button, pushTemplate.ctaButtonColor)

        // apply text to the cta button
        expandedLayout.setTextViewText(R.id.cta_button, pushTemplate.ctaButtonText)

        // apply the open uri action to the cta button
        expandedLayout.setOnClickPendingIntent(
            R.id.cta_button,
            PendingIntentUtils.createPendingIntent(
                context,
                trackerActivityClass,
                pushTemplate.ctaButtonUri,
                PushTemplateConstants.CatalogActionIds.CTA_BUTTON_CLICKED,
                pushTemplate.tag,
                pushTemplate.isNotificationSticky ?: false
            )
        )
    }

    /**
     * Sets custom colors to the product catalog CTA button.
     *
     * @param containerViewId [Int] containing the resource id of the push template notification RemoteViews
     * @param buttonColor [String] containing the hex color code for the cta button
     */
    private fun RemoteViews.setCtaButtonColor(
        containerViewId: Int,
        buttonColor: String?
    ) {
        // get custom color from hex string and set it the cta button
        if (buttonColor.isNullOrEmpty()) {
            Log.trace(
                LOG_TAG,
                SELF_TAG,
                "Empty cta button color hex string found, custom color will not be applied to the cta button."
            )
            return
        }
        setElementColor(
            containerViewId,
            "#$buttonColor",
            PushTemplateConstants.MethodNames.SET_BACKGROUND_COLOR,
            PushTemplateConstants.FriendlyViewNames.CTA_BUTTON
        )
    }

    /**
     * Creates a pending intent for a thumbnail interaction in a product catalog notification.
     *
     * @param context the application [Context]
     * @param broadcastReceiverClass the [Class] of the broadcast receiver to set in the created pending intent
     * @param channelId [String] containing the notification channel ID
     * @param pushTemplate the [ProductCatalogPushTemplate] object containing the product catalog push template data
     * @param currentIndex [Int] containing the index of the current product in the catalog
     * @return the created thumbnail interaction [PendingIntent]
     */
    private fun createThumbnailInteractionPendingIntent(
        context: Context,
        broadcastReceiverClass: Class<out BroadcastReceiver>?,
        channelId: String,
        pushTemplate: ProductCatalogPushTemplate,
        currentIndex: Int
    ): PendingIntent? {
        if (broadcastReceiverClass == null) {
            return null
        }

        Log.trace(
            LOG_TAG,
            SELF_TAG,
            "Creating a thumbnail interaction pending intent for thumbnail at index $currentIndex"
        )

        val thumbnailClickIntent = AEPPushNotificationBuilder.createIntent(intentActionList[currentIndex], pushTemplate).apply {
            setClass(context.applicationContext, broadcastReceiverClass)
            putExtra(PushTemplateConstants.PushPayloadKeys.CHANNEL_ID, channelId)
            putExtra(PushTemplateConstants.PushPayloadKeys.CATALOG_CTA_BUTTON_TEXT, pushTemplate.ctaButtonText)
            putExtra(PushTemplateConstants.PushPayloadKeys.CATALOG_CTA_BUTTON_COLOR, pushTemplate.ctaButtonColor)
            putExtra(PushTemplateConstants.PushPayloadKeys.CATALOG_CTA_BUTTON_URI, pushTemplate.ctaButtonUri)
            putExtra(PushTemplateConstants.PushPayloadKeys.CATALOG_LAYOUT, pushTemplate.displayLayout)
            putExtra(PushTemplateConstants.PushPayloadKeys.CATALOG_ITEMS, pushTemplate.rawCatalogItems)
            putExtra(PushTemplateIntentConstants.IntentKeys.CATALOG_ITEM_INDEX, currentIndex.toString())
        }

        return PendingIntent.getBroadcast(
            context,
            0,
            thumbnailClickIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}
