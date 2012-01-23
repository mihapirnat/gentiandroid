package si.formias.gentian.xml.config;

import gentian.util.Base64;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import si.formias.gentian.Util;
import si.formias.gentian.xml.Node;

public class GentianBuddy extends Node {
	public static final String USER="User";
	static final String NICK="Nick";
	public static final String SIGNMODULUS="SignModulus";
	public static final String SIGNPUBLICEXPONENT="SignPublicExponent";
	public static final String CRYPTMODULUS="CryptModulus";
	public static final String CRYPTPUBLICEXPONENT="CryptPublicExponent";
	static final String WAITING="WaitingMessages";
	public static final String TARGET="Target";
	public GentianBuddy(Attributes attributes) throws SAXException {
		super("GentianBuddy", attributes);
	}
	public GentianBuddy(String user,String nick,String signModulus,String signPublicExponent,String cryptModulus,String cryptPublicExponent) throws SAXException {
		super("GentianBuddy", null);
		setUser(user);
		setNick(nick);
		setSignModulus(new BigInteger(Base64.decode(signModulus)));
		setSignPublicExponent(new BigInteger(Base64.decode(signPublicExponent)));
		setCryptModulus(new BigInteger(Base64.decode(cryptModulus)));
		setCryptPublicExponent(new BigInteger(Base64.decode(cryptPublicExponent)));
	}
	private void setUser(String user) {
		setTextOf(USER,user);
	}
	public void setNick(String nick) {
		setTextOf(NICK,nick);
	}
	private void setSignModulus(BigInteger modulus) {
		setTextOf(SIGNMODULUS,Base64.encodeToString(modulus.toByteArray(),false));
	}

	private void setSignPublicExponent(BigInteger privateExponent) {
		setTextOf(SIGNPUBLICEXPONENT,Base64.encodeToString(privateExponent.toByteArray(),false));
	}
	private void setCryptModulus(BigInteger modulus) {
		setTextOf(CRYPTMODULUS,Base64.encodeToString(modulus.toByteArray(),false));
	}

	private void setCryptPublicExponent(BigInteger privateExponent) {
		setTextOf(CRYPTPUBLICEXPONENT,Base64.encodeToString(privateExponent.toByteArray(),false));
	}
	public String getUser() {
		return textOf(USER);
	}
	public String getNick() {
		return textOf(NICK);
	}
	public String getDisplayName() {
		String s= textOf(NICK);
		if (s==null || s.trim().length()==0) {
			return textOf(USER);
		} else {
			return s;
		}
	}
	public BigInteger getSignModulus() {
		String m =textOf(SIGNMODULUS);
		return new BigInteger(Base64.decode(m));
	}
	public BigInteger getSignPublicExponent() {
		return new BigInteger(Base64.decode(textOf(SIGNPUBLICEXPONENT)));
	}
	public BigInteger getCryptModulus() {
		String m =textOf(CRYPTMODULUS);
		return new BigInteger(Base64.decode(m));
	}
	public BigInteger getCryptPublicExponent() {
		return new BigInteger(Base64.decode(textOf(CRYPTPUBLICEXPONENT)));
	}
	private transient PublicKey pubKeyCrypt,pubKeySign;
	protected GentianAccount account;
	public byte[] encryptOutgoing(byte[] b) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		if (pubKeyCrypt==null) {
			KeyFactory fact = KeyFactory.getInstance("RSA");
			RSAPublicKeySpec pub = new RSAPublicKeySpec(getCryptModulus(),getCryptPublicExponent());
			pubKeyCrypt = fact.generatePublic(pub);
		}
		Cipher cipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, pubKeyCrypt);
		
		return cipher.doFinal(b);	
	}
	
	public boolean verifySignature(byte[] b,byte[] signature) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		md.update(b);
		byte[] mdbytes = md.digest();
		if (pubKeySign==null) {
			KeyFactory fact = KeyFactory.getInstance("RSA");
			RSAPublicKeySpec pub = new RSAPublicKeySpec(getSignModulus(),getSignPublicExponent());
			pubKeySign = fact.generatePublic(pub);
		}
		Cipher cipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
		cipher.init(Cipher.DECRYPT_MODE, pubKeySign);
		
		return Util.equal(mdbytes,cipher.doFinal(signature));
		
	}
	public GentianAccount getAccount() {
		return	account;
	}
	public String getIdString() {
		return getUser()+"@"+(account!=null?account.toString():"");
	}
	public void setWaiting(int messagesWaiting) {
		setTextOf(WAITING,Integer.toString(messagesWaiting));
		
	}
	public int getWaiting() {
		try {
			return Integer.parseInt(textOf(WAITING));
		} catch (Exception e) {
			return 0;
		}
	}
	public String getTarget() {
		return textOf(TARGET);
	}

	
}
