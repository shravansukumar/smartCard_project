import javacard.framework.*;

public class CardApplet extends Applet {

    private static final byte DUMMY = (byte) 0x52;

    protected CardApplet() {
        
        register();
    }

    @Override
    public boolean select() {
        // To choose this applet! This could change..
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
             default:
             System.out.println("Something went wrong"); 
        }
       } catch (ISOException expet) {
           System.out.println(expet.toString());
       } 
    }
}
