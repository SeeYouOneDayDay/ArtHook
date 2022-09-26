package me.larma.arthook;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.sip.SipAudioCall;
import android.util.Log;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.Date;

import de.larma.arthook.$;
import de.larma.arthook.ArtHook;
import de.larma.arthook.BackupIdentifier;
import de.larma.arthook.Hook;
import de.larma.arthook.OriginalMethod;

public class MyApplication extends Application {

    private static final String TAG = "sanbo.arthook.MyApplication";
    public static boolean madePiece = false;

    private static void d(String info) {
        Log.d(TAG, info);
    }

    private static void w(Throwable e) {
        Log.w(TAG, Log.getStackTraceString(e));
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ArtHook.hook(MyApplication.class);
        d("Dying soon...");
        try {
            d("..." + MyApplication.class.getDeclaredMethod("warGame"));
            d("..." + MyApplication.class.getDeclaredMethod("endGame"));
        } catch (NoSuchMethodException e) {
            w(e);
        }
        try {
            Camera.open();
        } catch (Exception e) {
            w(e);
        }

        try {
            d("Time:" + System.currentTimeMillis());
            d("BackupTime:" + OriginalMethod.byOriginal(System.class
                    .getDeclaredMethod("currentTimeMillis")).invokeStatic());
            pieceGame();
        } catch (Exception e) {
            w(e);
        }

        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.setNetworkPreference(0);
        } catch (Exception e) {
            w(e);
        }
    }



    public void pieceGame() {
        d("broken pieceGame()");
    }

    @Hook("me.larma.arthook.MyApplication->pieceGame")
    public static void fix_pieceGame(MyApplication app) {
        d("fixed pieceGame()");
        madePiece = true;
        OriginalMethod.by(new $() {
        }).invoke(app);
    }

    @Hook("android.net.sip.SipAudioCall->startAudio")
    public static void SipAudioCall_startAudio(SipAudioCall call) {
        d("SipAudioCall_startAudio");
        OriginalMethod.by(new $() {
        }).invoke(call);
    }

    @Hook("android.app.Activity-><init>")
    public static void Activity_init(Activity a) {
        d("Activity_init");
        OriginalMethod.by(new $() {
        }).invoke(a);
    }

    /**
     * Sample hook of a public member method
     */
    @Hook("android.app.Activity->setContentView")
    public static void Activity_setContentView(Activity activity, int layoutResID) {
        d("before Original[Activity.setContentView]");
        OriginalMethod.by(new $() {  /* do nothing*/
        }).invoke(activity, layoutResID);
        d("after Original[Activity.setContentView]");
        TextView text = ((TextView) activity.findViewById(R.id.helloWorldText));
        text.append("\n -- I am god and made " + (madePiece ? "piece" : "war"));
        text.append("\n " + new Date().toString());
        d("end Hook[Activity.setContentView]");
    }

    /**
     * Sample hook of a static method
     */
    @Hook("android.hardware.Camera->open")
    public static Camera Camera_open() {
        try {
            return OriginalMethod.by(new $() {
            }).invokeStatic();
        } catch (Exception e) {
            throw new SecurityException("We do not allow Camera access", e);
        }
    }

    /**
     * Sample hook of a static native method
     */
    @Hook("java.lang.System->currentTimeMillis")
    public static long System_currentTimeMillis() {
        d("currentTimeMillis is much better in seconds :)");
        return (long) OriginalMethod.by(new $() {/*do nothing*/
        }).invokeStatic() / 1000L;
    }

    /**
     * Hooking an empty method
     */
    @Hook("android.net.ConnectivityManager->setNetworkPreference")
    public static void ConnectivityManager_setNetworkPreference(ConnectivityManager manager, int preference) {
        d("Making something from nothing!");
        OriginalMethod.by(new $() {/*do nothing*/
        }).invoke(manager, preference);
    }

    /**
     * Sample hook of a member method used internally by the system
     * <p/>
     * Note how we use the BackupIdentifier here, because using reflection APIs to access
     * reflection APIs will cause loops...
     */
    @Hook("java.lang.Class->getDeclaredMethod")
    @BackupIdentifier("Class_getDeclaredMethod")
    public static Method Class_getDeclaredMethod(Class cls, String name, Class[] params) {
        d("I'm hooked in getDeclaredMethod: " + cls + " -> " + name);
        if (name.contains("War") || name.contains("war")) {
            d("make piece not war!"); // This is a political statement!
            name = name.replace("War", "Piece").replace("war", "piece");
        }
        return OriginalMethod.by("Class_getDeclaredMethod").invoke(cls, name, params);
    }
}