package ru.lessons.my.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Getter
@Setter
@Component
public class RsaKeyProperties {

    @Value("${rsa.public_key}")
    private RSAPublicKey publicKey;
    @Value("${rsa.private_key}")
    private RSAPrivateKey privateKey;
}
