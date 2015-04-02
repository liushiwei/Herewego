package com.hkd.socketclient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends Activity {

	private Telnet client = null;
	private PioneerController pioneer = null;
	private Toast fastToast;
	private static TextView et;
	private static NumberPicker numpicker;
	private static int SERVERPORT = 23;
	private static String SERVER_IP = "192.168.0.105";
	private static final int MIN_VOL = 0;
	private static final int MAX_VOL = 80;	

	@SuppressLint("ShowToast")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);		
		
		EditText etIp = (EditText) findViewById(R.id.EditTextIp);
		et = (TextView) findViewById(R.id.inputStreamTextView);
		et.setMovementMethod(new ScrollingMovementMethod());
		NumberPicker num = (NumberPicker) findViewById(R.id.volumePicker);
		numpicker=num;
		numpicker.setMinValue(MIN_VOL);
		numpicker.setMaxValue(MAX_VOL);

		
				
		fastToast = Toast.makeText(this,"", Toast.LENGTH_SHORT);
		
		SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
		if(sharedPref.contains("last_server"))
			etIp.setText(sharedPref.getString("last_server", ""));
	}
	
	public void onVolumeChange(View view){
		if(client==null || !client.isConnected()){
			toastFast("Not connected to a server");			
			return;
		}
		
		pioneer.changeVolume(numpicker.getValue());
		//pioneer.getVolume();
	}
	
	@Override
	protected void onStop() {
		if (client!=null && client.isConnected()) {
			if(disconnect()) toastFast("Disconnected from server");
			else toastFast("Error disconnecting from server");
		}		
		super.onStop();
	}

	
	void toastFast(String str) {
		
		fastToast.setText(str);
		fastToast.show();
	}

	private boolean etIsEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }
	
	
	private boolean disconnect(){
		if(client.disconnect()){
			setPower(false);
			return true;
		}
		return false;
	}
	
	public void onClickConnect(View view){
		EditText etIp = (EditText) findViewById(R.id.EditTextIp);
		//EditText etPort = (EditText) findViewById(R.id.EditTextPort);
		
		if(!etIsEmpty(etIp)){	
			String tmp = etIp.getText().toString();
			
			if(tmp.contains(":")){
				String[] address = tmp.split(":");
				SERVER_IP = address[0];
				SERVERPORT = Integer.parseInt(address[1]);
			}
			else{
				SERVER_IP = etIp.getText().toString();
			}
			
			SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putString("last_server", tmp);
			editor.commit();
			

		}
		else 
			toastFast("Enter a server IP");
		
		
		
		if(client!=null && client.isConnected()) 
			toastFast("Already connected");
		else
			try {
				client = new Telnet(this, SERVER_IP,SERVERPORT);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			pioneer = new PioneerController(this,client);
//			pioneer.pioneerIsOn(true);
//			numpicker.setValue((pioneer.getVolume(true)-1)/2);
		    GetResponseAsync responsetask = new GetResponseAsync("", 1000);
		    try {
                Log.e("Telnet", responsetask.execute().get());
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		
		return;
	}
	
	public void setPower(boolean on){
		ToggleButton power = (ToggleButton) findViewById(R.id.powerButton);
		power.setChecked(on);
		return;
	}
	
	public void onClickDisconnect(View view){
		if (client!=null && client.isConnected()) {
			if(disconnect()){
				toastFast("Disconnected from server");
			}
			else toastFast("Error disconnecting from server");
		}
		else{
			toastFast("Already disconnected");
		}
		return;
	}
	
	public void onClickSend(View view) {
		
		if(client==null || !client.isConnected()){
			toastFast("Not connected to a server");			
			return;
		}
		
		EditText et = (EditText) findViewById(R.id.EditTextCommand);
				
//		client.sendCommand(et.getText().toString());
		GetResponseAsync responsetask = new GetResponseAsync(et.getText().toString(), 1000);
        try {
            Log.e("Telnet", responsetask.execute().get());
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		
	}
	
	public void onClickPower(View view){
		if(client==null || !client.isConnected()){
			toastFast("Not connected to a server");			
			return;
		}
		
		pioneer.togglePower();
	}
	
	public void onClickPlay(View view){
		pioneer.play();
	}
	
	public void onClickStop(View view){
		pioneer.stop();
	}

	public void onClickXbox(View view){
		pioneer.input("Xbox");
	}

	public void onClickProjector(View view){
		pioneer.input("Projector");
	}
	
	public void onClickRP(View view){
		pioneer.input("Radio Paradise");
	}

	public void appendToConsole(String str){
		et.append(str);
		return;
	}
	
	public void resetConsole(){
		et.setText("");
		return;
	}
	
	public void setConsole(String str){
		et.setText(str);
		return;
	}
	
	public String getConsole(){
		return et.getText().toString();
	}
	
	private class  GetResponseAsync extends AsyncTask<Void,Void,String>{
        final String cmd;
        final int timeout;
        GetResponseTask response;
        
        public GetResponseAsync(String cmd, int timeout){
            this.cmd = cmd;
            this.timeout = timeout;
            response = client.getResponse(MainActivity.this);
        }
        
        
        protected void onPreExecute(){          
            response.execute(client,cmd);
        }
        
        @Override
        protected String doInBackground(Void... params) {
            String result;

            try {
                result = response.get(timeout,TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException
                    | TimeoutException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
            if(result==null){
                return "";
            }
            result = result.replace("\r\n", "");
            result = result.replace(" ", "");
            
            return result;
        }
        
        

    }
}