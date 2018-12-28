package exampleChain;

import java.util.ArrayList;
import java.util.Date;

//Die Klasse "TransaktionsBlock" wird fuer die Demonstration der Transaktionsfunktion genutzt.
//Da die Klasse "Block" damit lediglich erweitert wird, stellt diese Klasse eine Kindklasse dieser dar.
public class TransaktionBlock extends Block {
	
	//"topHash" enthaelt die Wurzel des Hashbaumes ueber alle Transaktionen im Block.
	private String topHash;
	
	//Die Transaktionen im TransaktionBlock werden in einer Liste gespeichert.
	private ArrayList<Transaktion> transaktionen = new ArrayList<Transaktion>();
	
	//Konstruktor
	public TransaktionBlock(String letzterHash)
	{
		//Nutzen des Attributes "letzterHash" aus der Elternklasse "Block".
		super(letzterHash);
		
		this.letzterHash = letzterHash;
		this.zeitstempel = new Date().getTime();
		this.dieserHash = berechneHash();
		}
	
	//getter
	public String getTopHash() {return this.topHash;}
	public ArrayList<Transaktion> getTransaktionen() {return this.transaktionen;}
	
	//Beinhaltet die zu verschluesselnden Daten.
	public String erhalteBlockdaten() {
		return letzterHash + Long.toString(zeitstempel) + Integer.toString(nonce) + topHash;
	}
	
	//Errechnet mit "ExampleChain.nutzeSha256(String)" einen Hash aus den definierten Daten in "erhalteBlockdaten()".
	//Rueckgabewert definiert die Variable "dieserHash" im Konstruktor.
	public String berechneHash() {
		String hash = ExampleChain.nutzeSha256(erhalteBlockdaten());
		return hash;
	}

	//Definierung des Attributes "topHash".
	//Pruefung des errechneten Hashwertes aus "berechneHash()" in Relation zu "schwierigkeit".
	public void hashFinden(int schwierigkeit) {
		topHash = bekommeTopHash(transaktionen);
		do{
			nonce+=1;
			dieserHash = berechneHash();
		}while(!dieserHash.substring( 0, schwierigkeit).equals(ExampleChain.zielwert)); 
	}
	
	//Fuegt eine Transaktion der Transaktionsliste des TransaktionBlock hinzu. Dabei wird die Methode transaktionBearbeiten() angestossen.
	public boolean transaktionHinzufuegen(Transaktion tr) {
		
		//Abbrechen, wenn die im Parameter uebergebene Transaktion nicht vorhanden ist. 
		if(tr == null) return false;
		
		//Abbrechen, wenn es sich nicht um die erste Transaktion handelt und die Transaktion ungueltig ist.
		if((!"0".equals(letzterHash)) && (tr.transaktionBearbeiten() != true) ) { 	
				System.out.println("Transaktion ungueltig!\ntransaktionHinzufuegen() liefert \"false\" zurueck!");
				return false;
		}
		
		//Transaktion wird in die Liste "transaktionen" hinzugefuegt.
		transaktionen.add(tr);
		System.out.println("Transaktion zum Block hinzufuegen: erfolgreich!");
		return true;
	}
	
	//Gegenseitige Verschluesselung der Hashwerte der Transaktionen, bis ein Hashwert alle Transaktionen repraesentiert.
	public static String bekommeTopHash(ArrayList<Transaktion> transaktionen) {
		
		//Anzahl der Transaktionen im TransaktionBlock
		int count = transaktionen.size();
		
		ArrayList<String> letzteEbene = new ArrayList<String>();
		
		//Lokale Liste "letzteEbene" mit den IDs der Transaktionen befuellen.
		for(int i = 0; i<transaktionen.size();i++) {
			letzteEbene.add(transaktionen.get(i).getTransaktionID());
		}
		
		//Behandlung der fehlschlagenden Testtransaktion.
		if(letzteEbene.size() == 0) return "";
		
		//Wenn nur eine Transaktion pro TransaktionBlock, gilt der Hashwert von diesem als topHash.
		if(letzteEbene.size() == 1) return letzteEbene.get(0);
		
		do{
			//Liste "dieseEbene" mit jedem Durchlauf neu instanziieren.
			ArrayList<String> dieseEbene = new ArrayList<String>();

			//Paarweise die transaktionsIDs miteinander verschlüsseln (Nur gerade Anzahl an Transaktionen im Block zulaessig).
			for(int i=1; i < letzteEbene.size(); i=i+2) {
				dieseEbene.add(ExampleChain.nutzeSha256(letzteEbene.get(i-1) + letzteEbene.get(i)));
			}
			
			//Aktuelle Anzahl an Hashwerten.
			count = dieseEbene.size();
			
			//Gleichsetzung der Listen, fuer die Bedingung der for-Schleife.
			letzteEbene = dieseEbene;
		}while(count == 1); //Austreten der Schleife, bei einer Listengroesse von 1. Dies ist der topHash.

		return letzteEbene.get(0);
	}
}