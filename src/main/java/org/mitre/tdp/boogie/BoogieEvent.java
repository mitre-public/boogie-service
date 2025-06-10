package org.mitre.tdp.boogie;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.springframework.context.ApplicationEvent;

import static java.util.Objects.requireNonNull;

public abstract class BoogieEvent extends ApplicationEvent {

  /**
   * An event indicating the application has detected a new 424 file on disk which other components of the system may want to get
   * access to and do something with.
   *
   * @param source              the source object which produced the new file event
   * @param fileContentProvider a supplier which can be used to "summon" the bytes of the new 424 file for processing
   * @return an event encapsulating the detection of a new file and a method for summoning its contents for processing
   */
  public static New424File new424File(Object source, Callable<InputStream> fileContentProvider) {
    return new New424File(source, fileContentProvider);
  }

  private final UUID uuid;

  protected BoogieEvent(Object source) {
    super(source);
    this.uuid = UUID.randomUUID();
  }

  public UUID uuid() {
    return uuid;
  }

  public static final class New424File extends BoogieEvent {

    private final Callable<InputStream> fileContentProvider;

    private New424File(Object source, Callable<InputStream> fileContentProvider) {
      super(source);
      this.fileContentProvider = requireNonNull(fileContentProvider);
    }

    /**
     * Note this method may throw whatever exception could be encountered by the delegated-to {@link Callable} whether loading
     * from the local FS or S3, etc.
     */
    public InputStream fileContents() throws Exception {
      return fileContentProvider.call();
    }
  }
}
