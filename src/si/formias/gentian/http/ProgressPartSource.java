/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package si.formias.gentian.http;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;

/**
 * <p>
 * Multipart POST part source. Can be memory data or file. Used at <a href=
 * "file:///home/miha/NetBeansProjects/emape/dist/javadoc/emape/Directory.html#upload(java.lang.String,%20java.util.List)"
 * >file upload</a>.
 * </p>
 * <p>
 * Upload progress is reported to <a href =
 * "ProgressObserver.html">ProgressObserver</a>'s of this connection.
 * </p>
 * 
 * 
 * @author miha
 */
public class ProgressPartSource {

	Map<InputStream, Long> position = new HashMap<InputStream, Long>();
	private final List<ProgressObserver> observers;

	void addTo(MultipartEntity reqEntity) throws FileNotFoundException {
		if (type == Type.BYTE_ARRAY) {
			InputStream stream = new ByteArrayInputStream(bytes) {

				@Override
				public synchronized int read(byte[] b, int off, int len) {
					long pos = position.get(this);

					int actual = super.read(b, off, len);
					for (ProgressObserver observer : observers) {
						observer.progress(ProgressObserver.Direction.Upload,
								name, pos + actual, getLength(), timestamp);
					}
					position.put(this, pos + actual);
					return actual;
				}

				@Override
				public synchronized int read(byte[] b) throws IOException {
					long pos = position.get(this);

					int actual = super.read(b);
					for (ProgressObserver observer : observers) {
						observer.progress(ProgressObserver.Direction.Upload,
								name, pos + actual, getLength(), timestamp);
					}
					position.put(this, pos + actual);
					return actual;

				}
			};
			position.put(stream, (long) 0);
			try {
				reqEntity.addPart(URLEncoder.encode(name, "UTF-8"),
						new InputStreamBody(stream, name));
			} catch (UnsupportedEncodingException ex) {
				Logger.getLogger(ProgressPartSource.class.getName()).log(
						Level.SEVERE, null, ex);
			}
		} else if (type == Type.FILE) {
			InputStream stream = new FileInputStream(file) {

				@Override
				public synchronized int read(byte[] b, int off, int len)
						throws IOException {
					long pos = position.get(this);

					int actual = super.read(b, off, len);
					for (ProgressObserver observer : observers) {
						observer.progress(ProgressObserver.Direction.Upload,
								name, pos + actual, getLength(), timestamp);
					}
					position.put(this, pos + actual);
					return actual;
				}

				@Override
				public synchronized int read(byte[] b) throws IOException {
					long pos = position.get(this);

					int actual = super.read(b);
					for (ProgressObserver observer : observers) {
						observer.progress(ProgressObserver.Direction.Upload,
								name, pos + actual, getLength(), timestamp);
					}
					position.put(this, pos + actual);
					return actual;

				}
			};
			position.put(stream, (long) 0);
			reqEntity.addPart(name, new InputStreamBody(stream, name) {

				@Override
				public String getCharset() {
					return "UTF-8";
				}

			});
		}
	}

	public Long getTimestamp() {
		if (type == Type.FILE) {
			return file.lastModified() / 1000;
		}
		return null;
	}

	static enum Type {

		BYTE_ARRAY, FILE
	}

	final String name;
	final Type type;
	final byte[] bytes;
	final File file;

	final long timestamp;

	/**
	 * Part source is byte[] in memory
	 * 
	 * @param emape
	 *            reference to emape connection
	 * @param name
	 *            name of file
	 * @param bytes
	 *            file as byte[]
	 * @param timestamp
	 *            unix timestamp of last modification
	 */
	public ProgressPartSource(List<ProgressObserver> observers, String name,
			byte[] bytes, long timestamp) {
		this.name = name;
		this.type = Type.BYTE_ARRAY;
		this.bytes = bytes;
		this.file = null;
		this.timestamp = timestamp;
		this.observers = observers;
	}

	/**
	 * Part source is file on local filesystem
	 * 
	 * @param emape
	 *            reference to emape connection
	 * @param file
	 *            local file
	 * 
	 */
	public ProgressPartSource(List<ProgressObserver> observers, File file) {
		this.name = file.getName();
		this.type = Type.FILE;
		this.bytes = null;
		this.file = file;
		this.timestamp = file.lastModified();
		this.observers = observers;
	}

	/**
	 * Returns length of this file
	 * 
	 * @return length of this file
	 */
	public long getLength() {
		if (type == Type.BYTE_ARRAY) {
			return bytes.length;
		} else if (type == Type.FILE) {
			return file.length();
		} else {
			return 0;
		}
	}

	public String toString() {
		if (type == Type.BYTE_ARRAY) {
			return name;
		} else if (type == Type.FILE) {
			return file.getName();
		} else {
			return "No name";
		}
	}

	/**
	 * Returns name of this file
	 * 
	 * @return file name
	 */
	public String getFileName() {
		return name;
	}

	/**
	 * Creates new InputStream that reads from this file
	 * 
	 * @return a fresh instance of InputStream that reads from this file
	 * @throws IOException
	 *             Network error
	 */
	public InputStream createInputStream() throws IOException {
		InputStream ret = null;

		if (type == Type.BYTE_ARRAY) {
			ret = new ByteArrayInputStream(bytes) {

				@Override
				public synchronized int read(byte[] b, int off, int len) {
					long pos = position.get(this);

					int actual = super.read(b, off, len);
					for (ProgressObserver observer : observers) {
						observer.progress(ProgressObserver.Direction.Upload,
								name, pos + actual, getLength(), timestamp);
					}
					position.put(this, pos + actual);
					return actual;
				}

				@Override
				public synchronized int read(byte[] b) throws IOException {
					long pos = position.get(this);

					int actual = super.read(b);
					for (ProgressObserver observer : observers) {
						observer.progress(ProgressObserver.Direction.Upload,
								name, pos + actual, getLength(), timestamp);
					}
					position.put(this, pos + actual);
					return actual;

				}
			};
		} else if (type == Type.FILE) {
			ret = new FileInputStream(file) {

				@Override
				public synchronized int read(byte[] b, int off, int len)
						throws IOException {
					long pos = position.get(this);

					int actual = super.read(b, off, len);
					for (ProgressObserver observer : observers) {
						observer.progress(ProgressObserver.Direction.Upload,
								name, pos + actual, getLength(), timestamp);
					}
					position.put(this, pos + actual);
					return actual;
				}

				@Override
				public synchronized int read(byte[] b) throws IOException {
					long pos = position.get(this);

					int actual = super.read(b);
					for (ProgressObserver observer : observers) {
						observer.progress(ProgressObserver.Direction.Upload,
								name, pos + actual, getLength(), timestamp);
					}
					position.put(this, pos + actual);
					return actual;
				}
			};

		}
		if (ret != null) {
			position.put(ret, (long) 0);

		}
		return ret;
	}
}
