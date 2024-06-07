package com.adobe.marketing.mobile.notificationbuilder.internal.templates

import com.adobe.marketing.mobile.notificationbuilder.PushTemplateConstants
import com.adobe.marketing.mobile.notificationbuilder.PushTemplateConstants.DEFAULT_DELETE_ICON_NAME
import com.adobe.marketing.mobile.notificationbuilder.internal.templates.MockMultiIconTemplateDataProvider.getMockedDataMapWithForMultiIcon
import com.adobe.marketing.mobile.notificationbuilder.internal.util.MapData
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.Test

@RunWith(MockitoJUnitRunner::class)
class MultiIconPushTemplateTests {

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }
    @Test
    fun testMultiIconNotificationWithAllKeys() {
        val multiIconPushTemplate = provideMockedMultiIconTemplateWithAllKeys()
        assertEquals(5, multiIconPushTemplate.templateItemList.size)
        assertEquals("delete", multiIconPushTemplate.cancelIcon)
    }

    @Test
    fun testMultiIconPushTemplateWithNoCrossButtonIconKey() {
        // Arrange
        val dataMap = getMockedDataMapWithForMultiIcon()
        dataMap.remove(PushTemplateConstants.PushPayloadKeys.MULTI_ICON_CLOSE_BUTTON)
        val data = MapData(dataMap)
        val multiIconPushTemplate = MultiIconPushTemplate(data)
        assertEquals(5, multiIconPushTemplate.templateItemList.size)
        assertEquals(DEFAULT_DELETE_ICON_NAME, multiIconPushTemplate.cancelIcon)
    }

    @Test
    fun testMultiIconPushTemplateOneInvalidUri() {
        // Arrange
        val dataMap = getMockedDataMapWithForMultiIcon()
        dataMap.replaceValueInMap(
            PushTemplateConstants.PushPayloadKeys.MULTI_ICON_ITEMS,
            MOCK_MULTI_ICON_ITEM_PAYLOAD_INVALID_IMAGE
        )
        val data = MapData(dataMap)
        val multiIconPushTemplate = MultiIconPushTemplate(data)
        assertEquals(3, multiIconPushTemplate.templateItemList.size)
    }
}