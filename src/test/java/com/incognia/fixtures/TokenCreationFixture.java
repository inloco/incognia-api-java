package com.incognia.fixtures;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import lombok.SneakyThrows;

public class TokenCreationFixture {
  @SneakyThrows
  public static String createToken() {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(1024);
    KeyPair keyPair = keyGen.generateKeyPair();
    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
    RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
    Algorithm algorithm = Algorithm.RSA256(publicKey, privateKey);
    return JWT.create()
        .withIssuer("incognia")
        .withExpiresAt(Date.from(Instant.now().plusSeconds(100)))
        .sign(algorithm);
  }
}
