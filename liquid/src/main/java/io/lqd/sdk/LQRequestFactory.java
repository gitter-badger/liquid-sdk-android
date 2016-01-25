/**
 * Copyright 2014-present Liquid Data Intelligence S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.lqd.sdk;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import io.lqd.sdk.model.LQNetworkRequest;

public class LQRequestFactory {

    protected static final String LIQUID_SERVER_BASE_URL = "https://api.lqd.io/collect/";
    protected static final String LIQUID_DATAPOINT_URL = LIQUID_SERVER_BASE_URL + "data_points";
    protected static final String LIQUID_ALIAS_URL = LIQUID_SERVER_BASE_URL + "aliases";
    protected static final String LIQUID_LQD_PACKAGE_URL = LIQUID_SERVER_BASE_URL + "users/%s/devices/%s/liquid_package";
    protected static final String LIQUID_INAPP_MESSAGES_URL = LIQUID_SERVER_BASE_URL + "users/%s/inapp_messages";
    protected static final String LIQUID_INAPP_MESSAGE_REPORT_URL = LIQUID_SERVER_BASE_URL + "formulas/%s/users/%s/report";
    protected static final String LIQUID_VARIABLES_URL = LIQUID_SERVER_BASE_URL + "variables";
    protected static final String LIQUID_UIELEMENTS_URL = LIQUID_SERVER_BASE_URL + "ui_elements/";
    protected static final String LIQUID_UIELEMENTS_ADD_URL = LIQUID_UIELEMENTS_URL + "add";
    protected static final String LIQUID_UIELEMENTS_REMOVE_URL = LIQUID_UIELEMENTS_URL + "remove";

    public static LQNetworkRequest createAliasRequest(String oldId, String newId) {
        JSONObject json;
        try {
            json = new JSONObject();
            json.put("unique_id", newId);
            json.put("unique_id_alias", oldId);
            return new LQNetworkRequest(LIQUID_ALIAS_URL, "POST", json.toString());
        } catch (JSONException e) {
            return null;
        }
    }

    public static LQNetworkRequest createDataPointRequest(String datapoint) {
        return new LQNetworkRequest(LIQUID_DATAPOINT_URL, "POST", datapoint);
    }

    public static LQNetworkRequest requestLiquidPackageRequest(String userId, String userDevice) {
            String url = String.format(LIQUID_LQD_PACKAGE_URL, Uri.encode(userId, "UTF-8"), userDevice);
        return new LQNetworkRequest(url, "GET", null);
    }

    public static LQNetworkRequest createVariableRequest(JSONObject variable) {
        return new LQNetworkRequest(LIQUID_VARIABLES_URL, "POST", variable.toString());
    }

    public static LQNetworkRequest inappMessagesRequest(String userId) {
           String url = String.format(LIQUID_INAPP_MESSAGES_URL, Uri.encode(userId, "UTF-8"));
        return new LQNetworkRequest(url, "GET", null);
    }

    public static LQNetworkRequest inappMessagesReportRequest(String userId, String formulaId, JSONObject payload) {
            String url = String.format(LIQUID_INAPP_MESSAGE_REPORT_URL, formulaId, Uri.encode(userId, "UTF-8"));
        return new LQNetworkRequest(url, "POST", payload.toString());
    }

    public static LQNetworkRequest inappMessagesReportRequest(String userId, String formulaId) {
            String url = String.format(LIQUID_INAPP_MESSAGE_REPORT_URL, formulaId, Uri.encode(userId, "UTF-8"));
        return new LQNetworkRequest(url, "POST", null);
    }

    public static LQNetworkRequest uiElementsToTrackRequest(String token) {
        if (token == null)
            return new LQNetworkRequest(LIQUID_UIELEMENTS_URL + "?platform=android", "GET", null);
        else
            return new LQNetworkRequest(LIQUID_UIELEMENTS_URL + "?platform=android&token=" + token, "GET", null);
    }

    public static LQNetworkRequest uiElementsAdd(String identifier, String eventname) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("identifier", identifier );
            payload.put("event_name", eventname);
            payload.put("platform", "android");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new LQNetworkRequest(LIQUID_UIELEMENTS_ADD_URL, "POST", payload.toString());
    }

    public static LQNetworkRequest uiElementsRemove(String identifier) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("identifier", identifier );
            payload.put("platform", "android");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new LQNetworkRequest(LIQUID_UIELEMENTS_REMOVE_URL, "POST", payload.toString());

    }

}
