import java.io.*;
import java.math.BigInteger;

import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;

public class KeyGenerator {

    public KeyPair getKeyPair() throws Exception, NoSuchAlgorithmException {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(1024);
            KeyPair keypair = generator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey)keypair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey)keypair.getPrivate();
            printStuff(publicKey);
            printStuff(privateKey);
        return keypair;
    }

    private void printStuff(Key key) {
        System.out.println(key);
    }
}
