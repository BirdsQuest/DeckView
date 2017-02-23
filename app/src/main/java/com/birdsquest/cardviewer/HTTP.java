package com.birdsquest.cardviewer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.io.BufferedOutputStream;

/*  manifest permissions
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

	gradle dependancies
	compile 'com.google.code.gson:gson:2.3'
    compile group: 'org.apache.httpcomponents' , name: 'httpclient-android' , version: '4.3.5.1'
*/
public class HTTP{
	public static void send(String url, HTTPListener listener){send(url,listener,new String[]{});}
	public static void send(final String url, final HTTPListener listener, final String[] params) {
		new Thread() {
			public void run(){
				try{
					String toPost=listener.MakeJSONToSend(params);
					HttpURLConnection connection=setConnection(url, toPost);
					postData(connection, toPost);
					getResponse(connection, listener);
					connection.disconnect();
				}catch(IOException e){e.printStackTrace();}
			}
		}.start();
	}

	static HttpURLConnection setConnection(String url,String toPost) throws IOException{
		HttpURLConnection connection=(HttpURLConnection)new URL(url).openConnection();
		connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
		connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
		connection.setConnectTimeout(15000);//milliseconds
		connection.setReadTimeout(10000);//milliseconds
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setDoInput(true);

		if(toPost!=null){connection.setFixedLengthStreamingMode(toPost.getBytes().length);}
		connection.connect();
		return connection;
	}

	static void postData(HttpURLConnection connection, String toPost) throws IOException{
		if(toPost!=null){//Start Sending
			BufferedOutputStream out = new BufferedOutputStream(connection.getOutputStream());
			out.write(toPost.getBytes());
			//Clear OutputStream when finished
			out.flush();
			out.close();
		}	}

	static void getResponse(HttpURLConnection connection, HTTPListener listener) throws IOException{
		String inputLine;
		StringBuffer responseBuffer=new StringBuffer();
		if(connection.getResponseCode()==HttpURLConnection.HTTP_OK){
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			while((inputLine=in.readLine())!=null){responseBuffer.append(inputLine);}
			listener.serverResponded(responseBuffer.toString());
			in.close();
		}
	}
	static void setImage(ImageView imageView,String url){
		Log.e("Tagg",url);
		new DownloadImageTask(imageView).execute(url);
	}
}
interface HTTPListener{
	public void serverResponded(String responseString);
	public String MakeJSONToSend(String[] param);
}
class DownloadImageTask extends AsyncTask<String, Void, Bitmap>{
	ImageView bmImage;

	public DownloadImageTask(ImageView bmImage) {
		this.bmImage = bmImage;
	}

	protected Bitmap doInBackground(String... urls) {
		String urldisplay = urls[0];
		Bitmap mIcon11 = null;
		try {
			InputStream in = new java.net.URL(urldisplay).openStream();
			mIcon11 = BitmapFactory.decodeStream(in);
		} catch (Exception e) {
			//Log.e("Error", e.getMessage());
			e.printStackTrace();
		}
		return mIcon11;
	}

	protected void onPostExecute(Bitmap result) {
		bmImage.setImageBitmap(result);
		Log.e("RRRR","Set Up Bitmap!");
	}
}