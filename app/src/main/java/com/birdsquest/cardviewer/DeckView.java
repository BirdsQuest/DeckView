package com.birdsquest.cardviewer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

public class DeckView extends SwipeRefreshLayout{
	Deck list;
	public DeckView(Context context){super(context);init(context,null);}
	public DeckView(Context context, AttributeSet attrs){super(context, attrs);init(context,attrs);}


	private void init(Context context, AttributeSet attrs){
		if(attrs!=null){
			//TypedArray colorSheme = getResources().obtainTypedArray(R.array.main_refresh_sheme);
			//setColorSchemeResources(colorSheme.getResourceId(0, -1), colorSheme.getResourceId(1, -1), colorSheme.getResourceId(2, -1), colorSheme.getResourceId(3, -1));


		}
		else{
			setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);
		}
		list=new Deck(context);
		addView(list);
		setOnRefreshListener(onRefresh);
		//list.setOnScrollListener(onScroll);

		//list.init(context);//, cardLayout, cardIDs
	}


	SwipeRefreshLayout.OnRefreshListener onRefresh=new SwipeRefreshLayout.OnRefreshListener(){
		@Override
		public void onRefresh() {
			fetchCards();
			//setRefreshing(false);
		}
	};
	private void fetchCards(){}

	@Override
	public boolean canScrollVertically(int direction) {
		// check if scrolling up
		if (direction < 1) {
			boolean original = super.canScrollVertically(direction);
			return !original && getChildAt(0) != null && getChildAt(0).getTop() < 0 || original;
		}
		return super.canScrollVertically(direction);

	}
	void onItemsLoadComplete(){
		list.getAdapter().notifyItemInserted(list.getAdapter().getItemCount());
		setRefreshing(false);
	}
	/*

	RecyclerView.OnScrollListener onScroll=new RecyclerView.OnScrollListener() {
		@Override
		public void onScrollStateChanged(RecyclerView view, int newState) {
			super.onScrollStateChanged(view,newState);
		}

		@Override
		public void onScrolled(RecyclerView view, int xDir, int yDir) {
			super.onScrolled(view,xDir,yDir);
			setEnabled(layoutParams.findFirstCompletelyVisibleItemPosition() == 0);//refreshLayout.
		}
	};

	@Override
	public boolean canChildScrollUp() {
		return list.getFirstVisiblePosition() > 0 ||
				list.getChildAt(0) == null ||
				list.getChildAt(0).getTop() < 0;	}

	}*/
}
