package com.parmet.squashlambdas;

import java.io.IOException;
import javax.mail.MessagingException;

public interface IoMsgFunction<T, R> {
  R apply(T t) throws IOException, MessagingException;
}
