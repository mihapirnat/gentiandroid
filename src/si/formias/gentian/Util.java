package si.formias.gentian;

import static si.formias.gentian.Util.wipe;
import gentian.util.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import si.formias.gentian.R;

public class Util {
	public static String readStream(InputStream in) throws UnsupportedEncodingException, IOException {
		return readStream(in,"utf-8");
		
	}
	   public static String readStream(InputStream in, String encoding) throws UnsupportedEncodingException, IOException {
	        BufferedReader r = new BufferedReader(new InputStreamReader(in, encoding),4096);
	        String line = null;
	        StringBuilder sb = new StringBuilder();
	        while ((line = r.readLine()) != null) {
	            sb.append(line);
	            sb.append("\n");
	        }
	        r.close();
	        return sb.toString().trim();
	    }

	   public static void writeStream(OutputStream o,String text) throws UnsupportedEncodingException {
		   PrintWriter w = new PrintWriter(new OutputStreamWriter(o,"utf-8"));
		   w.print(text);
		   w.close();
	   }
	   public static byte[][] splitBytes(byte[] source,int len) {
		   
		   if (source.length%len != 0) throw new RuntimeException("length: "+source.length+" split: "+len+" l%s="+source.length%len);
		   int n=source.length/len;
		   byte[][] ret = new byte[n][len];
		   for (int i=0; i<n; i++) {
			   for (int j=0; j<len;j++) {
				   ret[i][j]=source[i*len+j];
			   }
		   }
		   return ret;
	   }
	   public static byte[] encryptPassword(byte[] salt,String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		   // http://www.jasypt.org/howtoencryptuserpasswords.html
		   byte[] bytes=password.getBytes("utf-8");
		   return encryptPassword(salt, bytes);
	   }
	   public static byte[] encryptPassword(byte[] salt,byte[] bytes) throws NoSuchAlgorithmException, UnsupportedEncodingException {   
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			byte[] saltFixed="Remember remember".getBytes("utf-8");
			
			for (int i=0;i<1024;i++) {
				md.reset();
				md.update(saltFixed);
				md.update(salt);
				md.update(bytes);
				bytes = md.digest();
				//System.out.println("Hash round "+i+" "+Base64.encodeToString(bytes, false));
			}
			return bytes;
	   }
	   public static boolean verifyPassword(byte[] salt,String password,byte[] encryptedPassword) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		   byte[] input = encryptPassword(salt, password);
		   return equal(input, encryptedPassword);
		   
	   }
	   public static boolean equal(byte[] b,byte[] b1) {
		   if (b==null || b1==null) return false;
		  //System.out.println("Checking equals:\n "+Base64.encodeToString(b, false)+"\n and\n "+Base64.encodeToString(b1, false));
		   
		   if (b.length!=b1.length) { 
			   return false;
		   }
		   for (int i=0;i<b.length;i++) {
			   if (b[i]!=b1[i]) return false;
		   }
		   return true;
	   }
	public static byte[] readStreamBytes(InputStream in) throws IOException {
		byte[] buffer=new byte[4096];
		int read;
		ByteArrayOutputStream bout =new ByteArrayOutputStream();
		while ((read=in.read(buffer))!=-1) {
			bout.write(buffer,0,read);
		}
		return bout.toByteArray();
	}
	

	public static void wipe(final byte[] data) {
		if (data != null) {
			new Thread() {
				public void run() {
					byte[] wipe = new byte[data.length];
					SecureRandom r = new SecureRandom();
					for (int n = 0; n < 10; n++) {
						r.nextBytes(wipe);
						for (int i = 0; i < data.length; i++) {
							data[i] = wipe[i];
						}
					}
					
				}
			}.start();
		}
	}

	public static void wipe(final byte[][] dataArray) {
		if (dataArray != null) {
			new Thread() {
				public void run() {
					for (byte[] data : dataArray) {
						byte[] wipe = new byte[data.length];
						SecureRandom r = new SecureRandom();
						for (int n = 0; n < 10; n++) {
							r.nextBytes(wipe);
							for (int i = 0; i < data.length; i++) {
								data[i] = wipe[i];
							}
						}
					}
					
				}
			}.start();
		}
	}
	public static byte[] copy(byte[] salt) {
		byte[] copy =new byte[salt.length];
		for (int i=0;i<copy.length;i++) {
			copy[i]=salt[i];
		}
		return copy;
	}
	static byte[][] cut(byte[] source, int endOfFirstOffset) {
		byte[][] result = new byte[2][];
		byte[] first = new byte[endOfFirstOffset];
		int sourcelen = source.length;
		int secondlen = sourcelen - endOfFirstOffset;
		byte[] second = new byte[secondlen];
		for (int i = 0; i < endOfFirstOffset; i++) {
			first[i] = source[i];
		}
		for (int i = 0; i < secondlen; i++) {
			second[i] = source[i + endOfFirstOffset];
		}
	
		result[0] = first;
		result[1] = second;
		return result;
	}
}
