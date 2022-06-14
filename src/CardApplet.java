import javacard.framework.*;
import javacard.framework.Applet;

public class CardApplet extends Applet {

    private static final byte DUMMY = (byte) 0x52;

    protected CardApplet() {
        register();
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
             System.out.println("Dummy message received!!");
             break;
             default:
             System.out.println("Something went wrong"); 
        }
       } catch (ISOException expet) {
           System.out.println(expet.toString());
       } 
    }
}
