package com.parmet.squashlambdas.email;

import java.io.IOException;
import javax.mail.MessagingException;

interface IoMsgSupplier<R> {
  R get() throws IOException, MessagingException, Exception;
}
