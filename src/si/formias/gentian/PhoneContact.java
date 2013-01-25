package si.formias.gentian;

import si.formias.gentian.R;
import si.formias.gentian.dialog.NewContactAddDialog;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;

public class PhoneContact {
	public void applyCallback(GentianChat activity,NewContactAddDialog contactCallBack, Intent data) {

		try {
		// http://stackoverflow.com/questions/866769/how-to-call-android-contacts-list
		if (data==null) return;
   	 Cursor cursor =  activity.getContentResolver().query(data.getData(), null, null, null, null);      
   	   while (cursor.moveToNext()) 
   	   {           
   	       String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
   	    

   	       String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
   	    String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
   	       if ( hasPhone.equalsIgnoreCase("1"))
   	           hasPhone = "true";
   	       else
   	           hasPhone = "false" ;

   	       if (Boolean.parseBoolean(hasPhone)) 
   	       {
   	        Cursor phones = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId,null, null);
   	        String[] phoneNumber=new String[phones.getCount()];
   	     String[] contact=new String[phones.getCount()];
   	        int i=0;
   	        while (phones.moveToNext()) 
   	        {
   	        	contact[i]=displayName;
   	          phoneNumber[i++] = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
   	       		
   	        }
   	        phones.close();
   	        if (contactCallBack!=null) contactCallBack.onContactNumberPicked(phoneNumber,contact);
   	       }



   	  }  //while (cursor.moveToNext())        
   	   cursor.close();
		} catch (RuntimeException e ){
			e.printStackTrace();
			throw(e);
		}

	}

	public Intent createIntent() {
		Intent intent = new Intent(Intent.ACTION_PICK,  
	            Contacts.CONTENT_URI);
		return intent;
	}

}
