import java.nio.charset.StandardCharsets;

import javax.print.attribute.standard.MediaSize.ISO;
import javax.smartcardio.CommandAPDU;

import javacard.framework.*;
import javacard.framework.Applet;
import javacard.security.*;
import javacardx.crypto.*;
/*
 * Things to be persisted: balance, bruteForceCunter, PIN, state
 * Certificate: number, public key, expiry, cert_type: (card, terminal)
 * 
 */

public class CardApplet extends Applet {
    private static final byte DUMMY = (byte) 0x52;
    private static final byte INIT = (byte) 0x55;
    private byte [] test3;

    private short cardNumber;
    private short cardExpiry;

    RSAPublicKey publicKey;
    RSAPrivateKey privateKey;

    byte [] temporaryBuffer;
    byte [] cardTag;
    byte [] terminalMasterTag;

    private static final byte INIT_PUB_EXP = (byte) 0x10;
    private static final byte INIT_PUB_MOD = (byte) 0x11;
    private static final byte INIT_PRV_EXP = (byte) 0x12;
    private static final byte INIT_PRV_MOD = (byte) 0x13;
    private static final byte INIT_CARD_NUMBER = (byte) 0x14;
    private static final byte INIT_CARD_EXPIRY = (byte) 0x15;
    private static final byte INIT_CARD_TAG = (byte) 0x16;
    private static final byte INIT_TERMINAL_TAG = (byte) 0x17;
    private static final byte INIT_CARD_PIN = (byte) 0x18;

    protected CardApplet() {
        register();
        temporaryBuffer = JCSystem.makeTransientByteArray((short) 256, JCSystem.CLEAR_ON_DESELECT);
        publicKey = (RSAPublicKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PUBLIC,KeyBuilder.LENGTH_RSA_512,false);
        privateKey = (RSAPrivateKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PRIVATE,KeyBuilder.LENGTH_RSA_512,false);
        cardTag = JCSystem.makeTransientByteArray((short) 64, JCSystem.CLEAR_ON_DESELECT);
        terminalMasterTag = JCSystem.makeTransientByteArray((short) 64, JCSystem.CLEAR_ON_DESELECT);
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) throws SystemException {
       new CardApplet();

       // (new CardApplet()).register(bArray, (short)(bOffset + 1), bArray[bOffset]);
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
        short length = (short)(apduBuffer[ISO7816.OFFSET_LC] & 0x00FF);

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
                    break;
                case INIT:
                   // readBuffer(apdu, dest, offset, length);
                    System.out.println("Got stuff from terminal with INIT");
                    break;

                case INIT_PUB_EXP:
                System.out.println("Got the public exponent");
                handleIncomingAPDU(apdu, temporaryBuffer, (short) 0, length);
                publicKey.setExponent(temporaryBuffer, (short) 0, length);
                break;

                case INIT_PUB_MOD:
                handleIncomingAPDU(apdu, temporaryBuffer, (short) 0, length);
                publicKey.setModulus(temporaryBuffer, (short) 0, length);
                System.out.println("Got the public mod"); 
                break;

                case INIT_PRV_EXP:
                handleIncomingAPDU(apdu, temporaryBuffer, (short) 0, length);
                privateKey.setExponent(temporaryBuffer, (short) 0, length);
                System.out.println("Got the priv exp");
                break;

                case INIT_PRV_MOD:
                handleIncomingAPDU(apdu, temporaryBuffer, (short) 0, length);
                privateKey.setModulus(temporaryBuffer, (short) 0, length);
                System.out.println("Got the priv mod");
                break;

                case INIT_CARD_NUMBER:
                getCardNumber(apdu);
                System.out.println(cardNumber);
                System.out.println("Got the card number");
                break;

                case INIT_CARD_EXPIRY:
                getCardExpiry(apdu);
                System.out.println(cardExpiry);
                System.out.println("Got the card expiry");
                break;

                case INIT_CARD_TAG:
                handleIncomingAPDU(apdu, temporaryBuffer, (short) 0, length);
                Util.arrayCopy(temporaryBuffer, (short) 0, cardTag, (short) 0, length);

                case INIT_TERMINAL_TAG:
                handleIncomingAPDU(apdu, temporaryBuffer, (short) 0, length);
                Util.arrayCopy(temporaryBuffer, (short) 0, terminalMasterTag, (short) 0, length); 

                case INIT_CARD_PIN:
                setCardPIN(apdu);
                break;
                
                default:
                    System.out.println(apduBuffer[ISO7816.OFFSET_INS]);
                    break;
            }
        } catch (ISOException expet) {
            System.out.println(expet.toString());
        }
    }

    private void handleIncomingAPDU(APDU apdu, byte [] destination, short offset,short length) {
        byte [] buffer = apdu.getBuffer();
        short readCount = apdu.setIncomingAndReceive();
        short i = 0;
        Util.arrayCopy(buffer, ISO7816.OFFSET_CDATA, destination, offset, length);
    }

    private void getCardNumber(APDU apdu) {
        byte [] buffer = apdu.getBuffer();
        cardNumber = Util.makeShort(buffer[ISO7816.OFFSET_CDATA+1], buffer[ISO7816.OFFSET_CDATA]);
        //cardExpiry = (short) buffer[ISO7816.OFFSET_CDATA+1]; 
        //Util.arrayCopy(buffer, ISO7816.OFFSET_CDATA, cardNumber, (short) 0, (short) 1);
    }

    private void getCardExpiry(APDU apdu) {
        byte [] buffer = apdu.getBuffer();
        cardExpiry = Util.makeShort(buffer[ISO7816.OFFSET_CDATA+1], buffer[ISO7816.OFFSET_CDATA]);
        //cardExpiry = (short) buffer[ISO7816.OFFSET_CDATA+1]; 
        //Util.arrayCopy(buffer, ISO7816.OFFSET_CDATA, cardNumber, (short) 0, (short) 1);
    }

    private void setCardPIN(APDU apdu) {

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
