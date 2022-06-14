import com.licel.jcardsim.smartcardio.CardSimulator;
import com.licel.jcardsim.utils.AIDUtil;
import javacard.framework.AID;
//import src.main.java.applet.SecureChannelApplet;
import javacard.framework.SystemException;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import java.security.SecureRandom;


public class TerminalApp {

    private CardSimulator simulator;

    private static final byte CLA_SECURECHANNEL = (byte) 0xB0;
    private static final byte INS_CRYPTOGRAM = (byte) 0x51;
    private static final byte DUMMY = (byte) 0x52;
    //private static final String APPLET_AID = "12345678912345678900";
    final static byte[] pin = {'1', '2', '3', '4'}; 
    static final byte[] APPLET_AID = { (byte) 0x3B, (byte) 0x29,
        (byte) 0x63, (byte) 0x61, (byte) 0x6C, (byte) 0x63, (byte) 0x01 };

    public static void main(String[] args) throws Exception {
        System.out.println("**************** Entering main **************");
        try {
            TerminalApp terminalApp = new TerminalApp();  
            terminalApp.run();
            terminalApp.sendDummyMessage();
        } catch (Exception e) {
            e.getLocalizedMessage();
        }
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

    private void sendDummyMessage() {
        System.out.println("***************** Entering send block ****************"); 
        System.out.println("Terminal starting process");
        String dummyHelloWorld = "Hello";
        CommandAPDU command = new CommandAPDU(0,DUMMY,0,0,dummyHelloWorld.getBytes());
        ResponseAPDU response =  transmit(command);
        //System.out.println(response);
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
