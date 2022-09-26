package me.larma.arthook;


import android.app.Activity;
import android.util.Log;

public class MainActivity extends Activity {

    private static final String TAG = "sanbo.arthook.MyActivity";

    public static void d(String info) {
        Log.d(TAG, info);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            d("setContentView  + Integer.toHexString(R.layout.activity_my) + ");
            setContentView(R.layout.activity_my);
            //Activity.class.getDeclaredMethod("setContentView", int.class).invoke(this, R.layout.activity_my);
            //ArtHook.about(System.class.getDeclaredMethod("arraycopy", Object.class, int.class, Object.class, int.class, int.class));
            //ArtHook.backupMethods.get(new MethodInfo(Activity.class, "setContentView", int.class)).invoke(this, R.layout.activity_my);
        } catch (Exception e) {
            d(Log.getStackTraceString(e));
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        d( "before Activity.setContentView");
        super.setContentView(layoutResID);
        d( "after Activity.setContentView");
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.my, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            try {
//                SipAudioCall call = new SipAudioCall(this, null);
//                call.startAudio();
//            } catch (Exception e) {
//                Log.w(TAG, e);
//            }
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}
