package si.formias.gentian.tab.msgcrypt;

import gentian.crypt.GentianCrypt;
import gentian.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;

import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import si.formias.gentian.Base91;
import si.formias.gentian.R;
import si.formias.gentian.Util;
import si.formias.gentian.tab.Messages;
import si.formias.gentian.xml.config.GentianAccount;
import si.formias.gentian.xml.config.GentianBuddy;

import static si.formias.gentian.Util.wipe;

public class CryptShort {

	static final int EXTRA_START = 25;
	static final int BLOCK_LEN = 384;
	public static final String TMPKEYENCRYPT = "TMPKEYENCRYPT";
	public static final String TMPKEYDECRYPT = "TMPKEYDECRYPT";
	public static final String TMPHMACENCRYPT = "TMPHMACENCRYPT";
	public static final String TMPHMACDECRYPT = "TMPHMACDECRYPT";
	public static final String TMPECDHPUB1 = "TMPECPUB1";
	public static final String TMPECDHPUB2 = "TMPECPUB2";
	public static final String TMPECDHPRIV1 = "TMPECPRIV1";
	public static final String TMPECDHPRIV2 = "TMPECPRIV2";

	static final int KEYSLEN = 64;
	static final String COUNTENCRYPT = "COUNTENCRYPT";
	static final String COUNTDECRYPT = "COUNTDECRYPT";
	static final String TIMESTAMPDECRYPT = "TIMESTAMPDECRYPT";
	static final String CODE_KEEP_KEY = "K";
	static final String CODE_NEW_KEY = "N";
	static final String CODE_SESSION = "S";

	static final int COUNTLIMIT = 100;
	static boolean debug = false;
	static final int[] ALGORITHM = { GentianCrypt.Serpent, GentianCrypt.AES,
			GentianCrypt.Twofish };

	static byte[] join(byte[] one, byte[] two) {
		return join(one, two, true, true);
	}

	static byte[] join(byte[] one, byte[] two, boolean wipeFirst,
			boolean wipeSecond) {
		int lone = one.length;
		int ltwo = two.length;
		byte[] result = new byte[lone + ltwo];
		for (int i = 0; i < lone; i++) {
			result[i] = one[i];
		}
		for (int i = 0; i < ltwo; i++) {
			result[i + lone] = two[i];
		}
		if (wipeFirst) {
			 wipe(one);
		}
		if (wipeSecond) {
			 wipe(two);
		}
		return result;
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
		wipe(source);
		result[0] = first;
		result[1] = second;
		return result;
	}

	static byte[] createTimestamp() throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(bout);
		dout.writeLong(System.currentTimeMillis());
		dout.flush();
		return bout.toByteArray();
	}

	static long getTimestamp(byte[] b) throws IOException {
		ByteArrayInputStream bin = new ByteArrayInputStream(b);
		DataInputStream din = new DataInputStream(bin);
		long time = din.readLong();
		return time;
	}

	public static String decrypt(GentianAccount account, GentianBuddy buddy,
			String text) {

		// buddy.unset(TMPKEYDECRYPT); // TO DEBUG RSA ENCRYPTION
		int len = text.length();

		String code = text.substring(len - 1);
		text = text.substring(0, len - 1);
		if (debug)
			System.out.println("TMP DECRYPT:" + buddy.get(TMPKEYDECRYPT)
					+ " Count decrypt:" + buddy.get(COUNTDECRYPT));

		try {

			byte[] b = Base91.decode(text.getBytes("latin1"));
			if (b==null) {
				// not in right format, ignore
				return null;
			}
			if (code.equals(CODE_SESSION)) {
				if (buddy != null) {
					if (buddy.get(TMPKEYDECRYPT) != null
							&& buddy.get(TMPHMACDECRYPT) != null
							&& buddy.get(COUNTDECRYPT) != null
							&& Integer.parseInt(buddy.get(COUNTDECRYPT)) < (3 * COUNTLIMIT)) {

						int count = Integer.parseInt(buddy.get(COUNTDECRYPT));

						if (debug)
							System.out.println("Using existing key.");
						buddy.set(COUNTDECRYPT, Integer.toString(count + 1));
						byte[] iv = null;
						byte[][] keys = null;
						byte[] hmac = Base64.decode(buddy.get(TMPHMACDECRYPT));
						byte[] keybytes = Base64.decode(buddy
								.get(TMPKEYDECRYPT));
						byte[] msg = null;
						try {
							if (buddy.get(TMPECDHPRIV1) != null
									&& buddy.get(TMPECDHPRIV2) != null
									&& buddy.get(TMPECDHPUB1) != null
									&& buddy.get(TMPECDHPUB2) != null) {
								KeyAgreement aKeyAgree = KeyAgreement
										.getInstance("ECDH", "SC");

								aKeyAgree.init(decodePrivate(Base64
										.decode(buddy.get(TMPECDHPRIV1))));
								aKeyAgree.doPhase(decodePublic(Base64
										.decode(buddy.get(TMPECDHPUB1))), true);

								byte[] key1 = deriveKeyFromSecret(aKeyAgree
										.generateSecret());
								KeyAgreement bKeyAgree = KeyAgreement
										.getInstance("ECDH", "SC");

								bKeyAgree.init(decodePrivate(Base64
										.decode(buddy.get(TMPECDHPRIV2))));
								bKeyAgree.doPhase(decodePublic(Base64
										.decode(buddy.get(TMPECDHPUB2))), true);

								byte[] key2 = deriveKeyFromSecret(bKeyAgree
										.generateSecret());
								keybytes = join(keybytes, key1, true, false);
								keybytes = join(keybytes, key2, true, false);
								if (buddy.get(TMPKEYENCRYPT) != null) {
									buddy.set(TMPKEYDECRYPT, Base64
											.encodeToString(keybytes, false));
									byte[] keybytes2 = Base64.decode(buddy
											.get(TMPKEYENCRYPT));
									keybytes2 = join(keybytes2, key1, true,
											false);
									keybytes2 = join(keybytes2, key2, true,
											false);
									buddy.set(TMPKEYENCRYPT, Base64
											.encodeToString(keybytes2, false));
									buddy.unset(TMPECDHPRIV1);
									buddy.unset(TMPECDHPRIV2);
									buddy.unset(TMPECDHPUB1);
									buddy.unset(TMPECDHPUB2);
									wipe(keybytes2);
									if (debug)
										System.out
												.println("added ECDH encrypt keys");
								} else {
									if (debug)
										System.out
												.println("using but not added ECDH encrypt keys");
								}
								wipe(key1);
								wipe(key2);

							}

							keys = Util.splitBytes(keybytes, 32);

							byte[][] cut = cut(b, 16);
							iv = cut[0];
							b = cut[1];
							if (debug) {
								System.out.println("session using "
										+ keys.length + " keys...");
							}
							cut = Util.splitBytes(b, b.length / 2);
							byte[] pad = cut[0];
							b = cut[1];
							for (int i = keys.length - 1; i >= 1; i -= 2) {
								if (debug) {
									System.out.println("Pad Using key: "
											+ Base64.encodeToString(keys[i],
													false));
								}

								pad = GentianCrypt
										.decrypt(keys[i], iv, pad, i == 1,
												ALGORITHM[i % ALGORITHM.length]);
								if (debug) {
									System.out.println("Result: "
											+ Base64.encodeToString(pad, false)
											+ " " + new String(pad, "utf-8"));
								}
							}

							for (int i = keys.length - 2; i >= 0; i -= 2) {
								if (debug) {
									System.out.println("Text Using key: "
											+ Base64.encodeToString(keys[i],
													false));
								}

								b = GentianCrypt
										.decrypt(keys[i], iv, b, i == 0,
												ALGORITHM[i % ALGORITHM.length]);
								if (debug) {
									System.out.println("Result: "
											+ Base64.encodeToString(b, false)
											+ " " + new String(b, "utf-8"));
								}
							}
							b = xor(pad, b);
							msg = b;

							cut = cut(msg, 20);
							byte[] digest = cut[0];
							msg = cut[1];

							Mac mac = Mac.getInstance("HmacSHA1");
							SecretKeySpec secret = new SecretKeySpec(hmac,
									mac.getAlgorithm());
							mac.init(secret);
							byte[] digesttest = mac.doFinal(msg);

							if (Util.equal(digest, digesttest)) {
								cut = cut(msg, 8);
								long timestamp = getTimestamp(cut[0]);
								msg = cut[1];
								if (buddy != null) {
									long lasttimestamp = 0;

									String lasttimestring = buddy
											.get(TIMESTAMPDECRYPT);
									if (lasttimestring != null) {
										try {
											lasttimestamp = Long
													.parseLong(lasttimestring);
											if (debug)
												System.out
														.println("Received timestamp: "
																+ timestamp
																+ " last timestamp: "
																+ lasttimestamp);
											if (lasttimestamp >= timestamp) {
												if (debug)
													System.out
															.println("wrong timestamp, last:"+lasttimestamp+" current:"+timestamp);

												wipe(iv);
												wipe(b);
												wipe(keys);
												wipe(hmac);
												wipe(msg);

												return Messages.staticmain
														.getText(
																R.string.wrong_timestamp_smaller_or_equal_replay)
														.toString();
											}
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}

								Calendar calendar = Calendar.getInstance();
								calendar.setTimeInMillis(timestamp);

								buddy.set(TIMESTAMPDECRYPT,
										Long.toString(timestamp));
								String s = new String(msg, "utf-8")
										+ " ["
										+ String.format(
												"%1$tY/%1$tm/%1$td %1$tH:%1$tM",
												calendar) + "]";

								/*
								 * if (code.equals(CODE_NEW_KEY)) {
								 * 
								 * buddy.unset(TMPKEYENCRYPT); }
								 */

								wipe(iv);
								wipe(b);
								wipe(keys);
								wipe(hmac);
								wipe(msg);

								return s;
							} else {
								if (debug)
									System.out.println("Digest failed:"
											+ new String(msg, "utf-8"));
								buddy.unset(CryptShort.TMPKEYENCRYPT);
								buddy.unset(CryptShort.TMPKEYDECRYPT);
								return Messages.staticmain.getText(
										R.string.cant_decode_received_sms)
										.toString();
							}

						} catch (Exception e) {
							if (debug) {
								System.out.println("error using existing key.");
								e.printStackTrace();
							}

						} finally {
							wipe(iv);
							wipe(b);
							wipe(keys);
							wipe(hmac);
							wipe(msg);

						}
						// buddy.unset(TMPKEYDECRYPT);

					}
				}
			} else {
				byte[] iv = new byte[16];
				try {
					if (debug)
						System.out.println("base64 text:" + text);
					// byte[] bytes = Base64.decode(text);

					byte[] bytes = b;
					byte[] extrabytes = null;
					if (debug)
						System.out.println("bytes length:" + bytes.length);
					if (bytes.length > BLOCK_LEN) {

						byte[][] cut = cut(bytes, BLOCK_LEN);
						bytes = cut[0];
						extrabytes = cut[1];
						if (debug)
							System.out.println("b length:" + bytes.length
									+ " extra bytes length:"
									+ extrabytes.length);
					}
					bytes = account.decryptIncoming(bytes);

					byte[][] cut = cut(bytes, KEYSLEN);
					byte[] keybytes = cut[0];
					b = cut[1];

					byte[][] keys = Util.splitBytes(keybytes, 32);
					cut = cut(b, 32);
					byte[] hmac = cut[0];
					b = cut[1];
					cut = cut(b, 91 * 2);
					byte[][] ecpublicbytes = Util.splitBytes(cut[0], 91);
					buddy.set(TMPECDHPUB1,
							Base64.encodeToString(ecpublicbytes[0], false));
					buddy.set(TMPECDHPUB2,
							Base64.encodeToString(ecpublicbytes[1], false));
					if (debug) {
						System.out.println("Got public ECDH:\n"
								+ buddy.get(TMPECDHPUB1) + "\n"
								+ buddy.get(TMPECDHPUB2));
					}
					b = cut[1];

					byte[] iv2 = null;
					if (debug) {
						System.out.println("using " + keys.length + " keys...");
					}

					for (int i = keys.length - 1; i >= 0; i--) {
						b = GentianCrypt.decrypt(keys[i], iv, b, i == 0,
								ALGORITHM[i % ALGORITHM.length]);

					}

					
					if (extrabytes != null) {
						cut = cut(extrabytes, 16);
						iv2 = cut[0];
						extrabytes = cut[1];

						for (int i = keys.length - 1; i >= 0; i--) {
							if (debug) {
								System.out
										.println("Extra using key: "
												+ Base64.encodeToString(
														keys[i], false));
							}

							extrabytes = GentianCrypt.decrypt(keys[i], iv2,
									extrabytes, i == 0, ALGORITHM[i
											% ALGORITHM.length]);
							if (debug) {
								System.out.println("Result: "
										+ Base64.encodeToString(b, false));
							}

						}
						b = join(b, extrabytes);
					}
					byte[] msg = b;

					// cut = cut(msg,32);
					cut=cut(msg,1);
					byte SIGNATURE_LEN=cut[0][0];
					msg=cut[1];
					cut = cut(msg, SIGNATURE_LEN);
					// byte[] digest=cut[0];
					byte[] signature = cut[0];
					msg = cut[1];

					byte[] testbytes = null;
					if (buddy != null) {
						testbytes = join(
								ecpublicbytes[0],
								join(ecpublicbytes[1],
										join(hmac,
												join(keybytes, msg, false,
														false), false, true),
										false, true), false, true);
					}
					if (buddy == null
							|| buddy.verifySignature(testbytes, signature)) {
						wipe(testbytes);
						// extract timestamp
						cut = cut(msg, 8);
						long timestamp = getTimestamp(cut[0]);
						msg = cut[1];
						if (buddy != null) {
							long lasttimestamp = 0;

							String lasttimestring = buddy.get(TIMESTAMPDECRYPT);
							if (lasttimestring != null) {
								try {
									lasttimestamp = Long
											.parseLong(lasttimestring);
									if (debug)
										System.out
												.println("Received timestamp: "
														+ timestamp
														+ " last timestamp: "
														+ lasttimestamp);
									if (lasttimestamp >= timestamp) {
										if (debug)
											System.out
													.println("wrong timestamp");
										wipe(msg);
										wipe(keybytes);
										wipe(keys);
										wipe(hmac);

										return Messages.staticmain
												.getText(
														R.string.wrong_timestamp_smaller_or_equal_replay)
												.toString();
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}

						Calendar calendar = Calendar.getInstance();
						calendar.setTimeInMillis(timestamp);

						String s = new String(msg, "utf-8")
								+ " ["
								+ String.format(
										"%1$tY/%1$tm/%1$td %1$tH:%1$tM",
										calendar) + "]";
						if (buddy != null) {
							buddy.set(TMPKEYDECRYPT,
									Base64.encodeToString(keybytes, false));
							buddy.set(TMPHMACDECRYPT,
									Base64.encodeToString(hmac, false));
							buddy.set(TIMESTAMPDECRYPT,
									Long.toString(timestamp));
							buddy.set(COUNTDECRYPT, Integer.toString(1));
							if (code.equals(CODE_NEW_KEY)) {

								buddy.unset(TMPKEYENCRYPT);
							}
							if (buddy.get(TMPECDHPRIV1) == null
									|| buddy.get(TMPECDHPRIV2) == null) {
								buddy.unset(TMPKEYENCRYPT);
							}

						}
						wipe(msg);
						wipe(keybytes);
						wipe(keys);
						wipe(hmac);

						return s;
					} else {
						cut = cut(msg, 8);
						msg = cut[1];
						long timestamp = getTimestamp(cut[0]);
						Calendar calendar = Calendar.getInstance();
						calendar.setTimeInMillis(timestamp);

						String s = new String(msg, "utf-8")
								+ " ["
								+ String.format(
										"%1$tY/%1$tm/%1$td %1$tH:%1$tM",
										calendar) + "]";
						return Messages.staticmain.getText(
								R.string.wrong_signature).toString()
								+ ": " + s;
					}

				} catch (Exception e) {
					if (debug) {
						e.printStackTrace();
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		buddy.unset(TMPKEYENCRYPT);
		buddy.unset(TMPKEYDECRYPT);
		buddy.unset(TMPECDHPRIV1);
		buddy.unset(TMPECDHPRIV2);
		buddy.unset(TMPECDHPUB1);
		buddy.unset(TMPECDHPUB2);
		return null;
	}

	public static String encrypt(GentianBuddy buddy, String text) {
		// testECDH();
		// buddy.unset(TMPKEYENCRYPT); // TO DEBUG RSA ENCRYPTION

		if (buddy.get(TMPKEYENCRYPT) != null && buddy.get(COUNTENCRYPT) != null
				&& buddy.get(TMPHMACENCRYPT) != null
				&& Integer.parseInt(buddy.get(COUNTENCRYPT)) < COUNTLIMIT) {

			try {
				int count = Integer.parseInt(buddy.get(COUNTENCRYPT));
				buddy.set(COUNTENCRYPT, Integer.toString(count + 1));

				byte[] keybytes = Base64.decode(buddy.get(TMPKEYENCRYPT));
				if (buddy.get(TMPECDHPRIV1) != null
						&& buddy.get(TMPECDHPRIV2) != null
						&& buddy.get(TMPECDHPUB1) != null
						&& buddy.get(TMPECDHPUB2) != null) {
					KeyAgreement aKeyAgree = KeyAgreement.getInstance("ECDH",
							"SC");

					aKeyAgree.init(decodePrivate(Base64.decode(buddy
							.get(TMPECDHPRIV1))));
					aKeyAgree
							.doPhase(decodePublic(Base64.decode(buddy
									.get(TMPECDHPUB1))), true);

					byte[] key1 = deriveKeyFromSecret(aKeyAgree
							.generateSecret());
					KeyAgreement bKeyAgree = KeyAgreement.getInstance("ECDH",
							"SC");

					bKeyAgree.init(decodePrivate(Base64.decode(buddy
							.get(TMPECDHPRIV2))));
					bKeyAgree
							.doPhase(decodePublic(Base64.decode(buddy
									.get(TMPECDHPUB2))), true);

					byte[] key2 = deriveKeyFromSecret(bKeyAgree
							.generateSecret());

					keybytes = join(keybytes, key1, true, false);

					keybytes = join(keybytes, key2, true, false);

					if (buddy.get(TMPKEYDECRYPT) != null) {
						buddy.set(TMPKEYENCRYPT,
								Base64.encodeToString(keybytes, false));
						byte[] keybytes2 = Base64.decode(buddy
								.get(TMPKEYDECRYPT));
						keybytes2 = join(keybytes2, key1, true, false);
						keybytes2 = join(keybytes2, key2, true, false);
						buddy.set(TMPKEYDECRYPT,
								Base64.encodeToString(keybytes2, false));
						buddy.unset(TMPECDHPRIV1);
						buddy.unset(TMPECDHPRIV2);
						buddy.unset(TMPECDHPUB1);
						buddy.unset(TMPECDHPUB2);
						wipe(keybytes2);
						if (debug)
							System.out.println("added ECDH encrypt keys");
					} else {
						if (debug)
							System.out
									.println("using but not added ECDH encrypt keys");
					}
					wipe(key1);
					wipe(key2);

				}
				byte[][] keys = Util.splitBytes(keybytes, 32);
				byte[] b = text.getBytes("utf-8");

				b = join(createTimestamp(), b);

				Mac mac = Mac.getInstance("HmacSHA1");
				SecretKeySpec secret = new SecretKeySpec(Base64.decode(buddy
						.get(TMPHMACENCRYPT)), mac.getAlgorithm());
				mac.init(secret);
				byte[] digest = mac.doFinal(b);

				b = join(digest, b);

				SecureRandom r = new SecureRandom();
				byte[] iv = new byte[16];
				r.nextBytes(iv);
				if (debug) {
					System.out.println("session using " + keys.length
							+ " keys...");
				}

				byte[] pad = new byte[b.length];
				r.nextBytes(pad);
				byte[] padcrypted = Util.copy(pad);
				for (int i = 1; i < keys.length; i += 2) {
					if (debug) {
						System.out.println("Pad Using key: "
								+ Base64.encodeToString(keys[i], false));
					}
					padcrypted = GentianCrypt.encrypt(keys[i], iv, padcrypted,
							i == 1, ALGORITHM[i % ALGORITHM.length]);
					if (debug) {
						System.out.println("Result: "
								+ Base64.encodeToString(padcrypted, false));
					}

				}
				if (debug)
					System.out.println("xoring pad:"
							+ Base64.encodeToString(pad, false) + " text:"
							+ Base64.encodeToString(b, false));
				b = xor(pad, b);
				if (debug)
					System.out.println("xor result"
							+ Base64.encodeToString(b, false));
				for (int i = 0; i < keys.length; i += 2) {
					if (debug) {
						System.out.println("Text Using key: "
								+ Base64.encodeToString(keys[i], false));
					}
					b = GentianCrypt.encrypt(keys[i], iv, b, i == 0,
							ALGORITHM[i % ALGORITHM.length]);
					if (debug) {
						System.out.println("Result: "
								+ Base64.encodeToString(b, false));
					}

				}

				b = join(iv, join(padcrypted, b));
				String s = new String(Base91.encode(b), "latin1");

				buddy.set(COUNTENCRYPT, Integer.toString(Integer.parseInt(buddy
						.get(COUNTENCRYPT)) + 1));

				return s + CODE_SESSION;
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		if (buddy.get(COUNTENCRYPT) != null
				&& Integer.parseInt(buddy.get(COUNTENCRYPT)) >= COUNTLIMIT) {
			if (debug)
				System.out.println("sms count limit reached");
		}

		SecureRandom r = new SecureRandom();
		byte[] keybytes = new byte[KEYSLEN];
		r.nextBytes(keybytes);
		byte[] iv = new byte[16];
		byte[][] keys = Util.splitBytes(keybytes, 32);
		byte[] hmac = new byte[32];
		r.nextBytes(hmac);
		try {
			KeyPairGenerator g = KeyPairGenerator.getInstance("ECDH", "SC");

			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime256v1");

			g.initialize(ecSpec, new SecureRandom());
			KeyPair aKeyPair = g.generateKeyPair();

			KeyPair bKeyPair = g.generateKeyPair();
			buddy.set(TMPECDHPRIV2, Base64.encodeToString(bKeyPair.getPrivate()
					.getEncoded(), false));

			byte[] b = text.getBytes("utf-8");
			b = join(createTimestamp(), b);
			/*
			 * MessageDigest md = MessageDigest.getInstance("SHA-256");
			 * md.update(b); byte[] digest = md.digest();
			 */
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			byte[] signbytes = join(
					aKeyPair.getPublic().getEncoded(),
					join(bKeyPair.getPublic().getEncoded(),
							join(hmac, join(keybytes, b, false, false), false,
									true), false, true), false, true);
			signbytes=buddy.getAccount().signOutgoing(signbytes);
			bout.write((byte)signbytes.length);
			bout.write(signbytes);
			wipe(signbytes);
			// bout.write(digest);
			bout.write(b);
			b = bout.toByteArray();

			byte[] extrabytes = null;
			if (b.length > EXTRA_START) {
				byte[][] cut = cut(b, EXTRA_START);
				b = cut[0];
				extrabytes = cut[1];
			}
			for (int i = 0; i < keys.length; i++) {
				b = GentianCrypt.encrypt(keys[i], iv, b, i == 0, ALGORITHM[i
						% ALGORITHM.length]);

			}

			byte[] iv2 = null;
			if (extrabytes != null) {
				iv2 = new byte[16];
				r.nextBytes(iv2);
				for (int i = 0; i < keys.length; i++) {
					if (debug) {
						System.out.println("Extra using key: "
								+ Base64.encodeToString(keys[i], false));
					}
					extrabytes = GentianCrypt.encrypt(keys[i], iv2, extrabytes,
							i == 0, ALGORITHM[i % ALGORITHM.length]);
					if (debug) {
						System.out.println("Result: "
								+ Base64.encodeToString(extrabytes, false));
					}

				}

				extrabytes = join(iv2, extrabytes);

			}

			bout = new ByteArrayOutputStream();
			bout.write(keybytes);
			bout.write(hmac);
			buddy.set(TMPECDHPRIV1, Base64.encodeToString(aKeyPair.getPrivate()
					.getEncoded(), false));
			if (debug) {
				System.out.println("Sending public EC 1:"
						+ Base64.encodeToString(aKeyPair.getPublic()
								.getEncoded(), false));
			}
			bout.write(aKeyPair.getPublic().getEncoded());
			buddy.set(TMPECDHPRIV2, Base64.encodeToString(bKeyPair.getPrivate()
					.getEncoded(), false));
			if (debug) {
				System.out.println("Sending public EC 2:"
						+ Base64.encodeToString(bKeyPair.getPublic()
								.getEncoded(), false));
			}
			bout.write(bKeyPair.getPublic().getEncoded());
			bout.write(b);

			wipe(b);
			ByteArrayOutputStream bout2 = new ByteArrayOutputStream();
			byte[] bytesout = bout.toByteArray();
			bout2.write(buddy.encryptOutgoing(bytesout));
			wipe(bytesout);
			if (extrabytes != null) {
				if (debug)
					System.out.println("************ EXTRA BYTES: "
							+ extrabytes.length);
				bout2.write(extrabytes);
				wipe(extrabytes);
			}

			byte[] out = bout2.toByteArray();
			if (debug)
				System.out.println("out bytes length:" + out.length);
			String s = new String(Base91.encode(out), "latin1");
			wipe(out);
			buddy.set(TMPKEYENCRYPT, Base64.encodeToString(keybytes, false));
			buddy.set(TMPHMACENCRYPT, Base64.encodeToString(hmac, false));
			buddy.set(COUNTENCRYPT, Integer.toString(1));
			wipe(keybytes);
			wipe(keys);
			wipe(hmac);

			return s
					+ (buddy.get(TMPKEYDECRYPT) != null ? CODE_KEEP_KEY
							: CODE_NEW_KEY);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static void testECDH() {
		try {
			KeyPairGenerator g = KeyPairGenerator.getInstance("ECDH", "SC");

			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime256v1");

			g.initialize(ecSpec, new SecureRandom());

			//
			// a side
			//
			KeyPair aKeyPair = g.generateKeyPair();

			KeyAgreement aKeyAgree = KeyAgreement.getInstance("ECDH", "SC");

			aKeyAgree.init(aKeyPair.getPrivate());
			System.out.println("a public encoded length:"
					+ aKeyPair.getPublic().getEncoded().length);
			System.out.println("a private encoded length:"
					+ aKeyPair.getPrivate().getEncoded().length);
			//
			// b side
			//
			KeyPair bKeyPair = g.generateKeyPair();

			KeyAgreement bKeyAgree = KeyAgreement.getInstance("ECDH", "SC");

			bKeyAgree.init(bKeyPair.getPrivate());
			System.out.println("b public encoded length:"
					+ bKeyPair.getPublic().getEncoded().length);
			System.out.println("b private encoded length:"
					+ bKeyPair.getPrivate().getEncoded().length);
			//
			// agreement
			//
			aKeyAgree.doPhase(bKeyPair.getPublic(), true);
			bKeyAgree.doPhase(aKeyPair.getPublic(), true);

			byte[] aSecret = aKeyAgree.generateSecret();
			BigInteger k1 = new BigInteger(aSecret);
			BigInteger k2 = new BigInteger(bKeyAgree.generateSecret());

			if (!k1.equals(k2)) {
				System.out.println("ECDH 2-way test failed");
			} else {
				System.out.println("OK ;) ECDH 2-way test passed: "
						+ aSecret.length
						+ " derived key: "
						+ Base64.encodeToString(deriveKeyFromSecret(aSecret),
								false));
			}
			try {
				System.out.println("testing aKeyPair encoding");
				testECpublicEncoding(aKeyPair.getPublic());
				testECprivateEncoding(aKeyPair.getPrivate());
			} catch (Exception e) {
				System.out.println("testing aKeyPair encoding failed:" + e);
			}
			try {
				System.out.println("testing bKeyPair encoding");
				testECpublicEncoding(bKeyPair.getPublic());
				testECprivateEncoding(bKeyPair.getPrivate());
			} catch (Exception e) {
				System.out.println("testing bKeyPair encoding failed:" + e);
			}

		} catch (Exception e) {
			System.out.println("ECDH 2-way test failed - exception: " + e);
		}

	}

	public static boolean forwardSecrecy(GentianBuddy buddy) {
		if (buddy.get(TMPKEYENCRYPT) != null
				&& Base64.decode(buddy.get(TMPKEYENCRYPT)).length > KEYSLEN) {
			return (buddy.get(COUNTENCRYPT) != null && Integer.parseInt(buddy
					.get(COUNTENCRYPT)) < COUNTLIMIT);

		}
		return buddy.get(TMPECDHPRIV1) != null
				&& buddy.get(TMPECDHPRIV2) != null
				&& buddy.get(TMPECDHPUB1) != null
				&& buddy.get(TMPECDHPUB2) != null;
	}

	static byte[] deriveKeyFromSecret(byte[] secret)
			throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		md.update(secret);
		byte[] digest = md.digest();
		byte[][] split = cut(digest, 32);
		byte[] result = new byte[32];
		for (int i = 0; i < 32; i++) {
			result[i] = (byte) (split[0][i] ^ split[1][i]);
		}
		return result;
	}

	static byte[] xor(byte[] b, byte[] b2) {
		byte[] ret = new byte[b.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = (byte) (b[i] ^ b2[i]);
		}
		return ret;
	}

	private static void testECpublicEncoding(PublicKey publicKey)
			throws NoSuchAlgorithmException, NoSuchProviderException,
			InvalidKeySpecException {
		byte[] pubEnc = publicKey.getEncoded();
		ECPublicKey pubKey = decodePublic(pubEnc);

		if (!pubKey.getW().equals(((ECPublicKey) publicKey).getW())) {
			System.out.println(" expected " + pubKey.getW().getAffineX()
					+ " got " + ((ECPublicKey) publicKey).getW().getAffineX());
			System.out.println(" expected " + pubKey.getW().getAffineY()
					+ " got " + ((ECPublicKey) publicKey).getW().getAffineY());
			System.out.println("ECDH public key encoding (W test) failed");
		} else {
			System.out.println("ECDH public key encoding (W test) OK!");
		}

		if (!pubKey.getParams().getGenerator()
				.equals(((ECPublicKey) publicKey).getParams().getGenerator())) {
			System.out.println("ECDH public key encoding (G test) failed");
		} else {
			System.out.println("ECDH public key encoding (G test) OK!");
		}

	}

	private static ECPublicKey decodePublic(byte[] pubEnc)
			throws NoSuchAlgorithmException, NoSuchProviderException,
			InvalidKeySpecException {
		KeyFactory keyFac = KeyFactory.getInstance("ECDH", "SC");
		X509EncodedKeySpec pubX509 = new X509EncodedKeySpec(pubEnc);
		ECPublicKey pubKey = (ECPublicKey) keyFac.generatePublic(pubX509);
		return pubKey;
	}

	private static void testECprivateEncoding(PrivateKey privateKey)
			throws NoSuchAlgorithmException, NoSuchProviderException,
			InvalidKeySpecException {
		byte[] privEnc = privateKey.getEncoded();
		ECPrivateKey privKey = decodePrivate(privEnc);
		if (!privKey.getS().equals(((ECPrivateKey) privateKey).getS())) {
			System.out.println("ECDH private key encoding (S test) failed");
		} else {
			System.out.println("ECDH private key encoding (S test) OK!");
		}

		if (!privKey.getParams().getGenerator()
				.equals(((ECPrivateKey) privateKey).getParams().getGenerator())) {
			System.out.println("ECDH private key encoding (G test) failed");
		} else {
			System.out.println("ECDH private key encoding (G test) OK!");
		}

	}

	private static ECPrivateKey decodePrivate(byte[] privEnc)
			throws NoSuchAlgorithmException, NoSuchProviderException,
			InvalidKeySpecException {
		KeyFactory keyFac = KeyFactory.getInstance("ECDH", "SC");
		PKCS8EncodedKeySpec privPKCS8 = new PKCS8EncodedKeySpec(privEnc);
		ECPrivateKey privKey = (ECPrivateKey) keyFac.generatePrivate(privPKCS8);
		return privKey;

	}
}
