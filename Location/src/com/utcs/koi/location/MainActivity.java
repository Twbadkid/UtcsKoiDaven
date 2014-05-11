package com.utcs.koi.location;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	Button send;
	TextView rString;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		send = (Button) findViewById(R.id.Send);
		rString = (TextView) findViewById(R.id.rString);
		send.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				new Thread(new Runnable() {
					@Override
					public void run() {
						send.setClickable(false);
						// TODO Auto-generated method stub
						// Log.e("TEST", "RUN");
						try {
							Socket clientSocket = new Socket("61.31.108.99",
									27104);
							// Log.e("TEST", "CON");
							DataOutputStream toServer = new DataOutputStream(
									clientSocket.getOutputStream());
							BufferedReader fromServer = new BufferedReader(
									new InputStreamReader(clientSocket
											.getInputStream()));
							toServer.write("我是中文123:中文是我\n".getBytes("UTF-8"));
							// Log.e("TEST", "SEND");
							String in = fromServer.readLine();
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
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						send.setClickable(true);
					}
				}).start();
			}
		});

	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			rString.setText(msg.getData().getString("in"));
		}
	};

}
