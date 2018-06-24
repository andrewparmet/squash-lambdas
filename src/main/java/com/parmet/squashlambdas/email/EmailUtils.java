package com.parmet.squashlambdas.email;

import static com.google.common.base.Throwables.throwIfUnchecked;

import java.io.IOException;
import java.io.UncheckedIOException;
import javax.mail.MessagingException;

class EmailUtils {
  interface IoMsgFunction<T, R> {
    R apply(T t) throws IOException, MessagingException;
  }

  interface IoMsgSupplier<R> {
    R get() throws IOException, MessagingException;
  }

  public static <R> R get(IoMsgSupplier<R> sup) {
    try {
      return sup.get();
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    } catch (MessagingException ex) {
      throw new UncheckedMessagingException(ex);
    } catch (Exception ex) {
      throwIfUnchecked(ex);
      throw new RuntimeException(ex);
    }
  }
}
