package com.birdsquest.cardviewer;
/**In order to fit this into your package, please use change the above line to
 * your application and use ctrl+f to change Zodiac/zodiac to suit your objects
 * name. Also, ctrl+f the words "//Cards details" and change any lines below to
 * what your individual object is.
 */

import android.os.Parcel;
import android.view.View;
import java.util.ArrayList;
import android.widget.TextView;
import android.content.Context;
import android.util.AttributeSet;

public class Zodiac extends DeckView{
	private static final String TAG="Zodiac";

	//Constructors
	public Zodiac(Context context){super(context);init(context, null);}
	public Zodiac(Context context, AttributeSet attrs){super(context, attrs);init(context, attrs);}
	@Override void init(Context context, AttributeSet attrs){
		cardLayout=R.layout.zodiac;
		layoutIDs=new int[]{
				R.id.name,
				//Cards details
				R.id.description
		};
		super.init(context,attrs);
	}

	@Override public void fetchCards(){
		if(cards.size()==0){
			String[][] content={
					{"The Rat", "The Ox", "The Tiger", "The Rabbit", "The Dragon", "The Snake", "The Horse", "The Sheep", "The Monkey", "The Rooster", "The Dog", "The Pig"},
					{"climbed atop", "to get a better look at", "who was chasing", "who was watching", "who looked like", "with legs.", "was leisurely walking down the path with", "who was following", "who was ignoring everyone else.", "ran away from", "which was actually just chasing", "who was none the wiser"}};
			for(int index=0; index<content[0].length; index++){
				addCard(new ZodiacObject(index,content[0][index],content[1][index]).getCard());
			}
		}
	}

	class ZodiacObject extends DeckView.CardObject{
		//Cards details
		String description;

		public ZodiacObject(int id, String name, String description){
			super(id, name);
			//Cards details
			this.description=description;
		}

		public void setLayout(ArrayList<View> view){
			//Cards details
			((TextView)view.get(0)).setText(name);//name exists on the template
			((TextView)view.get(1)).setText(description);
		}

		public String toString(){
			//Cards details
			return id+") "+name+" "+description;
		}
	}

	@Override public ZodiacObject get(int index){
		return (ZodiacObject)cards.get(index).holder;
	}

	@Override public ZodiacObject loadParcel(Parcel in){
		ZodiacObject zodiacObject=(ZodiacObject)super.loadParcel(in);
		String[] data=new String[3];
		in.readStringArray(data);
		//Cards details
		zodiacObject.description=data[2];
		return zodiacObject;
	}

	@Override public void saveParcel(Parcel dest, Object cardObject) {
		ZodiacObject zodiac=(ZodiacObject)cardObject;
		dest.writeStringArray(new String[]{zodiac.id+"", zodiac.name,
			//Cards details
			zodiac.description
		});
	}
}
