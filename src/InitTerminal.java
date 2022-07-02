import java.security.KeyPair;
import java.security.*;

public class InitTerminal {
    public TerminalMasterKeyCerts terminalMasterKeyCerts = TerminalMasterKeyCerts.getInstance();
    private KeyGenerator keyGenerator = new KeyGenerator();
    private SignAndVerify signAndVerify = new SignAndVerify();
    private KeyPair cardKeyPair;
    private short cardNumber = (short)123456789;
    private short cardExpiry = (short)1030;
    byte [] cardCertificate = new byte[256];
    private static byte CARD = (byte)0x00;

    public KeyPair generateKeyPairForCard() {    
        try {
            cardKeyPair = keyGenerator.getKeyPair();
        } catch (Exception exc) {
            System.out.println(exc.getMessage());   
        }
        return cardKeyPair;
    }

    public void generateCardCerts() {
        byte [] valuesToBeSigned = new byte[20];
        byte [] publicKeyInBytes = cardKeyPair.getPublic().getEncoded();
        valuesToBeSigned[0] = (byte)cardNumber;
        valuesToBeSigned[1] = (byte) cardExpiry;
        valuesToBeSigned[2] = CARD;
        System.arraycopy(publicKeyInBytes, 0, valuesToBeSigned, 3, valuesToBeSigned.length);
        try {
            byte [] tag = signAndVerify.sign(terminalMasterKeyCerts.masterPrivateKey, valuesToBeSigned);
            System.arraycopy(tag, 0, cardCertificate, 0, tag.length);
            System.arraycopy(valuesToBeSigned, 0, cardCertificate, tag.length+1, valuesToBeSigned.length);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

}
