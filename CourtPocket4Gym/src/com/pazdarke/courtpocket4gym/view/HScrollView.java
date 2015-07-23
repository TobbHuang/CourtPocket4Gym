package com.pazdarke.courtpocket4gym.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class HScrollView extends HorizontalScrollView{

	private GestureDetector mGestureDetector = null;
	private OnScrollChangedListener listener = null;
	
	
	
	public void setListener(OnScrollChangedListener listener) {
		this.listener = listener;
	}

	public HScrollView(Context context,GestureDetector mDetector) {
		super(context);
		mGestureDetector = mDetector;
        setFadingEdgeLength(0);
        setHorizontalScrollBarEnabled(false);
	}

	public HScrollView(Context context, AttributeSet attrs,GestureDetector mDetector){
		 super(context, attrs);
		 mGestureDetector = mDetector;
         setFadingEdgeLength(0);
         setHorizontalScrollBarEnabled(false);
	}
	
    @Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
	     return super.onInterceptTouchEvent(ev) && mGestureDetector.onTouchEvent(ev);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if(listener != null)
			listener.onScrollChanged(this, l, t, oldl, oldt);
	}
	
    
    
}
