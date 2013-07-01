package si.formias.gentian;
import java.net.URL;



import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GCMIntentService extends com.google.android.gcm.GCMBaseIntentService{

	public GCMIntentService() {
		super(GCM.GCM_REGISTER);
	}

	@Override
	protected void onError(Context arg0, String arg1) {
		Log.d("GCMIntentService","onError"+arg1);
		
	}

	@Override
	protected void onMessage(Context arg0, Intent arg1) {
		Log.d("GCMIntentService","onMessage"+arg1);
		final String alias=arg1.getStringExtra("alias");
		final String server=arg1.getStringExtra("server");
		
		if (alias!=null) {
			Log.d("GCMIntentService","Refresh alias:"+alias);
			final ServiceClient client = new ServiceClient(arg0);
			client.registerToService=false;
			client.onServiceConnected=new Runnable() {
				
				@Override
				public void run() {
					client.refreshNow(alias,server);
					client.doUnbindService();
				}
			};
			client.doBindService();
		}
	}

	@Override
	protected void onRegistered(Context arg0, final String arg1) {
		Log.d("GCMIntentService","onRegistered"+arg1);
		final ServiceClient client = new ServiceClient(arg0);
		client.registerToService=false;
		client.onServiceConnected=new Runnable() {
			
			@Override
			public void run() {
				client.sendGCMReg(arg1);
				client.doUnbindService();
			}
		};
		client.doBindService();
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		Log.d("GCMIntentService","onUnregistered"+arg1);
		String url = GCM.getBase()+"unregister?gcmkey="+arg1;
		if (GCM.isDebug()) Log.d("GCMIntentService","Unregistering GCM: "+url);
		try {
			new URL(url).openStream().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 

	
}
