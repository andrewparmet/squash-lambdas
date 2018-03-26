package com.parmet.squashlambdas;

import java.io.IOException;
import javax.mail.MessagingException;

public interface IoMsgSupplier<R> {
  R get() throws IOException, MessagingException, Exception;
}
