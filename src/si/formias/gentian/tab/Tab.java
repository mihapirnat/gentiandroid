package si.formias.gentian.tab;

import si.formias.gentian.GentianChat;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public abstract class Tab extends LinearLayout {
	protected LinearLayout top;
	protected LinearLayout center;
	protected LinearLayout centerNoScroll;
	protected LinearLayout bottom;
	private GentianChat main;
	protected ScrollView scrollView;

	public Tab(GentianChat main) {
		super(main);
		this.main = main;
		setOrientation(LinearLayout.VERTICAL);
		center = new LinearLayout(main);
		center.setOrientation(LinearLayout.VERTICAL);
		centerNoScroll = new LinearLayout(main);
		centerNoScroll.setOrientation(LinearLayout.VERTICAL);
		centerNoScroll.setVisibility(View.GONE);
		top = new LinearLayout(main);
		top.setOrientation(LinearLayout.VERTICAL);
		scrollView = new ScrollView(main);
		TextView test = new TextView(main);

		center.addView(test);

		scrollView.addView(center);
		LinearLayout.LayoutParams parmsCenter = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, 0);
		parmsCenter.weight = 1;

		bottom = new LinearLayout(main);
		addView(top, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		addView(scrollView, parmsCenter);
		addView(centerNoScroll, parmsCenter);
		addView(bottom, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));

		setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		setBackgroundColor(Color.parseColor("#daffffff"));
	}

	public void addBottom(View v) {
		bottom.removeAllViews();
		bottom.addView(v, new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
	}

	public void clearBottom() {
		bottom.removeAllViews();
		bottom.addView(new TextView(main));
	}

	protected void scrollDown() {
		scrollView.post(new Runnable() {

			@Override
			public void run() {
				scrollView.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});

	}

	public abstract Object saveState();

	public abstract void loadState(Object o);
}
