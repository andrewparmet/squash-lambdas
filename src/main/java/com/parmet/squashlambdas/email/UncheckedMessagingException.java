package com.parmet.squashlambdas.email;

import javax.mail.MessagingException;

class UncheckedMessagingException extends RuntimeException {
  public UncheckedMessagingException(MessagingException ex) {
    super(ex);
  }

  private static final long serialVersionUID = 1L;
}
