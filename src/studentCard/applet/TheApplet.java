package applet;


import javax.swing.ButtonGroup;

import javacard.framework.*;



public class TheApplet extends Applet {

	static final short DATAMAXSIZE          = (short)20;
	static short index                      = (short)0;
	static short indexTmp                   = (short)0;
	static short nbFiles                    = (short)0;

	static final byte GETFILEBYNUMBER			= (byte)0x26;
	static final byte LISTFILESSTORED			= (byte)0x25;

	static final byte UPDATECARDKEY				= (byte)0x14;
	static final byte UNCIPHERFILEBYCARD			= (byte)0x13;
	static final byte CIPHERFILEBYCARD			= (byte)0x12;
	static final byte CIPHERANDUNCIPHERNAMEBYCARD		= (byte)0x11;
	static final byte READFILEFROMCARD			= (byte)0x10;
	static final byte WRITEFILETOCARD			= (byte)0x09;
	static final byte UPDATEWRITEPIN			= (byte)0x08;
	static final byte UPDATEREADPIN				= (byte)0x07;
	static final byte DISPLAYPINSECURITY			= (byte)0x06;
	static final byte DESACTIVATEACTIVATEPINSECURITY	= (byte)0x05;
	static final byte ENTERREADPIN				= (byte)0x04;
	static final byte ENTERWRITEPIN				= (byte)0x03;
	static final byte READNAMEFROMCARD			= (byte)0x02;
	static final byte WRITENAMETOCARD			= (byte)0x01;

	// consts added
	final static short  SW_VERIFICATION_FAILED = (short) 0x6300;
	final static short  SW_PIN_VERIFICATION_REQUIRED = (short) 0x6301;
	// definir le tableua NVR au debut du programme;
	final static short NVRSIZE      = (short)1024;
	static byte[] NVR               = new byte[NVRSIZE];
	static boolean pinsecurity  = false;   // #syef change it to true to activate pin security
	static OwnerPIN writepin;
	static OwnerPIN readpin;

	protected TheApplet() {
		byte[] readpincode = 
		{(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30}; // PIN code "0000"
		byte[] writepincode = {(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31}; // PIN code "1111"
		readpin = new OwnerPIN((byte)3,(byte)8);  			// 3 tries 8=Max Size
		readpin.update(readpincode,(short)0,(byte)4); 			// from pincode, offset 0, length 4
		writepin = new OwnerPIN((byte)3,(byte)8);  			// 3 tries 8=Max Size
		writepin.update(writepincode,(short)0,(byte)4); 			// from pincode, offset 0, length 4

		this.register();
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

			case UPDATECARDKEY: updateCardKey( apdu ); break;
			case UNCIPHERFILEBYCARD: uncipherFileByCard( apdu ); break;
			case CIPHERFILEBYCARD: cipherFileByCard( apdu ); break;
			case CIPHERANDUNCIPHERNAMEBYCARD: cipherAndUncipherNameByCard( apdu ); break;
			case READFILEFROMCARD: readFileFromCard( apdu ); break;
			case WRITEFILETOCARD: writeFileToCard( apdu ); break;
			case UPDATEWRITEPIN: updateWritePIN( apdu ); break;
			case UPDATEREADPIN: updateReadPIN( apdu ); break;
			case DISPLAYPINSECURITY: displayPINSecurity( apdu ); break;
			case DESACTIVATEACTIVATEPINSECURITY: desactivateActivatePINSecurity( apdu ); break;
			case ENTERREADPIN: enterReadPIN( apdu ); break;
			case ENTERWRITEPIN: enterWritePIN( apdu ); break;
			case READNAMEFROMCARD: 
				if (pinsecurity && ! readpin.isValidated() )
					ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
				readNameFromCard( apdu ); break;
			case WRITENAMETOCARD:
				if (pinsecurity && ! readpin.isValidated() )
					ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
				writeNameToCard( apdu ); break;
			default: ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}

	void getFileByNumber(APDU apdu){
		byte[] buffer = apdu.getBuffer();
		apdu.setIncomingAndReceive();

		if (buffer[2]==(byte)1){
			buffer[0]= (byte) buffer[3];
			buffer[(NVR[ getindex(buffer[3])]+1)]= NVR[NVR[ getindex(buffer[3]) ]+1];
			short val = (short) (NVR[getindex(buffer[3])]+2);
			Util.arrayCopy(NVR, (byte)(getindex(buffer[3])+1), buffer, (byte)1, (byte)NVR[getindex(buffer[3])] );
			apdu.setOutgoingAndSend( (short)0, val);
		}

		if(buffer[2]==(byte)2){
			buffer[0]= (byte) buffer[3];
			buffer[(NVR[ getindex(buffer[3])]+1)]= NVR[NVR[ getindex(buffer[3]) ]+1];
			short val = (short) (NVR[getindex(buffer[3])]+2);
			Util.arrayCopy(NVR, (byte)(getindex(buffer[3])+1), buffer, (byte)1, (byte)NVR[getindex(buffer[3])] );
			apdu.setOutgoingAndSend( (short)0, val);
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

/*
	void listFilesStored( APDU apdu ) {
		byte[] buffer = apdu.getBuffer();
		buffer[0]=(byte)nbFiles;
		
		for (byte i=0 ; i < nbFiles ;i++){
			buffer[0]=(byte)i;
			Util.arrayCopy(NVR, (byte)1, buffer, (byte)1, (byte)NVR[0]);
			buffer[(NVR[0]+1)]= NVR[NVR[0]+1];
			apdu.setOutgoingAndSend( (short)0, (byte)(NVR[0]+2));
		}
	}
*/

	void updateCardKey( APDU apdu ) {
		
	}


	void uncipherFileByCard( APDU apdu ) {

	}


	void cipherFileByCard( APDU apdu ) {

	}


	void cipherAndUncipherNameByCard( APDU apdu ) {
		
	}


	void readFileFromCard( APDU apdu ) {
		
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


	void updateWritePIN( APDU apdu ) {
		if (pinsecurity && ! writepin.isValidated() )
			ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
		byte [] buffer = apdu.getBuffer();
		apdu.setIncomingAndReceive();

		writepin.update(buffer,(short)5,(byte)4);
	}


	void updateReadPIN( APDU apdu ) {
		if (pinsecurity && ! readpin.isValidated())
			ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
		byte [] buffer = apdu.getBuffer();
		apdu.setIncomingAndReceive();
		readpin.update(buffer,(short)5,(byte)4);
	}


	void displayPINSecurity( APDU apdu ) {
		byte[] buffer = apdu.getBuffer();
		byte[] codepinsec = new byte[2];
		buffer[0]=buffer[1]=pinsecurity? (byte)1 : (byte)0;
		apdu.setOutgoingAndSend( (short)0, (short)2);
	}


	void desactivateActivatePINSecurity( APDU apdu ) {
		pinsecurity = !pinsecurity;
	}


	void enterReadPIN( APDU apdu ) {
		verify(readpin, apdu);
	}


	void enterWritePIN( APDU apdu ) {
		verify(writepin, apdu);
	}


	void readNameFromCard( APDU apdu ) {
		//le message sera dans le tableau NVR recuperer aussi la longuer de nom de l'étudiant
		byte[] buffer = apdu.getBuffer();
		Util.arrayCopy(NVR, (short)1, buffer, (short)0, NVR[0]);
		apdu.setOutgoingAndSend( (short)0, NVR[0]);
	}


	void writeNameToCard( APDU apdu ) {
		byte [] buffer = apdu.getBuffer();
		
		apdu.setIncomingAndReceive();
		Util.arrayCopy(buffer,(short)4,NVR,(short)0,(short)(buffer[4]+1));
		
	}


}
