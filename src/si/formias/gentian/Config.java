package si.formias.gentian;

import gentian.crypt.GentianCrypt;
import gentian.crypt.GentianEnvelope;
import gentian.crypt.UnwrapException;
import gentian.crypt.WrapException;
import gentian.crypt.keyprovider.ZipGentianKeyProvider;
import gentian.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.parsers.ParserConfigurationException;

import si.formias.gentian.dialog.NewMasterPasswordDialog;
import si.formias.gentian.dialog.OpenMasterPasswordDialog;
import si.formias.gentian.xml.Node;
import si.formias.gentian.xml.Parser;
import si.formias.gentian.xml.config.GentianAccount;
import si.formias.gentian.xml.config.GentianConfig;

import org.xml.sax.SAXException;

import android.os.Environment;
import static si.formias.gentian.Util.*;

public class Config {
	static {
		Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
	}

	volatile public GentianConfig configData;
	volatile public static File gentian;
	volatile ZipGentianKeyProvider masterProvider, walletProvider, walletProvider2;
	final File walletSaltFile;
	final File walletKeyPackFile;
	final File walletKeyPackFile2;
	final File walletControlFile;
	final File masterKeyPackFile;
	final File configFile;
	final File logFilenameKeysFile;
	volatile private byte[][] logFilenameKeys;
	volatile boolean inited;
	volatile public byte[] check;
	volatile public static GentianChat gentianChat;
	public final int SAVEPREFIX = 40;
	volatile byte[][] aes;
	static {
		{
			String state = Environment.getExternalStorageState();
			boolean mExternalStorageAvailable, mExternalStorageWriteable;
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				mExternalStorageAvailable = mExternalStorageWriteable = true;
			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
				mExternalStorageAvailable = true;
				mExternalStorageWriteable = false;
			} else {
				mExternalStorageAvailable = mExternalStorageWriteable = false;
			}
			if (!(mExternalStorageAvailable && mExternalStorageWriteable)
					&& gentianChat != null)
				gentianChat.displayNotice("External storage not available.",
						new Runnable() {
							public void run() {
								gentianChat.finish();
							}
						});
		}
		File storageDir = Environment.getExternalStorageDirectory();
		gentian = new File(storageDir, "gentian");
		if (gentian.exists())
			gentian.renameTo(new File("gentianold"));
		if (!gentian.exists()) {
			gentian.mkdir();
		}
	}

	public Config(final GentianChat gentianChat) {
		Config.gentianChat = gentianChat;
		if (!gentian.exists()) {
			gentian.mkdir();
		}
		walletSaltFile = new File(gentian, "walletsalt");
		walletKeyPackFile = new File(gentian, "walletkeys");
		walletKeyPackFile2 = new File(gentian, "walletkeys2");
		walletControlFile = new File(gentian, "walletcontrol");
		masterKeyPackFile = new File(gentian, "masterkeys");
		logFilenameKeysFile = new File(gentian, "logfilenamekeys");
		configFile = new File(gentian, "config");
		final byte[] salt = new byte[128];
		if (!masterKeyPackFile.exists()) {
			new NewMasterPasswordDialog(gentianChat,
					new NewMasterPasswordDialog.CallBack() {

						@Override
						public void passwordSet(String password) {
							SecureRandom r = new SecureRandom();

							r.nextBytes(salt);
							{
								FileOutputStream out;
								try {
									out = new FileOutputStream(walletSaltFile);
									out.write(salt);
									out.close();
								} catch (FileNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}
							try {
								byte[] pass = encryptPassword(salt, password);

								aes = splitBytes(pass, 32);
								// System.out.print("AES save: "+Base64.encodeToString(aes[0],
								// false)+ " "+Base64.encodeToString(aes[1],
								// false));
								GentianEnvelope.AUTOWIPE = false;
								walletProvider = new ZipGentianKeyProvider(
										walletKeyPackFile, 1, aes);
								walletProvider2 = new ZipGentianKeyProvider(
										walletKeyPackFile2, 1, walletProvider,
										4, 0, null);
								byte[] salt2 = Util.copy(salt);
								{

									byte[] check = GentianEnvelope.wrap(
											walletProvider2, salt, 8, 0, 0,
											false);

									FileOutputStream out = new FileOutputStream(
											walletControlFile);

									out.write(check);
									out.close();
									// System.out.println("initialize control:"+check.length+" "+Base64.encodeToString(check,
									// false));
									byte[] check2 = GentianEnvelope
											.unwrap(walletProvider2, check, 8,
													0, false);
									// System.out.println("Initial check:"+Util.equal(salt2,
									// check2)+" "+Base64.encodeToString(check2,
									// false));
								}
								masterProvider = new ZipGentianKeyProvider(
										masterKeyPackFile, 1, walletProvider2,
										4, 0, null);
								loadKeys(salt2, aes);
							} catch (NoSuchAlgorithmException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (UnwrapException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (WrapException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (InvalidKeyException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (NoSuchPaddingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalBlockSizeException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (BadPaddingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (InvalidAlgorithmParameterException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (NoSuchProviderException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
		} else {
			OpenMasterPasswordDialog dialog = new OpenMasterPasswordDialog(
					gentianChat, new OpenMasterPasswordDialog.CallBack() {

						@Override
						public void passwordSet(String password,Runnable onSuccess,Runnable onFail) {
							FileInputStream in;
							try {
								in = new FileInputStream(walletSaltFile);
								/*
								 * System.out.println("Read " + in.read(salt) +
								 * " salt bytes");
								 */
								in.read(salt);
								in.close();
								byte[] pass = encryptPassword(salt, password);
								/*
								 * System.out.println("Wallet passkey generated: "
								 * + Base64.encodeToString(pass, false));
								 */
								aes = splitBytes(pass, 32);
								// System.out.print("AES load: "+Base64.encodeToString(aes[0],
								// false)+ " "+Base64.encodeToString(aes[1],
								// false));
								if (!loadKeys(salt, aes)) {
									gentianChat.displayNotice(
											"Wrong password.", null);
//									return false;
									onFail.run();
								} else {
									onSuccess.run();
								}

							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (NoSuchAlgorithmException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							onFail.run();
						}

					});

		}
	}

	private boolean loadKeys(byte[] salt, byte[][] aes)
			throws NoSuchAlgorithmException, FileNotFoundException, IOException {
		GentianEnvelope.AUTOWIPE = false;
		if (walletProvider == null)
			walletProvider = new ZipGentianKeyProvider(walletKeyPackFile, aes);
		if (walletProvider2 == null)
			walletProvider2 = new ZipGentianKeyProvider(walletKeyPackFile2,
					walletProvider, 4, 0, null);

		byte[] control = new byte[384];
		{
			FileInputStream in = new FileInputStream(walletControlFile);
			/*
			 * System.out.println("Read " + in.read(control) +
			 * " control bytes");
			 */
			int read = in.read(control);
			System.out.println("Read " + read + " control bytes");
			in.close();
			// System.out.println("control loaded:"+control.length+" "+Base64.encodeToString(control,
			// false));
			byte[] check = null;
			try {
				check = GentianEnvelope.unwrap(walletProvider2, control, 8, 0,
						false);
			} catch (InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BadPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidAlgorithmParameterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnwrapException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (!equal(check, salt)) {
				// System.out.println("*** WRONG PASS");
				this.check = check;
				walletProvider=null;
				walletProvider2=null;
				masterProvider=null;
				return false;
			} else {
				// System.out.println("*** PAsS OK");
			}
		}

		if (masterProvider == null)
			masterProvider = new ZipGentianKeyProvider(masterKeyPackFile,
					walletProvider2, 4, 0, null);
		GentianEnvelope.AUTOWIPE = true;
		inited = true;
		if (configFile.exists()) {
			try {

				configData = (GentianConfig) loadNodeFile(configFile);
				// System.out.println("gentian config read");

			} catch (Exception e) {

			}
		}
		if (configData == null) {
			try {
				configData = new GentianConfig(null);

				try {
					configData.add(new GentianAccount());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				saveConfig();

				System.out.println("gentian config created");

			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		// System.out.println("Master keys loaded");
		if (logFilenameKeysFile.exists()) {
			try {
				logFilenameKeys = splitBytes(GentianEnvelope.unwrap(
						masterProvider, Util
								.readStreamBytes(new FileInputStream(
										logFilenameKeysFile)), 8, 0, false), 32);
			} catch (InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BadPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidAlgorithmParameterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnwrapException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			SecureRandom r = new SecureRandom();
			byte[] keys = new byte[32 * 8];
			r.nextBytes(keys);

			logFilenameKeys = splitBytes(keys, 32);
			try {
				byte[] encrypted = GentianEnvelope.wrap(masterProvider, keys,
						8, 0, 0, false);
				FileOutputStream fout = new FileOutputStream(
						logFilenameKeysFile);
				fout.write(encrypted);
				fout.close();
			} catch (InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BadPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidAlgorithmParameterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnwrapException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchProviderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		gentianChat.configLoaded();

		return true;
	}

	public Node loadNodeFile(File file) {
		try {
			byte[] bytes = Util.readStreamBytes(new FileInputStream(file));
			bytes = GentianEnvelope.unwrap(masterProvider, bytes, 8, 0, true);
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[][] cut = cut(bytes, SAVEPREFIX);
			cut = cut(cut[1], 32);
			md.update(cut[1]);
			byte[] digest = md.digest();
			if (equal(cut[0], digest)) {
				// valid checksum for xml
				ByteArrayInputStream bin = new ByteArrayInputStream(cut[1]);

				Parser p = new Parser();

				p.parse(bin);
				return p.root;
			} else {
				return null;
			}
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnwrapException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public synchronized void saveConfig() {

		saveNodeToFile(configData, configFile);
	}

	public void saveNodeToFile(Node root, File file) {
		try {

			SecureRandom r = new SecureRandom();
			byte[] seed = new byte[SAVEPREFIX];
			r.nextBytes(seed);

			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			bout.write(seed);
			byte[] content = root.toString(0).getBytes("utf-8");
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(content);
			byte[] digest = md.digest();
			bout.write(digest);
			bout.write(content);
			byte[] save = GentianEnvelope.wrap(masterProvider,
					bout.toByteArray(), 8, 0, 0, true);
			FileOutputStream fout = new FileOutputStream(file);
			fout.write(save);
			fout.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnwrapException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String encodeFilename(String filename) {
		try {
			return encodeFilename(filename.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String encodeFilename(byte[] b) {

		byte[] iv = new byte[16];
		try {

			for (int i = 0; i < logFilenameKeys.length; i++) {
				b = GentianCrypt.encrypt(logFilenameKeys[i], iv, b, i == 0);
			}
			return Base64.encodeToString(b, false).replace("+", "-")
					.replace("/", "_").replace("=", "~");
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String decodeFileName(String filename) {
		byte[] b = Base64.decode(filename.replace("-", "+").replace("_", "/")
				.replace("~", "="));
		byte[] iv = new byte[16];
		try {
			for (int i = logFilenameKeys.length - 1; i >= 0; i--) {
				b = GentianCrypt.decrypt(logFilenameKeys[i], iv, b, i == 0);
			}

			return new String(b, "utf-8");
		} catch (UnsupportedEncodingException e) {

			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void wipe() {
		try {
			if (masterProvider != null)
				masterProvider.wipe();
		} catch (Exception e) {

		}
		try {
			if (walletProvider != null)
				walletProvider.wipe();
		} catch (Exception e) {

		}
		try {
			if (walletProvider2 != null)
				walletProvider2.wipe();
		} catch (Exception e) {

		}
		try {
			Util.wipe(aes);
		} catch (Exception e) {

		}

	}
}