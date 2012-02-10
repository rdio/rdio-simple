package com.rdio.simple.examples;

import com.rdio.simple.RdioClient;

public class ConsumerCredentials_EXAMPLE extends RdioClient.Consumer {
  // you can get these by signing up for a developer account at:
  // http://developer.rdio.com/
  private static final String RDIO_CONSUMER_KEY = "";
  private static final String RDIO_CONSUMER_SECRET = "";

  public ConsumerCredentials_EXAMPLE() {
    super(RDIO_CONSUMER_KEY, RDIO_CONSUMER_SECRET);
  }
}
