import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.*;

public class TerminalMasterKeyCerts {
    // master terminal keypair
    public RSAPrivateKey masterPrivateKey;
    public RSAPublicKey masterPublicKey;

    // reload/POS terminal keypair
    public RSAPrivateKey terminalPrivateKey;
    public RSAPublicKey terminalPublicKey;

    private KeyGenerator keyGenerator;
    private SignAndVerify signAndVerify;

    private static TerminalMasterKeyCerts instance = null;

    private static byte INIT_TERMINAL = (byte)0x01;
    private static byte POS_RELOAD_TERMINAL = (byte) 0x02;

    byte [] certificate = new byte[1000];
    byte [] masterTerminalTag = new byte[256];
    byte [] otherDetails = new byte[2];
    byte [] terminalOtherDetails = new byte[2];
    byte [] terminalTag = new byte [256];

    private TerminalMasterKeyCerts() {
        keyGenerator = new KeyGenerator();
        signAndVerify = new SignAndVerify();
        // master key pair and certs
        generateMasterKeyPairs();
        generateMasterTerminalCerts();
        // terminal key pair and certs
        generateTerminalKeyPairs();
        generateTerminalCerts();
    }

    // This is for the master terminal
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

    // This is for the reload/POS terminal 
    /* :::DANGER ZONE:::
     * Please do not get confused between the master terminal keypairs/certs and the reload/POS key pair/certs.
     * They are different entities. We use the master terminal for signing the subsequent keys/certs.
     * I know, its confusing af!
     */
    private void generateTerminalKeyPairs() {
        try {
            KeyPair terminalKeyPair = keyGenerator.getKeyPair();
            terminalPrivateKey = (RSAPrivateKey) terminalKeyPair.getPrivate();
            terminalPublicKey = (RSAPublicKey) terminalKeyPair.getPublic();
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
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    private void generateTerminalCerts() {
        byte [] terminalValuesToBeSigned = new byte [256];
        terminalValuesToBeSigned[0] = (short) 2;
        terminalValuesToBeSigned[1] = POS_RELOAD_TERMINAL;
        System.arraycopy(terminalValuesToBeSigned, 0, terminalOtherDetails, 0, 2);
        System.arraycopy(terminalPublicKey, 0, terminalValuesToBeSigned, 2, terminalPublicKey.getEncoded().length);
        try {
            terminalTag = signAndVerify.sign(masterPrivateKey, terminalValuesToBeSigned);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    public byte [] getTerminalPublicKeyExponent() {
        return terminalPublicKey.getPublicExponent().toByteArray();
    }

    public byte [] getTerminalPublicModulo() {
        return terminalPublicKey.getModulus().toByteArray();
    }
    
    public static TerminalMasterKeyCerts getInstance() {
        if (instance == null) {
            instance = new TerminalMasterKeyCerts();
        }
        return instance;
    }
}
