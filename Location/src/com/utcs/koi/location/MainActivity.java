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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private LocationManager lms;
	private Button send;
	private TextView rString;
	private Double longitude;
	private Double latitude;
	private Location location;

	protected void onPause() {
		super.onPause();
		lms = null;
		location = null;
	}

	protected void onResume() {
		super.onResume();
		LocationManager status = (LocationManager) (this
				.getSystemService(Context.LOCATION_SERVICE));
		if (status.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			// 如果GPS或網路定位開啟，呼叫locationServiceInitial()更新位置
			locationServiceInitial();
		} else {
			Toast.makeText(this, "請開啟定位服務", Toast.LENGTH_LONG).show();
			startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)); // 開啟設定頁面
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		send = (Button) findViewById(R.id.Send);
		rString = (TextView) findViewById(R.id.rString);
		send.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				sendMessage();
			}
		});

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
					toServer.write(("我是中文123:中文是我:" + longitude + ":"
							+ latitude + "\n").getBytes("UTF-8"));
					// Log.e("TEST", "SEND");
					String in = fromServer.readLine();
					Message msg = new Message();
					Bundle se = new Bundle();
					se.putString("in", in + ":" + longitude + ":" + latitude);
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
					URL urlc = new URL(url);
					URLConnection conn = urlc.openConnection();
					conn.connect();
					BufferedReader in = new BufferedReader(
							new InputStreamReader(conn.getInputStream()));
					String read;
					String json = "";
					while ((read = in.readLine()) != null) {
						json += read;
					}
					in.close();
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	LocationListener lls = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub

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
	};

	private void locationServiceInitial() {
		lms = (LocationManager) getSystemService(LOCATION_SERVICE); // 取得系統定位服務
		location = lms.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); // 使用GPS定位座標
		getLocation(location);
	}

	private void getLocation(Location location) { // 將定位資訊顯示在畫面中
		if (location != null) {
			longitude = location.getLongitude(); // 取得經度
			latitude = location.getLatitude(); // 取得緯度
		} else {
			Toast.makeText(this, "無法定位座標", Toast.LENGTH_LONG).show();
		}
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			rString.setText(msg.getData().getString("in"));
		}
	};

}
