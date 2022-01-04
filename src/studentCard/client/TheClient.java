package client;

import java.io.*;

import opencard.core.service.*;
import opencard.core.terminal.*;
import opencard.core.util.*;
import opencard.opt.util.*;




public class TheClient {

	private PassThruCardService servClient = null;
	boolean DISPLAY = true;
	boolean loop = true;

	static final byte CLA					= (byte)0x00;
	static final byte P1					= (byte)0x00;
	static final byte P2					= (byte)0x00;
	
	static final String PATHTOFILES 		= "files\\";
	static final short DATAMAXSIZE          = 20;

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

	ResponseAPDU resp;
	CommandAPDU cmd;

	public TheClient() {
		try {
			SmartCard.start();
			System.out.print( "Smartcard inserted?... " ); 

			CardRequest cr = new CardRequest (CardRequest.ANYCARD,null,null); 

			SmartCard sm = SmartCard.waitForCard (cr);

			if (sm != null) {
				System.out.println ("got a SmartCard object!\n");
			} else
				System.out.println( "did not get a SmartCard object!\n" );

			this.initNewCard( sm ); 

			SmartCard.shutdown();

		} catch( Exception e ) {
			System.out.println( "TheClient error: " + e.getMessage() );
		}
		java.lang.System.exit(0) ;
	}

	private ResponseAPDU sendAPDU(CommandAPDU cmd) {
		return sendAPDU(cmd, true);
	}

	private ResponseAPDU sendAPDU( CommandAPDU cmd, boolean display ) {
		ResponseAPDU result = null;
		try {
			result = this.servClient.sendCommandAPDU( cmd );
			if(display)
				displayAPDU(cmd, result);
		} catch( Exception e ) {
			System.out.println( "Exception caught in sendAPDU: " + e.getMessage() );
			java.lang.System.exit( -1 );
		}
		return result;
	}


	/************************************************
	 * *********** BEGINNING OF TOOLS ***************
	 * **********************************************/


	private String apdu2string( APDU apdu ) {
		return removeCR( HexString.hexify( apdu.getBytes() ) );
	}


	public void displayAPDU( APDU apdu ) {
		System.out.println( removeCR( HexString.hexify( apdu.getBytes() ) ) + "\n" );
	}


	public void displayAPDU( CommandAPDU termCmd, ResponseAPDU cardResp ) {
		System.out.println( "--> Term: " + removeCR( HexString.hexify( termCmd.getBytes() ) ) );
		System.out.println( "<-- Card: " + removeCR( HexString.hexify( cardResp.getBytes() ) ) );
	}


	private String removeCR( String string ) {
		return string.replace( '\n', ' ' );
	}


	/******************************************
	 * *********** END OF TOOLS ***************
	 * ****************************************/


	private boolean selectApplet() {
		boolean cardOk = false;
		try {
			CommandAPDU cmd = new CommandAPDU( new byte[] {
				(byte)0x00, (byte)0xA4, (byte)0x04, (byte)0x00, (byte)0x0A,
				    (byte)0xA0, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x62, 
				    (byte)0x03, (byte)0x01, (byte)0x0C, (byte)0x06, (byte)0x01
			} );
			ResponseAPDU resp = this.sendAPDU( cmd );
			if( this.apdu2string( resp ).equals( "90 00" ) )
				cardOk = true;
		} catch(Exception e) {
			System.out.println( "Exception caught in selectApplet: " + e.getMessage() );
			java.lang.System.exit( -1 );
		}
		return cardOk;
	}


	private void initNewCard( SmartCard card ) {
		if( card != null )
			System.out.println( "Smartcard inserted\n" );
		else {
			System.out.println( "Did not get a smartcard" );
			System.exit( -1 );
		}

		System.out.println( "ATR: " + HexString.hexify( card.getCardID().getATR() ) + "\n");

		try {
			this.servClient = (PassThruCardService)card.getCardService( PassThruCardService.class, true );
		} catch( Exception e ) {
			System.out.println( e.getMessage() );
		}

		System.out.println("Applet selecting...");
		if( !this.selectApplet() ) {
			System.out.println( "Wrong card, no applet to select!\n" );
			System.exit( 1 );
			return;
		} else 
			System.out.println( "Applet selected" );

		mainLoop();
	}

	void compareTwoFiles(){

		String files= readKeyboard();
		
		String[] parts = files.split(" ");
		String filename1 = PATHTOFILES+parts[0];
		String filename2 = PATHTOFILES+parts[1];

		try {
			boolean flag = true;
			BufferedInputStream file1 = new BufferedInputStream(new FileInputStream(filename1));
			BufferedInputStream file2 = new BufferedInputStream(new FileInputStream(filename2));
			
			int ch = 0;
			
			while ((ch = file1.read()) != -1 && flag ) {
				
				if (ch != file2.read()) {
					flag = false;
				}
				System.out.println(ch);
			}
			if(file2.read() != -1) flag = false;

			if(flag){
				System.out.println("Files are the same");
			}else{
				System.out.println("Files are different");
			}

		} catch(IOException e){
			System.out.println("Error with the files");
		}

	}


	// P1 and P2 will help me to specify when we are asking for the number of files and the number of the file asked for
	// P1 = 1 asking for files infos 
	// P1 = 2 asking for a data of files
	// P2 = the number of the trunk data asked
	void getFileByNumber(){
		int filenumber= Integer.parseInt( readKeyboard());

		CommandAPDU cmd;
        ResponseAPDU resp;
		
	    byte[] cmd_1= {CLA,GETFILEBYNUMBER,(byte)1,(byte)filenumber,(byte)0};
        cmd = new CommandAPDU( cmd_1 );
        resp = this.sendAPDU( cmd, DISPLAY );
		byte[] bytes = resp.getBytes();

		String msg = "";
		msg += new StringBuffer("").append(bytes[0]);
		msg += new StringBuffer("").append(" ");
		for(int i=1; i<bytes.length-3;i++)
			msg += new StringBuffer("").append((char)bytes[i]);
		msg += new StringBuffer("").append(" ");
		msg += new StringBuffer("").append(bytes[bytes.length-3]);
		System.out.println(msg);

		String[] fileinfos = msg.split(" ");
		try{
			FileOutputStream out = new FileOutputStream(PATHTOFILES+"out-"+fileinfos[1]);
			int nbTrunks = Integer.parseInt(fileinfos[2]);
			
			//trunks handling
			byte[] trunk= new byte[DATAMAXSIZE] ;
			short finTrunkLen = (short)0;
			

			for (int j=0; j< nbTrunks ;j++){ // trunk data number
				cmd_1[2]= (byte)2;
				cmd_1[3]= (byte)j;
				cmd = new CommandAPDU( cmd_1 );
				
				resp = this.sendAPDU( cmd, DISPLAY );
				bytes = resp.getBytes();
				
				if(j == nbTrunks-1){  // for the last trunk
					finTrunkLen = (short)(DATAMAXSIZE - bytes[DATAMAXSIZE-1]);
					byte[] finTrunk= new byte[finTrunkLen] ;
					System.arraycopy(bytes,(short)0,finTrunk,(short)0,(short)finTrunkLen);
					out.write(finTrunk);
				}else{
					System.arraycopy(bytes,(short)0,trunk,(short)0,(short)DATAMAXSIZE);
					out.write(trunk);
				}
					
				/*
					msg="";
					for(int i=0; i<bytes.length-2;i++)
						msg += new StringBuffer("").append((char)bytes[i]);
					
					System.out.println(msg);
				*/
				
			}
			out.close();
		}catch(IOException e){
			System.out.println("Error with the output stream !!!!");
		}
		

	}

	// P1 and P2 will help me to specify when we are asking for the number of files and the number of the file asked for
	// P1 = 0 && P2 = 0 asking for the number of files
	// P1 = 1 asking for a specific file with the number specified in P2
	// P2 = the number of the file
	void listFilesStored(){
		CommandAPDU cmd;
        ResponseAPDU resp;
		
	    byte[] cmd_1= {CLA,LISTFILESSTORED,P1,P2,(byte)0};
        cmd = new CommandAPDU( cmd_1 );
        resp = this.sendAPDU( cmd, DISPLAY );
		byte[] bytes = resp.getBytes();

		byte nbfiles= bytes[0];
		String msg = "";

		for (int j=0; j< nbfiles;j++){
			cmd_1[2]= (byte)1;
			cmd_1[3]= (byte)j;
			cmd = new CommandAPDU( cmd_1 );

			resp = this.sendAPDU( cmd, DISPLAY );
			bytes = resp.getBytes();

			msg = "";
			msg += new StringBuffer("").append(bytes[0]);
			msg += new StringBuffer("").append(" ");
			for(int i=1; i<bytes.length-3;i++)
				msg += new StringBuffer("").append((char)bytes[i]);
			msg += new StringBuffer("").append(" ");
			msg += new StringBuffer("").append(bytes[bytes.length-3]);
			System.out.println(msg);
		}	
	}

	void updateCardKey() {
	}


	void uncipherFileByCard() {
	}


	void cipherFileByCard() {
	}


	void cipherAndUncipherNameByCard() {
	}


	void readFileFromCard() {

	}

	// P1 and P2 will help me to specify which number of chunk it is and if the transfer is finished
	// P1 = 0 && P2 = 0 sending the filename
	// P1 = 0  the transfer is not finished
	// P1 = 1 the transfer is finished (last chunk)
	// P2 = the number of the chunk sent

	void writeFileToCard() {

		byte[] filename= readKeyboard().getBytes();
		int Lc = filename.length;
		
		File file = new File(PATHTOFILES+(new String(filename)));

		byte[] header = {CLA,WRITEFILETOCARD,P1,P2,(byte)Lc};

		byte[] command1 = new byte[Lc+5];
		byte[] command2 = new byte[DATAMAXSIZE+5];
		System.arraycopy(header,(short)0,command1,(short)0,(short)5);
		System.arraycopy(filename,(short)0,command1,(short)5,(short)Lc);

		// sending the filename and his length
		cmd = new CommandAPDU(command1);
		this.sendAPDU(cmd, DISPLAY);



		try {
			BufferedInputStream file1 = new BufferedInputStream(new FileInputStream(file));
			int ch = 0;
			int nbChunk=0;
			int padding;
			byte[] chunk = new byte[DATAMAXSIZE];

			while (ch != -1 ) {
				padding=0;
				for(int i=0; i< DATAMAXSIZE ;i++){
					ch = file1.read();
					if(ch==-1){
						if (padding==0) padding = i;
						chunk[i]=(byte)(DATAMAXSIZE-padding);
					} else
						chunk[i]=(byte)ch;
				}
				
				nbChunk++;

				header[2] = (byte)0;
				if(ch==-1) header[2] = (byte)1;

				header[3] = (byte)nbChunk;
				header[4] = (byte)DATAMAXSIZE;

				System.arraycopy(header,(short)0,command2,(short)0,(short)5);
				System.arraycopy(chunk,(short)0,command2,(short)5,(short)DATAMAXSIZE);

				// sending the chunks
				cmd = new CommandAPDU(command2);
				this.sendAPDU(cmd, DISPLAY);
				
				// juste pour voir
				System.out.println(ch);
			}


		} catch(IOException e){
			System.out.println("Error with the files");
		}

	}


	void updateWritePIN() {
		byte[] pin= readKeyboard().getBytes();
		int Lc= pin.length;
		byte[] header= {CLA,UPDATEWRITEPIN,P1,P2,(byte)Lc};
		//byte[] data = name.getBytes();
		byte[] command = new byte[Lc+5];
		System.arraycopy(header,(short)0,command,(short)0,(short)5);
		System.arraycopy(pin,(short)0,command,(short)5,(short)Lc);
		//displayAPDU(buffer);
		cmd = new CommandAPDU(command);
		this.sendAPDU(cmd, DISPLAY);
	}


	void updateReadPIN() {
		byte[] pin= readKeyboard().getBytes();
		int Lc= pin.length;
		byte[] header= {CLA,UPDATEREADPIN,P1,P2,(byte)Lc};
		//byte[] data = name.getBytes();
		byte[] command = new byte[Lc+5];
		System.arraycopy(header,(short)0,command,(short)0,(short)5);
		System.arraycopy(pin,(short)0,command,(short)5,(short)Lc);
		//displayAPDU(buffer);
		cmd = new CommandAPDU(command);
		this.sendAPDU(cmd, DISPLAY);
	}


	void displayPINSecurity() {
		byte[] cmd_1= {CLA,DISPLAYPINSECURITY,P1,P2,(byte)2};
        cmd = new CommandAPDU( cmd_1 );
        resp = this.sendAPDU( cmd, DISPLAY );
		byte[] bytes = resp.getBytes();
	    String msg = "";
		if(bytes[0]==(byte)1 && bytes[1]==(byte)1)
			System.out.println("Pin security is activated");
		if(bytes[0]==(byte)0 && bytes[1]==(byte)0)
			System.out.println("Pin security is desactivated");
	}


	void desactivateActivatePINSecurity() {
		CommandAPDU cmd;
        ResponseAPDU resp;

	    byte[] cmd_1= {CLA,DESACTIVATEACTIVATEPINSECURITY,P1,P2}; 
		cmd = new CommandAPDU( cmd_1 );
        resp = this.sendAPDU( cmd, DISPLAY );
	}


	void enterReadPIN() {
		byte[] pin= readKeyboard().getBytes();
		int Lc= pin.length;
		byte[] header= {CLA,ENTERREADPIN,P1,P2,(byte)Lc};
		//byte[] data = name.getBytes();
		byte[] command = new byte[Lc+5];
		System.arraycopy(header,(short)0,command,(short)0,(short)5);
		System.arraycopy(pin,(short)0,command,(short)5,(short)Lc);
		//displayAPDU(buffer);
		cmd = new CommandAPDU(command);
		this.sendAPDU(cmd, DISPLAY);
	}


	void enterWritePIN() {
		byte[] pin= readKeyboard().getBytes();
		int Lc= pin.length;
		byte[] header= {CLA,ENTERWRITEPIN,P1,P2,(byte)Lc};
		//byte[] data = name.getBytes();
		byte[] command = new byte[Lc+5];
		System.arraycopy(header,(short)0,command,(short)0,(short)5);
		System.arraycopy(pin,(short)0,command,(short)5,(short)Lc);
		//displayAPDU(buffer);
		cmd = new CommandAPDU(command);
		this.sendAPDU(cmd, DISPLAY);
	}


	void readNameFromCard() {
		CommandAPDU cmd;
        ResponseAPDU resp;

	    byte[] cmd_1= {CLA,READNAMEFROMCARD,P1,P2,(byte)0};
            cmd = new CommandAPDU( cmd_1 );
            resp = this.sendAPDU( cmd, DISPLAY );
			byte[] bytes = resp.getBytes();
	    	String msg = "";
	    	for(int i=0; i<bytes.length-2;i++)
		    	msg += new StringBuffer("").append((char)bytes[i]);
	    	System.out.println(msg);
	}


	void writeNameToCard() {
		byte[] name= readKeyboard().getBytes();
		int Lc= name.length;
		byte[] header= {CLA,WRITENAMETOCARD,P1,P2,(byte)Lc};
		//byte[] data = name.getBytes();
		byte[] command = new byte[Lc+5];
		System.arraycopy(header,(short)0,command,(short)0,(short)5);
		System.arraycopy(name,(short)0,command,(short)5,(short)Lc);
		//displayAPDU(buffer);
		cmd = new CommandAPDU(command);
		this.sendAPDU(cmd, DISPLAY);
	}


	void exit() {
		loop = false;
	}


	void runAction( int choice ) {
		switch( choice ) {

			case 26: getFileByNumber(); break;
			case 25: listFilesStored(); break;
			case 23: compareTwoFiles(); break;
			
			case 14: updateCardKey(); break;
			case 13: uncipherFileByCard(); break;
			case 12: cipherFileByCard(); break;
			case 11: cipherAndUncipherNameByCard(); break;
			case 10: readFileFromCard(); break;
			case 9: writeFileToCard(); break;
			case 8: updateWritePIN(); break;
			case 7: updateReadPIN(); break;
			case 6: displayPINSecurity(); break;
			case 5: desactivateActivatePINSecurity(); break;
			case 4: enterReadPIN(); break;
			case 3: enterWritePIN(); break;
			case 2: readNameFromCard(); break;
			case 1: writeNameToCard(); break;
			case 0: exit(); break;
			default: System.out.println( "unknown choice!" );
		}
	}


	String readKeyboard() {
		String result = null;

		try {
			BufferedReader input = new BufferedReader( new InputStreamReader( System.in ) );
			result = input.readLine();
		} catch( Exception e ) {}

		return result;
	}


	int readMenuChoice() {
		int result = 0;

		try {
			String choice = readKeyboard();
			result = Integer.parseInt( choice );
		} catch( Exception e ) {}

		System.out.println( "" );

		return result;
	}


	void printMenu() {
		System.out.println( "" );

		System.out.println( "26: get file by number" );
		System.out.println( "25: list files of the card" );
		System.out.println( "23: compare two files" );

		System.out.println( "14: update the DES key within the card" );
		System.out.println( "13: uncipher a file by the card" );
		System.out.println( "12: cipher a file by the card" );
		System.out.println( "11: cipher and uncipher a name by the card" );
		System.out.println( "10: read a file from the card" );
		System.out.println( "9: write a file to the card" );
		System.out.println( "8: update WRITE_PIN" );
		System.out.println( "7: update READ_PIN" );
		System.out.println( "6: display PIN security status" );
		System.out.println( "5: desactivate/activate PIN security" );
		System.out.println( "4: enter READ_PIN" );
		System.out.println( "3: enter WRITE_PIN" );
		System.out.println( "2: read a name from the card" );
		System.out.println( "1: write a name to the card" );
		System.out.println( "0: exit" );
		System.out.print( "--> " );
	}


	void mainLoop() {
		while( loop ) {
			printMenu();
			int choice = readMenuChoice();
			runAction( choice );
		}
	}


	public static void main( String[] args ) throws InterruptedException {
		new TheClient();
	}

}
