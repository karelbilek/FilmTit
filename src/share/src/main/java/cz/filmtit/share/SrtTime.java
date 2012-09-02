package cz.filmtit.share;


import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.IsSerializable;
import cz.filmtit.share.exceptions.InvalidValueException;

import java.io.Serializable;

/**
 * Represents one time in an SRT file, which has the format of
 * <code>hh:mm:ss,ttt</code>.
 * Provides int getters, int and String setters, convertors to and from a String,
 * a comparator and a subtractor.
 * The object is internaly represented by four integers.
 * @author rur, KB
 *
 */
public class SrtTime implements Comparable<SrtTime>, Serializable, IsSerializable {
	
	private static final long serialVersionUID = 3413134158754151331L;
	
	// constants
	public static final char HM_DELIMITER = ':';
	public static final char MS_DELIMITER = ':';
	public static final char ST_DELIMITER = ',';
    public static final String ZERO_TIME = "00:00:00,000";
    
                                                            //negative time appears time to time
    public static final RegExp timeExtractor  = RegExp.compile("(-?[0-9]+):(-?[0-9]+):(-?[0-9]+)[,:.](-?[0-9]+)");
	
	// values (h m s could be byte)
	private int h;
	private int m;
	private int s;
	private int t;
	
	// getters to int
	public int getH() {
		return h;
	}
	public int getM() {
		return m;
	}
	public int getS() {
		return s;
	}
	public int getT() {
		return t;
	}
	
	// getter to String
	public String getStringH() {
		return Integer.toString(h);
	}
	public String getStringM() {
		return Integer.toString(m);
	}
	public String getStringS() {
		return Integer.toString(s);
	}
	public String getStringT() {
		return Integer.toString(t);
	}
	
	// setters from int
	public void setH(int h) throws InvalidValueException {
		if (0 <= h && h <= 99) {
			this.h = h;
		}
		else if (h < 0) {
            this.h=0;
        } else {
			throw new InvalidValueException("Value of h must be 0 <= h <= 99, '" + h + "' is invalid!");
		}
	}
	public void setM(int m) throws InvalidValueException {
		if (0 <= m && m <= 59) {
			this.m = m;
		}
		else if (m < 0) {
            this.m=0;
        } else{
			throw new InvalidValueException("Value of m must be 0 <= m <= 59, '" + m + "' is invalid!");
		}
	}
	public void setS(int s) throws InvalidValueException {
		if (0 <= s && s <= 59) {
			this.s = s;
		}
		else if (s < 0) {
            this.s=0;
        } else{
			throw new InvalidValueException("Value of s must be 0 <= s <= 59, '" + s + "' is invalid!");
		}
	}
	public void setT(int t) throws InvalidValueException {
		if (0 <= t && t <= 999) {
			this.t = t;
		}
		else if (t < 0) {
            this.t=0;
        } else {
			throw new InvalidValueException("Value of t must be 0 <= t <= 999, '" + t + "' is invalid!");
		}
	}

	// setters from String
	public void setH(String h) throws InvalidValueException {
		try {
			setH(Integer.parseInt(h));
		}
		catch (NumberFormatException e) {
			throw new InvalidValueException("Value of h must be an ineteger, '" + h + "' is invalid!");
		}
	}
	public void setM(String m) throws InvalidValueException {
		try {
			setM(Integer.parseInt(m));
		}
		catch (NumberFormatException e) {
			throw new InvalidValueException("Value of m must be an ineteger, '" + m + "' is invalid!");
		}
	}
	public void setS(String s) throws InvalidValueException {
		try {
			setS(Integer.parseInt(s));
		}
		catch (NumberFormatException e) {
			throw new InvalidValueException("Value of s must be an ineteger, '" + s + "' is invalid!");
		}
	}
	public void setT(String t) throws InvalidValueException {
		try {
			setT(Integer.parseInt(t));
		}
		catch (NumberFormatException e) {
			throw new InvalidValueException("Value of t must be an ineteger, '" + t + "' is invalid!");
		}
	}

	// constructors
	public SrtTime(int h, int m, int s, int t) throws InvalidValueException {
		setH(h);
		setM(m);
		setS(s);
		setT(t);
	}
	public SrtTime(String time) throws InvalidValueException {
        if (timeExtractor.test(time)) {
            MatchResult mr = timeExtractor.exec(time);
	        setH(mr.getGroup(1));
            setM(mr.getGroup(2));
            setS(mr.getGroup(3));
            setT(mr.getGroup(4));
        } else {
	        throw new InvalidValueException("Format of SRT time must be hh:mm:ss,ttt, '" + time + "' is invalid!");
        }
    }


	@SuppressWarnings("unused")
	private SrtTime() {
		// constructor for serialization support and hibernate
	}
	
	/**
	 * hh:mm:ss,ttt
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		// hh:
		if (h < 10) {
			sb.append('0');
		}
		sb.append(h);
		sb.append(HM_DELIMITER);
		// mm:
		if (m < 10) {
			sb.append('0');
		}
		sb.append(m);
		sb.append(MS_DELIMITER);
		// ss,
		if (s < 10) {
			sb.append('0');
		}
		sb.append(s);
		sb.append(ST_DELIMITER);
		// ttt
		if (t < 100) {
			sb.append('0');
		}
		if (t < 10) {
			sb.append('0');
		}
		sb.append(t);
		
		return sb.toString();
	}
	
	/**
	 * 
	 * @return The time represented by this object, in miliseconds.
	 */
	public int toMs() {
		int value = 0;
		value += h;
		value *= 60;
		value += m;
		value *= 60;
		value += s;
		value *= 1000;
		value += t;
		return value;
	}
	
	/**
	 * The comparator is optimized for comparing times
	 * having the same numbers in the higher orders
	 * but this being lower than that in the lower orders.
	 * This optimization was chosen so that one can quickly check
	 * the validity of the chunk start time and end time,
	 * this being the start time
	 * and that being the end time.
	 */
	@Override
	public int compareTo(SrtTime that) {
		if (this.h == that.h) {
			if (this.m == that.m) {
				if (this.s == that.s) {
					return compareInts(this.t, that.t);
				}
				else {
					return compareInts(this.s, that.s);
				}
			}
			else {
				return compareInts(this.m, that.m);
			}
		}
		else {
			return compareInts(this.h, that.h);
		}
	}
	
	/**
	 * Integer comparator, optimalized for a < b
	 * @param a
	 * @param b
	 * @return -1, 0 or 1 if a is <, == or > than b
	 */
	private static int compareInts (int a, int b) {
		if (a < b) {
			return -1;
		}
		else if (a > b) {
			return 1;
		}
		else {
			assert a == b;
			return 0;
		}
	}
	
	/**
	 * The subtractor is optimized for small time differences,
	 * i.e. for subtracting times
	 * having the same numbers in the higher orders.
	 * This optimization was chosen so that one can quickly compute
	 * the difference between the chunk start time and end time,
	 * minuend being the end time
	 * and subtrahend being the start time.
	 * @return minuend.toMs() - subtrahend.toMs()
	 */
	public static int subtract(SrtTime minuend, SrtTime subtrahend) {
		int result = minuend.t - subtrahend.t;
		if (minuend.s != subtrahend.s) {
			result += (minuend.s - subtrahend.s) * 1000;
		}
		if (minuend.m != subtrahend.m) {
			result += (minuend.m - subtrahend.m) * 60000;
		}
		if (minuend.h != subtrahend.h) {
			result += (minuend.h - subtrahend.h) * 3600000;
		}
		return result;
	}
	
	public static final String DISPLAY_INTERVAL_DELIMITER = " - ";
	
	// TODO: Object -> SrtTime
	/**
	 * hh:mm:ss,ttt - hh:mm:ss,ttt
	 * (to be used to display in GUI)
	 */
	public static String toDisplayInterval(Object start, Object end) {
		StringBuilder sb = new StringBuilder();
		sb.append(start);
		sb.append(DISPLAY_INTERVAL_DELIMITER);
		sb.append(end);
		return sb.toString();
	}
	
	public static final String SRT_INTERVAL_DELIMITER = " --> ";
	
	// TODO: Object -> SrtTime
	/**
	 * hh:mm:ss,ttt --> hh:mm:ss,ttt
	 * (to be used to export to SRT)
	 */
	public static String toSrtInterval(Object start, Object end) {
		StringBuilder sb = new StringBuilder();
		sb.append(start);
		sb.append(SRT_INTERVAL_DELIMITER);
		sb.append(end);
		return sb.toString();
	}
	
	public SrtTime clone() {
		SrtTime result = null;
		try {
			result = new SrtTime(h, m, s, t);
		} catch (InvalidValueException e) {
			assert false : "The original object is always valid.";
		}
		return result;
	}
	
}
