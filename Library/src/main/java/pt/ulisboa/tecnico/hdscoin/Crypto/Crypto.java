package pt.ulisboa.tecnico.hdscoin.Crypto;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.security.*;

public class Crypto {

    public static final String OTHER_MANAGER_PUB_KEY = "30820122300d06092a864886f70d01010105000382010f003082010a02820101008c360d4883c690da318613a8ecadf70b93c2e1f823e87eb418b3a120cada12f87e0d43ab6d6484143815d94d5b7565b95531f0e5418746f8d40c219d98ad39529feccb98e8bd18c858668696cdcbf7eb72cfe18daf7abd592898d98757f9069ca0cafc3415bab87f6c338fbb4504fbcbaa7271cd9c3a9131d119aeb2b3f418d37cee4a859885cb08284144f2934e6a20d3ba3a2912b3c944c3b61c97f9a39e4395b0252f162c873077148f4bdcf562fb1a5f3e2395d8c0f1d0bbc9709d291b66427748709434a0cb66e702e9c77006d8ddd5a7461ee135e6a30bf26defc668dcef0fbf50517c625cc0b3e5cc659f8dd6c8a1f0eb6b49aa9d44268c973f79d43d0203010001";
    public static final String MANAGER_PUB_KEY = "30820122300d06092a864886f70d01010105000382010f003082010a0282010100c105187797a1ce79087657d825796562b2143fb7a4f8fd829996ede398f9f3c2103aaf4cba7d10e0322cbd938b8a07b8ac6978db1c23f7b1b609b3bdb41702633d97b064ba74b5498e3850ff01ef9b3b637d4af30ac579ea9f7123cb6e17c5c83751829617e7bbc7a1dc4400bb8d596524572ace113a49ba961bd749e5cb223dfe1a7c0e11799c0e38f59dff5b0e120c66672a079ae1c7c143f5c197d344f45d665dc744e119b837b4a7a10389dba9d7513dbc2e5115d99a5138947738a2895b3b87cb7b21d4637f61b5f0aeaaec7e8c15314e0d6c5d998ecd99bcb0562c1c94c0e956ca7466f9beaf0799bd108a3b468579ca40937747bc2e34a260774f32a50203010001";

    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IOException {

       /* // == PC 1 ===

        //Create a message (Can be changed, as long as it remains serializable)
       // Message msg = new Message(2, "Gonçalo");
        //Init first manager for sending
        CryptoManager manager = new CryptoManager(MANAGER_PUB_KEY, "passwd");
        //Cipher the message
        CipheredMessage cipheredMessage = manager.makeCipheredMessage(msg,OTHER_MANAGER_PUB_KEY);

        // === PC 2 ====

        //Init second manager for receiving
        CryptoManager otherManager = new CryptoManager(OTHER_MANAGER_PUB_KEY, "passwd");
        //Decipher
        Message decipheredMessage = otherManager.decipherCipheredMessage(cipheredMessage, MANAGER_PUB_KEY);

        System.out.println("Original is " + msg);
        System.out.println("Ciphered result is " + cipheredMessage);
        System.out.println("Deiphered result is " + decipheredMessage);*/


    }



}
