package si.formias.gentian;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import si.formias.gentian.R;
import si.formias.gentian.service.AccountThread;
import si.formias.gentian.xml.Parser;
import si.formias.gentian.xml.config.GentianAccount;
import si.formias.gentian.xml.config.GentianConfig;
import si.formias.gentian.xml.messages.MessagesReply;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

public class GentianService extends Service {
    /** For showing and hiding our notification. */
    NotificationManager mNM;
    /** Keeps track of all current registered clients. */
    ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    /** Holds last value set by a client. */
    int mValue = 0;
    
    byte[] check;
    
    Map<String,AccountThread> threadMap=Collections.synchronizedMap(new LinkedHashMap<String,AccountThread>());
    
    /**
     * Command to the service to register a client, receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client where callbacks should be sent.
     */
    static final int MSG_REGISTER_CLIENT = 1;

    /**
     * Command to the service to unregister a client, ot stop receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client as previously given with MSG_REGISTER_CLIENT.
     */
    static final int MSG_UNREGISTER_CLIENT = 2;

    /**
     * Command to service to set a new value.  This can be sent to the
     * service to supply a new value, and will be sent by the service to
     * any registered clients with the new value.
     */
    static final int MSG_SET_VALUE = 3;

    static final int MSG_REGISTERED_WELCOME=4;
    
    static final int MSG_SET_CONFIG=5;
    
    static final int MSG_SEND_MESSAGE=6;
    
    static final int MSG_MESSAGE_REPLY=7;
    void initConnections() {
    	
    }
    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                	try {
                		Bundle bundle = msg.getData();
                		byte[] check=bundle.getByteArray("check");
                		if (GentianService.this.check==null || Util.equal(GentianService.this.check,check)) {
                			mClients.add(msg.replyTo);	
                			if (GentianService.this.check==null) {
                				GentianService.this.check=check;
                				initConnections();
                			}
                			msg.replyTo.send(Message.obtain(null,
                                    MSG_REGISTERED_WELCOME, 0, 0));
                		//	System.out.println("Gentian Service: Client registered");
                			
                			if (waitingNumberMessages>0) {
                				for (String user : waitingMessages.keySet()) {
                					List<si.formias.gentian.xml.messages.Message> list = waitingMessages.get(user);
                					MessagesReply reply = new MessagesReply(null);
                					reply.setUser(user);
                				reply.children().addAll(list);
                				Message msg1=Message.obtain(null,
                	                    MSG_MESSAGE_REPLY, mValue, 0);
                				Bundle bundle1 = new Bundle();
                				bundle1.putString("reply", reply.toString(0));
                				msg1.setData(bundle1);
                			//	System.out.println("Sent message reply to client..");
                				for (int i=mClients.size()-1; i>=0; i--) {
                					try {
                						
                						mClients.get(i).send(msg1);
                					} catch (RemoteException e) {
                	                // The client is dead.  Remove it from the list;
                	                // we are going through the list from back to front
                	                // so this is safe to do inside the loop.
                	                mClients.remove(i);
                					}
                				}
                				}

                			}
                			waitingNumberMessages=0;
                       		mNM.cancel(R.drawable.icon);
                		}
                	} catch (Exception e) {
                		
                	}
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
//                    System.out.println("Gentian Service: Client gone");
                    break;
                case MSG_SET_VALUE:
                    mValue = msg.arg1;
                    for (int i=mClients.size()-1; i>=0; i--) {
                        try {
                            mClients.get(i).send(Message.obtain(null,
                                    MSG_SET_VALUE, mValue, 0));
                        } catch (RemoteException e) {
                            // The client is dead.  Remove it from the list;
                            // we are going through the list from back to front
                            // so this is safe to do inside the loop.
                            mClients.remove(i);
                        }
                    }
                    break;
                case MSG_SET_CONFIG:
                {
                	Bundle bundle = msg.getData();
                	String configString = bundle.getString("config");
                	Parser p = new Parser();
                	try {
						p.parse(new ByteArrayInputStream(configString.getBytes("utf-8")));
	//					System.out.println("Gentian Service: Config set.");
						GentianConfig config = (GentianConfig) p.root;
						Set<String> accountKeys=new LinkedHashSet<String>();
						for (GentianAccount acc : config.accounts) {
							String id = acc.toString();
							accountKeys.add(id);
							if (!threadMap.containsKey(id)) {
								threadMap.put(id,new AccountThread(acc,GentianService.this));
							}
						}
						List<String> removeList=new ArrayList<String>();
						for (String key : threadMap.keySet()) {
							if (!accountKeys.contains(key)) {
								removeList.add(key);
							}
						}
						for (String threadId : removeList) {
							threadMap.remove(threadId).alive=false;
						}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                }	
                	break;
                case MSG_SEND_MESSAGE:
                {
                	Bundle bundle = msg.getData();
                    String acc=bundle.getString("account");
       //             System.out.println("looking for thread: "+acc);
                    AccountThread thread=threadMap.get(acc);
                    if (thread!=null) {
      //              	System.out.println("thread found: "+thread);
                    	thread.sendMessage(bundle.getString("target"), bundle.getString("msg"));
                    	
                    }
                }
                	break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.
        showNotification();
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(R.string.remote_service_started);
        for (String key : threadMap.keySet()) {
        	threadMap.get(key).alive=false;
        }
        threadMap.clear();
        // Tell the user we stopped.
        Toast.makeText(this, R.string.remote_service_stopped, Toast.LENGTH_SHORT).show();
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.remote_service_started);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.icon, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, GentianChat.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.app_name),
                       text, contentIntent);

        // Send the notification.
     //   We use a string id because it is a unique number.  We use it later to cancel.
     ///   mNM.notify(R.string.remote_service_started, notification);
    }
    volatile int waitingNumberMessages=0;
    Map<String,List<si.formias.gentian.xml.messages.Message>> waitingMessages=Collections.synchronizedMap(new LinkedHashMap<String,List<si.formias.gentian.xml.messages.Message>>());
	public void newMessageReply(GentianAccount account, MessagesReply reply) {
		if (mClients.size()>0) {
			Message msg=Message.obtain(null,
                    MSG_MESSAGE_REPLY, mValue, 0);
			Bundle bundle = new Bundle();
			bundle.putString("reply", reply.toString(0));
			msg.setData(bundle);
//			System.out.println("Sent message reply to client..");
			for (int i=mClients.size()-1; i>=0; i--) {
				try {
					
					mClients.get(i).send(msg);
				} catch (RemoteException e) {
                // The client is dead.  Remove it from the list;
                // we are going through the list from back to front
                // so this is safe to do inside the loop.
                mClients.remove(i);
				}
			}
        } else {
        	if (reply.messages.size()>0) {
        		for (si.formias.gentian.xml.messages.Message m : reply.messages) {
        			String user=account.getUser();
        			List<si.formias.gentian.xml.messages.Message> messages=waitingMessages.get(user);
        			if (messages == null) {
        				messages=new ArrayList<si.formias.gentian.xml.messages.Message>();
        				waitingMessages.put(user, messages);
        			}
        			messages.add(m);
        			waitingNumberMessages++;
        		}
        	}
        	if (waitingNumberMessages>0) {
        	String text=String.format(getText(R.string.waiting_messages).toString(),waitingNumberMessages);
  //      	System.out.println("gentian service notification: "+text);
        	Notification notification = new Notification(R.drawable.icon, text,
                    System.currentTimeMillis());
 
            // The PendingIntent to launch our activity if the user selects this notification
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, GentianChat.class), 0);

            notification.setLatestEventInfo(this, getText(R.string.app_name),
                    text, contentIntent);
        	mNM.notify(R.drawable.icon,notification);
        	} else {
        		mNM.cancel(R.drawable.icon);
        	}
        }
		
	}
}