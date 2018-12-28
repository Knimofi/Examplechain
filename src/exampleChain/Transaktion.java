package exampleChain;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;

public class Transaktion {
	
	private String transaktionID;	//Hash aus pkSender, pkEmpfaenger, wert und count(laufende Nummer).
	private PublicKey pkSender;		//Adresse des Senders.
	private PublicKey pkEmpfaenger; //Adresse des Empfaengers.
	private int wert; 				//Wertemenge, welcher an einen Empfaenger versendet wird.
	private byte[] signatur; 		//Beinhaltet eine generierte Signatur aus pkSender, pkEmpfaenger und wert.
	
	//Listen der beinhaltenden Eingaben und Ausgaben in der Transaktion.
	private ArrayList<TransaktionEingabe> eingaben = new ArrayList<TransaktionEingabe>();
	private ArrayList<TransaktionAusgabe> ausgaben = new ArrayList<TransaktionAusgabe>();
	
	//Um zwei gleiche "transaktionID" Hashes zu vermeiden, wird eine laufende Nummer in "berechneHash()" eingebunden.
	private static int count = 0;
	
	//Konstruktor
	public Transaktion(PublicKey von, PublicKey an, int wert,  ArrayList<TransaktionEingabe> eingaben) {
		this.pkSender = von;
		this.pkEmpfaenger = an;
		this.wert = wert;
		this.eingaben = eingaben;
	}
	
	//getter
	public String getTransaktionID() {return this.transaktionID;}
	public PublicKey getPkSender() {return this.pkSender;}
	public PublicKey getPkEmpfaenger() {return this.pkEmpfaenger;}
	public int getWert() {return this.wert;}
	public byte[] getSignatur() {return this.signatur;}
	public ArrayList<TransaktionEingabe> getEingaben() {return this.eingaben;}
	public ArrayList<TransaktionAusgabe> getAusgaben() {return this.ausgaben;}
	
	//Zu signierende Daten.
	public String erhalteDaten() {
		return stringVonPk(pkSender) + stringVonPk(pkEmpfaenger) + Integer.toString(wert);
	}
	
	//Uebergebener Public-Key wird als String zurueckgegeben.
	public static String stringVonPk(PublicKey pk) {
			return Base64.getEncoder().encodeToString(pk.getEncoded());
	}
	
	//Stoeßt den Prozess an, eine Transaktion auszuführen.
	//Die Methode wird in "TransaktionBlock.transaktionHinzufuegen(Transaktion)" aufgerufen.
	public boolean transaktionBearbeiten() {
		
		//Verifizierung der Signatur.
		if(!(pruefeSignatur() == true)) {
			System.out.println("\nVerifizierung der Signatur fehlgeschlagen!");
			return false;
		}		
		
		//Sicherstellung, dass die Transaktionseingaben nicht schon ausgegeben worden sind.
		//Schaut in die HashMap"restTrAusgaben" nach jeden Eintrag in der Liste "eingaben" nach der trAusgabeID (ist gleich: TransaktionAusgabe.ID)
		//und speichert den passenden HashMap Eintrag im jeweiligen Eintrag von "eingaben" in restTrAusgabe.
		for(int i=0; i<this.eingaben.size(); i++){
			this.eingaben.get(i).setRestTrAusgabe(ExampleChain.restTrAusgaben.get(this.eingaben.get(i).getTrAusgabeID()));
		}
		
		//Generierung der Transaktionsausgaben und hinzufuegen zu der Liste "ausgaben".
		
		//Der Transaktionswert wird von der Summe der Werte in der Liste "eingaben" abgezogen
		int restwert = bekommeEingabewert() - wert;
		
		//transaktionsID wird vergeben
		transaktionID = berechneHash();
		
		//Empfaenger erhealt von Summe der Werte in der Liste "eingaben" den Transaktionswert.
		this.ausgaben.add(new TransaktionAusgabe(this.pkEmpfaenger, wert, transaktionID));
		
		//Der Absender erhaelt den uebrigen Rest	aus der Liste "eingaben".
		this.ausgaben.add(new TransaktionAusgabe(this.pkSender, restwert, transaktionID));			

		if(wert>=100){
			//Coinbase-Transaktion (1% ab dem wert 100)
			this.ausgaben.add(new TransaktionAusgabe(ExampleChain.wallets.get(3).getPk(), wert/100, transaktionID));
		}else {
			//Coinbase-Transaktion (wert=1 bei <100)
			this.ausgaben.add(new TransaktionAusgabe(ExampleChain.wallets.get(3).getPk(), 1, transaktionID));
		}
		
		//Transaktionausgaben der Liste "ausgaben" in die HashMap "restTrAusgaben" einfuegen.
		for(int a=0; a<this.ausgaben.size(); a++){
			ExampleChain.restTrAusgaben.put(this.ausgaben.get(a).ID, this.ausgaben.get(a));
		}
		
		//Loescht in der HashMap "restTrAusgaben" die Eintraege der Liste "eingaben".
		for(int e=0; e<this.eingaben.size(); e++){
			if(this.eingaben.get(e).getRestTrAusgabe() == null) continue; //Ungefundene Transaktionen werden uebersprungen.
			ExampleChain.restTrAusgaben.remove(this.eingaben.get(e).getRestTrAusgabe().ID);
		}		
		return true;
	}
	
	//Eingabewert ist gleich die Summe der Werte aller "restTrAusgabe" Eintraege der Liste "eingaben".
	public int bekommeEingabewert() {
		int sum = 0;
		for(int i=0; i<this.eingaben.size(); i++){
			if(this.eingaben.get(i).getRestTrAusgabe() == null) continue; //Ungefundene Transaktionen werden uebersprungen.
			sum = sum + this.eingaben.get(i).getRestTrAusgabe().getWert();
			}
		return sum;
	}
	
	//Der uebergebene PrivateKey (sk) wird zusammen mit den Daten "pkSender", "pkEmpfaenger" und "wert" verschluesselt.
	//Die Daten "pkSender", "pkEmpfaenger" und "wert" werden von hier aus bei der Ausfuerung in der Konsole ausgegeben.
	public void generiereSignatur(PrivateKey sk) {
		System.out.println("Generiere Signatur der Transaktion mit folgenden Daten:\nAbsender: " + stringVonPk(pkSender) + "\nEmpfänger: " + stringVonPk(pkEmpfaenger) + "\nWert: " + wert + " TestCoins\n");
		Signature sig;
		
		try {
			sig = Signature.getInstance("ECDSA", "BC");
			sig.initSign(sk);
			byte[] temp = erhalteDaten().getBytes();
			sig.update(temp);
			
			this.signatur = sig.sign();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	//Verifiziert eine Signatur:
	//Fuehrt die Funktion "ExampleChain.pruefeECDSASignierung" mit den PublicKey des Absenders, den oben definierten "daten"
	//und der, mit "generiereSignatur", generierten Signatur aus.
	public boolean pruefeSignatur() {
		try {
			Signature sig = Signature.getInstance("ECDSA", "BC");
			sig.initVerify(this.pkSender);
			sig.update(erhalteDaten().getBytes());
			boolean check = sig.verify(this.signatur);
			return check;
			
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	//Ausgabewert ist gleich die Summe der Werte aller Eintraege der Liste "ausgaben".
	public int bekommeAusgabewert() {
		int sum = 0;
		for(int a=0;a<this.ausgaben.size();a++){
			
			//Coinbase-Transaktion wird uebersprungen.
			if(this.ausgaben.get(a).getNeuerBesitzer().equals(ExampleChain.wallets.get(3).getPk())) continue;
			sum = sum + this.ausgaben.get(a).getWert();
		}
		return sum;
	}
	
	//Errechnet mit "ExampleChain.nutzeSha256(String)" einen Hash aus den oben definierten Daten in "daten" und "count".
	//Vergibt in der Methode "transaktionBearbeiten" die "transaktionID". 
	private String berechneHash() {
		
		//Um zwei identische Transaktionen zu vermeiden, wird die laufende Nummer "count" ebenfalls uebergeben.
		count++;
		String hash = ExampleChain.nutzeSha256(erhalteDaten() + count);
		return hash;
	}
}