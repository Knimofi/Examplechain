package exampleChain;

import java.util.Date;

public class Block {

	protected long berechnungszeit;		//Beinhaltet die Zeit, die die Methode nutzeSha256(String) zur Berechnung benötigt.
	protected long zeitstempel; 		//Zeiterfassung, welche bei Instanziierung im Konstruktor aufgerufen wird.
	protected int nonce;				//Ergaenzung zu den verschlüsselten Daten, um Möglichkeit auf Rückschluesse zu vermeiden. 

	protected String letzterHash;		//beinhaltet die digitale Signatur des letzten Blocks.
	protected String dieserHash;		//beinhaltet den Hashwert des aktuellen Blocks.
	protected String daten;				//beinhaltet beliebige Daten in Form einer Zeichenkette.
	
	
	//Konstruktor
	public Block(String letzterHash, String daten){
		this.letzterHash = letzterHash;
		this.daten = daten;
		this.zeitstempel = new Date().getTime();	//zeitstempel als Nummer in Millisekunden.
		
		this.dieserHash = berechneHash();
		}
	
	//Konstruktor für die Kindklasse "TransaktionBlock".
	protected Block(String letzterHash) {this.letzterHash = letzterHash;}

	//getter
	public String getLetzterHash() {return this.letzterHash;}
	public String getDieserHash() {return this.dieserHash;}
	public String getDaten() {return this.daten;}
	
	//Gibt die zu verschlüsselnden Daten an.
 	public String erhalteBlockdaten() {
		return letzterHash + Integer.toString(nonce) + Long.toString(zeitstempel) + daten;
	}
	
	//Errechnet mit "ExampleChain.nutzeSha256(String)" einen Hash aus den definierten Daten.
	//Rueckgabewert definiert die Variable "dieserHash" im Konstruktor.
	public String berechneHash(){
		long berechneHashBeginn = new Date().getTime();
		String hash = ExampleChain.nutzeSha256(erhalteBlockdaten());
		berechnungszeit = (berechneHashBeginn - zeitstempel);
		
		return hash;
	}
	
	//Pruefung des errechneten Hashwertes aus "berechneHash()" in Relation zu "schwierigkeit".
	public void hashFinden(int schwierigkeit){
		System.out.println("Berechne gültigen Hashwert für den nächsten Block!");
		
		//Solange im Praefix nicht die geforderte Anzahl Nullen gegeben sind, wird "dieserHash" neu berechnet.
		do{
			nonce+=1;
			dieserHash = berechneHash();
		}while(!dieserHash.substring( 0, schwierigkeit).equals(ExampleChain.zielwert)); 
	}
	
	//Informationsausgabe auf der Konsole
	public void neuerBlockInfo() {
		System.out.println("\nNeuen Block angefügt!\nletzten Hashwert: " + letzterHash + "\nHashwert: " + dieserHash + "\nBerechnungszeit: " + berechnungszeit + " Millisekunden\nNonce: " + nonce + "\nInhalt: " + daten + "\n");
	}
}
