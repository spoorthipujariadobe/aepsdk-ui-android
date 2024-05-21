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

object PushTemplateIntentConstants {
    object IntentActions {
        const val FILMSTRIP_LEFT_CLICKED = "filmstrip_left"
        const val FILMSTRIP_RIGHT_CLICKED = "filmstrip_right"
        const val REMIND_LATER_CLICKED = "remind_clicked"
        const val MANUAL_CAROUSEL_LEFT_CLICKED = "manual_left"
        const val MANUAL_CAROUSEL_RIGHT_CLICKED = "manual_right"
        const val INPUT_RECEIVED = "input_received"
    }

    object IntentKeys {
        const val CENTER_IMAGE_INDEX = "centerImageIndex"
        const val IMAGE_URI = "imageUri"
        const val IMAGE_URLS = "imageUrls"
        const val IMAGE_CAPTIONS = "imageCaptions"
        const val IMAGE_CLICK_ACTIONS = "imageClickActions"
        const val ACTION_URI = "actionUri"
        const val ACTION_TYPE = "actionType"
        const val CHANNEL_ID = "channelId"
        const val CUSTOM_SOUND = "customSound"
        const val TITLE_TEXT = "titleText"
        const val BODY_TEXT = "bodyText"
        const val EXPANDED_BODY_TEXT = "expandedBodyText"
        const val NOTIFICATION_BACKGROUND_COLOR = "notificationBackgroundColor"
        const val TITLE_TEXT_COLOR = "titleTextColor"
        const val EXPANDED_BODY_TEXT_COLOR = "expandedBodyTextColor"
        const val BADGE_COUNT = "badgeCount"
        const val LARGE_ICON = "largeIcon"
        const val SMALL_ICON = "smallIcon"
        const val SMALL_ICON_COLOR = "smallIconColor"
        const val PRIORITY = "priority"
        const val VISIBILITY = "visibility"
        const val IMPORTANCE = "importance"
        const val REMIND_DELAY_SECONDS = "remindDelaySeconds"
        const val REMIND_EPOCH_TS = "remindEpochTimestamp"
        const val REMIND_LABEL = "remindLaterLabel"
        const val ACTION_BUTTONS_STRING = "actionButtonsString"
        const val STICKY = "sticky"
        const val TAG = "tag"
        const val TICKER = "ticker"
        const val PAYLOAD_VERSION = "version"
        const val TEMPLATE_TYPE = "templateType"
        const val CAROUSEL_OPERATION_MODE = "carouselOperationMode"
        const val CAROUSEL_LAYOUT_TYPE = "carouselLayoutType"
        const val CAROUSEL_ITEMS = "carouselItems"
        const val INPUT_BOX_HINT = "inputBoxHint"
        const val INPUT_BOX_FEEDBACK_TEXT = "feedbackText"
        const val INPUT_BOX_FEEDBACK_IMAGE = "feedbackImage"
        const val INPUT_BOX_RECEIVER_NAME = "feedbackReceiverName"
        const val CATALOG_CTA_BUTTON_TEXT = "ctaButtonText"
        const val CATALOG_CTA_BUTTON_COLOR = "ctaButtonColor"
        const val CATALOG_CTA_BUTTON_URI = "ctaButtonUri"
        const val CATALOG_LAYOUT = "displayLayout"
        const val CATALOG_ITEMS = "catalogItems"
        const val CATALOG_ITEM_INDEX = "catalogIndex"
    }
}
