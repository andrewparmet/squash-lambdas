package com.parmet.squashlambdas.email;

import java.io.IOException;
import javax.mail.MessagingException;

interface IoMsgFunction<T, R> {
  R apply(T t) throws IOException, MessagingException;
}
