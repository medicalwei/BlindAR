package edu.ntou.blindar;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;

// ----------------------------------------------------------------------

public class blindar extends Activity {    
    private Preview mPreview;
    private DrawOnTop mDrawOnTop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the window title.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Create our Preview view and set it as the content of our activity.
        // Create our DrawOnTop view.
        mDrawOnTop = new DrawOnTop(this);
        mPreview = new Preview(this, mDrawOnTop);
        setContentView(mPreview, new LayoutParams(480, 320));
        addContentView(mDrawOnTop, new LayoutParams(480, 320));
    }
} 