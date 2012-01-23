package si.formias.gentian;


import java.io.*;
import java.util.*;
import si.formias.gentian.R;

import android.telephony.SmsManager;
/**
 * Replace code added to encode it to GSM alphabet
 * 
 * 
 * 
 * Modified version of Jochaim Henke's original code from
 * http://base91.sourceforge.net/
 *
 * basE91 encoding/decoding routines
 *
 * Copyright (c) 2000-2006 Joachim Henke All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. - Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. - Neither the name of Joachim Henke nor the names of
 * his contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * @author Joachim Henke (Original version)
 * @author Benedikt Waldvogel (Modifications)
 */
public class Base91 {

    private static  byte[] ENCODING_TABLE;
    private static  byte[] DECODING_TABLE;
    private static  int BASE;
    private static  float AVERAGE_ENCODING_RATIO = 1.2297f;
    private static Map<Byte,Byte> encodeMap=new HashMap<Byte,Byte>();
    private static Map<Byte,Byte> decodeMap=new HashMap<Byte,Byte>();
    static {
	try {

   String ts2 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@£$¥èéùìòÇØøÅå_ÆæßÉ !\"#¤%&'()*+,-./";
	String ts = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!#$%&()*+,./:;<=>?@[]^_`{|}~\"";
	ENCODING_TABLE = ts.getBytes("latin1");
	
	BASE = ENCODING_TABLE.length;
	byte[] ENCREPLACE=ts2.getBytes("latin1");
	System.out.println(ENCODING_TABLE.length+" "+ENCREPLACE.length);
/*	SmsManager sms = SmsManager.getDefault();
	StringBuilder sb = new StringBuilder();
	for (int i=0;i<160;i++) {
		sb.append("1");
	}
	String test=sb.toString();*/
	for (int i=0;i<BASE;i++) {
		encodeMap.put(ENCODING_TABLE[i],ENCREPLACE[i]);
		decodeMap.put(ENCREPLACE[i],ENCODING_TABLE[i]);
				
	}	
	/*for (int i=0;i<ENCREPLACE.length;i++) {
		String s = new String(new byte[]{ENCREPLACE[i]},"latin1");
		int len=sms.divideMessage(s+test).get(0).length();
		if (len<153) {
			System.out.print(i+":"+s+":"+len+"  after:"+new String(new byte[]{ENCREPLACE[i-1]},"latin1"));
		}
	}

	System.out.println("---------");*/
	DECODING_TABLE = new byte[256];
	for (int i = 0; i < 256; ++i)
	    DECODING_TABLE[i] = -1;

	for (int i = 0; i < BASE; ++i)
	    DECODING_TABLE[ENCODING_TABLE[i]] = (byte) i;

	} catch ( UnsupportedEncodingException  e) {
		e.printStackTrace();
	}
    }

    public static byte[] encode(byte[] data) {

	int estimatedSize = (int) Math.ceil(data.length * AVERAGE_ENCODING_RATIO);
	ByteArrayOutputStream output = new ByteArrayOutputStream(estimatedSize);

	int ebq = 0;
	int en = 0;
	for (int i = 0; i < data.length; ++i) {
	    ebq |= (data[i] & 255) << en;
	    en += 8;
	    if (en > 13) {
		int ev = ebq & 8191;

		if (ev > 88) {
		    ebq >>= 13;
		    en -= 13;
		} else {
		    ev = ebq & 16383;
		    ebq >>= 14;
		    en -= 14;
		}
		output.write(ENCODING_TABLE[ev % BASE]);
		output.write(ENCODING_TABLE[ev / BASE]);
	    }
	}

	if (en > 0) {
	    output.write(ENCODING_TABLE[ebq % BASE]);
	    if (en > 7 || ebq > 90) {
		output.write(ENCODING_TABLE[ebq / BASE]);
	    }
	}
	byte[] out= output.toByteArray();
	for (int i=0;i<out.length;i++) {
		out[i]=encodeMap.get(out[i]);
	}
	return out;
    }

    public static byte[] decode(byte[] data) {

	for (int i=0;i<data.length;i++) {
		data[i]=decodeMap.get(data[i]);
	}

	// if (data.length == 0)
	// return new byte[] {};

	int dbq = 0;
	int dn = 0;
	int dv = -1;

	int estimatedSize = Math.round(data.length / AVERAGE_ENCODING_RATIO);
	ByteArrayOutputStream output = new ByteArrayOutputStream(estimatedSize);

	for (int i = 0; i < data.length; ++i) {
	    if (DECODING_TABLE[data[i]] == -1)
		continue;
	    if (dv == -1)
		dv = DECODING_TABLE[data[i]];
	    else {
		dv += DECODING_TABLE[data[i]] * BASE;
		dbq |= dv << dn;
		dn += (dv & 8191) > 88 ? 13 : 14;
		do {
		    output.write((byte) dbq);
		    dbq >>= 8;
		    dn -= 8;
		} while (dn > 7);
		dv = -1;
	    }
	}

	if (dv != -1) {
	    output.write((byte) (dbq | dv << dn));
	}

	return output.toByteArray();
    }
}