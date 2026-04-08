package com.incognia.api.clients;

import com.incognia.common.Token;
import com.incognia.common.exceptions.IncogniaException;

public interface TokenProvider {
  Token getToken() throws IncogniaException;
}
