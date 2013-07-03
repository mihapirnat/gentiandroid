package si.formias.gentian;

public class GCM {
	public static final String GCM_REGISTER = "1006386613129";
	static final String BASE = "http://seniorita.freenode.si/gcm/";
	static final String TORBASE = "http://wjh4ivb7eqydqdvl.onion/gcm/";

	static final boolean debug = true;

	public static String getBase() {
		return BASE;
	}

	public static String getTorBase() {
		return TORBASE;
	}

	public static boolean isDebug() {
		return debug;
	}
}
