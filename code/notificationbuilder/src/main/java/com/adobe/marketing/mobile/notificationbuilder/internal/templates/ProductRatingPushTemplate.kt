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

import com.adobe.marketing.mobile.notificationbuilder.PushTemplateIntentConstants
import com.adobe.marketing.mobile.notificationbuilder.internal.PushTemplateConstants
import com.adobe.marketing.mobile.notificationbuilder.internal.util.NotificationData
import com.adobe.marketing.mobile.services.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

internal class ProductRatingPushTemplate(data: NotificationData) : AEPPushTemplate(data) {

    private val SELF_TAG = "RatingPushTemplate"

    class RatingAction(val link: String?, type: String) {
        val type: PushTemplateConstants.ActionType

        init {
            this.type = try {
                PushTemplateConstants.ActionType.valueOf(type)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid action type provided, defaulting to NONE. Error : ${e.localizedMessage}")
            }
        }

        companion object {
            private const val SELF_TAG = "RatingAction"

            /**
             * Converts the json object representing the action on selecting the rating to a [RatingAction].
             *
             * @param jsonObject [JSONObject] containing the action action details
             * @return an [RatingAction] or null if the conversion fails
             */
            fun getRatingActionFromJSONObject(jsonObject: JSONObject): RatingAction? {
                return try {
                    var uri: String? = null
                    val type = jsonObject.getString(PushTemplateConstants.RatingAction.TYPE)
                    if (type == PushTemplateConstants.ActionType.WEBURL.name || type == PushTemplateConstants.ActionType.DEEPLINK.name) {
                        uri = jsonObject.optString(PushTemplateConstants.RatingAction.URI)
                    }
                    Log.trace(
                        PushTemplateConstants.LOG_TAG,
                        SELF_TAG,
                        "Creating a rating action with uri ($uri), and type ($type)."
                    )
                    RatingAction(uri, type)
                } catch (e: JSONException) {
                    Log.warning(
                        PushTemplateConstants.LOG_TAG,
                        SELF_TAG,
                        "Exception in converting rating action json string to json object, Error : ${e.localizedMessage}."
                    )
                    null
                }
            }
        }
    }

    internal var ratingUnselectedIcon: String
        private set

    internal var ratingSelectedIcon: String
        private set
    internal var ratingActionString: String
        private set

    internal var ratingActionList: List<RatingAction>
        private set

    internal var ratingSelected: Int = -1
        private set

    init {
        ratingUnselectedIcon = data.getRequiredString(PushTemplateConstants.PushPayloadKeys.RATING_UNSELECTED_ICON)
        ratingSelectedIcon = data.getRequiredString(PushTemplateConstants.PushPayloadKeys.RATING_SELECTED_ICON)
        ratingActionString = data.getRequiredString(PushTemplateConstants.PushPayloadKeys.RATING_ACTIONS)
        ratingActionList = getRatingActionsFromString(ratingActionString)
            ?: throw IllegalArgumentException("Required field \"${PushTemplateConstants.PushPayloadKeys.RATING_ACTIONS}\" is invalid.")
        if (ratingActionList.size < 3 || ratingActionList.size > 5) {
            throw IllegalArgumentException("\"${PushTemplateConstants.PushPayloadKeys.RATING_ACTIONS}\" field must have 3 to 5 rating actions")
        }
        ratingSelected = data.getInteger(PushTemplateIntentConstants.IntentKeys.RATING_SELECTED) ?: -1
    }

    private fun getRatingActionsFromString(ratingUriString: String?): List<RatingAction>? {
        if (ratingUriString.isNullOrEmpty()) {
            Log.debug(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Exception in converting rating uri json string to json array, Error :" +
                    " rating uris is null"
            )
            return null
        }
        val ratingActionList = mutableListOf<RatingAction>()
        try {
            val jsonArray = JSONArray(ratingUriString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val ratingAction = RatingAction.getRatingActionFromJSONObject(jsonObject) ?: return null
                ratingActionList.add(ratingAction)
            }
        } catch (e: Exception) {
            Log.debug(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Exception in converting rating uri json string to json array, Error : ${e.localizedMessage}"
            )
            return null
        }
        return ratingActionList
    }
}
