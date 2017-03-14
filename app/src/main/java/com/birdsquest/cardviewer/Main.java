package com.birdsquest.cardviewer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class Main extends Activity{

	public News deck;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		deck=(News)findViewById(R.id.deck);

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.e("Main","SaveInstance");
		deck.save(outState);
	}

	@Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Log.e("Main","RestoreInstance");
		deck.load(savedInstanceState);
	}

	@Override protected void onResume(){
		super.onResume();
		Log.e("Main","Resume");
		deck.load(null);
	}
}
