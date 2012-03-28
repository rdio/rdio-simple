/*
 * (c) 2011 Rdio Inc
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.rdio.simple;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * An ordered dictionary of String key/value pairs for holding OAuth parameters.
 */
@SuppressWarnings("UnusedDeclaration")
public class Parameters extends TreeMap<String, String> {
  private static final long serialVersionUID = 1L;

  /**
   * Build a new Parameters object with on pair.
   * @param key   the key
   * @param value the value
   * @return      the new Parameters object
   */
  public static Parameters build(String key, String value) {
    Parameters p = new Parameters();
    p.put(key, value);
    return p;
  }

  /**
   * Add another key/value pair to the Parameters.
   * @param key   the key
   * @param value the value
   * @return      the object
   */
  public Parameters and(String key, String value) {
    put(key, value);
    return this;
  }

  /**
   * Create an empty Parameters object.
   */
  public Parameters() {
    super();
  }

  /**
   * Create a Parameters object with the contents of another Map.
   * @param other the other Map
   */
  public Parameters(Map<String, String> other) {
    super(other);
  }

  /**
   * Create a Parameters object with the pairs in a percent encoded string.
   * @param percentEncoded the percent encoded string
   * @return               a new Parameters object
   */
  public static Parameters fromPercentEncoded(String percentEncoded) {
    Parameters params = new Parameters();
    if (percentEncoded.length() == 0) {
      return params;
    }
    String[] encoded_params = percentEncoded.split("&");
    for (String encoded_param : encoded_params) {
      String[] pair = encoded_param.split("=", 2);
      if (pair.length == 2) {
        params.put(percentDecode(pair[0]), percentDecode(pair[1]));
      } else {
        params.put(percentDecode(pair[0]), "");
      }
    }
    return params;
  }

  /**
   * Serialize the object to a percent encoded string.
   * @return a percent encoded string
   */
  public String toPercentEncoded() {
    StringBuilder escaped = new StringBuilder();
    Iterator<String> iter = keySet().iterator();
    boolean first = true;
    while (iter.hasNext()) {
      if (!first) escaped.append('&');
      else first = false;
      String key = iter.next();
      escaped.append(percentEncode(key));
      escaped.append('=');
      escaped.append(percentEncode(get(key)));
    }
    return escaped.toString();
  }

  /**
   * Serialize the object to an HTTP header style list of pairs. For example:
   *   key="value", other="thing"
   * @return a header formatted string
   */
  public String toHeader() {
    StringBuilder header = new StringBuilder();
    Iterator<String> iter = keySet().iterator();
    boolean first = true;
    while (iter.hasNext()) {
      if (!first) header.append(", ");
      else first = false;
      String key = iter.next();
      header.append(key);
      header.append("=\"");
      header.append(get(key));
      header.append('"');
    }
    return header.toString();
  }

  /**
   * Percent decode a string.
   * @param s the encoded string
   * @return  the decoded string
   */
  public static String percentDecode(String s) {
    try {
      return URLDecoder.decode(s, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return "";
    }
  }

  /**
   * Percent encode a string
   * @param s the string
   * @return  the encoded string
   */
  public static String percentEncode(String s) {
    byte[] bytes;
    try {
      bytes = s.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return "";
    }
    StringBuilder encoded = new StringBuilder();
    for (byte b : bytes) {
      if ((b >= '0' && b <= '9') ||
          (b >= 'A' && b <= 'Z') ||
          (b >= 'a' && b <= 'z') ||
          b == '-' || b == '.' || b == '_' || b == '~') {
        encoded.append((char) b);
      } else {
        encoded.append(String.format("%%%02X", b));
      }
    }
    return encoded.toString();
  }

  /**
   * an interface for filtering Parameters.
   */
  public interface Filter {
    /**
     * Filter an item in the Parameters
     * @param key   the key
     * @param value the value
     * @return      true if the item should be included, false if the item should be excluded
     */
    public boolean filter(String key, String value);
  }

  /**
   * Return a new Parameters object with a filtered subset of this Parameters object
   * @param filter the filter condition
   * @return       the new object
   */
  public Parameters filter(Filter filter) {
    Parameters filtered = new Parameters();
    for (String key : keySet()) {
      String value = get(key);
      if (filter.filter(key, value)) {
        filtered.put(key, value);
      }
    }
    return filtered;
  }

}
