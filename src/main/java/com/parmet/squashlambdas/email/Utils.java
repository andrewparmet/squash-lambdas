package com.parmet.squashlambdas.email;

import static com.google.common.base.Throwables.throwIfUnchecked;

import java.io.IOException;
import java.io.UncheckedIOException;
import javax.mail.MessagingException;

class Utils {
  public static <T, R> R wrap(IoMsgSupplier<R> sup) {
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
