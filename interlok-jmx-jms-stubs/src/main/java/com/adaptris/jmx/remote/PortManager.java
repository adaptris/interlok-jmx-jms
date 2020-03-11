package com.adaptris.jmx.remote;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

// This is to remove dependencies on core + core-stubs!
public class PortManager {
  private static final int PORT_RANGE = 3000;

  private static Random random;
  private static final Set<Integer> usedPorts = Collections.synchronizedSet(new HashSet<Integer>());

  static {
    random = new SecureRandom();
  }

  public static Integer nextUnusedPort(int offset) {
    int port = random.nextInt(PORT_RANGE) + offset;
    while (!isPortAvailable(port)) {
      port = random.nextInt(PORT_RANGE) + offset;
    }
    return port;
  }

  public static void release(Integer port) {
    usedPorts.remove(port);
  }

  private static boolean isPortAvailable(Integer port) {
    boolean result = false;
    if (usedPorts.contains(port)) {
      return result;
    }
    try {
      ServerSocket srv = new ServerSocket(port);
      srv.close();
      srv = null;
      result = true;
    }
    catch (IOException e) {
      result = false;
    }
    usedPorts.add(port);
    return result;
  }
}
