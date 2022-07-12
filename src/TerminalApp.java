
import com.licel.jcardsim.smartcardio.CardSimulator;

import javacard.framework.AID;

import javacard.framework.SystemException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

public class TerminalApp {

    private CardSimulator simulator;
    public TerminalMasterKeyCerts terminalMasterKeyCerts;
    private Utils utils;

    private State state = State.Init;

    private static final byte DUMMY = (byte) 0x52;

    private static final byte INIT_PUB_EXP = (byte) 0x10;
    private static final byte INIT_PUB_MOD = (byte) 0x11;
    private static final byte INIT_PRV_EXP = (byte) 0x12;
    private static final byte INIT_PRV_MOD = (byte) 0x13;
    private static final byte INIT_CARD_NUMBER = (byte) 0x14;
    private static final byte INIT_CARD_EXPIRY = (byte) 0x15;
    private static final byte INIT_CARD_TAG = (byte) 0x16;
    private static final byte INIT_MASTER_TERMINAL_TAG = (byte) 0x17;
    private static final byte INIT_CARD_PIN = (byte) 0x18;

    // Mutual auth stuff
    private static final byte MUTUAL_AUTH_RN = (byte) 0x19;
    private static final byte MUTUAL_AUTH_TERMINAL_TAG = (byte) 0x20;
    private static final byte MUTUAL_AUTH_TERMINAL_PUBLIC_KEY_EXPONENT = (byte) 0x21; 
    private static final byte MUTUAL_AUTH_TERMINAL_PUBLIC_KEY_MODULO = (byte) 0x22;  
     


    final static byte[] pin = {'1', '2', '3', '4'}; 
    static final byte[] APPLET_AID = { (byte) 0x3B, (byte) 0x29,
        (byte) 0x63, (byte) 0x61, (byte) 0x6C, (byte) 0x63, (byte) 0x01 };

    public TerminalApp() {
        terminalMasterKeyCerts = TerminalMasterKeyCerts.getInstance();
        utils = new Utils();
    }

    private void runApp() {
        switch (state) {
            case Init:
                InitTerminal initTerminal = new InitTerminal();
                initTerminal.generateKeyPairForCard();
                initTerminal.generateCardCerts();

                //Card public key exponent
                prepareAndSendData(initTerminal.getCardPublicKeyExponent(), INIT_PUB_EXP);

                //Card Public key modulo
                prepareAndSendData(initTerminal.getCardPublicKeyModulo(), INIT_PUB_MOD); 

                //Card Private key exponent
                prepareAndSendData(initTerminal.getCardPrivateKeyExponent(), INIT_PRV_EXP);

                //Card Private key modulo
                prepareAndSendData(initTerminal.getCardPrivateKeyModulo(), INIT_PRV_MOD);

                //Card Number
                prepareAndSendData(initTerminal.getBytesForShort(initTerminal.cardNumber), INIT_CARD_NUMBER);

                // Card Expiry
                prepareAndSendData(initTerminal.getBytesForShort(initTerminal.cardExpiry), INIT_CARD_EXPIRY);

                // Terminal master tag
                prepareAndSendData(terminalMasterKeyCerts.masterTerminalTag, INIT_MASTER_TERMINAL_TAG);

                // Card tag
                prepareAndSendData(initTerminal.cardTag, INIT_CARD_TAG);

                // Card PIN, balance, brute_force_counter
                prepareAndSendData(initTerminal.getBytesForShort(initTerminal.cardPIN), INIT_CARD_PIN);
                // TODO: ANKIT stash in backend..
                state = State.POS;
                runApp();
                break;

            case POS:
                MutualAuthenticator mutualAuthenticator = new MutualAuthenticator();
                // Random Number 1
                short randomNumber = mutualAuthenticator.generateRandomNumber();
                prepareAndSendData(utils.getBytesForShort(randomNumber), MUTUAL_AUTH_RN);

                // Terminal Tag
                prepareAndSendData(terminalMasterKeyCerts.terminalTag, MUTUAL_AUTH_TERMINAL_TAG);

                // Terminal public key exponent
                prepareAndSendData(terminalMasterKeyCerts.getTerminalPublicKeyExponent(), MUTUAL_AUTH_TERMINAL_PUBLIC_KEY_EXPONENT);

                // Terminal public key modulo
                prepareAndSendData(terminalMasterKeyCerts.getTerminalPublicModulo(), MUTUAL_AUTH_TERMINAL_PUBLIC_KEY_MODULO);
                break;

            case Reload:
                break;
            case Other:
                break;
        }
    }

    public static void main(String[] args) throws Exception {
        // First point of entry
        TerminalApp terminalApp = new TerminalApp();  
        terminalApp.run();
        terminalApp.runApp();
        //terminalApp.sendDummyMessage();
    }

    private void run() {
        System.out.println("***************** Entering run ****************");
        simulator = new CardSimulator();
        AID appletAID = new AID(APPLET_AID, (byte)0, (byte)7); //AIDUtil.create(APPLET_AID);
        try {
            simulator.installApplet(appletAID, CardApplet.class, pin, (short)0, (byte)0);
            simulator.selectApplet(appletAID);
        } catch (SystemException e) {
            e.printStackTrace();
        }
    }

    private void prepareAndSendData(byte [] data, byte commandType) {
        System.out.println("size of blocks: "+ data.length);
        CommandAPDU initCommand = new CommandAPDU(0, commandType, 0, 0, data);
        ResponseAPDU initResponse = transmit(initCommand);
        System.out.println(initResponse);
    }

    // private void encryptWith(Key keyToBeUsed, int operation) {
    //     try {
    //         Cipher rsaCipher = Cipher.getInstance("RSA");
    //         rsaCipher.init(operation, masterPrivateKey);
    //         byte [] encryptedMessage = JCSystem.makeTransientByteArray((short) 256, JCSystem.CLEAR_ON_DESELECT);
    //         //rsaCipher.doFinal(input, inputOffset, inputLen, output, outputOffset);
    //     } catch (NoSuchAlgorithmException |  NoSuchPaddingException | InvalidKeyException e) {
    //         System.out.println(e.getMessage());
    //         System.exit(-1);
    //     }
    // }

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

    private ResponseAPDU transmit(CommandAPDU commandAPDU) {
        System.out.println("Terminal: Sending Command");
        ResponseAPDU response = simulator.transmitCommand(commandAPDU);
        return response;
    }
}