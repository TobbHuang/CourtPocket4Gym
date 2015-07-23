package com.pazdarke.courtpocket4gym.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.ScrollView;

public class VScrollView extends ScrollView{

	private GestureDetector mGestureDetector = null;
	@SuppressWarnings("deprecation")
	public VScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mGestureDetector = new GestureDetector(mGestureListener);
        setFadingEdgeLength(0);
        setVerticalScrollBarEnabled(false);
	}

	@SuppressWarnings("deprecation")
	public VScrollView(Context context) {
		super(context);
		mGestureDetector = new GestureDetector(mGestureListener);
        setFadingEdgeLength(0);
        setVerticalScrollBarEnabled(false);
	}
	
	 @Override
		public boolean onInterceptTouchEvent(MotionEvent ev) {
		     return super.onInterceptTouchEvent(ev) && mGestureDetector.onTouchEvent(ev);
		}
		
		private SimpleOnGestureListener mGestureListener = new SimpleOnGestureListener(){

			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2,
					float distanceX, float distanceY) {
				if(Math.abs(distanceX) < Math.abs(distanceY)) {
	                return true;
				}
				return false;
			}
			
		};
		
		
}
