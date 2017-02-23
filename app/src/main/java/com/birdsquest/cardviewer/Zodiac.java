package com.birdsquest.cardviewer;

import android.util.Log;
import android.view.View;
import android.os.Bundle;
import java.util.ArrayList;
import android.content.Context;
import android.widget.TextView;
import android.util.AttributeSet;
import android.support.annotation.Nullable;

public class Zodiac extends Card{
	private static final String TAG="Zodiac";
	String description="";

	//Constructors
	public Zodiac(Context context){super(context);init(context, null,-1);}
	public Zodiac(Context context, AttributeSet attrs){super(context, attrs);init(context, attrs,-1);}
	public Zodiac(Context context, AttributeSet attrs, int defStyleAttr){super(context, attrs, defStyleAttr);init(context, attrs, defStyleAttr);}

	private void init(Context context, AttributeSet attrs, int defStyleAttr){
		if(attrs!=null){}
		if(defStyleAttr!=-1){}
	}

	public void set(int id, String name, String description){
		super.set(id, name);
		this.description=description;
	}
	@Override public void setLayout(ArrayList<View> view){
		((TextView)view.get(0)).setText(name);
		((TextView)view.get(1)).setText(description);
	}
	public static void save(Deck deck, Bundle outState){
		Zodiac zodiac;
		Card.save(deck,outState);
		String[] description=new String[deck.cards.size()];
		for(int index=0;index<deck.cards.size();index++){
			zodiac=(Zodiac)deck.cards.get(index);
			description[index]=zodiac.description;
		}
		outState.putStringArray("description",description);
	}

	@Override public Object create(Context context){return new Zodiac(context);}

	public static void load(Deck deck, @Nullable Bundle savedInstanceState){
		if(savedInstanceState!=null){
			if(!savedInstanceState.containsKey("description")){return;}
			String[] description=savedInstanceState.getStringArray("description");
			if(description.length==0){fetchCards(deck);}
			else{
				Zodiac zodiac=new Zodiac(deck.getContext());
				zodiac.fillDeck(deck,description.length);
				for(int index=0; index<deck.cards.size(); index++){
					zodiac=(Zodiac)deck.cards.get(index);
					zodiac.description=description[index];
				}
				Card.load(deck,savedInstanceState);
			}
		}else if(deck.cards.size()==0){fetchCards(deck);}
	}

	public static void fetchCards(Deck deck){
		Log.e("Fetch","Cards");
		if(deck.cards.size()==0){
			Zodiac zodiac;
			Context context=deck.getContext();
			String[][] content={
					{"The Rat", "The Ox", "The Tiger", "The Rabbit", "The Dragon", "The Snake", "The Horse", "The Sheep", "The Monkey", "The Rooster", "The Dog", "The Pig"},
					{"climbed atop", "to get a better look at", "who was chasing", "who was watching", "who looked like", "with legs.", "was leisurely walking down the path with", "who was following", "who was ignoring everyone else.", "ran away from", "which was actually just chasing", "who was none the wiser"}};
			for(int index=0; index<content[0].length; index++){
				zodiac=new Zodiac(context);
				zodiac.set(index,content[0][index],content[1][index]);
				deck.addCard(zodiac);
			}
		}
	}

	public String toString(){
		return id+") "+name+" "+description;
	}
}
