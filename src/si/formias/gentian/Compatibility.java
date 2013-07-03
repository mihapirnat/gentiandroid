package si.formias.gentian;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import si.formias.gentian.R;

import android.content.Context;
import android.os.PowerManager;

public class Compatibility {

	static {
		initCompatibility();
	};

	private static void initCompatibility() {

	}

	public static boolean isScreenOn(Context main) {
		Method method_isScreenOn;
		if (main == null)
			return true;
		try {
			method_isScreenOn = PowerManager.class.getMethod("isScreenOn",
					new Class[] {});

			if (method_isScreenOn != null) {

				PowerManager powerManager = (PowerManager) main
						.getSystemService(Context.POWER_SERVICE);

				Boolean b = (Boolean) (method_isScreenOn.invoke(powerManager,
						null));

				return b;
			}
		} catch (NoSuchMethodException nsme) {
			/* failure, must be older device */
			System.out.println("isScreenOn: NOT supported");

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {

		}

		return true;
	}
}
