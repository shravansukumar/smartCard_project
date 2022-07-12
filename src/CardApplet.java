import java.nio.charset.StandardCharsets;
import javacard.security.RandomData;
import javacard.security.Signature;

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

    // Card details
    private short cardNumber;
    private short cardExpiry;
    private short cardPIN;
    private short bruteForceCounter;
    private short cardBalance;
    private byte cardState;
    private short randomNumber1;
    private Signature signature;

    private RandomData rng;
    private byte [] randomBuffer;

    OwnerPIN pin = new OwnerPIN((byte) 3, (byte) 6);

    // Card States
    private static final byte CARD_READY_TO_USE = (byte) 0x90;
    private static final byte CARD_BLOCKED = (byte) 0x91;

    private static byte POS_RELOAD_TERMINAL = (byte) 0x02;

    RSAPublicKey publicKey;
    RSAPrivateKey privateKey;
    RSAPublicKey terminaPublicKey;

    byte [] temporaryBuffer;
    byte [] cardTag;
    byte [] terminalMasterTag;
    byte [] terminalTag;

    // Init terminal stuff
    private static final byte INIT_PUB_EXP = (byte) 0x10;
    private static final byte INIT_PUB_MOD = (byte) 0x11;
    private static final byte INIT_PRV_EXP = (byte) 0x12;
    private static final byte INIT_PRV_MOD = (byte) 0x13;
    private static final byte INIT_CARD_NUMBER = (byte) 0x14;
    private static final byte INIT_CARD_EXPIRY = (byte) 0x15;
    private static final byte INIT_CARD_TAG = (byte) 0x16;
    private static final byte INIT_MASTER_TERMINAL_TAG = (byte) 0x17;
    private static final byte INIT_CARD_PIN = (byte) 0x18;

    // Mutual Auth stuff
    private static final byte MUTUAL_AUTH_RN = (byte) 0x19;
    private static final byte MUTUAL_AUTH_TERMINAL_TAG = (byte) 0x20;
    private static final byte MUTUAL_AUTH_TERMINAL_PUBLIC_KEY_EXPONENT = (byte) 0x21; 
    private static final byte MUTUAL_AUTH_TERMINAL_PUBLIC_KEY_MODULO = (byte) 0x22;  

    protected CardApplet() {
        register();

        temporaryBuffer = JCSystem.makeTransientByteArray((short) 256, JCSystem.CLEAR_ON_DESELECT);
        publicKey = (RSAPublicKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PUBLIC,KeyBuilder.LENGTH_RSA_512,false);
        privateKey = (RSAPrivateKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PRIVATE,KeyBuilder.LENGTH_RSA_512,false);
        terminaPublicKey = (RSAPublicKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PUBLIC,KeyBuilder.LENGTH_RSA_512,false);
        cardTag = JCSystem.makeTransientByteArray((short) 64, JCSystem.CLEAR_ON_DESELECT);
        terminalMasterTag = JCSystem.makeTransientByteArray((short) 64, JCSystem.CLEAR_ON_DESELECT);
        terminalTag = JCSystem.makeTransientByteArray((short) 64, JCSystem.CLEAR_ON_DESELECT);
        randomNumber1 = (short) 0;

        signature = Signature.getInstance(Signature.ALG_RSA_SHA_256_PKCS1, false);

        rng = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
        randomBuffer = JCSystem.makeTransientByteArray((short) 1, JCSystem.CLEAR_ON_DESELECT);
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
        short length = (short)(apduBuffer[ISO7816.OFFSET_LC] & 0x00FF);

        try {
            switch (apduBuffer[ISO7816.OFFSET_INS]) {
                case DUMMY:
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
                break;

                case INIT_MASTER_TERMINAL_TAG:
                handleIncomingAPDU(apdu, temporaryBuffer, (short) 0, length);
                Util.arrayCopy(temporaryBuffer, (short) 0, terminalMasterTag, (short) 0, length); 
                break;

                case INIT_CARD_PIN:                
                //setCardPIN(apdu);
                System.out.println("Got stuff for card pin");
                handleIncomingAPDU(apdu, temporaryBuffer, (short) 0, length);
                pin.update(temporaryBuffer, (short) 0, (byte) length);
                bruteForceCounter = (short) 0;
                cardBalance = (short) 0;
                cardState = CARD_READY_TO_USE;
                break;

                case MUTUAL_AUTH_RN:
                System.out.println("hello from mutual auth");
                randomNumber1 = Util.makeShort(apduBuffer[ISO7816.OFFSET_CDATA+1], apduBuffer[ISO7816.OFFSET_CDATA]);
                System.out.println(randomNumber1);
                break;

                case MUTUAL_AUTH_TERMINAL_TAG:
                handleIncomingAPDU(apdu, temporaryBuffer, (short) 0, length);
                Util.arrayCopy(temporaryBuffer, (short) 0, terminalTag, (short) 0, length); 
                break;

                case MUTUAL_AUTH_TERMINAL_PUBLIC_KEY_EXPONENT:
                handleIncomingAPDU(apdu, temporaryBuffer, (short) 0, length);
                terminaPublicKey.setExponent(temporaryBuffer, (short) 0, length); 
                break;

                case MUTUAL_AUTH_TERMINAL_PUBLIC_KEY_MODULO:
                handleIncomingAPDU(apdu, temporaryBuffer, (short) 0, length);
                terminaPublicKey.setModulus(temporaryBuffer, (short) 0, length);
                generateRandomNumber();
                // Verify terminal tag with master terminal public key
                verifyTerminalCerts();
                System.out.println(randomBuffer);
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

    private void generateRandomNumber() {
        rng.generateData(randomBuffer, (short) 0, (short)1);
    }

    private void verifyTerminalCerts() {
        byte[] terminalCert = JCSystem.makeTransientByteArray((short) 100, JCSystem.CLEAR_ON_RESET);
        terminalCert[0] = POS_RELOAD_TERMINAL;
        terminalCert[1] = (short) 2;
        Util.arrayCopy(terminaPublicKey.get, 0, terminalCert, (short) 2, terminaPublicKey.getSize());


    }

    private void handleIncomingBytesToShort(APDU apdu, short destination) {
        byte [] buffer = apdu.getBuffer();
        destination = Util.makeShort(buffer[ISO7816.OFFSET_CDATA+1], buffer[ISO7816.OFFSET_CDATA]);
    }

    private void setCardPIN(APDU apdu) {
        byte [] buffer = apdu.getBuffer();
        cardPIN = Util.makeShort(buffer[ISO7816.OFFSET_CDATA+1], buffer[ISO7816.OFFSET_CDATA]);
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
