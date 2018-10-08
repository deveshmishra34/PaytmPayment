package cordova.plugin.paytmpayment;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.Context;
import android.content.ClipData;
import android.content.ContentResolver;
import android.webkit.MimeTypeMap;
import android.os.Build;
import android.os.Bundle;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.ArrayList;
import android.util.Log;

/**
 * This class echoes a string called from JavaScript.
 */
public class PaytmPayment extends CordovaPlugin {

        private CallbackContext PUBLIC_CALLBACKS = null;
    private static final String LOG_TAG = "Paytm Plugin";

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("pay")) {
            this.pay(args, callbackContext);
            return true;
        }
        return false;
    }

    private void pay(JSONArray args, CallbackContext callbackContext) {
        try{
            PUBLIC_CALLBACKS = callbackContext;
            Context context = this.cordova.getActivity().getApplicationContext();
            Intent intent = new Intent(context, PaytmActivity.class);
		
	    Log.d("Paytm Payment", args.toString());
            intent.putExtra("TYPE", args.getJSONObject(0).getString("TYPE"));
            intent.putExtra("MID", args.getJSONObject(0).getString("MID"));
            intent.putExtra("ORDER_ID", args.getJSONObject(0).getString("ORDER_ID"));
            intent.putExtra("CUST_ID", args.getJSONObject(0).getString("CUST_ID"));
            intent.putExtra("CHANNEL_ID", args.getJSONObject(0).getString("CHANNEL_ID"));
            intent.putExtra("TXN_AMOUNT", args.getJSONObject(0).getString("TXN_AMOUNT"));
            intent.putExtra("WEBSITE", args.getJSONObject(0).getString("WEBSITE"));
            intent.putExtra("CALLBACK_URL", args.getJSONObject(0).getString("CALLBACK_URL"));
            intent.putExtra("CHECKSUMHASH", args.getJSONObject(0).getString("CHECKSUMHASH"));
            intent.putExtra("INDUSTRY_TYPE_ID", args.getJSONObject(0).getString("INDUSTRY_TYPE_ID"));

            cordova.startActivityForResult((CordovaPlugin) this, intent, 0);
            Log.d("Paytm Payment", "Called");
            // Send no result, to execute the callbacks later
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true); // Keep callback
        }catch(Exception e){
            callbackContext.error("Something went wrong");
        }
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        // if (resultCode == cordova.getActivity().RESULT_OK) {
            Log.d("Instamojo", "Return" + cordova.getActivity().RESULT_OK);
            //Bundle extras = data.getExtras();// Get data sent by the Intent
            //String data = extras.getString("data"); // data parameter will be send from the other activity.
            if(data != null){
                // String information = "{'orderId': '" + orderId + "', 'status': '" + status + "'}";
                PluginResult result = new PluginResult(PluginResult.Status.OK, getIntentJson(data));
                result.setKeepCallback(true);
                PUBLIC_CALLBACKS.sendPluginResult(result);
            }else{
                String information = "{'status':  'PAYMENT_CANCELED'}";
                PluginResult result = new PluginResult(PluginResult.Status.OK, information);
                result.setKeepCallback(true);
                PUBLIC_CALLBACKS.sendPluginResult(result);
            }

        // }
    }

    private JSONObject getIntentJson(Intent intent) {
        JSONObject intentJSON = null;
        ClipData clipData = null;
        JSONObject[] items = null;
        ContentResolver cR = this.cordova.getActivity().getApplicationContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            clipData = intent.getClipData();
            if (clipData != null) {
                int clipItemCount = clipData.getItemCount();
                items = new JSONObject[clipItemCount];

                for (int i = 0; i < clipItemCount; i++) {

                    ClipData.Item item = clipData.getItemAt(i);

                    try {
                        items[i] = new JSONObject();
                        items[i].put("htmlText", item.getHtmlText());
                        items[i].put("intent", item.getIntent());
                        items[i].put("text", item.getText());
                        items[i].put("uri", item.getUri());

                        if (item.getUri() != null) {
                            String type = cR.getType(item.getUri());
                            String extension = mime.getExtensionFromMimeType(cR.getType(item.getUri()));

                            items[i].put("type", type);
                            items[i].put("extension", extension);
                        }

                    } catch (JSONException e) {
                        Log.d(LOG_TAG, " Error thrown during intent > JSON conversion");
                        Log.d(LOG_TAG, e.getMessage());
                        Log.d(LOG_TAG, Arrays.toString(e.getStackTrace()));
                    }

                }
            }
        }

        try {
            intentJSON = new JSONObject();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (items != null) {
                    intentJSON.put("clipItems", new JSONArray(items));
                }
            }

            intentJSON.put("type", intent.getType());
            intentJSON.put("extras", toJsonObject(intent.getExtras()));
            intentJSON.put("action", intent.getAction());
            intentJSON.put("categories", intent.getCategories());
            intentJSON.put("flags", intent.getFlags());
            intentJSON.put("component", intent.getComponent());
            intentJSON.put("data", intent.getData());
            intentJSON.put("package", intent.getPackage());

            return intentJSON;
        } catch (JSONException e) {
            Log.d(LOG_TAG, " Error thrown during intent > JSON conversion");
            Log.d(LOG_TAG, e.getMessage());
            Log.d(LOG_TAG, Arrays.toString(e.getStackTrace()));

            return null;
        }
    }

    private static JSONObject toJsonObject(Bundle bundle) {
        // Credit: https://github.com/napolitano/cordova-plugin-intent
        try {
            return (JSONObject) toJsonValue(bundle);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Cannot convert bundle to JSON: " + e.getMessage(), e);
        }
    }

    private static Object toJsonValue(final Object value) throws JSONException {
        // Credit: https://github.com/napolitano/cordova-plugin-intent
        if (value == null) {
            return null;
        } else if (value instanceof Bundle) {
            final Bundle bundle = (Bundle) value;
            final JSONObject result = new JSONObject();
            for (final String key : bundle.keySet()) {
                result.put(key, toJsonValue(bundle.get(key)));
            }
            return result;
        } else if ((value.getClass().isArray())) {
            final JSONArray result = new JSONArray();
            int length = Array.getLength(value);
            for (int i = 0; i < length; ++i) {
                result.put(i, toJsonValue(Array.get(value, i)));
            }
            return result;
        } else if (value instanceof ArrayList<?>) {
            final ArrayList arrayList = (ArrayList<?>) value;
            final JSONArray result = new JSONArray();
            for (int i = 0; i < arrayList.size(); i++)
                result.put(toJsonValue(arrayList.get(i)));
            return result;
        } else if (value instanceof String || value instanceof Boolean || value instanceof Integer
                || value instanceof Long || value instanceof Double) {
            return value;
        } else {
            return String.valueOf(value);
        }
    }
}
