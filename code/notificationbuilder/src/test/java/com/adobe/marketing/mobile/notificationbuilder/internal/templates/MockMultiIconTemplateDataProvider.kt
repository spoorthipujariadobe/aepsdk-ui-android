package com.adobe.marketing.mobile.notificationbuilder.internal.templates

import com.adobe.marketing.mobile.notificationbuilder.PushTemplateConstants
import com.adobe.marketing.mobile.notificationbuilder.internal.PushTemplateType

object MockMultiIconTemplateDataProvider {

    fun getMockedDataMapWithForMultiIcon(): MutableMap<String, String> {
        return mutableMapOf(
            PushTemplateConstants.PushPayloadKeys.TAG to MOCKED_TAG,
            PushTemplateConstants.PushPayloadKeys.TITLE to MOCKED_TITLE,
            PushTemplateConstants.PushPayloadKeys.TEMPLATE_TYPE to PushTemplateType.MULTI_ICON.value,
            PushTemplateConstants.PushPayloadKeys.BODY to "",
            PushTemplateConstants.PushPayloadKeys.CHANNEL_ID to MOCKED_CHANNEL_ID,
            PushTemplateConstants.PushPayloadKeys.PRIORITY to MOCKED_PRIORITY,
            PushTemplateConstants.PushPayloadKeys.VISIBILITY to MOCKED_VISIBILITY,
            PushTemplateConstants.PushPayloadKeys.SOUND to "bell",
            PushTemplateConstants.PushPayloadKeys.SMALL_ICON to MOCKED_SMALL_ICON,
            PushTemplateConstants.PushPayloadKeys.VERSION to MOCKED_PAYLOAD_VERSION,
            PushTemplateConstants.PushPayloadKeys.STICKY to "true",
            PushTemplateConstants.PushPayloadKeys.MULTI_ICON_CLOSE_BUTTON to "delete",
            PushTemplateConstants.PushPayloadKeys.MULTI_ICON_ITEMS to MOCK_MULTI_ICON_ITEM_PAYLOAD,
        )
    }

}