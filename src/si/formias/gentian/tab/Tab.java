package si.formias.gentian.tab;

import si.formias.gentian.GentianChat;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public abstract class Tab extends LinearLayout{
	protected LinearLayout center;
	protected LinearLayout bottom;
	private GentianChat main;
	private ScrollView v;
	public Tab(GentianChat main) {
		super(main);
		this.main=main;
		setOrientation(LinearLayout.VERTICAL);
		center=new LinearLayout(main);
		center.setOrientation(LinearLayout.VERTICAL);
		v=new ScrollView(main);
		TextView test =new TextView(main);
	
		center.addView(test);
		
		v.addView(center);
		LinearLayout.LayoutParams parmsCenter= new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,0);
		parmsCenter.weight=1;
		
		bottom=new LinearLayout(main);
		addView(v,parmsCenter);
		addView(bottom,new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		
		setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		setBackgroundColor(Color.parseColor("#daffffff"));
	} 
	public void addBottom(View v) {
		bottom.removeAllViews();
		bottom.addView(v,new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
	}
	public void clearBottom() {
		bottom.removeAllViews();
		bottom.addView(new TextView(main));
	}
	protected void scrollDown() {
		v.post(new Runnable() {

	        @Override
	        public void run() {
	            v.fullScroll(ScrollView.FOCUS_DOWN);
	        }
	    });

	}
	public abstract Object saveState();
	public abstract void loadState(Object o);
}
