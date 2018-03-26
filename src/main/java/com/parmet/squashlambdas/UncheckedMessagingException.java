package com.parmet.squashlambdas;

import javax.mail.MessagingException;

public class UncheckedMessagingException extends RuntimeException {
  public UncheckedMessagingException(MessagingException ex) {
    super(ex);
  }

  private static final long serialVersionUID = 1L;
}
