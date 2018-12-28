package exampleChain;

import java.security.PublicKey;

public class TransaktionAusgabe {
	
	private PublicKey neuerBesitzer; 	//neuer Besitzer der Coins.
	private int wert; 					//Wert der Coins.
	private String idStammTransaktion;	//Die ID der Transaktion, in der die TransaktionAusgabe erstellt worden ist.	
	public String ID;					//Identifizierung dieser TransaktionAusgabe (Hashwert aller restlichen Attribute).
	
	//Konstruktor
	public TransaktionAusgabe(PublicKey neuerBesitzer, int wert, String idStammTransaktion) {
		this.neuerBesitzer = neuerBesitzer;
		this.wert = wert;
		this.idStammTransaktion = idStammTransaktion;
		
		this.ID = ExampleChain.nutzeSha256(Transaktion.stringVonPk(neuerBesitzer)+Integer.toString(wert)+idStammTransaktion);
	}
	//getter
	public PublicKey getNeuerBesitzer() {return this.neuerBesitzer;}
	public int getWert() {return this.wert;}
	public String getIdStammTransaktion() {return  this.idStammTransaktion;}
	public String getID() {return this.ID;}	
	
	//gehoertMir prueft, ob die ein Public-Key als Parameter der eingetragene Empfaenger ("neuerBesitzer") ist.
	public boolean gehoertMir(PublicKey pk) {
		return (pk == neuerBesitzer);
	}
	
}