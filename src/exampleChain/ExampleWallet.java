package exampleChain;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExampleWallet {
	
	private PrivateKey sk;		//geheimer Schlüssel
	private PublicKey pk;		//öffentlicher Schlüssel
	
	//Liste ueber die Transaktionsausgaben, auf die die ExampleWallet Zugriff hat.
	private HashMap<String,TransaktionAusgabe> restTrAusgaben = new HashMap<String,TransaktionAusgabe>();
	
	//Konstruktor
	public ExampleWallet() {
		generiereSchluessel();
	}
	
	//getter
	public PrivateKey getSk() {return this.sk;}
	public PublicKey getPk() {return this.pk;}
	public HashMap<String,TransaktionAusgabe> getRestTrAusgaben(){return this.restTrAusgaben;}
		
	//Generierung des Schluesselpaares nach ECDSA.
	public void generiereSchluessel() {
		try {
			ECGenParameterSpec ecGenSpec = new ECGenParameterSpec("prime192v1");
		
			//PublicKey/PrivateKey-Generator instantiiert. Als Algrorithmus wird "ESDSA" genutzt. Der Provider ist BouncyCastle ("BC").
			KeyPairGenerator g = KeyPairGenerator.getInstance("ECDSA","BC");
			g.initialize(ecGenSpec, new SecureRandom());
			
			//Schluesselpaar wird generiert.
	        KeyPair Schluessel = g.generateKeyPair();
			
	        sk = Schluessel.getPrivate();	//PrivateKey wird vergeben
	        pk = Schluessel.getPublic();	//PublicKey wird vergeben
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	//Gibt den Kontostand der ExampleWallet zurueck
	public int bekommeKontostand() {
		int sum = 0;	
        for (Map.Entry<String, TransaktionAusgabe> iterator: ExampleChain.restTrAusgaben.entrySet()){

        	//vom entsprechenden Eintrag von den allgemeinen Wertresten ("ExampleChain.restTrAusgaben") wird der Wert gespeichert.
        	TransaktionAusgabe restwert = iterator.getValue();
        	
        	//vom gespeichertem Wert wird geprueft, ob dieser der ausfuehrenden ExampleWallet gehoert.
            if(restwert.gehoertMir(pk)) {
            	
            	//Der Eintrag von den Restwerten der Blockchain ("ExampleChain.restTrAusgaben") wird in den Restwerten der ExampleWallet gespeichert.
            	restTrAusgaben.put(restwert.ID,restwert);
            	
            	//Alle Restwerte, welche die Bedingungen erfuellen, werden addiert und zurueckgegeben.
            	sum = sum + restwert.getWert();
            }
        }
		return sum;
	}
	
	//Versendet Coins zu einer anderen ExampleWallet.
	public Transaktion versendeCoins(PublicKey pkEmpfaenger,int wert ) {
		
		//Wenn der uebergebene Wert ("wert") groesser als der Kontostand ist.
		if(bekommeKontostand() < wert) {
			System.out.println("\nNicht genuegend Coins auf dem Konto! Transaktion nicht ausgefuehrt!\n");
			return null;
		}
		
		//Lokale ArrayList "eingaben", um die angesprochenen Ausgaben für die Transaktion aus restTrAusgaben zu loeschen.
		ArrayList<TransaktionEingabe> verbrauchteEingaben = new ArrayList<TransaktionEingabe>();
		
		int sum = 0;
		for (Map.Entry<String, TransaktionAusgabe> iterator: restTrAusgaben.entrySet()){
			
			//vom entsprechenden Eintrag von den eigenen Wertresten ("restTrAusgaben") wird der Wert gespeichert.
			TransaktionAusgabe restTrAusgabe = iterator.getValue();
			
			//Die Werte der Eintraege werden in "sum" zusammenaddiert.
			sum = sum + restTrAusgabe.getWert();
			
			//ID's von dem jeweiligen Wertrest ("restTrAusgaben") wird in die lokale Liste "eingaben" gespeichert.
			verbrauchteEingaben.add(new TransaktionEingabe(restTrAusgabe.ID));
			
			//Sind die summierten Werte der Eintraege groesser als der versendete Wert "wert", wird aus der Schleife ausgebrochen.
			if(sum > wert) break;
		}

		//Eine neue Transaktion wird instantiiert. In den Parametern findet sich die lokale Liste wieder, aus denen die Wertemenge bezogen wird.
		Transaktion neueTransaktion = new Transaktion(this.pk, pkEmpfaenger , wert, verbrauchteEingaben);
		
		//Die Transaktion wird mit dem Private-Key ("sk") der ExampleWallet signiert.
		neueTransaktion.generiereSignatur(sk);
		
		//Loeschung der, fuer die Transaktion genutzten, Listeneintraege in restTrAusgaben.
		for(int i=0; i<verbrauchteEingaben.size(); i++){
			
			//Von den Restwerten der ExampleWallet werden die Eintraege der lokalen Liste "eingaben" geloescht, da nun bereits ausgegeben.
			restTrAusgaben.remove(verbrauchteEingaben.get(i).getTrAusgabeID());
		}
		
		return neueTransaktion;
	}
	
}