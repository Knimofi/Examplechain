package exampleChain;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

import org.bouncycastle.util.encoders.Hex;


public class ExampleChain {

	//Liste von allen Blocks fuer die Demonstration der Hashlogik.
	public static ArrayList<Block> exampleChainData = new ArrayList<Block>();
	
	//Liste von allen Blocks fuer die Demonstration der Transaktionsfunktion.
	public static ArrayList<TransaktionBlock> exampleChainTransaktion = new ArrayList<TransaktionBlock>();
	
	//HashMap über alle unverbrauchten Transaktionsausgaben.
	public static HashMap<String,TransaktionAusgabe> restTrAusgaben = new HashMap<String,TransaktionAusgabe>();
	
	//HashMap äquivalent zu restTrAusgaben. Wird in der Methode validiereExampleChainTransaktion() genutzt.
	public static HashMap<String,TransaktionAusgabe> tempRestTrAusgaben = new HashMap<String,TransaktionAusgabe>();
	
	//"ersteTransaktion" stellt ein Sonderfall dar, da "letzterHash" gleich "0" sein muss.
	//Kommt in der Demonstration der Transaktionsfunktion vor.
	public static Transaktion ersteTransaktion;

	//"schwierigkeit" bestimmt die Anzahl Nullen, die ein Hashwert als Praefix haben muss.
	public static final int schwierigkeit = 4;
	
	//"transaktionPerBlock" bestimmt die maximale Anzahl an Transaktionen in einem Block.
	public static final int transaktionPerBlock = 4;
	
	//Die Liste beinhaltet alle benötigten Wallets bzw. Nutzer für die Transaktionsfunktion(ursprung, Alice, Bob, miner)
	public static ArrayList<ExampleWallet> wallets = new ArrayList<ExampleWallet>();
	
	//"zielwert" besteht aus einer Zeichenkette von Nullen ("0") in der Länge der Schwierigkeit
	public static String zielwert = new String(new char[schwierigkeit]).replace("\0", "0");
	
	//------------------------------------------------------------------------//
	//----------------------------------MAIN----------------------------------//
	//------------------------------------------------------------------------//
	public static void main(String[] args) throws IOException {
		
		//"demoOption" stellt die Wahl der beabsichtigte Demonstration dar.
		//"br" stellt die Anzahl Coins, die in der Transaktionsfunktion uebertragen werden dar.
		BufferedReader demoOption = new BufferedReader(new InputStreamReader(System.in));
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));			
	    
		System.out.println("Möchten Sie die Hashlogik der Blockchain demonstrieren (1) ?\nMöchten Sie die Transaktionsfunktion demonstrieren (2) ?\nEingabe: ");
		
		String strOption = demoOption.readLine(); //Einlesen von "demoOption" als String in "strOption".
	    int option = Integer.parseInt(strOption); //Umwandlung von "strOption" zum Datentyp Integer in "option".

		System.out.println("Option: " + option);  //Ausgabe des eingegebenen Wertes in der Konsole.
		
		//In der switch-Verzweigung bestimmt sich die Art der Demonstration der Funktionsweise der Blockchain-Technologie
		//Option 1: Hashlogik erkennbar. Daten sind beliebige Zeichenketten.
		//Option 2: Hashlogik, Signatur- und Transaktionsfunktion erkennbar. Daten sind Integerwerte, welche zwischen Wallets(Konten) ueberwiesen werden.
		switch (option) {
		case 1:
			int i=1;		//laufende Nummer. Bestimmt in der while-Schleife den ausgewaehlten Listeneintrag in "exampleChainData".
			String input;	//In "input" wird in der while-Schleife die eingegebene Zeichenkette gespeichert.
	    
			//Die ersten zwei Blocks werden außerhalb der Schleife erstellt,
			//da der erste Block mit dem Wert "0" für "letzterHash" eine Außnahme darstellt.
			//Der zweite Block ist als Anleitung zur Anwendung gedacht und in diesem Fall optional.

			exampleChainData.add(new Block("0", "Hallo Welt! Ich bin der erste von allen kommenden Blocks, deswegen ist mein letzter Hashwert 0!"));	
			exampleChainData.get(0).hashFinden(schwierigkeit);	//berechnet gueltigen Hashwert fuer den entsprechenden Block.
			exampleChainData.get(0).neuerBlockInfo();			//Zeigt in der Konsole die Attribute des entsprechenden Blocks an.
		
			exampleChainData.add(new Block(exampleChainData.get(exampleChainData.size()-1).dieserHash, "WILLKOMMEN! Das ist der 2. Block. Die Inhalte der nächsten Blocks können Sie selbst wählen."));
			exampleChainData.get(1).hashFinden(schwierigkeit);	//berechnet gueltigen Hashwert fuer den entsprechenden Block
			exampleChainData.get(1).neuerBlockInfo();			//Zeigt in der Konsole die Attribute des entsprechenden Blocks an

			//Schleife zur Generierung von neuen Blocks mit beliebigen Inhalt in Form einer Zeichenkette.
			while(true)
			{
				i = i + 1;	//Laufende Nummer.
				System.out.print("Inhalt vom nächsten Block: ");
			    input = br.readLine();	//Eingabe des Strings und Speicherung in "input"
			    
			    //Blockgenerierung und anfuegen an die BlockChain.
				exampleChainData.add(new Block(exampleChainData.get(exampleChainData.size()-1).dieserHash, input));
				
				//Berechnung des Hashwertes in Abhaengigkeit der Schwierigkeit.
				exampleChainData.get(i).hashFinden(schwierigkeit);

				//Anzeige der Berechnungszeit und des berechneten Hashwertes des neuen Blocks.
				exampleChainData.get(i).neuerBlockInfo();
				
				//Validiert die Korrektheit der Hashwerte in der Blockchain ("exampleChainData").
				validiereExampleChainData();
			}
		case 2:
			//BouncyCastle als SecurityProvider hinzufuegen.
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
			
			//Aktuell angesprochener Block.
			ArrayList<TransaktionBlock> trBlockList = new ArrayList<TransaktionBlock>();

			String sum, nextSender;			//Variablen zur Eingabe in der Konsole.
			int SumToInt, nextSenderToInt;	//Variablen zur Typumwandlung von "sum" und "nextSender".

			int count = 4;					//Eintrittswert in der Schleife (wegen automatisierten ersten Transaktionen beginnt dieser bei 4)
			int tmp = count;				//Vergleichswert zur Prüfung, ob der aktuelle Block in der Schleife sein Maximum an Transaktionen erreicht hat.
			Transaktion neueTransaktion;	//Instanz für neu ausgeführte Transaktion in der Schleife.
			
			ExampleWallet ursprung = new ExampleWallet();	//ExampleWallet fuer die erste Transaktion zum Beispielnutzer Alice.
			ExampleWallet Alice = new ExampleWallet();		//Beispielnutzer der ExampleChain.
			ExampleWallet Bob = new ExampleWallet();		//Beispielnutzer der ExampleChain.
			ExampleWallet miner  = new ExampleWallet();		//Empfaenger der Coinbase-Transaktion.
			
			//Befüllen der Liste "wallets" mit den deklarierten ExampleWallets.
			wallets.add(0,ursprung);
			wallets.add(1,Alice);
			wallets.add(2,Bob);
			wallets.add(3,miner);
					
			//"ursprung" versendet die ersteTransaktion (1000 Coins) an "Alice".
			ersteTransaktion = new Transaktion(wallets.get(0).getPk(), wallets.get(1).getPk(), 1000, null);
			
			//Die erste Transaktion wird mit dem Private-Key von "ursprung" signiert.
			ersteTransaktion.generiereSignatur(wallets.get(0).getSk());	
				
			//Die Transaktionsausgabe wird der Transaktion hinzugefuegt.
			ersteTransaktion.getAusgaben().add(new TransaktionAusgabe(ersteTransaktion.getPkEmpfaenger(), ersteTransaktion.getWert(), ersteTransaktion.getTransaktionID()));
			
			//die erste Transaktion wird in der HashMap "restTrAusgaben" gespeichert, um bei den naechsten Transaktionen die ID als Input auszuwaehlen.
			restTrAusgaben.put(ersteTransaktion.getAusgaben().get(0).ID, ersteTransaktion.getAusgaben().get(0)); 
			
			System.out.println("Erster Block wird der Blockchain angefuegt...\n");
			
			//Der erste Block in der Liste erhaelt als "letzterHash" den Wert "0", da es keinen letzten Hash gibt, an den referenziert werden kann.
			trBlockList.add(new TransaktionBlock("0"));
			
			trBlockList.get(0).transaktionHinzufuegen(ersteTransaktion);
			
			//Die erste Stelle der Liste wird der BlockChain "exampleChainTransaktion" angefuegt
			blockHinzufuegen(trBlockList.get(0));
			
			//Einige automatisch ausgefuehrte Transaktionen zum Testen
			//"Alice" sendet 400 Coins an "Bob"
			trBlockList.add(new TransaktionBlock(trBlockList.get(0).dieserHash));
			System.out.println("\nDer Kontostand von Alice betraegt: " + wallets.get(1).bekommeKontostand());
			System.out.println("\nAlice initiiert eine Ueberweisung von 400 Coins an Bob\n");
			trBlockList.get(1).transaktionHinzufuegen(wallets.get(1).versendeCoins(wallets.get(2).getPk(), 400)); //Transaktion zur Liste hinzufuegen
			blockHinzufuegen(trBlockList.get(1)); //Listeneintrag in die BlockChain anfuegen
			
			zeigeStatus(1, trBlockList);	//Anzeige auf der Konsole ueber den Kontostand von "Alice" und "Bob"

			//"Alice" sendet an "Bob" mehr Coins als sie hat
			trBlockList.add(new TransaktionBlock(trBlockList.get(1).dieserHash));
			System.out.println("\nAlice initiiert eine Ueberweisung von 1000 Coins an Bob\n");
			trBlockList.get(2).transaktionHinzufuegen(wallets.get(1).versendeCoins(wallets.get(2).getPk(), 1000));
			blockHinzufuegen(trBlockList.get(2));
			
			zeigeStatus(2, trBlockList);	//Anzeige auf der Konsole ueber den Kontostand von "Alice" und "Bob"
			
			//"Bob" sendet 200 Coins an "Alice"
			trBlockList.add(new TransaktionBlock(trBlockList.get(2).dieserHash));
			System.out.println("\nBob initiiert eine Ueberweisung von 200 Coins an Alice");
			trBlockList.get(3).transaktionHinzufuegen(wallets.get(2).versendeCoins( wallets.get(1).getPk(), 400));
			blockHinzufuegen(trBlockList.get(3));
			
			zeigeStatus(3, trBlockList);	//Anzeige auf der Konsole ueber den Kontostand von "Alice" und "Bob"
			
			validiereExampleChainTransaktion();
			trBlockList.add(new TransaktionBlock(trBlockList.get(count-1).dieserHash));	//Neuen Block, mit dem Hashwert des letzten Listeneintrages, in die Liste "trBlockList" einfuegen


			while(true) {
				//Auswahl des Senders ueber die Konsole.
				System.out.println("\nSoll Alice (1) oder Bob (2) Coins versenden?");
				nextSender = br.readLine();						//Eingabe als String speichern.
				nextSenderToInt = Integer.parseInt(nextSender);	//Eingabe in Integer umwandeln.

				if(tmp != count) {
					//Neuen Block, mit dem Hashwert des letzten Listeneintrages als Verkettungsbeziehung, in die Liste "trBlockList" einfuegen.
					trBlockList.add(new TransaktionBlock(trBlockList.get(count-1).dieserHash));
				}
				
				//Wenn "Alice" als Absender gewaehlt worden ist.
				if(nextSenderToInt == 1) {
				
					//Den Wert der Transaktion ueber die Konsole bestimmen.
					System.out.println("\nWieviel Coins soll Alice an Bob versenden?");
					sum = br.readLine();						//Eingabe als String speichern.
					SumToInt=Integer.parseInt(sum);				//Eingabe in Integer umwandeln.
									
					System.out.println("\nAlice initiiert eine Ueberweisung von " + SumToInt + " Coins an Bob\n");
					
					//Eingegebener Wert, Absender und Empfaenger werden in eine Transaktion gespeichert.
					neueTransaktion = wallets.get(1).versendeCoins( wallets.get(2).getPk(), SumToInt);
					
					//Transaktion wird geprueft und evtl. zum Block hinzugrfuegt.
					trBlockList.get(count).transaktionHinzufuegen(neueTransaktion);
					
					//Pruefung, ob Block sein Maximum an Transaktionen erreicht hat.
					//Wenn ja: Block an die Blockchain anfügen, Blockchain anschließend validieren und Status anzeigen.
					//Wenn nein: Nur den Kontostand von Alice, Bob und dem Miner anzeigen.
					if(trBlockList.get(count).getTransaktionen().size() > transaktionPerBlock-1) {
						
						//Ausgewaehlter Block in der Liste wird mitsamt beinhaltenden Transaktionen der Blockchain hinzugefuegt.
						blockHinzufuegen(trBlockList.get(count));
						
						//Anzeige auf der Konsole ueber den Kontostand von "Alice" und "Bob" und den Status der Blockchain.
						zeigeStatus(count, trBlockList);
						
						//Inklusive den neuen Block, die gesamte Blockchain ueberpruefen.
						validiereExampleChainTransaktion();
					}else {
						zeigeBilanz(count, trBlockList);
					}
				}
				//Wenn "Bob" als Absender gewaehlt worden ist.
				else if(nextSenderToInt == 2) {
					System.out.println("\nWieviel Coins soll Bob an Alice versenden?");

					sum = br.readLine();
				    SumToInt=Integer.parseInt(sum);
					
				    System.out.println("\nBob initiiert eine Ueberweisung von " + SumToInt + " Coins an Alice");
					neueTransaktion = wallets.get(2).versendeCoins( wallets.get(1).getPk(), SumToInt);
					trBlockList.get(count).transaktionHinzufuegen(neueTransaktion);
					
					if(trBlockList.get(count).getTransaktionen().size() > transaktionPerBlock-1) {
						blockHinzufuegen(trBlockList.get(count)); 
						zeigeStatus(count, trBlockList);
						validiereExampleChainTransaktion();
					}else {
						zeigeBilanz(count, trBlockList);
					}
				}
				//Behandlung von ungueltiger Eingabe bei der Auswahl des Absenders.
				else if(nextSenderToInt != 1 || nextSenderToInt != 2){
					System.out.println("Ungueltige Eingabe!");
					break;
				}
				
				//Falls ein neuer Block angefügt worden ist, count um 1 aufzählen.
				//Dadurch wird beim nächsten Schleifendurchlauf ein neuer Block erzeugt.
				if(trBlockList.get(count).getTransaktionen().size() > transaktionPerBlock-1) {
					tmp = count; count++;
				}else {
					tmp = count;
				}
			}
		//Bei ungueltiger Eingabe bei der Auswahl der Demonstration.
		default:
			System.out.println("\nUngueltige Eingabe!");
		};
	}
	
	//Verschluesselt die Daten im Parameter "daten" mit dem SHA256-Algorithmus und gibt sie als String zurueck.
	public static String nutzeSha256(String daten){
			try {
				MessageDigest msd = MessageDigest.getInstance("SHA-256");
		        
				//Wandelt die Eingabedaten in einen Hashwert um.
				byte[] hash = msd.digest(daten.getBytes(StandardCharsets.UTF_16));
				
				//Wandelt den generierten Hashwert in einen String um. (Hex.encode(x) wird von Bouncycastle bereitgestellt).
				String strHash = new String(Hex.encode(hash));
				return strHash;
			}
			catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
	
	//Pruefung der Blockchain auf drei Bedingungen:
	//		Gleichheit zwischen berechneten Hashwert und eingetragenen Hashwert
	//		Gleichheit zwischen eingetragenem letzten Hashwert und Hashwert des letzten Blocks
	//		Einhaltung der Schwierigkeit des zu berechnenden Hashwertes
	public static Boolean validiereExampleChainData() {
		Block dieserBlock, letzterBlock; 
		
		//Pruefung ueber jeden Eintrag/Block in der Liste "exampleChainData"
		for(int i=1; i < exampleChainData.size(); i++) {
			
			dieserBlock = exampleChainData.get(i);			//Jeweiliger Eintrag in "dieserBlock" speichern
			letzterBlock = exampleChainData.get(i-1);		//Jeweiliger vorheriger Eintrag in "letzterBlock" speichern
			
			
			//Vergleich ueber die Gleichheit des eingetragenen vorherigen Hashwertes und dem Hashwert des letzten Blocks
			if(!letzterBlock.dieserHash.equals(dieserBlock.letzterHash) ) {
				System.out.println("/nDer letzte Hashwert des Blocks an der Stelle " + i + " ist ungueltig!");
				return false;
			}
			//Pruefung ueber der Einhaltung des Schwierigkeitsgrades des Hashwertes
			if(!dieserBlock.dieserHash.substring( 0, schwierigkeit).equals(zielwert)) {
				System.out.println("\nHashwert in Bezug auf die Schwierigkeit an der Stelle " + i +" ungueltig!");
				return false;
				}
			//Vergleich ueber die Gleichheit des eingetragenen Hashwertes und einer Neuberechnung mit gleichen Bedingungen
			if(!dieserBlock.dieserHash.equals(dieserBlock.berechneHash()) ){
				System.out.println("/nDer Hashwert des Blocks an der Stelle " + i + " ist ungueltig!");
				return false;
				}
			}
		//Sind alle Eintraege der Liste in keine Pruefung aufgefallen, wird es in der Konsole angezeigt
		System.out.println("\nBlockchain wurde erfolgreich geprueft!");
		return true;
	}
			
	//Pruefung der Blockchain auf drei Bedingungen:
	//		Gleichheit zwischen berechneten Hashwert und eingetragenen Hashwert
	//		Gleichheit zwischen eingetragenem letzten Hashwert und Hashwert des letzten Blocks
	//		Einhaltung der Schwierigkeit des zu berechnenden Hashwertes
	public static Boolean validiereExampleChainTransaktion() {
		
		TransaktionBlock dieserBlock, letzterBlock; 
		
		//Pruefung ueber jeden Eintrag/Block in der Liste "exampleChainTransaktion"
		for(int i=1; i < exampleChainTransaktion.size(); i++) {
			dieserBlock = exampleChainTransaktion.get(i);			//Jeweiliger Eintrag in "dieserBlock" speichern
			letzterBlock = exampleChainTransaktion.get(i-1);		//Jeweiliger vorheriger Eintrag in "letzterBlock" speichern
			
			//Vergleich ueber die Gleichheit des eingetragenen Hashwertes und einer Neuberechnung mit gleichen Bedingungen
			if(!dieserBlock.dieserHash.equals(dieserBlock.berechneHash()) ){
				System.out.println("/nDer Hashwert des Blocks an der Stelle " + i + " ist ungueltig!");
				return false;
			}
			//Vergleich ueber die Gleichheit des eingetragenen vorherigen Hashwertes und dem Hashwert des letzten Blocks
			if(!letzterBlock.dieserHash.equals(dieserBlock.letzterHash) ) {
				System.out.println("\nDer letzte Hashwert des Blocks an der Stelle " + i + " ist ungueltig!");
				return false;
			}
			//Pruefung ueber der Einhaltung des Schwierigkeitsgrades des Hashwertes
			if(!dieserBlock.dieserHash.substring( 0, schwierigkeit).equals(zielwert)) {
				System.out.println("\nHashwert in Bezug auf die Schwierigkeit an der Stelle " + i +" ungueltig!");
				return false;
			}
			
			//Pruefung aller Transaktionen
			tempRestTrAusgaben.put(ersteTransaktion.getAusgaben().get(0).ID, ersteTransaktion.getAusgaben().get(0)); //ersteTransaktion Ausgabe

			TransaktionAusgabe tmpAusgabe;
			Transaktion dieseTransaktion;

			//Pruefung ueber jeden Eintrag/Transaktion in der Liste "transaktionen"
			for(int b=0; b <dieserBlock.getTransaktionen().size(); b++) {
				dieseTransaktion = dieserBlock.getTransaktionen().get(b);		//Speicherung der jeweiligen Transaktion in "dieseTransaktion"
				
				//Signatur wird mitteld der java.security.Signature.* Methoden geprueft
				if(dieseTransaktion.pruefeSignatur() == false) {
					System.out.println("\nDie Signatur der Transaktion an Stelle " + b + " ist fehlerhaft!\n");
					return false; 
				}
				
				//Pruefung ueber den Ursprung der Ausgabe in der Transaktion
				if(dieseTransaktion.bekommeEingabewert() != dieseTransaktion.bekommeAusgabewert()) {
					System.out.println("Die Eingaben der Transaktion " + b + " sind mit dessen Ausgaben nicht identisch!");
					return false; 
				}
				
				//Pruefung der Eintraege der Liste "ausgaben" auf zwei Bedingungen:
				//Der eingetragene Besitzer der Coins in der Liste und in der Transaktion wird verglichen
				if( !(dieseTransaktion.getAusgaben().get(0).getNeuerBesitzer() == dieseTransaktion.getPkEmpfaenger())) {
					System.out.println("Bei der Transaktion " + b + " ist der Empfaenger ungleich dem in der Liste \"ausgaben\"!");
					return false;
				}
				//Pruefung, ob der Absender der Transaktion den gesendeten Mehrwert zurueckerhaelt
				if( !(dieseTransaktion.getAusgaben().get(1).getNeuerBesitzer() == dieseTransaktion.getPkSender())) {
					System.out.println("Bei der Transaktion " + b + " hat der Absender ungleich an Coins zurueckbekommen, als er sollte!");
					return false;
				}				

				//Pruefung der Eintraege der Liste "eingaben" auf zwei Bedingungen:
				//		Die Assoziation von Listeneintrag und der Ausgabe, durch die der Eintrag entstanden ist
				//		Gleichheit der unverbrauchten Transaktionsausgaben und dem entsprechenden Wert in der HashMap
				for(int j = 0; j<dieseTransaktion.getEingaben().size(); j++){
					tmpAusgabe = tempRestTrAusgaben.get(dieseTransaktion.getEingaben().get(j).getTrAusgabeID());
					
					//Wenn der Ursprung einer Ausgabe nicht auffindbar ist
					if(tmpAusgabe == null) {
						System.out.println("Ursprung der Ausgabe in Block " + i + " in Transaktion " + b + " nicht auffindbar!");
						return false;
					}				
					//Vergleich der unverbrauchten Transaktionsausgabe und dem Wert in der HashMap
					if(!(dieseTransaktion.getEingaben().get(j).getRestTrAusgabe().getWert() == tmpAusgabe.getWert())) {
						System.out.println("Unverbrauchte Transaktionsausgaben der Transaktion " + b + " in Block " + i + " ist ungleich dem Eintrag in der HashMap!");
						return false;
					}
				}
				for(int o=0;o<dieseTransaktion.getAusgaben().size();o++) {
					tempRestTrAusgaben.put(dieseTransaktion.getAusgaben().get(o).ID, dieseTransaktion.getAusgaben().get(o));
				}				
			}			
		}
		System.out.println("\nBlockchain wurde erfolgreich geprueft!");
		return true;
	}
	
	//Einen neuen Block uebergeben und der BlockChain anfuegen
	public static void blockHinzufuegen(TransaktionBlock neuerBlock) {
		neuerBlock.hashFinden(schwierigkeit);
		exampleChainTransaktion.add(neuerBlock);
		System.out.println("Block erfolgreich der Blockchain angefuegt!\n");
	}
	
	//Ausgabe des Kontostandes und der Hashwerte auf der Konsole
	public static void zeigeStatus(int count, ArrayList<TransaktionBlock> bl){
		System.out.println("Letzter Hash: " + bl.get(count-1).dieserHash);
		System.out.println("Dieser  Hash: " + bl.get(count).dieserHash);
		System.out.println("TopHash: " + bl.get(count).getTopHash());
		zeigeBilanz(count, bl);
	}
	public static void zeigeBilanz(int count, ArrayList<TransaktionBlock> bl){
		System.out.println("\nKontostand von Alice: " + wallets.get(1).bekommeKontostand());
		System.out.println("Kontostand von Bob: " + wallets.get(2).bekommeKontostand());
		System.out.println("Kontostand vom Miner: " + wallets.get(3).bekommeKontostand());
		}
	
}