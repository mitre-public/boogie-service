package org.mitre.tdp.boogie.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;

import org.mitre.caasd.commons.fileutil.FileUtils;

import static java.util.Objects.requireNonNull;

final class FileInputStreamProvider implements Callable<InputStream> {

  private final File file;

  FileInputStreamProvider(File file) {
    this.file = requireNonNull(file);
  }

  @Override
  public InputStream call() throws Exception {
    return FileUtils.isGZipFile(file)
        ? new GZIPInputStream(new FileInputStream(file))
        : new FileInputStream(file);
  }
}
