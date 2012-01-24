package si.formias.gentian;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import javax.crypto.Cipher;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.xml.sax.SAXException;
import si.formias.gentian.R;
import si.formias.gentian.dialog.NewContactAddDialog;
import si.formias.gentian.http.HttpMagic;
import si.formias.gentian.tab.Contacts;
import si.formias.gentian.tab.Messages;

import si.formias.gentian.tab.msgcrypt.CryptShort;
import si.formias.gentian.xml.Parser;
import si.formias.gentian.xml.config.GentianAccount;
import si.formias.gentian.xml.config.GentianBuddy;
import si.formias.gentian.xml.messages.MessagesReply;


import com.google.zxing.integration.android.IntentIntegrator;

import gentian.crypt.GentianCrypt;
import gentian.crypt.GentianEnvelope;
import gentian.crypt.GentianKey;
import gentian.crypt.keyprovider.TestGentianKeyProvider;
import gentian.crypt.keyprovider.ZipGentianKeyProvider;
import gentian.util.Base64;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import static si.formias.gentian.Util.*;

public class GentianChat extends Activity {
	NotificationManager mNM;
	public static volatile GentianChat activeChat;
	public static final boolean CONVERTPHONENUMBERS=true;
	public static volatile boolean running;
	private static final int PICK_CONTACT = 1001;
	volatile boolean inited = false;
	public TopButtons topButtons;
	LinearLayout center;
	LinearLayout log;
	ScrollView logScroll;
	public static final int WHITE = Color.WHITE;
	public static final int GREEN = Color.GREEN;
	public static final int RED = Color.RED;
	public static final int BLUE = Color.parseColor("#6ea6e9");
	int build = 1;
	volatile Handler logHandler, noticeHandler;
	volatile String notice;
	public Config config;
 	
	public Contacts contacts;
	public Messages messages;
	public boolean landscape;
	TextView statusEncryption;
	/*final String[] quotes = new String[] {
			"You said you wanted to live without fear. I wish there'd been an easier way, but there wasn't.",
			"I know you may never forgive me... but nor will you understand how hard it was for me to do what I did. Every day I saw in myself everything you see in me now. Every day I wanted to end it, but each time you refused to give in, I knew I couldn't.",
			"Your own father said that artists use lies to tell the truth. Yes, I created a lie. But because you believed it, you found something true about yourself.",
			"The only verdict is vengeance; a vendetta, held as a votive, not in vain, for the value and veracity of such shall one day vindicate the vigilant and the virtuous.",
			"It means that I, like God, do not play with dice and I don't believe in coincidences.",
			"Remember, remember the 5th of November. The gunpowder, treason, and plot. I know of no reason why the gunpowder treason should ever be forgot.",
			"I remember how the meaning of words began to change. How unfamiliar words like \"collateral\" and \"rendition\" became frightening, while things like Norsefire and the Articles of Allegiance became powerful. I remember how \"different\" became dangerous. I still don't understand it, why they hate us so much.",
			"We're oft to blame, and this is too much proved, that with devotion's visage and pious action we do sugar on the devil himself.",
			"Today, however, is a day, sadly, no longer remembered. So, I thought we could mark this November the 5th by taking some time out of our daily lives to sit down and have a little chat. Of course, there are those who do not want us to speak. I suspect, even now, orders are being shouted into telephones and men with guns are racing to this station. But regardless of what weapons they try to use to effect silence, words will always retain their power. Words are the means to meaning, and for some, the annunciation of truth. And the truth is, there is something terribly wrong with this country. ",
			"I, like God, do not play with dice and do not believe in coincidence.",
			"Wait! Here comes the crescendo!",
			"People should not be afraid of their governments. Governments should be afraid of their people.",
			"There is a face beneath this mask but it's not me. I'm no more that face than I am the muscles beneath it or the bones beneath them.",
			"More than 400 years ago a great citizen wished to embed the fifth of November forever in our memory. His hope was to remind the world that fairness, justice, and freedom are more than words, they are perspectives. So if you've seen nothing, if the crimes of government remain unknown to you, then I suggest you allow the fifth of November to pass unmarked.",
			"What was done to me created me. It's a basic principle of the Universe that every action will create an equal and opposing reaction.",
			"There are 872 songs on here. I've listened to them all... but I've never danced to any of them." };*/
	public float density;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		final UncaughtExceptionHandler defaultUEH = Thread
		.getDefaultUncaughtExceptionHandler();

Thread
		.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				
				
				final Writer result = new StringWriter();
				final PrintWriter printWriter = new PrintWriter(result);
				ex.printStackTrace(printWriter);
				String stacktrace = "GENTIAN CRASH REPORT\n\n At "
						+ new java.util.Date().toString()
						+ "\n\n Error:\n"
						+ result.toString();
				printWriter.close();
				String filename = "error.txt";

				try {
					BufferedWriter bos = new BufferedWriter(
							new FileWriter(new File(Config.gentian,filename),
									true));
					bos.write(stacktrace);
					bos.flush();
					bos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				wipe();
				defaultUEH.uncaughtException(thread, ex);

			}

		});
	
		System.out.println("gen oncreate");
		super.onCreate(savedInstanceState);
		running=true;
		
		Display display = getWindowManager().getDefaultDisplay();
		landscape = display.getWidth() > display.getHeight();
		density = getResources().getDisplayMetrics().density;
		ImageView backgroundImage = new ImageView(this);
		backgroundImage.setImageResource(R.drawable.gentianinternets);

		FrameLayout frame = new FrameLayout(this);
		frame.addView(backgroundImage);

		center= new LinearLayout(this);
		center.setOrientation(LinearLayout.VERTICAL);
		log = new LinearLayout(this);
		log.setOrientation(LinearLayout.VERTICAL);
		logScroll=new ScrollView(this);
		logScroll.addView(log);
		frame.addView(center);
		contacts=new Contacts(this);
		messages=new Messages(this);
		LinearLayout screen=new LinearLayout(this);
		screen.setOrientation(LinearLayout.VERTICAL);
		topButtons = new TopButtons(this);
		screen.addView(topButtons,new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		screen.addView(frame,new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		setContentView(screen);
		text = null; 
		logHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				logDirect(text, color);
				text = null;
			}

		};
		noticeHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				displayNotice(notice,null);
				notice = null;
			}

		};
		
		/*LinearLayout main = new LinearLayout(this);
		main.setBackgroundColor(Color.parseColor("#70000000"));
		frame.addView(main, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));*/
		ScreenState state = (ScreenState) getLastNonConfigurationInstance();
		if (state != null) {
			
			this.inited = state.inited;
			this.config=state.config;
			this.currentThread = state.currentThread;
			if (this.currentThread != null) {
				for (MyThread t :this.currentThread) {
					t.main = this;
				}
			}

			for (LogEntry entry : state.log) {
				logDirect(entry.text, entry.color);
			}
			this.logList = state.log;
			topButtons.setTabIndex(state.tabIndex);
			contacts.loadState(state.contactsState);
			messages.loadState(state.messagesState);
	
		}
		if (config==null || !config.inited) config=new Config(this);
		if (!inited && currentThread.size()==0) {
			this.text = null;
			log(String.format(getText(R.string.log_started).toString(),build), WHITE);
			
			initConnections();
			// inited=true;
			// testEncryption();
		}
		Intent intent = getIntent();
		Bundle bundle=intent.getExtras();
		if (bundle!=null && bundle.getString("smsphone")!=null) {
			
			receiveSms(bundle.getString("smsphone"),System.currentTimeMillis(),bundle.getString("smstext"));
		}
		if (config.inited) {
			Config.gentianChat=this;
			configLoaded();
			
		}
	
		/*IntentIntegrator
				.shareText(
						GentianChat.this,
						"gentian://czqh2mlgi5fh6oe4:8050/YW55IGNgsaafahDdsdsdsgsgdgsdgsgsgsgsdfdfdhcm5hbCBwbGVhc3VyZS4=");
		Uri data = getIntent().getData();
		if (data != null) {
			String scheme = data.getScheme(); // "http"
			String host = data.getHost(); // "twitter.com"
			List<String> params = data.getPathSegments();
			System.out.println("Scheme: " + scheme);
			System.out.println("Host: " + host);
			if (params.size() > 0) {
				String first = params.get(0); // "status"
				System.out.println("First:" + first);
				if (params.size() > 1) {
					String second = params.get(1); // "1234"
					System.out.println("Second:" + second);
				}

			}
		}*/
		/*SecureRandom sr = new SecureRandom();
		byte[] salt=new byte[64];
		sr.nextBytes(salt);
		try {
			long startTime= System.currentTimeMillis();
			byte[] encrypted=encryptPassword(salt, "testpass");
			System.out.println(System.currentTimeMillis()-startTime+" ms to hash password");
			System.out.println("Verify pass: "+verifyPassword(salt, "testpass", encrypted));
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
	}

	List<MyThread> currentThread=Collections.synchronizedList(new ArrayList<MyThread>());

	public static class MyThread extends Thread {

		public GentianChat main;
		List<MyThread> currentThread;
		public void run() {
			currentThread.remove(this);
		}
		public void log(String s,int c) {
			main.log(s,c);
		}
	};
	public void registerTaskThread(MyThread t) {
		
		t.main=GentianChat.this;
		currentThread.add(t);
		t.currentThread=currentThread;
		
		t.start();
	}
	private void initConnections() {

		MyThread t = new MyThread() {
			public void run() {
				Random r = new Random();

				//log(quotes[r.nextInt(quotes.length)], BLUE);
				log(getText(R.string.log_connecting_master).toString(), WHITE);
				try {
					InputStream in = new URL(
							"http://q77wrnihwcsk3ff2.onion:8050/server.txt")
							.openConnection(
									new Proxy(Proxy.Type.HTTP,
											new InetSocketAddress("localhost",
													8118))).getInputStream();
					String reply = readStream(in, "utf-8");
					StringTokenizer st = new StringTokenizer(reply, "\n");
					int count = 0;
					while (st.hasMoreTokens()) {
						String name = st.nextToken();
						String target = st.nextToken();
						count++;
					}
					FileOutputStream fos = openFileOutput("server.txt",
							Context.MODE_PRIVATE);
					writeStream(fos, reply);

					log(String.format(getText(R.string.log_server_list_updated).toString(),count), GREEN);
				} catch (ConnectException e) {
					log(String.format(getText(R.string.log_orbot_connect_failed).toString(),e.getMessage()), RED);
					/*notice = getText(R.string.log_orbot_description).toString();
					noticeHandler.sendEmptyMessage(0);*/
				} catch (Exception e) {
					log(String.format(getText(R.string.log_server_list_update_failed).toString(), e.getMessage()), RED);
				}

				main.inited = true;

				super.run();
			}
		};
		
		t.setDaemon(true);
		registerTaskThread(t);
	}

	
	volatile String text;
	volatile int color;

	public void log(String text, int color) {
		//System.out.println("Gentian log: " + text);
		while (this.text != null) {

		}
		logList.add(new LogEntry(text, color));
		this.text = text;
		this.color = color;
		logHandler.sendEmptyMessage(0);

	}

	public void logDirect(String text, int color) {
		TextView view = new TextView(GentianChat.this);
		view.setTextColor(color);
		view.setText(text);
		text = null;
		log.addView(view, 0);

	}

	List<LogEntry> logList = new ArrayList<LogEntry>();

	@Override
	public Object onRetainNonConfigurationInstance() {
		for (Dialog d : dialogs) {
			try {
				d.dismiss();
			} catch (Exception e) {
				
			}
		}
		return new ScreenState(inited, logList, currentThread,config,topButtons.getTabIndex(),contacts.saveState(),messages.saveState());
	}

	public static class LogEntry {
		private String text;
		private int color;

		LogEntry(String text, int color) {
			this.text = text;
			this.color = color;
		}
	}
	Handler displayNoticeHandler=new Handler() {

		@Override
		public void handleMessage(Message msg) {
			try {
				class ClickClass implements DialogInterface.OnClickListener  {
					Dialog d;
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
		
							removeDialog(d);
							break;

						case DialogInterface.BUTTON_NEGATIVE:
							removeDialog(d);
							break;
						}
					}
				};
				ClickClass dialogClickListener = new ClickClass();
				AlertDialog.Builder builder = new AlertDialog.Builder(
						GentianChat.this);
				Bundle bundle = msg.getData();
				Dialog d = builder.setMessage(bundle.getString("notice")).setPositiveButton("OK",
						dialogClickListener)

				.show();
				dialogClickListener.d=d;
				addDialog(d);
			} catch (Exception e) {

			}

		}
		
	};
	public void displayNotice(String string) {
		Bundle b = new Bundle();
		b.putString("notice",string);
		Message msg = Message.obtain();
		msg.setData(b);
		displayNoticeHandler.sendMessage(msg);
		
	}
	public void displayNotice(String string,final Runnable r) {
		try {
			class ClickClass implements DialogInterface.OnClickListener  {
				Dialog d;
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						if (r!=null) r.run();
						removeDialog(d);
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						removeDialog(d);
						break;
					}
				}
			};
			ClickClass dialogClickListener = new ClickClass();
			AlertDialog.Builder builder = new AlertDialog.Builder(
					GentianChat.this);
			Dialog d = builder.setMessage(string).setPositiveButton(getText(R.string.buttons_ok).toString(),
					dialogClickListener)

			.show();
			dialogClickListener.d=d;
			addDialog(d);
		} catch (Exception e) {

		}

	}
	public void askYesNo(String string,final Runnable r) {
		try {
			class ClickClass implements DialogInterface.OnClickListener  {
				Dialog d;
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						if (r!=null) r.run();
						removeDialog(d);
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						removeDialog(d);
						break;
					}
				}
			};
			ClickClass dialogClickListener = new ClickClass();
			AlertDialog.Builder builder = new AlertDialog.Builder(
					GentianChat.this);
			Dialog d = builder.setMessage(string).setPositiveButton(getText(R.string.buttons_ok).toString(),
					dialogClickListener).setNegativeButton(getText(R.string.buttons_cancel).toString(),
							dialogClickListener)

			.show();
			dialogClickListener.d=d;
			addDialog(d);
		} catch (Exception e) {

		}

	}
	List<Dialog> dialogs=new ArrayList<Dialog>();
	public void removeDialog(Dialog viewDialog) {
		dialogs.remove(viewDialog);
		
	}
	public void addDialog(Dialog viewDialog) {
		dialogs.add(viewDialog);
	}
	
	
	
	
	
	
	/** Messenger for communicating with service. */
	Messenger mService = null;
	/** Flag indicating whether we have called bind on the service. */
	boolean mIsBound;
	/** Some text view we are using to show state information. */
	

	/**
	 * Handler of incoming messages from service.
	 */
	class IncomingHandler extends Handler {
	    @Override
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	            case GentianService.MSG_SET_VALUE:
	            	logDirect("Received from service: " + msg.arg1,Color.YELLOW);
	                break;
	            case GentianService.MSG_REGISTERED_WELCOME:
	            	//logDirect("Service accepted this client.",Color.YELLOW);
	            	break;
	            case GentianService.MSG_MESSAGE_REPLY:
	            {
	            	Parser parser = new Parser();
	            	try {
						parser.parse(new ByteArrayInputStream(msg.getData().getString("reply").getBytes("utf-8")));
						MessagesReply reply = (MessagesReply) parser.root;
			//			System.out.println("Received msg reply: "+reply.messages.size()+" messages");
						for (si.formias.gentian.xml.messages.Message m : reply.messages) {
							System.out.println("new message chat");
							contacts.newMessage(m,messages.newMessage(m));
							
						}
						if (reply.messages.size()>0) {
							contacts.updateConfig();
						}
					} catch (Exception e) {
						e.printStackTrace();
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

	/**
	 * Class for interacting with the main interface of the service.
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className,
	            IBinder service) {
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  We are communicating with our
	        // service through an IDL interface, so get a client-side
	        // representation of that from the raw service object.
	        mService = new Messenger(service);
	    

	        // We want to monitor the service for as long as we are
	        // connected to it.
	        try {
	            Message msg = Message.obtain(null,
	                    GentianService.MSG_REGISTER_CLIENT);
	            Bundle bundle = new Bundle();
	            bundle.putByteArray("check", config.check);
	            msg.setData(bundle);
	            msg.replyTo = mMessenger;
	            mService.send(msg);

	            sendConfigToService();
	         /*   // Give it some value as an example.
	            msg = Message.obtain(null,
	                    GentianService.MSG_SET_VALUE, this.hashCode(), 0);
	            mService.send(msg);
	            
	            // Give it some value as an example.
	            msg = Message.obtain(null,
	                    GentianService.MSG_SET_VALUE, this.hashCode(), 0);
	            mService.send(msg);*/
	            
	        } catch (RemoteException e) {
	            // In this case the service has crashed before we could even
	            // do anything with it; we can count on soon being
	            // disconnected (and then reconnected if it can be restarted)
	            // so there is no need to do anything here.
	        }

	        // As part of the sample, tell the user what happened.
	        
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        mService = null;
	        logDirect("Disconnected.",Color.YELLOW);


	    }
	};

	protected void doBindService() {
	    // Establish a connection with the service.  We use an explicit
	    // class name because there is no reason to be able to let other
	    // applications replace our component.
	    Intent intent = new Intent(GentianChat.this, 
	            GentianService.class);
	    
	      // start the service explicitly.
	      // otherwise it will only run while the IPC connection is up.
	      startService(intent);
	 
	      bindService(intent,mConnection, 0);
	 
	    mIsBound = true;
	    
	}
	public void stopService() {
		   Intent intent = new Intent(GentianChat.this, 
		            GentianService.class);
		    
		      // start the service explicitly.
		      // otherwise it will only run while the IPC connection is up.
		      stopService(intent);
	}
	void doUnbindService() {
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
	        unbindService(mConnection);
	        mIsBound = false;
	        
	    }
	}

	@Override
	protected void onDestroy() {
		
		// TODO Auto-generated method stub
		super.onDestroy();
		try {
			doUnbindService();
		} catch (Exception e) {
			e.printStackTrace();
		}
		running=false;
	}
	public void configLoaded() {
		contacts.updateContacts();
		activeChat=this;
		// doBindService();   // we'll do service later
		readSMS();
		Intent intent=getIntent();
		if (intent!=null) {
			Bundle bundle=intent.getExtras();
			if (bundle!=null && bundle.getString("smsphone")!=null) {
				//readSMS();
			} else {
				addContact(intent);
			}
		}
		
	}
	private void readSMS() {
		File f= new File(config.gentian,"smswaiting");
		if (f.exists()) {
			try {
				BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f),"utf-8"));
				String line;
				Set<String> lines = new LinkedHashSet<String>();
				while ((line=r.readLine())!=null) {
					lines.add(line);
				}
				r.close();
				//System.out.println("READ SMS, lines:"+lines);
				for (String l : lines) {
					int index=l.indexOf(":");
					
					if (index!=-1) {
						int index2=l.indexOf(":",index+1);
						
						receiveSms(l.substring(0,index),Long.parseLong(l.substring(index+1,index2)), l.substring(index2+1));
					}
				}
				f.delete();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	
		if (mNM ==null) {
        	mNM = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        	
        }
		mNM.cancel(R.drawable.smscontact);
	}
	public void addContact(Intent i) {
		if (i!=null) {
			addContact(i.getData()); 
		}
	}
	public void addContact (Uri data) {
		if (config==null || config.configData==null) return;
		if (data != null) {
			String scheme = data.getScheme(); // "http"
			String host = data.getHost(); // "twitter.com"
			List<String> params = data.getPathSegments();
			//System.out.println("Scheme: " + scheme);
//			System.out.println("Host: " + host);
	//		System.out.println("Port: " + data.getPort());
			if (params.size() > 0) {
				final String user = params.get(0).replace("-","+").replace("|","/");
				String second="";
				String third="";
				String fourth="";
				String fifth="";
		//		System.out.println("User:" + user);
				if (params.size() > 1) {
					second = params.get(1); // "1234"
			//		System.out.println("Second:" + second);
					if (params.size()>2) {
						third=params.get(2);
						if (params.size()>3) {
							fourth=params.get(3);
							if (params.size()>4) {
								fifth=params.get(4);	
							}
						}
					}
				}
				 
				if (scheme.equals("gentian")) {
					GentianBuddy buddy=null;
					/*if (config.configData.getAccount(host,data.getPort(),user)!=null) {
						System.out.println("This is me!");
						displayNotice(getText(R.string.newcontactadd_that_is_me),null);
					}
					else*/ if ((buddy=config.configData.getContact(host,data.getPort(),user))!=null) {
					displayNotice(String.format(getText(R.string.newcontactadd_already_added).toString(),buddy.getDisplayName()),null);  
					} else {
						final String cryptModulusString =second.replace("-","+").replace("|","/");;
						final String cryptPublicExponentString =third.replace("-","+").replace("|","/");;
						final String signModulusString =fourth.replace("-","+").replace("|","/");;
						final String signPublicExponentString =fifth.replace("-","+").replace("|","/");;
						//System.out.println("New contact");
						new NewContactAddDialog(GentianChat.this, new NewContactAddDialog.CallBack() {
							
							@Override
							public void newAccountChosen(String host, int port,final String nick) {
									//System.out.println("Create new account "+host+":"+port);
									registerAccount(host, port,new OnRegistrationRunnable() {public void run() {
										registerContactImpl(account,user,nick,container);
											
									}});
							}
							
							@Override
							public void existingAccountChosen(GentianAccount acc,String nick) {
									//System.out.println("Use existing account "+acc);
									registerContact(acc,user,nick);
								
							}

							@Override
							public void smsContact(GentianAccount acc,
									String nick, String phone) {
								GentianBuddy buddy;
								try {
									buddy = new GentianBuddy(null);
									buddy.setNick(nick);
									buddy.setTextOf(GentianBuddy.USER,user);
									buddy.setTextOf(GentianBuddy.CRYPTPUBLICEXPONENT,cryptPublicExponentString);
									buddy.setTextOf(GentianBuddy.CRYPTMODULUS, cryptModulusString);
									buddy.setTextOf(GentianBuddy.SIGNPUBLICEXPONENT,signPublicExponentString);
									buddy.setTextOf(GentianBuddy.SIGNMODULUS, signModulusString);
									
									buddy.setTextOf(GentianBuddy.TARGET,phone);
									acc.add(buddy);
									if (GentianChat.CONVERTPHONENUMBERS) {

										if (buddy.getAccount().getServer().equals(GentianAccount.SMS) && buddy.getTarget()!=null && !buddy.getTarget().startsWith("+")) {
											System.out.println("converting to international");
											TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
											/*System.out.println("Sim country:"
													+ manager.getSimCountryIso()+" Sim operator:"+manager.getSimOperator());*/
											buddy.setTextOf(GentianBuddy.TARGET, CountryPhone.prefixForOperator(manager.getSimOperator())+buddy.getTarget().substring(1));
											//System.out.println(CountryPhone.prefixForOperator(manager.getSimOperator())+buddy.getTarget().substring(1));
										}
									}
									config.saveConfig();
									sendConfigToService();
								} catch (SAXException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
								updateContacts();
								


							}
						}, config.configData.getAccounts(host,data.getPort()),  host, data.getPort(),"");
						
					} 
				}
			}
		}
		
	}
	@Override
	protected void onNewIntent(Intent intent) {
		//System.out.println("On new intent..."+intent);
		readSMS();
		Bundle bundle=intent.getExtras();
		
		if (bundle!=null && bundle.getString("smsphone")!=null) {
			//System.out.println("Receive sms"+bundle.getString("smstext"));
			//receiveSms(bundle.getString("smsphone"),bundle.getString("smstext"));
		} else {
			addContact(intent);
		}
		
	}
	String lastsms;
	boolean receiveSms(String phone, long time, String text) {
	
		if (config==null || config.configData==null) {
			System.out.println("sms: will wait");
			File f= new File(Config.gentian,"smswaiting");
			ArrayList<String> lines = new ArrayList<String>();
			if (f.exists()) {
				try {
					BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f),"utf-8"));
					String line;
					
					while ((line=r.readLine())!=null) {
						lines.add(line);
					}
					r.close();
					
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			try {
			lines.add(phone+":"+System.currentTimeMillis()+":"+text);
			PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f),"utf-8"));
			for (String l : lines) {
				w.println(l);
			}
			w.close();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
		System.out.println("sms: receive");
		if (lastsms!=null && lastsms.equals(text)) return true;
		
		lastsms=text;
		GentianAccount smsacc=config.configData.accounts.get(0);
		si.formias.gentian.xml.messages.Message m;
		try {
			m = new si.formias.gentian.xml.messages.Message(null);
			String user=smsacc.getUserByTarget(phone);
			m.setText(text);
			m.setTimeStamp(time);
			if (user!=null) {
				m.setUser(user);
				contacts.newMessage(m,messages.newMessage(m));
			
				contacts.updateConfig();
			} else {
				displayNotice(getText(R.string.received_sms_unknown_phone).toString()+": "+phone+"\n"+CryptShort.decrypt(smsacc,null, text),null);
				//System.out.println("Received sms from unknown phone: "+phone+" "+text);
			}
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		return true;
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//System.out.println("Activity result "+requestCode+","+resultCode);
		if (requestCode==IntentIntegrator.REQUEST_CODE) {
			  if (resultCode == RESULT_OK) {
		            String contents = data.getStringExtra("SCAN_RESULT");
		            String format = data.getStringExtra("SCAN_RESULT_FORMAT");
		            addContact(Uri.parse(contents));
		            // Handle successful scan
		        }
		} else if(requestCode== PICK_CONTACT) {
			//System.out.println("Pick contact result");
			new PhoneContact().applyCallback(this, contactCallBack, data);
			
		}

	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (activeChat==this) activeChat=null;
		if (config.inited) {
			doUnbindService();
		}
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (config.inited) {
			doBindService();
			activeChat=this;
		}
	}
	
	public void sendConfigToService() {
		if (config.inited) {
            Message msg = Message.obtain(null,
                    GentianService.MSG_SET_CONFIG);
            Bundle bundle = new Bundle();
            bundle.putString("config", config.configData.toString(0));
            msg.setData(bundle);
            msg.replyTo = mMessenger;
            try {
				mService.send(msg);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	static abstract class OnRegistrationRunnable implements Runnable {
		GentianAccount account;
		MyThread container;
	}
	public void registerAccount(final String host, final int port,final OnRegistrationRunnable onSuccess) {
		registerTaskThread(new MyThread(){public void run() {
			try {
				main.log(String.format(main.getText(R.string.newaccount_register_generatingrsa).toString(),host,port),GentianChat.WHITE);
				long start=System.currentTimeMillis();
				GentianAccount acc=new GentianAccount(host, port);
			
				main.log(String.format(main.getText(R.string.newaccount_register_rsagenerated).toString(),Integer.toString(GentianAccount.KEYLEN),(System.currentTimeMillis()-start)),GentianChat.BLUE);
				main.log(main.getText(R.string.newaccount_register_withserver).toString(),GentianChat.WHITE);
				Map<String,String> postMap=new LinkedHashMap<String,String>();
				postMap.put("user", acc.getUser());
				postMap.put("password",acc.getPassword());
				postMap.put("cryptmodulus",acc.getCryptModulusString());
				postMap.put("cryptpublicexponent",acc.getCryptPublicExponentString());
				postMap.put("signmodulus",acc.getCryptModulusString());
				postMap.put("signpublicexponent",acc.getCryptPublicExponentString());
				
				HttpMagic magic=new HttpMagic("utf-8");
				HttpEntity entity;
				
				try {
					entity = magic.postURL("http://" +acc.getServer()+".onion:"+acc.getPort()+ "/register/",magic.getPostData(postMap), null);
					String s=Util.readStream(entity.getContent());
					if (s.equals("OK")) {
						main.log(main.getText(R.string.newaccount_register_success).toString(),GentianChat.GREEN);
						main.config.configData.add(acc);
						main.config.saveConfig();
						main.sendConfigToService();
						main.updateContacts();
						if (onSuccess!=null) {
							onSuccess.container=this;
							onSuccess.account=acc;
							onSuccess.run();
						}
					} else {
						main.log(main.getText(R.string.newaccount_register_fail).toString(),GentianChat.RED);
					}
					
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			super.run();
			}});					;		
	}
	Handler updateContantsHandler=new Handler()
	{

		@Override
		public void handleMessage(Message msg) {
			contacts.updateContacts();
		}
		
	};
	private NewContactAddDialog contactCallBack;
	public void updateContacts() {
		updateContantsHandler.sendEmptyMessage(0);
		
	}
	public void registerContact(final GentianAccount account,final String user,final String nick) {
		registerTaskThread(new MyThread(){public void run() {
			registerContactImpl(account, user, nick,this);
			super.run();
		}});
	}
	private void registerContactImpl(GentianAccount account,String user,String nick,MyThread container) {
		Map<String,String> postMap=new LinkedHashMap<String,String>();
		postMap.put("user", user);
		HttpMagic magic=new HttpMagic("utf-8");
		HttpEntity entity;
		
		try {
			entity = magic.postURL("http://" +account.getServer()+".onion:"+account.getPort()+ "/user/",magic.getPostData(postMap), null);
			Parser parser = new Parser();
			parser.parse(entity.getContent());
			
			GentianBuddy buddy = (GentianBuddy) parser.root;
			buddy.setNick(nick);
				String msg=container.main.getText(R.string.newcontactadd_gotuser).toString();
				container.log(msg,GentianChat.GREEN);
				account.add(buddy);
				container.main.config.saveConfig();
				container.main.sendConfigToService();
				container.main.updateContacts();
				container.main.displayNotice(msg);
			
			
			
		} catch (Exception e1) {
			String msg=container.main.getText(R.string.newcontactadd_failuser).toString();
			container.log(msg,GentianChat.RED);
			container.main.displayNotice(msg);
			e1.printStackTrace();
		}
		
	}
	public void doLaunchContactPicker(NewContactAddDialog callback) {
		this.contactCallBack = callback;

		startActivityForResult(new PhoneContact().createIntent(), PICK_CONTACT);

	}


	public void sendText(GentianBuddy buddy, String text) {
		if (buddy.getAccount().getServer().equals(GentianAccount.SMS)) {
			if (config.inited) {
//				System.out.println("text:"+text);
//				System.out.println("text len:"+text.length());
			    
			    ArrayList<String> list = new ArrayList<String>();
			    boolean start=false;
			    int index=0;
			    StringBuilder sb=null;
			    int count=0;
//			    System.out.println("System will divide it into:"+SmsManager.getDefault().divideMessage(text).get(0).length()+" bytes");
			    for (char c : text.toCharArray()) {
			    	if (start==false) {
			    		if (sb!=null) {
			    			list.add(sb.toString());
			    		}
			    		start=true;
			    		index++;
			    		sb=new StringBuilder();
			    		//if (index>5) return;
			    		if (index>8) return;
			    		count=0;
			    		
			    	}
			    	count++;
			    	sb.append(c);
			    	
			    	if (count>151) {
			    	//if (count>152) {
			    		start=false;
			    	}
			    }
			    if (sb!=null) {
	    			list.add(sb.toString());
	    		}
			    String target=buddy.getTarget().trim();
			    
//			    System.out.println("target:'"+target+"'");
			    for (int i=0;i<list.size();i++) {
			    	//String s=/*"gentian"+*/Character.toString((char)('A'+i+((list.size()-1)*5)))+list.get(i);
			    	String s=/*"gentian"+*/Integer.toString(i)+Integer.toString((list.size()-1))+list.get(i);
			    	//System.out.println("split("+i+")=("+s.length()+") "+s);
			    	sendSMS(target,s);
			    }
			    
			    

			}
		} else {
			if (config.inited) {
	            Message msg = Message.obtain(null,
	                    GentianService.MSG_SEND_MESSAGE);
	            Bundle bundle = new Bundle();
	            bundle.putString("account", buddy.getAccount().toString());
	            bundle.putString("target", buddy.getUser());
	            bundle.putString("msg", text);
	            
	            msg.setData(bundle);
	            msg.replyTo = mMessenger;
	            try {
					mService.send(msg);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}
	 private void sendSMS(String phoneNumber, String message)
	 //http://mobiforge.com/developing/story/sms-messaging-android
	    {        
	        String SENT = "SMS_SENT";
	        String DELIVERED = "SMS_DELIVERED";
	 
	        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
	            new Intent(SENT), 0);
	 
	        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
	            new Intent(DELIVERED), 0);
	 
	        //---when the SMS has been sent---
	        registerReceiver(new BroadcastReceiver(){
	            @Override
	            public void onReceive(Context arg0, Intent arg1) {
	                switch (getResultCode())
	                {
	                    case Activity.RESULT_OK:
	                        /*Toast.makeText(getBaseContext(), "SMS sent", 
	                                Toast.LENGTH_SHORT).show();*/
	                        break;
	                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
	                        Toast.makeText(getBaseContext(), "Generic failure", 
	                                Toast.LENGTH_SHORT).show();
	                        break;
	                    case SmsManager.RESULT_ERROR_NO_SERVICE:
	                        Toast.makeText(getBaseContext(), "No service", 
	                                Toast.LENGTH_SHORT).show();
	                        break;
	                    case SmsManager.RESULT_ERROR_NULL_PDU:
	                        Toast.makeText(getBaseContext(), "Null PDU", 
	                                Toast.LENGTH_SHORT).show();
	                        break;
	                    case SmsManager.RESULT_ERROR_RADIO_OFF:
	                        Toast.makeText(getBaseContext(), "Radio off", 
	                                Toast.LENGTH_SHORT).show();
	                        break;
	                }
	            }
	        }, new IntentFilter(SENT));
	 
	        //---when the SMS has been delivered---
	        /*registerReceiver(new BroadcastReceiver(){
	            @Override
	            public void onReceive(Context arg0, Intent arg1) {
	                switch (getResultCode())
	                {
	                    case Activity.RESULT_OK:
	                        Toast.makeText(getBaseContext(), "SMS delivered", 
	                                Toast.LENGTH_SHORT).show();
	                        break;
	                    case Activity.RESULT_CANCELED:
	                        Toast.makeText(getBaseContext(), "SMS not delivered", 
	                                Toast.LENGTH_SHORT).show();
	                        break;                        
	                }
	            }
	        }, new IntentFilter(DELIVERED));*/        
	 
	        SmsManager sms = SmsManager.getDefault();
	        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);        
	    }
	 
	 @Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			
			if (keyCode == KeyEvent.KEYCODE_BACK) { 
					wipe();				
			}
			return super.onKeyDown(keyCode, event);
		}
		
	 
	 boolean wiped=false;
	 void wipe() {
		 if (wiped) return;
		 new Thread() {
			 public void run() {
				 if (config!=null) {
						config.wipe();
						
					}	 
			 }
		 }.start();
			wiped=true;
	 }
}