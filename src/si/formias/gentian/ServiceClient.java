package si.formias.gentian;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class ServiceClient {
	/** Messenger for communicating with service. */
	Messenger mService = null;
	/** Flag indicating whether we have called bind on the service. */
	boolean mIsBound;
	private Context context;

	public ServiceClient(Context context) {
		this.context = context;
		startService();
	}

	/**
	 * Handler of incoming messages from service.
	 */
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			/*
			 * case KatalogService.MSG_SET_VALUE:
			 * System.out.println("Received from service: " + msg.arg1); break;
			 */

			default:
				super.handleMessage(msg);
			}
		}

	}

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	/**
	 * Class for interacting with the main interface of the service.
	 */
	public boolean registerToService = true;
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. We are communicating with our
			// service through an IDL interface, so get a client-side
			// representation of that from the raw service object.
			mService = new Messenger(service);
			Log.d("ServiceClient", "Attached.");

			// We want to monitor the service for as long as we are
			// connected to it.
			try {
				Message msg = Message.obtain(null,
						GentianService.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				Bundle bundle = new Bundle();
				bundle.putString("caller", context.getClass().toString());
				bundle.putBoolean("register", registerToService);
				msg.setData(bundle);
				mService.send(msg);

				// Give it some value as an example.
				/*
				 * msg = Message.obtain(null, KatalogService.MSG_SET_VALUE,
				 * this.hashCode(), 0); mService.send(msg);
				 */
			} catch (RemoteException e) {
				// In this case the service has crashed before we could even
				// do anything with it; we can count on soon being
				// disconnected (and then reconnected if it can be restarted)
				// so there is no need to do anything here.
			}

			// As part of the sample, tell the user what happened.

			if (onServiceConnected != null) {
				onServiceConnected.run();

			}
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mService = null;
			System.out.println("Disconnected.");

			// As part of the sample, tell the user what happened.

		}
	};
	public Runnable onServiceConnected;

	private void startService() {
		Intent intent = new Intent(context, GentianService.class);
		context.startService(intent);
	}

	public void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because there is no reason to be able to let other
		// applications replace our component.
		startService();
		Intent intent = new Intent(context, GentianService.class);

		context.bindService(intent, mConnection, 0);

		mIsBound = true;
		System.out.println("Binding.");
	}

	public boolean isConnected() {
		return mIsBound && mService != null;
	}

	public void doUnbindService() {
		if (mIsBound) {
			// If we have received the service, and hence registered with
			// it, then now is the time to unregister.
			if (mService != null) {
				try {
					Message msg = Message.obtain(null,
							GentianService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {
					// There is nothing special we need to do if the service
					// has crashed.
				}
			}

			// Detach our existing connection.
			context.unbindService(mConnection);
			mIsBound = false;
			mService = null;
			System.out.println("Unbinding.");
		}
	}

	public void refreshNow(String alias, String server) {
		Message msg = Message.obtain(null, GentianService.MSG_REFRESHNOW);
		msg.replyTo = mMessenger;
		Bundle b = new Bundle();

		if (alias != null) {
			b.putString("alias", alias);
			b.putString("server", server);
		}
		msg.setData(b);
		try {
			mService.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendGCMReg(String regId) {
		Message msg = Message.obtain(null, GentianService.MSG_SEND_GCM_REG);
		msg.replyTo = mMessenger;
		Bundle bundle = new Bundle();
		bundle.putString("gcmregid", regId);
		Log.d("ServiceClient",
				"Sending gcm regId:" + bundle.getString("gcmregid"));
		msg.setData(bundle);
		try {
			mService.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
