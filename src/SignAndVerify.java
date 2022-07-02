import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class SignAndVerify {
    public byte [] sign(RSAPrivateKey key, byte [] message) throws  NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        byte [] signedBytes;
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(key);
            sign.update(message);
            byte [] signedStuff = sign.sign();
            System.out.println("Signed " + signedStuff.toString());
            signedBytes = signedStuff;
        return signedBytes;
    }

    public void verify(RSAPublicKey publicKey, byte [] sign, String message) {
        try {
            Signature verifySign = Signature.getInstance("SHA256withRSA");
            verifySign.initVerify(publicKey);
            verifySign.update(message.getBytes());
            boolean isSignatureVaid = verifySign.verify(sign);
            if (isSignatureVaid) {
                System.out.println("signature is valid");
            } else {
                System.out.println("signature is NOT valid");
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
    }
}
