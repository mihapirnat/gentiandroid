package si.formias.gentian;

import java.util.List;

import si.formias.gentian.R;
import si.formias.gentian.GentianChat.LogEntry;
import si.formias.gentian.GentianChat.MyThread;

public class ScreenState {
	public final boolean inited;
	public final List<GentianChat.LogEntry> log;
	public final List<MyThread> currentThread;
	public final Config config;
	public final int tabIndex;
	public final Object contactsState;
	public final Object messagesState;
	public ScreenState(boolean inited, List<LogEntry> logList,List<MyThread> currentThread,Config config,int tabIndex, Object contactsState, Object messagesState) {
		this.inited=inited;
		this.log=logList;
		this.currentThread=currentThread;
		this.config=config;
		this.tabIndex=tabIndex;
		this.contactsState=contactsState;
		this.messagesState=messagesState;
	}
	
	
}
