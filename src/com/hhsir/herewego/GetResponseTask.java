package com.hhsir.herewego;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

public class GetResponseTask extends AsyncTask<Object, Void, String> {
	Activity main;
	
	public GetResponseTask(Activity activity){
		this.main = activity;
	}

	@Override
	protected String doInBackground(Object... params) {
		try {
			Telnet client = (Telnet) params[0];
			BufferedInputStream instr = client.getStream();
			String cmd = (String) params[1];
			
			
			
			int len=instr.available();
			Log.e("Telnet", "len = "+len);
			byte[] buff = new byte[2048];
			int ret_read = 0;
			
//			instr.read(buff,0,len);
			Log.e("Telnet", "instr.read = "+instr.read(buff,0,len));
			
			client.sendCommand(cmd);
			publishProgress();
			try {
				//Need to implement listener ASAP
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			len=instr.available();
			while(len==0){
			    try {
	                //Need to implement listener ASAP
	                Thread.sleep(300);
	            } catch (InterruptedException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }
	            len=instr.available();
			}
			Log.e("Telnet", "len = "+len);
			if(len>0){
				ret_read=instr.read(buff,0,len);
				Log.e("Telnet", "ret_read = "+ret_read);
			}
			if(ret_read>0){
				String res = new String(buff,0,ret_read);
				Log.i("readline", res);
					
				return res;
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		super.onProgressUpdate(values);
	}

}
