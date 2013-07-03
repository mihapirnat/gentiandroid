package si.formias.gentian.dialog;

import si.formias.gentian.GentianChat;

public interface DialogLauncher {
	public void launchDialog(GentianChat main,Object savedInstance);
	public Object retainDialogData();
	
}
