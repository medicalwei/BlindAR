package edu.ntou.blindar;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;

// ----------------------------------------------------------------------

public class blindar extends Activity{    
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
        setContentView(mPreview, new LayoutParams(320, 240));
        addContentView(mDrawOnTop, new LayoutParams(320, 240));
    }
    
    /* TODO: Auto Focus and Begin draw edge on one click >w</ */

    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	switch(keyCode)
    	{
    	case KeyEvent.KEYCODE_DPAD_CENTER: // DPAD_CENTER
    		mPreview.mCamera.autoFocus(null);
    		break;
    		
    	case KeyEvent.KEYCODE_MENU: // MENU
    		mDrawOnTop.mDrawEdges = !mDrawOnTop.mDrawEdges;
    		break;
    	}
		return super.onKeyDown(keyCode, event);
    }
	


} 