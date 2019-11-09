package opencv.filters;

import java.util.HashMap;

public class FilterArguments {
  private HashMap<String, Object> arguments = new HashMap<>();

  public void putBoolean(String key, boolean value) {
    arguments.put(key, value);
  }

  void putByte(String key, byte value) {
    arguments.put(key, value);
  }

  void putChar(String key, char value) {
    arguments.put(key, value);
  }

  void putShort(String key, short value) {
    arguments.put(key, value);
  }

  public void putInt(String key, int value) {
    arguments.put(key, value);
  }

  public void putLong(String key, long value) {
    arguments.put(key, value);
  }

  void putFloat(String key, float value) {
    arguments.put(key, value);
  }

  public void putDouble(String key, double value) {
    arguments.put(key, value);
  }

  public void putString(String key, String value) {
    arguments.put(key, value);
  }

  public boolean getBoolean(String key) throws NoArgumentFound {
    final Object o = arguments.get(key);
    if(o == null) throw new NoArgumentFound();
    try {
      return (boolean) o;
    } catch (ClassCastException e) {
      e.printStackTrace();
      throw new NoArgumentFound();
    }
  }

  public byte getByte(String key) throws NoArgumentFound {
    final Object o = arguments.get(key);
    if(o == null) throw new NoArgumentFound();
    try {
      return (byte) o;
    } catch (ClassCastException e) {
      e.printStackTrace();
      throw new NoArgumentFound();
    }
  }

  public char getChar(String key) throws NoArgumentFound{
    final Object o = arguments.get(key);
    if(o == null) throw new NoArgumentFound();
    try {
      return (char) o;
    } catch (ClassCastException e) {
      e.printStackTrace();
      throw new NoArgumentFound();
    }
  }

  public short getShort(String key) throws NoArgumentFound {
    final Object o = arguments.get(key);
    if(o == null) throw new NoArgumentFound();
    try {
      return (short) o;
    } catch (ClassCastException e) {
      e.printStackTrace();
      throw new NoArgumentFound();
    }
  }

  public int getInt(String key) throws NoArgumentFound {
    final Object o = arguments.get(key);
    if(o == null) throw new NoArgumentFound();
    try {
      return (int) o;
    } catch (ClassCastException e) {
      e.printStackTrace();
      throw new NoArgumentFound();
    }
  }

  public long getLong(String key) throws NoArgumentFound {
    final Object o = arguments.get(key);
    if(o == null) throw new NoArgumentFound();
    try {
      return (long) o;
    } catch (ClassCastException e) {
      e.printStackTrace();
      throw new NoArgumentFound();
    }
  }

  public float getFloat(String key) throws NoArgumentFound {
    final Object o = arguments.get(key);
    if(o == null) throw new NoArgumentFound();
    try {
      return (float) o;
    } catch (ClassCastException e) {
      e.printStackTrace();
      throw new NoArgumentFound();
    }
  }

  public double getDouble(String key) throws NoArgumentFound {
    final Object o = arguments.get(key);
    if(o == null) throw new NoArgumentFound();
    try {
      return (double) o;
    } catch (ClassCastException e) {
      e.printStackTrace();
      throw new NoArgumentFound();
    }
  }

  public String getString(String key) throws NoArgumentFound {
    final Object o = arguments.get(key);
    if(o == null) throw new NoArgumentFound();
    try {
      return (String) o;
    } catch (ClassCastException e) {
      e.printStackTrace();
      throw new NoArgumentFound();
    }
  }

  public static class NoArgumentFound extends Exception { }
}
