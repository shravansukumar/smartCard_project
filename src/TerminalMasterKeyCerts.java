import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.*;

public class TerminalMasterKeyCerts {
    public RSAPrivateKey masterPrivateKey;
    public RSAPublicKey masterPublicKey;
    //public byte [] masterTerminalCert = new byte [256];
    private KeyGenerator keyGenerator;
    private SignAndVerify signAndVerify;
    private static TerminalMasterKeyCerts instance = null;
    private static byte INIT_TERMINAL = (byte)0x01;
    byte [] certificate = new byte[1000];
    byte [] masterTerminalTag = new byte[256];
    byte [] otherDetails = new byte[2];


    private TerminalMasterKeyCerts() {
        keyGenerator = new KeyGenerator();
        signAndVerify = new SignAndVerify();
        generateMasterKeyPairs();
        generateMasterTerminalCerts();
    }

    private void generateMasterKeyPairs() {
        try {
            KeyPair keyPair = keyGenerator.getKeyPair();
            masterPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
            masterPublicKey = (RSAPublicKey) keyPair.getPublic();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    private void generateMasterTerminalCerts() {
        byte [] valuesToBeSigned = new byte[256];
        valuesToBeSigned[0] = (short)1;
        valuesToBeSigned[1] = INIT_TERMINAL;
        System.arraycopy(valuesToBeSigned, 0, otherDetails, 0, 2);
        System.arraycopy(masterPublicKey.getEncoded(), 0, valuesToBeSigned, 2, masterPublicKey.getEncoded().length);
        try {
            masterTerminalTag = signAndVerify.sign(masterPrivateKey, valuesToBeSigned);
          // byte [] tag = signAndVerify.sign(masterPrivateKey, valuesToBeSigned);
           //System.arraycopy(tag, 0, certificate, 0, tag.length);
           //System.arraycopy(valuesToBeSigned, 0,certificate, tag.length+1, tag.length);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }
    
    public static TerminalMasterKeyCerts getInstance() {
        if (instance == null) {
            instance = new TerminalMasterKeyCerts();
        }
        return instance;
    }
}
