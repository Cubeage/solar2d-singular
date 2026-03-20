// ----------------------------------------------------------------------------
// LuaLoader.java – Solar2D plugin bridge for Singular Attribution SDK 12.x
// Package: plugin.singular
//
// Lua API:
//   singular.init(options)
//     options.apiKey      (string, required)
//     options.secretKey   (string, required)
//
//   singular.event(name [, args])
//     name  (string)
//     args  (table, optional) – key/value pairs sent as event attributes
//
//   singular.adRevenue(data)
//     data.adPlatform  (string) – e.g. "ironSource"
//     data.currency    (string) – e.g. "USD"
//     data.revenue     (number)
// ----------------------------------------------------------------------------

package plugin.singular;

import android.util.Log;

import com.ansca.corona.CoronaActivity;
import com.ansca.corona.CoronaEnvironment;
import com.ansca.corona.CoronaLua;
import com.ansca.corona.CoronaRuntime;
import com.ansca.corona.CoronaRuntimeListener;

import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.NamedJavaFunction;

import com.singular.sdk.Singular;
import com.singular.sdk.SingularConfig;
import com.singular.sdk.SingularAdData;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

/**
 * Solar2D plugin entry point for Singular Attribution SDK 12.x
 */
public class LuaLoader implements JavaFunction, CoronaRuntimeListener {

    private static final String TAG = "SingularPlugin";

    private CoronaRuntime fRuntime;

    // -------------------------------------------------------------------------
    // CoronaRuntimeListener
    // -------------------------------------------------------------------------

    @Override
    public void onLoaded(CoronaRuntime runtime) {
        fRuntime = runtime;
    }

    @Override
    public void onStarted(CoronaRuntime runtime) {}

    @Override
    public void onSuspended(CoronaRuntime runtime) {}

    @Override
    public void onResumed(CoronaRuntime runtime) {}

    @Override
    public void onExiting(CoronaRuntime runtime) {
        fRuntime = null;
    }

    // -------------------------------------------------------------------------
    // JavaFunction – called when Lua does require("plugin.singular")
    // -------------------------------------------------------------------------

    @Override
    public int invoke(LuaState L) {
        CoronaEnvironment.addRuntimeListener(this);

        L.newTable();

        L.pushJavaFunction(new InitWrapper());
        L.setField(-2, "init");

        L.pushJavaFunction(new EventWrapper());
        L.setField(-2, "event");

        L.pushJavaFunction(new AdRevenueWrapper());
        L.setField(-2, "adRevenue");

        return 1;
    }

    // -------------------------------------------------------------------------
    // init(options)
    // -------------------------------------------------------------------------

    private class InitWrapper implements NamedJavaFunction {
        @Override
        public String getName() { return "init"; }

        @Override
        public int invoke(LuaState L) {
            if (L.getTop() < 1 || !L.isTable(1)) {
                Log.e(TAG, "singular.init() – arg 1 must be an options table");
                return 0;
            }

            L.getField(1, "apiKey");
            final String apiKey = L.isString(-1) ? L.toString(-1) : null;
            L.pop(1);

            L.getField(1, "secretKey");
            final String secretKey = L.isString(-1) ? L.toString(-1) : null;
            L.pop(1);

            if (apiKey == null || apiKey.isEmpty()) {
                Log.e(TAG, "singular.init() – options.apiKey is required");
                return 0;
            }
            if (secretKey == null || secretKey.isEmpty()) {
                Log.e(TAG, "singular.init() – options.secretKey is required");
                return 0;
            }

            final CoronaActivity activity = CoronaEnvironment.getCoronaActivity();
            if (activity == null) return 0;

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        SingularConfig config = new SingularConfig(apiKey, secretKey);
                        Singular.init(activity, config);
                        Log.d(TAG, "Singular SDK initialized");
                    } catch (Exception e) {
                        Log.e(TAG, "singular.init() error: " + e.getMessage(), e);
                    }
                }
            });

            return 0;
        }
    }

    // -------------------------------------------------------------------------
    // event(name [, args])
    // -------------------------------------------------------------------------

    private class EventWrapper implements NamedJavaFunction {
        @Override
        public String getName() { return "event"; }

        @Override
        public int invoke(LuaState L) {
            if (!L.isString(1)) {
                Log.e(TAG, "singular.event() – arg 1 must be event name string");
                return 0;
            }
            final String name = L.toString(1);

            final Map<String, Object> args = new HashMap<>();
            if (L.getTop() >= 2 && L.isTable(2)) {
                L.pushNil();
                while (L.next(2)) {
                    if (L.isString(-2)) {
                        String key = L.toString(-2);
                        if (L.isString(-1)) {
                            args.put(key, L.toString(-1));
                        } else if (L.isNumber(-1)) {
                            args.put(key, L.toNumber(-1));
                        } else if (L.isBoolean(-1)) {
                            args.put(key, L.toBoolean(-1));
                        }
                    }
                    L.pop(1);
                }
            }

            final CoronaActivity activity = CoronaEnvironment.getCoronaActivity();
            if (activity == null) return 0;

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (args.isEmpty()) {
                            Singular.event(name);
                        } else {
                            Singular.eventJSON(name, new JSONObject(args));
                        }
                        Log.d(TAG, "Singular event tracked: " + name);
                    } catch (Exception e) {
                        Log.e(TAG, "singular.event() error: " + e.getMessage(), e);
                    }
                }
            });

            return 0;
        }
    }

    // -------------------------------------------------------------------------
    // adRevenue(data)
    // -------------------------------------------------------------------------

    private class AdRevenueWrapper implements NamedJavaFunction {
        @Override
        public String getName() { return "adRevenue"; }

        @Override
        public int invoke(LuaState L) {
            if (L.getTop() < 1 || !L.isTable(1)) {
                Log.e(TAG, "singular.adRevenue() – arg 1 must be a table");
                return 0;
            }

            L.getField(1, "adPlatform");
            final String adPlatform = L.isString(-1) ? L.toString(-1) : "unknown";
            L.pop(1);

            L.getField(1, "currency");
            final String currency = L.isString(-1) ? L.toString(-1) : "USD";
            L.pop(1);

            L.getField(1, "revenue");
            final double revenue = L.isNumber(-1) ? L.toNumber(-1) : 0.0;
            L.pop(1);

            final CoronaActivity activity = CoronaEnvironment.getCoronaActivity();
            if (activity == null) return 0;

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        SingularAdData adData = new SingularAdData(adPlatform, currency, revenue);
                        Singular.adRevenue(adData);
                        Log.d(TAG, "Singular adRevenue reported: " + revenue + " " + currency + " via " + adPlatform);
                    } catch (Exception e) {
                        Log.e(TAG, "singular.adRevenue() error: " + e.getMessage(), e);
                    }
                }
            });

            return 0;
        }
    }
}
