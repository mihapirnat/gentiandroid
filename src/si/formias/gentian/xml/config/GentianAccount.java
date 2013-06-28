package si.formias.gentian.xml.config;

import gentian.util.Base64;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;



import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import si.formias.gentian.xml.Node;

public class GentianAccount extends Node {
	public final List<GentianBuddy> buddies=Collections.synchronizedList(new ArrayList<GentianBuddy>());
	public static int KEYLEN=3072;
	static final String SERVER="Server";
	static final String PORT="Port";
	static final String USER="User";
	static final String PASSWORD="Password";
	static final String ALIAS="Alias";
	static final String CRYPTMODULUS="CryptModulus";
	static final String CRYPTPUBLICEXPONENT="CryptPublicExponent";
	static final String CRYPTPRIVATEEXPONENT="CryptPrivateExponent";
	//static final String SIGNMODULUS="SignModulus";
	//static final String SIGNPUBLICEXPONENT="SignPublicExponent";
	//static final String SIGNPRIVATEEXPONENT="SignPrivateExponent";
	static final String SIGNPUBLIC="SignPublicEC";
	static final String SIGNPRIVATE="SignPrivateEC";
	static final String TAIL="Tail";
	public static final String SMS="SMS";
	public GentianAccount(Attributes attributes)
			throws SAXException {
		super("GentianAccount", attributes);
	}
	public GentianAccount() throws SAXException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, InvalidAlgorithmParameterException {
		this(SMS,0);
	}
	public GentianAccount(String server, int port) throws SAXException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, InvalidAlgorithmParameterException {
		super("GentianAccount", null);
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		/*if (server.equals(SMS)) {
			kpg.initialize(1024);	
		} else {*/
		kpg.initialize(KEYLEN);
		//}
		
		setServer(server);
		setPort(port);
		SecureRandom r=new SecureRandom();
		byte[] userBytes=new byte[16];
		r.nextBytes(userBytes);
		byte[] passBytes=new byte[32];
		r.nextBytes(passBytes);
		byte[] aliasBytes=new byte[32];
		r.nextBytes(aliasBytes);
		setUser(Base64.encodeToString(userBytes, false));
		setPassword(Base64.encodeToString(passBytes, false));
		setAlias(Base64.encodeToString(aliasBytes, false));
		KeyPair kp = kpg.genKeyPair();

		KeyFactory fact = KeyFactory.getInstance("RSA");
		RSAPublicKeySpec pub = fact.getKeySpec(kp.getPublic(),
				RSAPublicKeySpec.class);
		RSAPrivateKeySpec priv = fact.getKeySpec(kp.getPrivate(),
				RSAPrivateKeySpec.class);
		

		setCryptModulus(pub.getModulus());
		setCryptPublicExponent(pub.getPublicExponent());
		setCryptPrivateExponent(priv.getPrivateExponent());
		
		KeyPairGenerator g = KeyPairGenerator.getInstance("ECDSA", "SC");

		ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime256v1");
		g.initialize(ecSpec, new SecureRandom());
		KeyPair aKeyPair = g.generateKeyPair();
		setSignPublicEC(aKeyPair.getPublic().getEncoded());
		setSignPrivateEC(aKeyPair.getPrivate().getEncoded());
		/*kp = kpg.genKeyPair();
		fact = KeyFactory.getInstance("RSA");
		pub = fact.getKeySpec(kp.getPublic(),
				RSAPublicKeySpec.class);
		priv = fact.getKeySpec(kp.getPrivate(),
				RSAPrivateKeySpec.class);

		setSignModulus(pub.getModulus());
		setSignPublicExponent(pub.getPublicExponent());
		setSignPrivateExponent(priv.getPrivateExponent());*/
	}
	@Override
	public void add(Node n) {
		super.add(n);
		if (n instanceof GentianBuddy) {
			GentianBuddy buddy=(GentianBuddy)n;
			buddy.account=this;
			buddies.add(buddy);
		}
	}
	
	@Override
	public boolean remove(Node n) {
		if (n instanceof GentianBuddy) {
			GentianBuddy buddy=(GentianBuddy)n;
			buddy.account=null;
			buddies.remove(buddy);
		}
		return super.remove(n);
	}
	private void setServer(String server) {
		setTextOf(SERVER, server);
	}
	private void setPort(int port) {
		setTextOf(PORT, Integer.toString(port));
	}
	private void setUser(String user) {
		setTextOf(USER,user);
	}
	private void setPassword(String password) {
		setTextOf(PASSWORD,password);
	}
	private void setAlias(String alias) {
		setTextOf(ALIAS,alias);
	}
	private void setCryptModulus(BigInteger modulus) {
		setTextOf(CRYPTMODULUS,Base64.encodeToString(modulus.toByteArray(),false));
	}
	private void setCryptPrivateExponent(BigInteger privateExponent) {
		setTextOf(CRYPTPRIVATEEXPONENT,Base64.encodeToString(privateExponent.toByteArray(),false));
	}
	private void setCryptPublicExponent(BigInteger privateExponent) {
		setTextOf(CRYPTPUBLICEXPONENT,Base64.encodeToString(privateExponent.toByteArray(),false));
	}
	private void setSignPrivateEC(byte[] encoded) {
		setTextOf(SIGNPRIVATE,Base64.encodeToString(encoded,false));
	}
	private void setSignPublicEC(byte[] encoded) {
		setTextOf(SIGNPUBLIC,Base64.encodeToString(encoded,false));
	}
	
	public String getServer() {
		return textOf(SERVER);
	}
	public int getPort() {
		try {
			return Integer.parseInt(textOf(PORT));
		} catch (Exception e) {
		
		}
		return 0;
	}
	public String getUser() {
		return textOf(USER);
	}
	public String getPassword() {
		return textOf(PASSWORD);
	}
	public String getAlias() {
		return textOf(ALIAS);
	}
	public BigInteger getCryptModulus() {
		return new BigInteger(Base64.decode(textOf(CRYPTMODULUS)));
	}
	private BigInteger getCryptPrivateExponent() {
		return new BigInteger(Base64.decode(textOf(CRYPTPRIVATEEXPONENT)));
	}
	public BigInteger getCryptPublicExponent() {
		return new BigInteger(Base64.decode(textOf(CRYPTPUBLICEXPONENT)));
	}
	public byte[] getSignPublicEC() {
		return Base64.decode(textOf(SIGNPUBLIC));
	}
	private byte[] getSignPrivateEC() {
		return Base64.decode(textOf(SIGNPRIVATE));
	}
	
	private transient PrivateKey privKeyCrypt,privKeySign;
	public byte[] decryptIncoming(byte[] b) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		if (privKeyCrypt==null) {
			KeyFactory fact = KeyFactory.getInstance("RSA");
			RSAPrivateKeySpec priv = new RSAPrivateKeySpec(getCryptModulus(),getCryptPrivateExponent());
			privKeyCrypt = fact.generatePrivate(priv);
		}
		Cipher cipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
		cipher.init(Cipher.DECRYPT_MODE, privKeyCrypt);
		
		return cipher.doFinal(b);	
	}
	
	public byte[] signOutgoing(byte[] b) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, SignatureException {
		
		if (privKeySign==null) {
             privKeySign=decodePrivate(getSignPrivateEC());
            
		}
		Signature signature = Signature.getInstance("SHA384/ECDSA","SC");
        signature.initSign(privKeySign);
        signature.update(b);
		byte[] bytes= signature.sign();
		System.out.println("Signature length: "+bytes.length);
		return bytes;
		
	}
	
	@Override
	public String toString() {
		if (getServer().equals(SMS)) {
			return SMS;
		} else {
		return getUser()+"@"+getServer()+":"+getPort();
		}
	}

	public String getSignPublic() {
		return textOf(SIGNPUBLIC);
	}
	public String getCryptModulusString() {
		return textOf(CRYPTMODULUS);
	}
	public String getCryptPublicExponentString() {
		return textOf(CRYPTPUBLICEXPONENT);
	}
	public void setTail(long id) {
		set(TAIL,Long.toString(id));
	}
	public long getTail() {
		try {
			return Long.parseLong(get(TAIL));
		} catch (Exception e) {
			return 0;
		}
	}
	Map<String,String> target2user;
	public String getUserByTarget(String target) {
		if (target2user==null) {
			// will cache later, need to invalidate when number changes...
			Map<String,String> target2user=new LinkedHashMap<String,String>();
			for (GentianBuddy buddy : buddies) {
				String t=buddy.getTarget();
				if (target!=null) {
					target2user.put(t, buddy.getUser());
				}
			}
			return target2user.get(target);
		}
		return target2user.get(target);
	}
	
	private static ECPrivateKey decodePrivate(byte[] privEnc) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
		KeyFactory          keyFac = KeyFactory.getInstance("ECDSA", "SC"); 
	        PKCS8EncodedKeySpec privPKCS8 = new PKCS8EncodedKeySpec(privEnc);
	        ECPrivateKey        privKey = (ECPrivateKey)keyFac.generatePrivate(privPKCS8);
	        return privKey;
	}
}
