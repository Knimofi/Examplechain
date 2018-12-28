package exampleChain;

public class TransaktionEingabe {
	private String trAusgabeID; 				//Ist gleich der ID der referenzierten TransaktionAusgabe.	
	
	//restTrAusgabe beinhaltet die unverbrauchte Transaktionsausgabe, auf die sich die TransaktionEingabe bezieht.
	//Der Eintrag in der HashMap "ExampleChain.restTrAusgaben" bei entsprechender "trAusgabeID", wird hier uebertragen.
	private TransaktionAusgabe restTrAusgabe;

	//Konstruktor
	public TransaktionEingabe(String trAusgabeID) {
		this.trAusgabeID = trAusgabeID;
	}
	
	//getter
	public String getTrAusgabeID() {return this.trAusgabeID;}
	public TransaktionAusgabe getRestTrAusgabe() {return this.restTrAusgabe;}
	
	//setter
	public void setRestTrAusgabe(TransaktionAusgabe restTrAusgabe) {this.restTrAusgabe = restTrAusgabe;}
	
}