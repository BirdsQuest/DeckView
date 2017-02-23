package com.birdsquest.cardviewer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class Main extends Activity{

	public Deck deck;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		deck=(Deck)findViewById(R.id.deck);

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.e("Main","SaveInstance");
		Zodiac.save(deck,outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Log.e("Main","RestoreInstance");
		Zodiac.load(deck,savedInstanceState);
	}

	@Override
	protected void onResume(){
		super.onResume();
		Log.e("Main","Resume");
		Zodiac.load(deck,null);
	}
}
