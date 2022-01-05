package applet;


import javax.swing.ButtonGroup;

import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.*;


public class TheApplet extends Applet {

	static final short DATAMAXSIZE          = (short)20;
	static short index                      = (short)0;
	static short indexTmp                   = (short)0;
	static short nbFiles                    = (short)0;
	static byte numFileReq                 = (byte)0;

	static final byte GETFILEBYNUMBER			= (byte)0x26;
	static final byte LISTFILESSTORED			= (byte)0x25;

	private final static byte INS_DES_ECB_NOPAD_ENC           	= (byte)0x20;
    private final static byte INS_DES_ECB_NOPAD_DEC           	= (byte)0x21;

	static final byte UNCIPHERFILEBYCARD			= (byte)0x13;
	static final byte CIPHERFILEBYCARD			= (byte)0x12;
	static final byte WRITEFILETOCARD			= (byte)0x09;

	// consts added
	final static short  SW_VERIFICATION_FAILED = (short) 0x6300;
	final static short  SW_PIN_VERIFICATION_REQUIRED = (short) 0x6301;

	static final byte[] theDESKey = 
		new byte[] { (byte)0xCA, (byte)0xCA, (byte)0xCA, (byte)0xCA, (byte)0xCA, (byte)0xCA, (byte)0xCA, (byte)0xCA };
	// definir le tableua NVR au debut du programme;
	final static short NVRSIZE      = (short)1024;
	static byte[] NVR               = new byte[NVRSIZE];
	static boolean pinsecurity  = false;   // #syef change it to true to activate pin security
	static OwnerPIN writepin;
	static OwnerPIN readpin;

	// cipher instances
    private Cipher 
	    cDES_ECB_NOPAD_enc, cDES_ECB_NOPAD_dec;


    // key objects
			
    private Key 
	    secretDESKey, secretDES2Key, secretDES3Key;

	boolean 
		pseudoRandom, secureRandom,
	    SHA1, MD5, RIPEMD160,
	    keyDES, DES_ECB_NOPAD, DES_CBC_NOPAD;

	private byte[] dataToCipher = {1,2,3,4,5,6,7,8};
	private byte[] ciphered = new byte[8];

	protected TheApplet() {
		initKeyDES(); 
	    initDES_ECB_NOPAD();

		byte[] readpincode = 
		{(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30}; // PIN code "0000"
		byte[] writepincode = {(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31}; // PIN code "1111"
		readpin = new OwnerPIN((byte)3,(byte)8);  			// 3 tries 8=Max Size
		readpin.update(readpincode,(short)0,(byte)4); 			// from pincode, offset 0, length 4
		writepin = new OwnerPIN((byte)3,(byte)8);  			// 3 tries 8=Max Size
		writepin.update(writepincode,(short)0,(byte)4); 			// from pincode, offset 0, length 4

		this.register();
	}

	private void initKeyDES() {
	    try {
		    secretDESKey = KeyBuilder.buildKey(KeyBuilder.TYPE_DES, KeyBuilder.LENGTH_DES, false);
		    ((DESKey)secretDESKey).setKey(theDESKey,(short)0);
		    keyDES = true;
	    } catch( Exception e ) {
		    keyDES = false;
	    }
    }


    private void initDES_ECB_NOPAD() {
	    if( keyDES ) try {
		    cDES_ECB_NOPAD_enc = Cipher.getInstance(Cipher.ALG_DES_ECB_NOPAD, false);
		    cDES_ECB_NOPAD_dec = Cipher.getInstance(Cipher.ALG_DES_ECB_NOPAD, false);
		    cDES_ECB_NOPAD_enc.init( secretDESKey, Cipher.MODE_ENCRYPT );
		    cDES_ECB_NOPAD_dec.init( secretDESKey, Cipher.MODE_DECRYPT );
		    DES_ECB_NOPAD = true;
	    } catch( Exception e ) {
		    DES_ECB_NOPAD = false;
	    }
    }



	public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException {
		new TheApplet();
	} 


	public boolean select() {
		if ( readpin.getTriesRemaining() == 0 || writepin.getTriesRemaining() ==0 )
			return false;
		else
			return true;
	} 


	public void deselect() {
		readpin.reset();
		writepin.reset();
	}

	void verify(OwnerPIN pin ,APDU apdu ) {
		apdu.setIncomingAndReceive();
		byte[] buffer = apdu.getBuffer();
		if( !pin.check( buffer, (byte)5, buffer[4] ) )
			ISOException.throwIt( SW_VERIFICATION_FAILED );
	}


	public void process(APDU apdu) throws ISOException {
		if( selectingApplet() == true )
			return;

		byte[] buffer = apdu.getBuffer();

		switch( buffer[1] )		{
			case GETFILEBYNUMBER: getFileByNumber(apdu ); break;
			case LISTFILESSTORED: listFilesStored( apdu ); break;
			//case UNCIPHERFILEBYCARD: uncipherFileByCard( apdu ); break;
			//case CIPHERFILEBYCARD: cipherFileByCard( apdu ); break;

			case INS_DES_ECB_NOPAD_ENC: if( DES_ECB_NOPAD )
                cipherGeneric( apdu, cDES_ECB_NOPAD_enc, KeyBuilder.LENGTH_DES ); break;//chiffrer  les données venant du pc
            case INS_DES_ECB_NOPAD_DEC: if( DES_ECB_NOPAD ) 
				cipherGeneric( apdu, cDES_ECB_NOPAD_dec, KeyBuilder.LENGTH_DES  ); break;
				
			case WRITEFILETOCARD: writeFileToCard( apdu ); break;
			default: ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}

	void getFileByNumber(APDU apdu){
		byte[] buffer = apdu.getBuffer();
		apdu.setIncomingAndReceive();

		if (buffer[2]==(byte)1){
			numFileReq= (byte) buffer[3]; // num file requested
			buffer[0]= (byte) buffer[3];
			buffer[(NVR[ getindex(buffer[3])]+1)]= NVR[NVR[ getindex(buffer[3]) ]+1];
			short val = (short) (NVR[getindex(buffer[3])]+2);
			Util.arrayCopy(NVR, (byte)(getindex(buffer[3])+1), buffer, (byte)1, (byte)NVR[getindex(buffer[3])] );
			apdu.setOutgoingAndSend( (short)0, val);
		}

		if(buffer[2]==(byte)2){
			short val = (short)(getindex(numFileReq)  +(DATAMAXSIZE* buffer[3]) +(NVR[ getindex(numFileReq) ]+2) ) ; // index of data
			Util.arrayCopy(NVR, val, buffer, (byte)0, (byte)DATAMAXSIZE );
			apdu.setOutgoingAndSend( (short)0, (byte)DATAMAXSIZE);
		}
	}

	void listFilesStored( APDU apdu ) {
		byte[] buffer = apdu.getBuffer();
		apdu.setIncomingAndReceive();

		

		if (buffer[2]==0 && buffer[3]==0){
			buffer[0]= (byte) nbFiles;
			apdu.setOutgoingAndSend( (short)0, (byte)(1));
		}
		if(buffer[2]==(byte)1){
			
			buffer[0]= (byte) buffer[3];
			buffer[(NVR[ getindex(buffer[3])]+1)]= NVR[NVR[ getindex(buffer[3]) ]+1];
			short val = (short) (NVR[getindex(buffer[3])]+2);
			Util.arrayCopy(NVR, (byte)(getindex(buffer[3])+1), buffer, (byte)1, (byte)NVR[getindex(buffer[3])] );
			apdu.setOutgoingAndSend( (short)0, val);
		}
	}

	// get the index of the file by his number in the NVR Table
	short getindex(byte filenumber){
		short index=0;
		for(byte i=0; i< filenumber ; i++ )
			index += 2 + NVR[index] + NVR[(NVR[index]+1)] * DATAMAXSIZE ;

		return index;
	}

	private void cipherGeneric( APDU apdu, Cipher cipher, short keyLength ) {
        byte[] buffer = apdu.getBuffer();
        
        apdu.setIncomingAndReceive();

		cipher.doFinal( buffer, (short)5, (short)buffer[4], buffer, (short)5);
        
        apdu.setOutgoingAndSend( (short)5, (short)buffer[4]);

		// Write the method ciphering/unciphering data from the computer.
		// The result is sent back to the computer.
	}


	void uncipherFileByCard( APDU apdu ) {

	}


	void cipherFileByCard( APDU apdu ) {

	}

	void writeFileToCard( APDU apdu ) {
		
		byte [] buffer = apdu.getBuffer();
		apdu.setIncomingAndReceive();

		if (buffer[2]==0 && buffer[3]==0){
			Util.arrayCopy(buffer,(short)4,NVR,(short)index,(short)(buffer[4]+1));
			indexTmp += (buffer[4] + 2);
			
		}else if(buffer[2]==0){
			Util.arrayCopy(buffer,(short)5,NVR,(short)(index+indexTmp),(short)DATAMAXSIZE);
			indexTmp += buffer[4];

		}else if (buffer[2]==1){
			Util.arrayCopy(buffer,(short)5,NVR,(short)(index+indexTmp),(short)DATAMAXSIZE);
			NVR[(short)(index+(NVR[index]+1))]= buffer[3];
			indexTmp += buffer[4];
			
			index += indexTmp;
			indexTmp =0;

			nbFiles++;
		}

	}


}
