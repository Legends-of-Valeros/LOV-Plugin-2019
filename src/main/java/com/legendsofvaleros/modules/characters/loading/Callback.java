package com.legendsofvaleros.modules.characters.loading;

/**
 * Represents a method which may be called once an event has transpired or a result has been
 * computed asynchronously.
 * 
 * @param <V> the type of result.
 */
public interface Callback<V> {

  /**
   * Called when the result is done.
   * 
   * @param value The result of the computation or event.
   * @param error The error(s) that occurred, if any.
   */
  void callback(V value, Throwable error);

}
