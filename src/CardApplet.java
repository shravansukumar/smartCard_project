import java.nio.charset.StandardCharsets;

import javacard.framework.*;
import javacard.framework.Applet;



/*
 * Things to be persisted: balance, bruteForceCunter, PIN, state
 * Certificate: number, public key, expiry, cert_type: (card, terminal)
 * 
 */

public class CardApplet extends Applet {
    private static final byte DUMMY = (byte) 0x52;
    private byte [] test3;

    protected CardApplet() {
        register();
        test3 = JCSystem.makeTransientByteArray((short) 256, JCSystem.CLEAR_ON_DESELECT);
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) throws SystemException {
        new CardApplet();
    }

    @Override
    public boolean select() {
        // To choose this applet!
        return true;
    }

    @Override
    public void deselect() {
        // Do anything to clear stuff
    }

    @Override
    public void process(APDU apdu) throws ISOException {
        byte[] apduBuffer = apdu.getBuffer();
        try {
            switch (apduBuffer[ISO7816.OFFSET_INS]) {
                case DUMMY:
                    // readBuffer(apdu);
                    readBuffer(apdu, test3, (short)0, (short)5);
                    System.out.println("Dummy message received!!");
                    //short len = apdu.setIncomingAndReceive();
                    //apdu.setOutgoingAndSend((short)99, (short)4);
                   // apdu.setOutgoing();
                    //apdu.sendBytesLong("outData".getBytes(), (short)4, (short)11);
                    //("outData".getBytes(), (short)0, (short) 7);
                default:
                    System.out.println(apduBuffer[ISO7816.OFFSET_INS]);
            }
        } catch (ISOException expet) {
            System.out.println(expet.toString());
        }
    }

    private void readBuffer(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byte numBytes = buffer[ISO7816.OFFSET_LC];
        byte byteRead = (byte) (apdu.setIncomingAndReceive());
        if (byteRead != 5) {
            System.out.println("something");
        }
        short i = 0;
        byte[] testMessage;
        while (i < numBytes) {

        }

        byte[] message ;
        //String newMsg = new String(message, StandardCharsets.UTF_8);

       // System.out.println("" + message);
    }

    private void readBuffer(APDU apdu, byte[] dest, short offset, short length) {
        byte[] buffer = apdu.getBuffer();
        short readCount = apdu.setIncomingAndReceive();
        short i = 0;
        Util.arrayCopy(buffer, ISO7816.OFFSET_CDATA, dest, offset, readCount);
        String random = new String(test3,StandardCharsets.UTF_8);
        //System.out.println(random);
//        apdu.setOutgoingAndSend((short) 999,(short) 3);
        // while (i <= length) {
        //     i += readCount;
        //     offset += readCount;
        //     readCount = (short) apdu.receiveBytes(ISO7816.OFFSET_CDATA);
        //     Util.arrayCopy(buffer, ISO7816.OFFSET_CDATA, dest, offset, readCount);
        // }
       // String random2 = new String(test3, StandardCharsets.UTF_8); 
        //System.out.println(random2);
    }
}
