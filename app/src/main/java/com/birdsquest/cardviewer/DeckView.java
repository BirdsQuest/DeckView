package com.birdsquest.cardviewer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class DeckView extends SwipeRefreshLayout{
	private static final String TAG="TestDeck";
	Recycler list;

	protected int[] layoutIDs=new int[]{R.id.name};

	private static final int PENDING_REMOVAL_TIMEOUT = 3000; // 3sec
	public ArrayList<Card> cards=new ArrayList<>(),
			pendingRemoval=new ArrayList();
	HashMap<Card, Runnable> pendingRunnables = new HashMap<>(); // map of items to pending runnables, so we can cancel a removal if need be
	private Handler handler = new Handler(); // hanlder for running delayed runnables

	Drawable leftColor=new ColorDrawable(Color.RED),
			rightColor=new ColorDrawable(Color.YELLOW),
			background=leftColor,
			leftIcon,rightIcon;
	int xMarkMargin;

	boolean longPressDragEnabled=true,
			itemViewSwipeEnabled=true,
			swipingLeft=false;

	public int cardLayout=R.layout.card;

	int currentTop=0;

	public DeckView(Context context){super(context);init(context,null);}
	public DeckView(Context context, AttributeSet attrs){super(context, attrs);init(context,attrs);}

	public CardObject get(int index){
		return (CardObject)cards.get(index).holder;
	}

	void init(Context context, AttributeSet attrs){
		if(attrs!=null){
			//TypedArray colorSheme = getResources().obtainTypedArray(R.array.main_refresh_sheme);
			//setColorSchemeResources(colorSheme.getResourceId(0, -1), colorSheme.getResourceId(1, -1), colorSheme.getResourceId(2, -1), colorSheme.getResourceId(3, -1));


		}
		else{
			setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);
		}
		list=new Recycler(context);
		addView(list);
		setOnRefreshListener(onRefresh);
		//list.setOnScrollListener(onScroll);

		//list.init(context);//, cardLayout, cardIDs
	}

	/**public void save(Deck deck, Bundle outState)
	 *
	 * To be called in Main.java:
	 * @Override protected void onSaveInstanceState(Bundle outState) {
	 *     super.onSaveInstanceState(outState);
	 *     deck.save(outState);
	 * }
	 *
	 * @param outState
	 */
	public void save(Bundle outState){
		outState.putParcelableArrayList(TAG+"_cards", cards);
		outState.putInt(TAG+"_currentTop", currentTop);
	}

	/**
	 * @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
	 *     super.onRestoreInstanceState(savedInstanceState);
	 *     deck.load(savedInstanceState);
	 * }
	 *
	 * @Override protected void onResume(){
	 *     super.onResume();
	 *     deck.load(null);
	 * }
	 *
	 * @param savedInstanceState
	 */
	public void load(Bundle savedInstanceState){
		if(savedInstanceState!=null){
			if(!savedInstanceState.containsKey(TAG+"_cards")){return;}
			cards=savedInstanceState.getParcelableArrayList(TAG+"_cards");
			currentTop=savedInstanceState.getInt(TAG+"_currentTop");
			if(cards.size()==0){fetchCards();}
		}else if(cards.size()==0){fetchCards();}
	}

	SwipeRefreshLayout.OnRefreshListener onRefresh=new SwipeRefreshLayout.OnRefreshListener(){
		@Override
		public void onRefresh() {
			fetchCards();
		}
	};
	public void fetchCards(){}

	void addCard(Card card){
		cards.add(card);
		adapter.notifyItemInserted(adapter.getItemCount());
	}

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
		adapter.notifyItemInserted(adapter.getItemCount());
		setRefreshing(false);
	}

	public boolean isPendingRemoval(int position){
		return pendingRemoval.contains(cards.get(position));
	}
	public void removalPending(int position) {
		final Card card = cards.get(position);
		if (!pendingRemoval.contains(card)) {
			pendingRemoval.add(card);
			// this will redraw row in "undo" state
			adapter.notifyItemChanged(position);
			// let's create, store and post a runnable to remove the item
			Runnable pendingRemovalRunnable = new Runnable() {
				@Override
				public void run() {
					removeCard(cards.indexOf(card));
				}
			};
			handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
			pendingRunnables.put(card, pendingRemovalRunnable);
		}
	}
	void removeCard(int position){
		Card card = cards.get(position);
		if (pendingRemoval.contains(card)) {
			pendingRemoval.remove(card);
		}
		if (cards.contains(card)) {
			cards.remove(position);
			adapter.notifyItemRemoved(position);
		}
		//cards.remove(position);
		//getAdapter().notifyItemRemoved(position);
	}


	class Recycler extends RecyclerView{
		public Recycler(Context context){
			super(context);

			itemTouchHelper.attachToRecyclerView(list);
			Log.e(TAG,this.getClass().getSimpleName());
			setLayoutManager(new LinearLayoutManager(context));
			setItemAnimator(new DefaultItemAnimator());
			setAdapter(adapter);
			addItemDecoration(itemDecoration);//new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
			setHasFixedSize(false);
			//addOnScrollListener();
			leftIcon=ContextCompat.getDrawable(context,android.R.drawable.ic_menu_delete);
			leftIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
			rightIcon=ContextCompat.getDrawable(context,android.R.drawable.btn_star);
			rightIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
			xMarkMargin=(int) context.getResources().getDimension(R.dimen.ic_clear_margin);

		}
	}

	RecyclerView.Adapter<Layout> adapter=new RecyclerView.Adapter<Layout>(){
		@Override public Layout onCreateViewHolder(ViewGroup parent, int viewType){
			Card card=new Card(getContext());
			card.addView(LayoutInflater.from(getContext()).inflate(cardLayout, parent, false));
			card.setLayoutParams(new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT));
			return new Layout(card.getRootView(),layoutIDs);
		}
		@Override public void onBindViewHolder(Layout holder, int position){
			//holder.bind((Card)cards.get(position));
			Layout viewHolder = (Layout)holder;
			final Card card=(Card)cards.get(position);
			//viewHolder.undo.setVisibility(View.GONE);
			if (pendingRemoval.contains(card)) {
				// we need to show the "undo" state of the row
				viewHolder.itemView.setBackgroundColor(Color.RED);
				viewHolder.content.setVisibility(View.GONE);
				viewHolder.undo.setHeight(viewHolder.content.getHeight());
				viewHolder.undo.setVisibility(View.VISIBLE);
				viewHolder.undo.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// user wants to undo the removal, let's cancel the pending task
						Runnable pendingRemovalRunnable = pendingRunnables.get(card);
						pendingRunnables.remove(card);
						if (pendingRemovalRunnable != null) handler.removeCallbacks(pendingRemovalRunnable);
						pendingRemoval.remove(card);
						// this will rebind the row in "normal" state
						notifyItemChanged(cards.indexOf(card));
					}
				});
			} else {
				// we need to show the "normal" state
				viewHolder.itemView.setBackgroundColor(Color.WHITE);
				viewHolder.content.setVisibility(View.VISIBLE);
				viewHolder.undo.setVisibility(View.GONE);
				viewHolder.undo.setOnClickListener(null);
				((CardObject)card.holder).setLayout(viewHolder.views);
			}
		}
		@Override public int getItemCount(){return cards.size();}
	};

	public static class Layout extends RecyclerView.ViewHolder {
		ArrayList<View> views=new ArrayList<>();
		LinearLayout content;
		Button undo;
		public Layout(View itemView, int[] cardIDs){
			super(itemView);
			content=(LinearLayout)itemView.findViewById(R.id.content);
			undo=(Button)itemView.findViewById(R.id.undo);
			for(int index=0;index<cardIDs.length;index++){
				views.add(itemView.findViewById(cardIDs[index]));
	}	}	}


	//SimpleCallback takes in the directions that you want to enable drag-and-drop and the directions that you want to enable swiping.
	ItemTouchHelper itemTouchHelper=new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
		@Override public boolean onMove(RecyclerView deck, RecyclerView.ViewHolder dragged, RecyclerView.ViewHolder draggedOver){
			requestDisallowInterceptTouchEvent(true);
			int from=dragged.getAdapterPosition(),
					to=draggedOver.getAdapterPosition();
			if(from<to){
				for(int index=from;index<to;index++){
					Collections.swap(cards,index,index+1);
				}   }else{
				for(int index=from;index>to;index--){
					Collections.swap(cards,index,index-1);
				}	}
			adapter.notifyItemMoved(from,to);
			return true;
			/*
			setEnabled(false);
			deck.getAdapter().notifyItemMoved(dragged.getAdapterPosition(),draggedOver.getAdapterPosition());

			return false;*/
		}

		//ActionState=IDLE, SWIPE, DRAG
		@Override public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir){
			int position=viewHolder.getAdapterPosition();
			if(swipeDir==ItemTouchHelper.LEFT){swipeLeft(viewHolder, position);}
			else if(swipeDir==ItemTouchHelper.RIGHT){swipeRight(viewHolder, position);}
		}

		public void swipeLeft(RecyclerView.ViewHolder viewHolder, int position){removalPending(position);}
		public void swipeRight(RecyclerView.ViewHolder viewHolder, int position){}

		@Override public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
			return isPendingRemoval(viewHolder.getAdapterPosition())
					?0:super.getSwipeDirs(recyclerView, viewHolder);
		}

		public void drawBackground(Canvas canvas, View item, int xOffset){
			Drawable icon;
			int itemHeight=item.getBottom()-item.getTop(),
					iconWidth,iconRight,iconLeft,bgLeft;

			if(xOffset<0){
				icon=leftIcon;
				background=leftColor;
				bgLeft=item.getRight()+xOffset;
				iconWidth=icon.getIntrinsicWidth();
				iconRight=item.getRight()-xMarkMargin;
				iconLeft=item.getRight()-xMarkMargin-iconWidth;
			}else{
				icon=rightIcon;
				background=rightColor;
				bgLeft=item.getLeft();
				iconWidth=icon.getIntrinsicWidth();
				iconLeft = item.getLeft()+xMarkMargin;
				iconRight = item.getLeft()+xMarkMargin+iconWidth;
			}
			int iconHeight=icon.getIntrinsicHeight(),
					iconTop=item.getTop()+(itemHeight-iconHeight)/2,
					iconBottom=iconTop+iconHeight;

			background.setBounds(bgLeft, item.getTop(), item.getRight(), item.getBottom());
			icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

			background.draw(canvas);
			icon.draw(canvas);
		}

		@Override public void onChildDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive){
			if(viewHolder.getAdapterPosition()==-1){return;}//The viewholders already swiped away
			drawBackground(canvas, viewHolder.itemView, (int)dX);
			super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
		}

		@Override public boolean isLongPressDragEnabled() {return longPressDragEnabled;}
		@Override public boolean isItemViewSwipeEnabled(){return itemViewSwipeEnabled;}

		/*

		@Override public void onSelectedChanged(ViewHolder dragged, int actionState){
			super.onSelectedChanged(dragged,actionState);
			//dragged.itemView.animate().scaleX(1.5f).scaleY(1.5f).setDuration(250l).start();
		}

		@Override public boolean canDropOver(RecyclerView deck, ViewHolder dragged, ViewHolder draggedOver){
			return true;
		}
		@Override public void clearView(RecyclerView deck, ViewHolder dragged){
			super.clearView(deck, dragged);
			dragged.itemView.animate().scaleX(1f).scaleY(1f).setDuration(250l).start();
		}


*/
		//ItemDecoration functions
		//If the user is controlling, swiping isItemActve=true if it is sliding back, false
		/*public void onChildDraw(Canvas c, RecyclerView recyclerView, ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive){
			if(actionState==ItemTouchHelper.ACTION_STATE_SWIPE){
				int width=viewHolder.itemView.getWidth();
				float alpha=1f-Math.abs(dX)/width;
				viewHolder.itemView.setAlpha(alpha);
			}
			super.onChildDraw(c,recyclerView,viewHolder,dX,dY,actionState,isCurrentlyActive);
		}*/
		//public void onChildDrawOver(Canvas c, RecyclerView recyclerView, ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive){super.onChildDraw(c,recyclerView,viewHolder,dX,dY,actionState,isCurrentlyActive);}
	});


	/*some items might be animating down and some items might be animating up to close the gap left by the removed item
	this is not exclusive, both movement can be happening at the same time
	to reproduce this leave just enough items so the first one and the last one would be just a little off screen
	then remove one from the middle
	find first child with translationY > 0
	and last one with translationY < 0
	we're after a rect that is not covered in recycler-view views at this point in time
	*/

	/*View lastViewComingDown = null;
				View firstViewComingUp = null;
				// this is fixed
				int left = 0;
				int right = parent.getWidth();
				// this we need to find out
				int top = 0;
				int bottom = 0;
				// find relevant translating views
				int childCount = parent.getLayoutManager().getChildCount();
				for (int i = 0; i < childCount; i++) {
					View child = parent.getLayoutManager().getChildAt(i);
					if (child.getTranslationY() < 0) {
						// view is coming down
						lastViewComingDown = child;
					} else if (child.getTranslationY() > 0) {
						// view is coming up
						if (firstViewComingUp == null) {
							firstViewComingUp = child;
				}	}	}

				if (lastViewComingDown != null && firstViewComingUp != null) {
					// views are coming down AND going up to fill the void
					top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
					bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
				} else if (lastViewComingDown != null) {
					// views are going down to fill the void
					top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
					bottom = lastViewComingDown.getBottom();
				} else if (firstViewComingUp != null) {
					// views are coming up to fill the void
					top = firstViewComingUp.getTop();
					bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
				}//*/
	//background.setBounds(left, top, right, bottom);
	RecyclerView.ItemDecoration itemDecoration=new RecyclerView.ItemDecoration(){
		@Override public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state){
			if(parent.getItemAnimator().isRunning()){
				background.draw(c);
			}
			super.onDraw(c, parent, state);
		}
	};

	public class Card extends CardView implements Parcelable{
		Object holder;
		public Card(Context context){super(context);}
		public Card(Context context, Object object){
			super(context);
			holder=object;
			setPreventCornerOverlap(true);
			setFocusableInTouchMode(true);
			setFocusable(true);
		}

		public Card(Context context, Parcel in){
			super(context);
			holder=loadParcel(in);
		}

		public final Creator<Card> CREATOR=new Creator<Card>(){
			@Override
			public Card createFromParcel(Parcel in){
				return new Card(getContext(), in);
			}

			@Override
			public Card[] newArray(int size){
				return new Card[size];
			}
		};

		@Override
		public int describeContents(){
			return 0;
		}

		@Override
		public void writeToParcel(Parcel parcel, int i){
			saveParcel(parcel, holder);
		}
	}

	class CardObject{
		private static final String TAG="Card";
		String name="";
		int id;

		public CardObject(int id, String name){
			this.id=id;
			this.name=name;
		}

		public void setLayout(ArrayList<View> view){
			((TextView)view.get(0)).setText(name);
		}
		public Card getCard(){return new Card(getContext(),this);}
	}
	//Parcelling
	public CardObject loadParcel(Parcel in){
		String[] data=new String[2];
		in.readStringArray(data);
		return new CardObject(Integer.parseInt(data[0]),data[1]);
	}
	public void saveParcel(Parcel dest, Object card){
		CardObject cardObject=(CardObject)card;
		dest.writeStringArray(new String[] {cardObject.id+"", cardObject.name});
	}
}