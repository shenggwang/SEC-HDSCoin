package pt.ulisboa.tecnico.hdscoin.Crypto;

    

import pt.ulisboa.tecnico.hdscoin.interfaces.KeystoreManager;

import javax.crypto.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Enumeration;

/**
 * Class for handling Crypto specific to our encryption protocol.
 *  Actual primitives for encryption can be found in {@link CryptoUtil}
 *
 *  Handles encryption, decryption, signature verification and key management.
 */
public class CryptoManager {

    /**
     * This node's RSA public key
     */
    private PublicKey pubKey;

    /**
     * This node's RSA private key
     */
    private PrivateKey privKey;

    private KeystoreManager keyPairManager;
    private boolean testMode;
    private int testAttack;

    public CryptoManager(PublicKey publicKey, PrivateKey privateKey, KeystoreManager keyPairManager, boolean testMode, int testAttack){
        
    	this.pubKey=publicKey;
    	this.privKey=privateKey;
    	this.keyPairManager = keyPairManager;
    	this.testMode=testMode;
    	this.testAttack=testAttack;
        System.out.println("CRYPTO MANAGER STARTED");
    }
    
    

    /**
     * Maps the public key string for each peer to an actual {@link PublicKey} that can be used
     * for encryption and decryption
     */
    //private HashMap<String, PublicKey> peerKeys;
    /**
     * Initializes CryptoManager by loading it's own keys and populating the peer keys
     * @param publicKey public key for this node
     * @param keysFile file where the keys can be found.
     */
    /*
    public CryptoManager(String publicKey, String keysFile){
        peerKeys = new HashMap<>();
        try {
            loadKeysFromFile(publicKey, keysFile);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException | FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("CRYPTO MANAGER STARTED");
    }
    */
    
    /**
     * Loads a set of previously computed RSA keys from a file and assigns the values to the correct variables
     * Private keys of other peers are ignored
     * @param publicKey public key for this node
     * @param keysFile file where the keys can be found
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws FileNotFoundException
     */
    /*
    private void loadKeysFromFile(String publicKey, String keysFile) throws InvalidKeySpecException, NoSuchAlgorithmException, FileNotFoundException {
        Scanner scanner = new Scanner(new File(keysFile));
        while (scanner.hasNextLine()){
            String[] keys = scanner.nextLine().split(" ");
            if(keys[0].equals(publicKey)){
                pubKey = (PublicKey) KeyUtils.stringToKey(keys[0], true);
                privKey = (PrivateKey) KeyUtils.stringToKey(keys[1], false);
                //peerKeys.put(keys[0], pubKey);
            } else {
                PublicKey key = (PublicKey) KeyUtils.stringToKey(keys[0], true);
                peerKeys.put(keys[0], key);
            }
        }
    }
    */

    /**
     * Creates a {@link CipheredMessage} that can be sent through the network
     * @param message The message that should be ciphered
     * @param receiverPubKey public key of the destination node
     * @return {@link CipheredMessage} object containing the ciphered Message
     */
    public CipheredMessage makeCipheredMessage(Message message, PublicKey receiverPubKey){
        CipheredMessage cipheredMessage = null;
        try {

        	KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        	kpg.initialize(1024);
        	KeyPair keys = kpg.generateKeyPair();

            //Required params
            byte[] IV = generateIV();

            //AES ciphering of Message
            SecretKey aesKey = generateAESKey();
            byte[] cipheredContent = cipherContent(message, IV, aesKey);

            //Signature generation
            byte[] digitalSig;
            byte[] concatParams = concatHashParams(message, message.getTimestamp());

            if(testMode && testAttack==1)
            	digitalSig = CryptoUtil.makeDigitalSignature(concatParams, keys.getPrivate());
            else
            	digitalSig = CryptoUtil.makeDigitalSignature(concatParams, privKey);
            IntegrityCheck integrityCheck = new IntegrityCheck(digitalSig, message.getTimestamp(), message.getTimestamp());
            byte[] integrityCheckBytes = toBytes(integrityCheck);

            //AES ciphering of Signature and params
            byte[] cipheredIntegrityCheck = CryptoUtil.symCipher(integrityCheckBytes, IV, aesKey);

            //RSA ciphering of AES key
            byte[] keyBytes = toBytes(aesKey);
            if(receiverPubKey==null)
            	System.out.println("Receiver key is null");
            byte[] cipheredKey = CryptoUtil.asymCipher(keyBytes, receiverPubKey);
            if(testMode && testAttack==2) {
            	Message test = new Message(message.getSender(), message.getDestination(), message.getTimestamp()+100);
            	cipheredContent = cipherContent(test, IV, aesKey);
            }
            cipheredMessage = new CipheredMessage(cipheredContent, IV, cipheredIntegrityCheck, cipheredKey);
        } catch (NoSuchAlgorithmException | IOException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            System.out.println("Cipher error1");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Cipher error2");
        }
        return cipheredMessage;
    }


    /**
     * Deciphers a {@link CipheredMessage} object to obtain the original message, and verifies
     * digital signature to ensure integrity and non-repudiation
     * @param cipheredMessage The received ciphered message
     * @return {@link Message} object that was encapsulated
     * @throws Exception 
     */

    public Message decipherCipheredMessage(CipheredMessage cipheredMessage){
        Message deciphMsg = null;
        try {
            SecretKey key = (SecretKey) fromBytes(CryptoUtil.asymDecipher(cipheredMessage.getKey(), privKey));
            byte[] decipheredContent = CryptoUtil.symDecipher(cipheredMessage.getContent(), cipheredMessage.getIV(), key);
            deciphMsg = (Message) fromBytes(decipheredContent);
            byte[] decipheredIntegrityBytes = CryptoUtil.symDecipher(cipheredMessage.getIntegrityCheck(), cipheredMessage.getIV(), key);
            IntegrityCheck check = (IntegrityCheck) fromBytes(decipheredIntegrityBytes);
            if(verifyIntegrity(deciphMsg, check, deciphMsg.getSender())) return deciphMsg;
            else throw new IllegalStateException("Invalid Signature");
        } catch (ClassNotFoundException | IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            System.out.println("Decipher error...");
        }
        return deciphMsg;
    }

    public IntegrityCheck decipherIntegrityCheck(CipheredMessage cipheredMessage){
        IntegrityCheck deciphCheck = null;
        try {
            SecretKey key = (SecretKey) fromBytes(CryptoUtil.asymDecipher(cipheredMessage.getKey(), privKey));
            byte[] decipheredContent = CryptoUtil.symDecipher(cipheredMessage.getContent(), cipheredMessage.getIV(), key);
            Message deciphMsg = (Message) fromBytes(decipheredContent);
            byte[] decipheredIntegrityBytes = CryptoUtil.symDecipher(cipheredMessage.getIntegrityCheck(), cipheredMessage.getIV(), key);
            deciphCheck = (IntegrityCheck) fromBytes(decipheredIntegrityBytes);
            if(verifyIntegrity(deciphMsg, deciphCheck, deciphMsg.getSender())) return deciphCheck;
            else throw new IllegalStateException("Invalid Signature");
        } catch (ClassNotFoundException | IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            System.out.println("Decipher error...");
        }
        return deciphCheck;
    }
    
    public IntegrityCheck getDigitalSign(CipheredMessage message){
    	
    	try {
    		SecretKey key = (SecretKey) fromBytes(CryptoUtil.asymDecipher(message.getKey(), privKey));
	        byte[] decipheredContent = CryptoUtil.symDecipher(message.getContent(), message.getIV(), key);
	        Message deciphMsg = (Message) fromBytes(decipheredContent);
	        byte[] decipheredIntegrityBytes = CryptoUtil.symDecipher(message.getIntegrityCheck(), message.getIV(), key);
	        IntegrityCheck check = (IntegrityCheck) fromBytes(decipheredIntegrityBytes);

	    	return check;
	    } catch (ClassNotFoundException | IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
	        e.printStackTrace();
	        System.out.println("Decipher error...");
	        return null;
	    }
    }

    /**
     * Calculates digital sign on the receiver end and compares with the one received
     * @param msg The decrypted {@link Message}
     * @param check Contains the rest of the parameters used in the signature
     * @param key The public Key of the origin
     * @return true if signature matches, false otherwise
     * @throws IllegalBlockSizeException
     * @throws NoSuchPaddingException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InvalidAlgorithmParameterException
     */
    public boolean verifyIntegrity(Message msg, IntegrityCheck check, PublicKey key) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, ClassNotFoundException, InvalidAlgorithmParameterException {
        byte[] concatParams = concatHashParams(msg, check.getNonce());
       return CryptoUtil.verifyDigitalSignature(check.getDigitalSignature(), concatParams, key);
    }



    /**
     * Ciphers a {@link Message} object with AES
     * @param message msg to be ciphered
     * @param IV Initialization Vector
     * @param skey AES key
     * @return byte array containing the ciphered contents
     */
    private byte[] cipherContent(Message message, byte[] IV, SecretKey skey){
        byte[] cipheredMessage = new byte[0];
        try {
            cipheredMessage = CryptoUtil.symCipher(toBytes(message), IV, skey);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | IOException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return cipheredMessage;
    }

    /**
     * Converts a generic argument to a byte array
     * @param toConvert What we want to convert
     * @param <T> Could by any object
     * @return byte array representation of the original object
     * @throws IOException
     */
    private <T> byte[] toBytes(T toConvert) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(byteStream);
        os.writeObject(toConvert);
        os.flush();
        return byteStream.toByteArray();
    }



    /**
     * Converts a byte array to a Java object.
     * It's not possible to know which object it is so it needs to be casted when storing in a variable
     * Use only when you know what type of object it is
     * @param toConvert byte array that should be converted
     * @return Object obtained from byte array
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private Object fromBytes(byte[] toConvert) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteInStream = new ByteArrayInputStream(toConvert);
        ObjectInputStream is = new ObjectInputStream(byteInStream);
        return is.readObject();
    }

    /**
     * Concatenates parameters that go into the hash and turns it into a byte array
     * @param message Message that should be sent
     * @param timestamp timestamp of the message
     * @return byte array of msg||IV||t
     * @throws IOException
     */
    private byte[] concatHashParams(Message message, long timestamp) throws IOException {
        byte[] msgBytes = toBytes(message);
        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
        byteBuffer.putLong(timestamp);
        byte[] timestampBytes = byteBuffer.array();
        byte[] concatedParams = new byte[msgBytes.length + timestampBytes.length];
        System.arraycopy(msgBytes, 0, concatedParams, 0, msgBytes.length);
        System.arraycopy(timestampBytes, 0, concatedParams, msgBytes.length, timestampBytes.length);
        return concatedParams;
    }

    /**
     * Generates an init vector for AES ciphering
     * @return
     */
    private byte[] generateIV(){
        SecureRandom random = new SecureRandom();
        byte[] initializationVector = new byte[128/8];
        random.nextBytes(initializationVector);
        return initializationVector;
    }


    /**
     * Generates an AES Key for ciphering
     * @return AES key
     * @throws NoSuchAlgorithmException
     */
    private SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        SecretKey skey = keygen.generateKey();
        return skey;
    }
    
    public PublicKey getPublicKey() {
    	return pubKey;
    }
    public PrivateKey getPrivateKey() {
    	return privKey;
    }
    
    public PublicKey getPublicKeyBy(String alias) throws Exception{
    	return keyPairManager.getPublicKeyByName(alias);
    }
    
    public String getNamebyAlias(PublicKey publickey) {
    	String alias=null;
    	try {
			Enumeration<String> es = keyPairManager.getKeyStore().aliases();
			while (es.hasMoreElements()) {
				alias = (String) es.nextElement();
				if(getPublicKeyBy(alias).equals(publickey)) {
					break;
				}
			}
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return alias;
    }
    		

}
