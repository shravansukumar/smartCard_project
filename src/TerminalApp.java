import com.licel.jcardsim.smartcardio.CardSimulator;
import com.licel.jcardsim.utils.AIDUtil;
import javacard.framework.AID;
import javacard.framework.JCSystem;
//import src.main.java.applet.SecureChannelApplet;
import javacard.framework.SystemException;
import javacard.security.*;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import java.security.SecureRandom;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.interfaces.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class TerminalApp {

    private CardSimulator simulator;

    private RSAPrivateKey masterPrivateKey;
    private RSAPublicKey masterPublicKey;
    private SignAndVerify s;
    private String root_cert;

    private static byte INIT_TERMINAL = (byte)0x01;
    private static byte POS_TERMINAL = (byte)0x02;
    private static byte RELOAD_TERMINAL = (byte)0x03;
    private static byte CARD = (byte)0x00;
    private State state = State.Init;

    private static final byte DUMMY = (byte) 0x52;
    //private static final String APPLET_AID = "12345678912345678900";
    final static byte[] pin = {'1', '2', '3', '4'}; 
    static final byte[] APPLET_AID = { (byte) 0x3B, (byte) 0x29,
        (byte) 0x63, (byte) 0x61, (byte) 0x6C, (byte) 0x63, (byte) 0x01 };

    public TerminalApp() {
        KeyGenerator generator = new KeyGenerator();
        try {
            KeyPair keyPair = generator.getKeyPair();
            masterPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
            masterPublicKey = (RSAPublicKey) keyPair.getPublic();
            String message = INIT_TERMINAL +";"+ masterPublicKey+";0;None;"; 
            // format of cerificate (type,publicKey, ID,expiry)
            s= new SignAndVerify();
            //byte [] cert_tag = s.sign(masterPrivateKey,message);
            //root_cert=message+cert_tag;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }    
    }

    private void runApp() {
        switch (state) {
            case Init:
                break;
            case POS:
                break;
            case Reload:
                break;
            case Other:
                break;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("**************** Entering main **************");  
        TerminalApp terminalApp = new TerminalApp();  
        System.out.println(terminalApp.root_cert);
        terminalApp.run();
        terminalApp.runApp();
        terminalApp.sendDummyMessage();
    }

    private void run() {
        System.out.println("***************** Entering run ****************");
        simulator = new CardSimulator();
        AID appletAID = new AID(APPLET_AID, (byte)0, (byte)7); //AIDUtil.create(APPLET_AID);
        System.out.println(appletAID);
        try {
            simulator.installApplet(appletAID, CardApplet.class, pin, (short)0, (byte)0);
            simulator.selectApplet(appletAID);
        } catch (SystemException e) {
            e.printStackTrace();
        }
    }

    private void encryptWith(Key keyToBeUsed, int operation) {
        try {
            Cipher rsaCipher = Cipher.getInstance("RSA");
            rsaCipher.init(operation, masterPublicKey);
            byte [] encryptedMessage = JCSystem.makeTransientByteArray((short) 256, JCSystem.CLEAR_ON_DESELECT);
            //rsaCipher.doFinal(input, inputOffset, inputLen, output, outputOffset);
        } catch (NoSuchAlgorithmException |  NoSuchPaddingException | InvalidKeyException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    private void sendDummyMessage() {
        System.out.println("***************** Entering send block ****************"); 
        System.out.println("Terminal starting process");
        String dummyHelloWorld = "Hello";
        short test = 99;
        byte [] test1 = dummyHelloWorld.getBytes(); 
        CommandAPDU command = new CommandAPDU(10,DUMMY,10,10,test1);
        ResponseAPDU response =  transmit(command);
        byte [] datareceived = response.getData();
        System.out.println(datareceived.length);
    }

    //private byte[] encrypty(byte[] data) 
      //  throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        //    return sessionEncrypt.doFinal(data);
        //}
    

    private ResponseAPDU transmit(CommandAPDU commandAPDU) {
        System.out.println("Terminal: Sending Command");
        ResponseAPDU response = simulator.transmitCommand(commandAPDU);
        System.out.println(response);
        return response;
    }
}