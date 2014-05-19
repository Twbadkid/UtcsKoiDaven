package com.utcs.koi.location;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements LocationListener{
	private LocationManager lms;
	private Button send;
	private Button refresh;
	private TextView rString;
	private Double longitude;
	private Double latitude;
	private Location location;
	private Spinner spinner;
	private List<String> list = new ArrayList<String>();
	private ArrayAdapter<String> listAdapter;

	protected void onPause() {
		super.onPause();
		lms.removeUpdates(this);
		location = null;
	}

	protected void onResume() {
		super.onResume();
		LocationManager status = (LocationManager) (this
				.getSystemService(Context.LOCATION_SERVICE));
		if (status.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
				|| status.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			// 如果GPS或網路定位開啟，呼叫locationServiceInitial()更新位置
		} else {
			Toast.makeText(this, "請開啟定位服務", Toast.LENGTH_LONG).show();
			startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)); // 開啟設定頁面
		}
		if(lms!=null)
		lms.requestLocationUpdates(bestProvider, 10, 1, this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		send = (Button) findViewById(R.id.Send);
		refresh = (Button) findViewById(R.id.refresh);
		rString = (TextView) findViewById(R.id.rString);
		send.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				sendMessage();
			}
		});
		refresh.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				refresh.setText("Waiting");
				list.clear();
				locationServiceInitial();
				getPlace();
			}
		});
		spinner = (Spinner) findViewById(R.id.spinner1);
		listAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, list);
		spinner.setAdapter(listAdapter);
	}

	private void sendMessage() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				send.setClickable(false);
				// Log.e("TEST", "RUN");
				try {
					Socket clientSocket = new Socket("61.31.108.99", 27104);
					// Log.e("TEST", "CON");
					DataOutputStream toServer = new DataOutputStream(
							clientSocket.getOutputStream());
					BufferedReader fromServer = new BufferedReader(
							new InputStreamReader(clientSocket.getInputStream()));
					toServer.write(("0:koi:koi\n").getBytes("UTF-8"));
					// Log.e("TEST", "SEND");
					String in = fromServer.readLine();
					in.split(":");
					Message msg = new Message();
					Bundle se = new Bundle();
					se.putString("in", in);
					msg.setData(se);
					mHandler.sendMessage(msg);

					// System.out.println(fromServer.readLine());
					toServer.close();
					fromServer.close();
					clientSocket.close();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {

					e.printStackTrace();
				}
				send.setClickable(true);
			}
		}).start();
	}

	private String getJson(String url) throws IOException, JSONException {
		URL urlc = new URL(url);
		URLConnection conn = urlc.openConnection();
		conn.connect();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		String read;
		String json = "";
		while ((read = in.readLine()) != null) {
			json += read;
		}
		// System.out.println(json);
		in.close();
		return json;
	}

	private void getPlace() {
		new Thread(new Runnable() {
			public void run() {
				getLocation(location);
				String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
						+ latitude
						+ ","
						+ longitude
						+ "&radius=500&sensor=false&key=AIzaSyB6FQZ2zLkw0_pPa_zScW3GNfqWa9sEOig";
				// System.out.println(url);
				try {
					String fulurl = url;
					do {
						String json = getJson(fulurl);
						JSONObject js = new JSONObject(json);
						JSONArray result = js.getJSONArray("results");
						for (int i = 0; i < result.length(); i++) {
							JSONObject re1 = result.getJSONObject(i);
							// System.out.println(re1.getString("name"));
							list.add(re1.getString("name"));
						}
						try {
							String afurl = js.getString("next_page_token")
									.replace("\"", "");
							fulurl = url + "&pagetoken=" + afurl;
							Thread.sleep(2000);
						} catch (JSONException e) {
							Message msg = new Message();
							listup.sendMessage(msg);
							break;
						}
						// System.out.println(nextpage);
						catch (InterruptedException e) {
							e.printStackTrace();
						}
					} while (true);
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
		


	private String bestProvider = LocationManager.GPS_PROVIDER; // 最佳資訊提供者

	private void locationServiceInitial() {
		lms = (LocationManager) getSystemService(LOCATION_SERVICE); // 取得系統定位服務
		Criteria criteria = new Criteria(); // 資訊提供者選取標準
		bestProvider = lms.getBestProvider(criteria, true); // 選擇精準度最高的提供者
		Location location = lms.getLastKnownLocation(bestProvider);
		getLocation(location);
	}

	private void getLocation(Location location) { // 將定位資訊顯示在畫面中
		if (location != null) {
			longitude = location.getLongitude(); // 取得經度
			latitude = location.getLatitude(); // 取得緯度
		} else {
			//Toast.makeText(this, "無法定位座標", Toast.LENGTH_LONG).show();
		}
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			rString.setText(msg.getData().getString("in"));

		}
	};
	private Handler listup = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			listAdapter.notifyDataSetChanged();
			refresh.setText("Refresh");
		}
	};

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		System.out.println(location.getLatitude()+":"+location.getLongitude());
		getLocation(location);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

}
