package com.datish.copycat;

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.NettyTransport;
import io.atomix.copycat.server.CopycatServer;
import io.atomix.copycat.server.storage.Storage;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Value state machine example.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class ValueStateMachineExample {

  /**
   * Starts the server.
   */
  public static void main(String[] args) throws Exception {
    if (args.length < 2)
      throw new IllegalArgumentException("must supply a path and set of host:port tuples");

    // Parse the address to which to bind the server.
    String[] mainParts = args[1].split(":");
    Address address = new Address(mainParts[0], Integer.valueOf(mainParts[1]));

    // Build a list of all member addresses to which to connect.
    List<Address> members = new ArrayList<>();
    for (int i = 1; i < args.length; i++) {
      String[] parts = args[i].split(":");
      members.add(new Address(parts[0], Integer.valueOf(parts[1])));
    }

    CopycatServer server = CopycatServer.builder(address)
      .withStateMachine(ValueStateMachine::new)
      .withTransport(new NettyTransport())
      .withStorage(Storage.builder()
        .withDirectory(args[0])
        .withMaxSegmentSize(1024 * 1024 * 32)
        .withMinorCompactionInterval(Duration.ofMinutes(1))
        .withMajorCompactionInterval(Duration.ofMinutes(15))
        .build())
      .build();

    server.serializer().register(SetCommand.class, 1);
    server.serializer().register(GetQuery.class, 2);
    server.serializer().register(DeleteCommand.class, 3);

    server.bootstrap(members).join();
    while (server.isRunning()) {
      Thread.sleep(1000);
    }
  }

}
