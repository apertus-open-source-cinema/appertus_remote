package Appertus.Test;

import java.util.ArrayList;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ApertusSlider extends HorizontalScrollView {
    private static final int SWIPE_MIN_DISTANCE = 5;
    private static final int SWIPE_THRESHOLD_VELOCITY = 300;
    private TextView ShowPos;

    private GestureDetector mGestureDetector;
    private int mActiveFeature = 0;

    public ApertusSlider(Context context, AttributeSet attrs, int defStyle) {
	super(context, attrs, defStyle);
	this.setVerticalScrollBarEnabled(false);
	this.setHorizontalScrollBarEnabled(false);
	Init();
    }

    public ApertusSlider(Context context, AttributeSet attrs) {
	super(context, attrs);
	this.setVerticalScrollBarEnabled(false);
	this.setHorizontalScrollBarEnabled(false);
	Init();
    }

    public ApertusSlider(Context context) {
	super(context);
	this.setVerticalScrollBarEnabled(false);
	this.setHorizontalScrollBarEnabled(false);
	Init();
    }

    public void SetTextView(TextView label) {
	ShowPos = label;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
	// TODO Auto-generated method stub
	// Log.i("Scrolling", "X from ["+oldl+"] to ["+l+"]");
	ShowPos.setText("Position: " + l);
	super.onScrollChanged(l, t, oldl, oldt);
    }

    public void Init() {
	setOnTouchListener(new View.OnTouchListener() {
	    @Override
	    public boolean onTouch(View v, MotionEvent event) {
		// If the user swipes
		if (mGestureDetector.onTouchEvent(event)) {
		    System.out.println("onTouchEvent");
		    return true;
		} else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
		    // Snap to closes value
		    int scrollX = getScrollX();
		    int featureWidth = 50;// Snapping to
		    mActiveFeature = ((scrollX + (featureWidth / 2)) / featureWidth);
		    int scrollTo = mActiveFeature * featureWidth;
		    smoothScrollTo(scrollTo, 0);
		    System.out.println("Snapping to: " + scrollTo);
		    return true;
		} else {
		    return false;
		}
	    }
	});
	mGestureDetector = new GestureDetector(new MyGestureDetector());
    }

    class MyGestureDetector extends SimpleOnGestureListener {
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
	    try {
		// right to left
		if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
		    int featureWidth = getMeasuredWidth();
		    //mActiveFeature = (mActiveFeature < (mItems.size() - 1)) ? mActiveFeature + 1 : mItems.size() - 1;
		    //smoothScrollTo(mActiveFeature * featureWidth, 0);
		    return true;
		}
		// left to right
		else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
		    int featureWidth = getMeasuredWidth();
		    mActiveFeature = (mActiveFeature > 0) ? mActiveFeature - 1 : 0;
		    smoothScrollTo(mActiveFeature * featureWidth, 0);
		    return true;
		}
	    } catch (Exception e) {

		Log.e("Fling", "There was an error processing the Fling event:" + e.getMessage());
	    }
	    return false;
	}
    }
}
