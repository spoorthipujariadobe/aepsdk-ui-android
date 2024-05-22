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

package com.adobe.marketing.mobile.notificationbuilder.internal.templates

import android.content.Intent
import com.adobe.marketing.mobile.notificationbuilder.internal.PushTemplateConstants.PushPayloadKeys
import com.adobe.marketing.mobile.notificationbuilder.internal.util.NotificationData

/**
 * This class is used to parse the push template data payload or an intent and provide the necessary information
 * to build a notification containing an input box.
 */
internal class InputBoxPushTemplate(data: NotificationData, fromIntent: Boolean = false) : AEPPushTemplate(data, fromIntent) {
    // Required, the intent action name to be used when the user submits the feedback.
    internal val inputBoxReceiverName: String

    // Optional, If present, use it as the placeholder text for the text input field. Otherwise, use the default placeholder text of "Reply".
    internal val inputTextHint: String?

    // Optional, once feedback has been submitted, use this text as the notification's body
    internal val feedbackText: String?

    // Optional, once feedback has been submitted, use this as the notification's image
    internal val feedbackImage: String?

    /**
     * Initializes the input box push template with the provided data.
     *
     * @param data the notification data payload or an intent
     * @param fromIntent `true` if the data is from an intent, `false` otherwise
     * @throws IllegalArgumentException if the required fields are not found in the data
     */
    init {
        inputBoxReceiverName = data.getString(PushPayloadKeys.INPUT_BOX_RECEIVER_NAME)
            ?: throw IllegalArgumentException("Required field \"${PushPayloadKeys.INPUT_BOX_RECEIVER_NAME}\" not found.")
        inputTextHint = data.getString(PushPayloadKeys.INPUT_BOX_HINT)
        feedbackText = data.getString(PushPayloadKeys.INPUT_BOX_FEEDBACK_TEXT)
        feedbackImage = data.getString(PushPayloadKeys.INPUT_BOX_FEEDBACK_IMAGE)
    }

    override fun createIntent(action: String): Intent {
        return super.createIntent(action)
            .putExtra(PushPayloadKeys.INPUT_BOX_RECEIVER_NAME, inputBoxReceiverName)
            .putExtra(PushPayloadKeys.INPUT_BOX_HINT, inputTextHint)
            .putExtra(PushPayloadKeys.INPUT_BOX_FEEDBACK_TEXT, feedbackText)
            .putExtra(PushPayloadKeys.INPUT_BOX_FEEDBACK_IMAGE, feedbackImage)
    }
}
