package si.formias.gentian;

import java.util.List;

import si.formias.gentian.R;
import si.formias.gentian.GentianChat.LogEntry;
import si.formias.gentian.GentianChat.MyThread;
import si.formias.gentian.dialog.DialogLauncher;

public class ScreenState {
	public final boolean inited;
	public final List<GentianChat.LogEntry> log;
	public final List<MyThread> currentThread;
	public final Config config;
	public final int tabIndex;
	public final Object contactsState;
	public final Object messagesState;
	public boolean torChoice;
	public final DialogLauncher dialogLauncher;
	public final Object dialogLauncherData;

	public ScreenState(boolean inited, List<LogEntry> logList,
			List<MyThread> currentThread, Config config, int tabIndex,
			Object contactsState, Object messagesState, boolean torChoice, DialogLauncher dialogLauncher) {
		this.inited = inited;
		this.log = logList;
		this.currentThread = currentThread;
		this.config = config;
		this.tabIndex = tabIndex;
		this.contactsState = contactsState;
		this.messagesState = messagesState;
		this.torChoice = torChoice;
		this.dialogLauncher=dialogLauncher;
		if (dialogLauncher!=null) {
			this.dialogLauncherData=dialogLauncher.retainDialogData();
		} else {
			this.dialogLauncherData=null;
		}
	}

}
