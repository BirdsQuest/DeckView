package com.birdsquest.cardviewer;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/*
	The News Object is an individual news article derived from parsed JSON
 */
public class News extends DeckView implements HTTPListener{
	private static final String TAG="NEWS";
	private static String baseURL="http://www3.nhk.or.jp/news/easy/",
			VideoBaseUrl="rtmp://flv.nhk.or.jp/ondemand/flv/news/&movie=",
			newsList="news-list.json";

	//Constructors
	public News(Context context){super(context);init(context, null);}
	public News(Context context, AttributeSet attrs){super(context, attrs);init(context, attrs);}
	@Override void init(Context context, AttributeSet attrs){
		cardLayout=R.layout.news;
		layoutIDs=new int[]{R.id.name,R.id.url};
		super.init(context,attrs);
	}

	@Override public void fetchCards(){
		HTTP.send(baseURL+newsList,this);
	}

	class NewsObject extends DeckView.CardObject{
		String newsID, url, image, sound, movie;
		Date date;

		public NewsObject(int id, String newsID, String name, String url, String image, String sound, String movie, Date date){
			super(id, name);
			//Cards details
			this.newsID=newsID;
			this.url=url;
			this.image=image;
			this.sound=sound;
			this.movie=movie;
			this.date=date;
		}
		public NewsObject(int id, JSONObject article){
			super(id,null);
		/* JSON Structure
			[{"2017-01-18":[{"news_priority_number":"1",
					"news_prearranged_time":"2017-01-18 17:20:00",
					"news_id":"k10010843261000",
					"name":"\u8efd\u3044\u75c5\u6c17\u3084\u3051\u304c\u306f\u5e97\u3067\u8cb7\u3063\u305f\u85ac\u3067\u6cbb\u3059\u3068\u7a0e\u91d1\u304c\u5b89\u304f\u306a\u308b",
					"title_with_ruby":"<ruby>\u8efd<rt>\u304b\u308b<\/rt><\/ruby>\u3044<ruby>\u75c5\u6c17<rt>\u3073\u3087\u3046\u304d<\/rt><\/ruby>\u3084\u3051\u304c\u306f<ruby>\u5e97<rt>\u307f\u305b<\/rt><\/ruby>\u3067<ruby>\u8cb7<rt>\u304b<\/rt><\/ruby>\u3063\u305f<ruby>\u85ac<rt>\u304f\u3059\u308a<\/rt><\/ruby>\u3067<ruby>\u6cbb<rt>\u306a\u304a<\/rt><\/ruby>\u3059\u3068<ruby>\u7a0e\u91d1<rt>\u305c\u3044\u304d\u3093<\/rt><\/ruby>\u304c<ruby>\u5b89<rt>\u3084\u3059<\/rt><\/ruby>\u304f\u306a\u308b",
					"news_file_ver":true,
					"news_creation_time":"2017-01-18 17:38:00",
					"news_preview_time":"2017-01-18 17:38:00",
					"news_publication_time":"2017-01-18 17:17:19",
					"news_publication_status":true,
					"has_news_web_image":true,
					"has_news_web_movie":true,
					"has_news_easy_image":false,
					"has_news_easy_movie":false,
					"has_news_easy_voice":true,
					"news_web_image_uri":"http:\/\/www3.nhk.or.jp\/news\/html\/20170118\/..\/20170118\/K10010843261_1701180512_1701180514_01_03.jpg?utm_int=nsearch_contents_search-items_001",
					"news_web_movie_uri":"k10010843261_201701180512_201701180513.mp4",
					"news_easy_image_uri":"''",
					"news_easy_movie_uri":"''",
					"news_easy_voice_uri":"k10010843261000.mp3",
					"news_display_flag":true,
					"news_web_url":"http:\/\/www3.nhk.or.jp\/news\/html\/20170118\/k10010843261000.html?utm_int=nsearch_contents_search-items_001"
				},//Next Article of Day
			],//Next Days Articles
		}]*/
		try{
			String[] split;
			//Log.e("TAGG",article.toString());
			newsID=article.getString("news_id");
			name=article.getString("title");
			//Switch the URL from the default news site to the easy one
			split=article.getString("news_web_url").split("/");
			split[4]="easy";
			split[5]=newsID;
			url=TextUtils.join("/",split);
			//Log.e(article.getString("news_web_url"),url);

			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				date=format.parse(article.getString("news_publication_time"));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			if(article.getBoolean("has_news_easy_voice")){sound=article.getString("news_easy_voice_uri");}

			if(article.getBoolean("has_news_web_image")){
				//Log.e("imageA",article.getString("news_web_image_uri"));
				split=article.getString("news_web_image_uri").split("/");
				if(split[2]=="www3.nhk.or.jp"){
					image=split[0]+"/"+split[1]+"/"+split[2]+"/"+split[3]+"/"+split[4]+"/"+split[7]+"/"+split[8];
					//Log.e("imageB",image);
				}
			}
			//if(article.getBoolean("has_news_easy_image")){image=article.getString("news_easy_image_uri");}




			if(article.getBoolean("has_news_web_movie")){movie=article.getString("news_web_movie_uri");}
			else if(article.getBoolean("has_news_easy_movie")){movie=article.getString("news_easy_movie_uri");}
		}catch(JSONException e){
			e.printStackTrace();
		}
	}
		public void setLayout(ArrayList<View> view){
			((TextView)view.get(0)).setText(name);
			((TextView)view.get(1)).setText(url);
			//if(image!=null){HTTP.setImage((ImageView)view.get(2),image);}
		}

		public String toString(){
			return newsID+"("+date+"): "+name;
		}
	}

	@Override public NewsObject get(int index){
		return (NewsObject)cards.get(index).holder;
	}

	@Override public News.NewsObject loadParcel(Parcel in){
		News.NewsObject news=(News.NewsObject)super.loadParcel(in);
		String[] data=new String[3];
		in.readStringArray(data);
		//Cards details
		news.newsID=data[2];
		news.url=data[3];
		news.image=data[4];
		news.sound=data[5];
		news.movie=data[6];
		news.date=new Date(data[7]);
		return news;
	}

	@Override public void saveParcel(Parcel dest, Object cardObject) {
		News.NewsObject news=(News.NewsObject)cardObject;
		dest.writeStringArray(new String[]{news.id+"", news.name,
			//Cards details
			news.newsID,
			news.url,
			news.image,
			news.sound,
			news.movie,
			news.date.toString()
		});
	}

	@Override public void serverResponded(final String responseString){
		final Context context=getContext();
		((Activity)context).runOnUiThread(
		new Runnable() {
			@Override
			public void run(){
				try{
					JSONObject jsonObject=new JSONArray(responseString).getJSONObject(0);
					Iterator<?> keys=jsonObject.keys();
					JSONArray jsonArray;
					while(keys.hasNext()){
						String key=(String)keys.next();
						if(jsonObject.get(key) instanceof JSONArray){
							jsonArray=(JSONArray)jsonObject.get(key);
							for(int index=0; index<jsonArray.length(); index++){
								NewsObject newsObj=new NewsObject(index,(JSONObject)((JSONArray)jsonObject.get(key)).get(index));
								addCard(newsObj.getCard());
							}
						}
					}
				}catch(JSONException e){
					e.printStackTrace();
				}
			}
		});
	}

	@Override public String MakeJSONToSend(String[] param){
		return null;
	}

	public Drawable FetchImage(String url, NewsObject article) {
		try{
			InputStream input=(InputStream) new URL(url).getContent();
			return Drawable.createFromStream(input, article.newsID);
		}catch(Exception e){return null;}
	}
	public Uri videoURI(NewsObject article){
		if(article.movie!=null){
			return Uri.parse(VideoBaseUrl+article.movie);
		}else{return null;}
	}
}