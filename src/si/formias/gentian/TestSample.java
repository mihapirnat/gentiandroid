package si.formias.gentian;

import si.formias.gentian.R;

/*
import gentian.crypt.GentianCrypt;
import gentian.crypt.GentianEnvelope;
import gentian.crypt.GentianKey;
import gentian.crypt.keyprovider.ZipGentianKeyProvider;
import gentian.util.Base64;

import java.io.File;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Random;

import javax.crypto.Cipher;

import si.formias.gentian.GentianChat.MyThread;

import android.graphics.Color;*/

public class TestSample {
/*
private void testEncryption() {
		currentThread = new MyThread() {
			public void run() {

				System.out.println("Gentian test thread run");
				main.log("Testing encryption: AES-256", WHITE);

				try {
					Random r = new Random();
					GentianKey key = new GentianKey(null);

					byte[] raw = key.getRawKey();
					{
						String textSample = quotes[r.nextInt(quotes.length - 1)];
						byte[] text = textSample.getBytes("utf-8");
						byte[] iv = GentianCrypt.createIV();
						byte[] crypt = GentianCrypt
								.encrypt(raw, iv, text, true);
						byte[] clear = GentianCrypt.decrypt(raw, iv, crypt,
								true);
						main.log("AES-256 test: key generated: "
								+ Base64.encodeToString(raw, false)
								+ "\nencryption: " + new String(crypt, "utf-8")

						, GREEN);
						main.log(new String(clear, "utf-8"), BLUE);
					}
					main.log("Testing encryption: RSA-2048", WHITE);
					KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
					kpg.initialize(2048);
					KeyPair kp = kpg.genKeyPair();
					Key publicKey = kp.getPublic();
					Key privateKey = kp.getPrivate();
					KeyFactory fact = KeyFactory.getInstance("RSA");
					RSAPublicKeySpec pub = fact.getKeySpec(kp.getPublic(),
							RSAPublicKeySpec.class);
					RSAPrivateKeySpec priv = fact.getKeySpec(kp.getPrivate(),
							RSAPrivateKeySpec.class);
					RSAPublicKeySpec keySpec = new RSAPublicKeySpec(pub
							.getModulus(), pub.getPublicExponent());

					PublicKey pubKey = fact.generatePublic(keySpec);
					Cipher cipher = Cipher.getInstance("RSA");
					cipher.init(Cipher.ENCRYPT_MODE, pubKey);
					byte[] cipherData = cipher.doFinal(raw);

					RSAPrivateKeySpec keySpec2 = new RSAPrivateKeySpec(priv
							.getModulus(), priv.getPrivateExponent());
					PrivateKey privKey = fact.generatePrivate(keySpec2);
					Cipher cipher2 = Cipher.getInstance("RSA");
					cipher2.init(Cipher.DECRYPT_MODE, privKey);
					byte[] raw2 = cipher2.doFinal(cipherData);
					String rsaresult = "RSA-2048: encrypting test AES key with generated public key:\n"
							+ Base64.encodeToString(cipherData, false)
							+ "\nDecrypted by private key:"
							+ Base64.encodeToString(raw2, false);
					main.log(rsaresult, GREEN);
					String keys = Base64.encodeToString(raw, false);
					String keyt = Base64.encodeToString(raw2, false);
					main.log("Comparing original AES key: " + keys
							+ " to transfered AES key:" + keyt, Color.WHITE);
					if (keys.equals(keyt)) {
						main.log("Transfer key test successful", GREEN);

					} else {
						main.log("Transfer key test failed", RED);
						return;
					}
					main.log("Testing SHA-512", WHITE);
					MessageDigest md = MessageDigest.getInstance("SHA-512");
					md.update(rsaresult.getBytes("utf-8"));
					byte[] mdbytes = md.digest();
					main.log("SHA-512 digest:"
							+ Base64.encodeToString(mdbytes, false), GREEN);

					File tempZip = File.createTempFile("test", "genkey");
					main.log("Testing Gentian envelopes...", WHITE);
					ZipGentianKeyProvider provider = new ZipGentianKeyProvider(
							tempZip, 1);

					byte[] sourceBytes = quotes[r.nextInt(quotes.length - 1)]
							.getBytes("utf-8");
					main.log("Source text length: " + sourceBytes.length
							+ " bytes", GREEN);
					int envelopesn = 6;
					byte[] wrapped = GentianEnvelope.wrap(provider,
							sourceBytes, envelopesn, 0, 0);

					String wrapBase64 = Base64.encodeToString(wrapped, false);
					main.log("Crypt text length: " + wrapped.length
							+ " bytes, %16=" + (wrapped.length % 16) + ", "
							+ envelopesn + " envelopes", GREEN);
					main.log("Final package: " + wrapBase64.length() + " "
							+ wrapBase64, GREEN);

					provider = new ZipGentianKeyProvider(tempZip);
					byte[] clearText = GentianEnvelope.unwrap(provider,
							wrapped, envelopesn, 0);

					tempZip.delete();
					main.log(new String(clearText, "utf-8"), BLUE);
					main.log("Encryption test successful!", WHITE);
					main.inited = true;

				} catch (Exception e) {
					main.log("Test key generation and encryption failed."
							+ e.getMessage(), RED);
					e.printStackTrace();
				}
				main.currentThread = null;
			}
		};
		currentThread.main = this;
		currentThread.setDaemon(true);
		currentThread.start();
	}

 */

}
