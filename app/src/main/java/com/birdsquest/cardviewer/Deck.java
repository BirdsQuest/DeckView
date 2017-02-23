package com.birdsquest.cardviewer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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


public class Deck extends RecyclerView{
	private static final String TAG="Deck";
	private static final int PENDING_REMOVAL_TIMEOUT = 3000; // 3sec
	public ArrayList cards=new ArrayList<>(),
			pendingRemoval=new ArrayList();
	HashMap<Object, Runnable> pendingRunnables = new HashMap<>(); // map of items to pending runnables, so we can cancel a removal if need be
	private Handler handler = new Handler(); // hanlder for running delayed runnables

	Drawable background,
			 leftColor=new ColorDrawable(Color.RED),
			 rightColor=new ColorDrawable(Color.YELLOW),
			 leftIcon,rightIcon;
	int xMarkMargin;

	boolean longPressDragEnabled=true,
			itemViewSwipeEnabled=true,
			swipingLeft=false;

	public Deck(Context context){super(context);init(context, null);}
	public Deck(Context context, AttributeSet attrs){super(context, attrs);init(context, attrs);}
	public Deck(Context context, AttributeSet attrs, int defStyle){super(context, attrs, defStyle);init(context, attrs);}

	int cardLayout;
	ArrayList<Integer> cardIDs=new ArrayList<>();
	private void init(Context context, AttributeSet attrs){
		itemTouchHelper.attachToRecyclerView(this);
		setLayoutManager(new LinearLayoutManager(context));
		setItemAnimator(new DefaultItemAnimator());
		setAdapter(new Adapter());
		addItemDecoration(itemDecoration);//new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
		//addOnScrollListener();
		leftIcon=ContextCompat.getDrawable(context,android.R.drawable.ic_menu_delete);
		leftIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
		rightIcon=ContextCompat.getDrawable(context,android.R.drawable.btn_star);
		rightIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
		xMarkMargin=(int) context.getResources().getDimension(R.dimen.ic_clear_margin);


		TypedArray cardArray;
		if(attrs==null){cardArray=getResources().obtainTypedArray(R.array.card);}
		else{
			TypedArray a=context.obtainStyledAttributes(attrs, R.styleable.Deck);
			cardLayout=a.getResourceId(R.styleable.Deck_cardLayout, 0);
			cardArray=getResources().obtainTypedArray(cardLayout);
			a.recycle();
		}
		cardLayout=cardArray.getResourceId(0, 0);
		for(int index=0;index<cardArray.length();index++){
			cardIDs.add(cardArray.getResourceId(index, -1));
		}
		cardIDs.remove(0);
		cardArray.recycle();
	}

	void addCard(Object card){
		cards.add(card);
		getAdapter().notifyItemInserted(getAdapter().getItemCount());
	}
	void removeCard(int position){
		Object item = cards.get(position);
		if (pendingRemoval.contains(item)) {
			pendingRemoval.remove(item);
		}
		if (cards.contains(item)) {
			cards.remove(position);
			getAdapter().notifyItemRemoved(position);
		}
		//cards.remove(position);
		//getAdapter().notifyItemRemoved(position);
	}

	public boolean isPendingRemoval(int position){
		return pendingRemoval.contains(cards.get(position));
	}
	public void removalPending(int position) {
		final Object card = cards.get(position);
		if (!pendingRemoval.contains(card)) {
			pendingRemoval.add(card);
			// this will redraw row in "undo" state
			getAdapter().notifyItemChanged(position);
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

	class Adapter extends RecyclerView.Adapter<Card.Layout>{
		public Adapter(){}
		@Override public Card.Layout onCreateViewHolder(ViewGroup parent, int viewType){
			return new Card.Layout(LayoutInflater.from(parent.getContext()).inflate(cardLayout, parent, false),cardIDs);
		}
		@Override public void onBindViewHolder(Card.Layout holder, int position){
			//holder.bind((Card)cards.get(position));
			Card.Layout viewHolder = (Card.Layout)holder;
			final Card item = (Card)cards.get(position);
			//viewHolder.undo.setVisibility(View.GONE);
			if (pendingRemoval.contains(item)) {
				// we need to show the "undo" state of the row
				viewHolder.itemView.setBackgroundColor(Color.RED);
				viewHolder.content.setVisibility(View.GONE);
				viewHolder.undo.setHeight(viewHolder.content.getHeight());
				viewHolder.undo.setVisibility(View.VISIBLE);
				viewHolder.undo.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// user wants to undo the removal, let's cancel the pending task
						Runnable pendingRemovalRunnable = pendingRunnables.get(item);
						pendingRunnables.remove(item);
						if (pendingRemovalRunnable != null) handler.removeCallbacks(pendingRemovalRunnable);
						pendingRemoval.remove(item);
						// this will rebind the row in "normal" state
						notifyItemChanged(cards.indexOf(item));
					}
				});
			} else {
				// we need to show the "normal" state
				viewHolder.itemView.setBackgroundColor(Color.WHITE);
				viewHolder.content.setVisibility(View.VISIBLE);
				viewHolder.undo.setVisibility(View.GONE);
				viewHolder.undo.setOnClickListener(null);
				item.setLayout(viewHolder.views);
			}
		}
		@Override public int getItemCount(){return cards.size();}

	}

	//SimpleCallback takes in the directions that you want to enable drag-and-drop and the directions that you want to enable swiping.
	ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
		@Override public boolean onMove(RecyclerView deck, ViewHolder dragged, ViewHolder draggedOver){
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
			getAdapter().notifyItemMoved(from,to);
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

	ItemDecoration itemDecoration=new ItemDecoration(){
		@Override public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state){
			if (parent.getItemAnimator().isRunning()) {

				// some items might be animating down and some items might be animating up to close the gap left by the removed item
				// this is not exclusive, both movement can be happening at the same time
				// to reproduce this leave just enough items so the first one and the last one would be just a little off screen
				// then remove one from the middle

				// find first child with translationY > 0
				// and last one with translationY < 0
				// we're after a rect that is not covered in recycler-view views at this point in time
				View lastViewComingDown = null;
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
						}
					}
				}

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
				}

				//background.setBounds(left, top, right, bottom);
				background.draw(c);

			}
			super.onDraw(c, parent, state);
		}
	};
}
class CardObject{
	private static final String TAG="Card";
	String name="";
	int id;

	public void CardObject(int id, String name){
		this.id=id;
		this.name=name;
	}
}
class Card extends CardView{
	private static final String TAG="Card";
	String name="";
	int id;

	public Card(Context context){super(context);init(context, null);}
	public Card(Context context, AttributeSet attrs){super(context, attrs);init(context, attrs);}
	public Card(Context context, AttributeSet attrs, int defStyleAttr){super(context, attrs, defStyleAttr);init(context, attrs);}

	private void init(Context context, AttributeSet attrs){
		if(attrs==null){
			setPreventCornerOverlap(true);
			setFocusableInTouchMode(true);
			setFocusable(true);
	}	}

	public void set(int id, String name){
		this.id=id;
		this.name=name;
	}

	public void setLayout(ArrayList<View> view){
		((TextView)view.get(0)).setText(name);
	}

	public static class Layout extends RecyclerView.ViewHolder {
		ArrayList<View> views=new ArrayList<>();
		LinearLayout content;
		Button undo;
		public Layout(View itemView, ArrayList<Integer> cardIDs){
			super(itemView);
			content=(LinearLayout)itemView.findViewById(R.id.content);
			undo=(Button)itemView.findViewById(R.id.undo);
			for(int index=0;index<cardIDs.size();index++){
				views.add(itemView.findViewById(cardIDs.get(index)));
			}
		}
	}

	public Object create(Context context){return new Card(context);}

	public void fillDeck(Deck deck, int count){
		Context context=deck.getContext();

		for(int index=0;index<count;index++){
			deck.addCard(create(context));
		}
	}

	public static void save(Deck deck, Bundle outState){
		Card card;
		int[]    id=new int[deck.cards.size()];
		String[] name=new String[deck.cards.size()];
		for(int index=0;index<deck.cards.size();index++){
			card=(Card)deck.cards.get(index);
			id[index]=card.id;
			name[index]=card.name;
		}
		outState.putIntArray("id",id);
		outState.putStringArray("name",name);
	}

	public static void load(Deck deck, @Nullable Bundle savedInstanceState){
		if(savedInstanceState!=null){
			Card card;
			int[]    id=savedInstanceState.getIntArray("id");
			String[] name=savedInstanceState.getStringArray("name");
			for(int index=0; index<deck.cards.size(); index++){
				card=(Card)deck.cards.get(index);
				card.id=id[index];
				card.name=name[index];
			}
		}
	}
}



/*I'm wanting Deck to instead extend SwipeRefreshLayout

//Add this into the onattachview
Context context=getContext();
Recycler list=new Recycler(context);
list.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
view.addView(list);
list.init(context, cardLayout, cardIDs

init(){
setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);
setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                card.fetchCards();
                //setRefreshing(false);
            }
        });
        }



        Inside recycler views init
        setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(int newState) {
            }

            @Override
            public void onScrolled(int dx, int dy) {
                refreshLayout.setEnabled(layoutParams.findFirstCompletelyVisibleItemPosition() == 0);
            }
        });
		 */