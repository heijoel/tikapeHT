package tikapeht1;

/**
 *
 * @author joel
 */
import java.sql.*;
import java.util.*;

public class TikapeHT1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException {

        Scanner input = new Scanner(System.in);

        //Ohjelma pyytää käyttäjää syöttämään suoritettavan toiminnon (1,2,3,4)
        //Mikäli käyttäjä antaa jonkin muun syötteen, ohjelman suoritus päättyy.
        while (true) {
            System.out.print("Valitse toiminto: ");
            String toiminto = input.nextLine();

            if (toiminto.equals("1")) {
                opintopisteetVuodelta();
                System.out.println("");
            } else if (toiminto.equals("2")) {
                opiskelijanSuoritukset();
                System.out.println("");
            } else if (toiminto.equals("3")) {
                keskiarvoKurssilta();
                System.out.println("");
            } else if (toiminto.equals("4")) {
                topOpettajatOpMukaan();
                System.out.println("");
            } else {
                break;
            }
        }

    }

    //Tässä, kuten muissakin ohelman metodeissa muodostetaan yhteys satunnaisgeneroitua
    //kurssidataa sisältävään kurssit.db -tietokantaan JDBC-ajurin avulla.
    //Metodissa 1 pyydetään käyttäjältä vuotta, jolta halutaan opintopisteiden yhteismäärä.
    //Parametrisoitu SQL-kysely hakee opintopisteiden yhteismäärän käyttäjän syöttämältä vuodelta.
    public static void opintopisteetVuodelta() throws SQLException {
        Scanner input = new Scanner(System.in);
        Connection db = DriverManager.getConnection("jdbc:sqlite:kurssit.db");

        System.out.print("Anna vuosi: ");
        String opintopisteetVuodelta = input.nextLine();

        try {
            PreparedStatement p = db.prepareStatement("SELECT SUM(K.laajuus) AS summa\n"
                    + "FROM Suoritukset S, Kurssit K\n"
                    + "WHERE S.kurssi_id = K.id AND S.paivays LIKE ?");
            p.setString(1, opintopisteetVuodelta + "%");

            ResultSet r = p.executeQuery();

            //if (r.next() && r.getString("summa")!= null) {    //halutaanko kirjallinen ilmoitus vai vaan 0?
            if (r.next()) {
                System.out.println("Opintopisteiden määrä: " + r.getInt("summa"));
            }

        } catch (SQLException e) {
            System.out.println("Tietokantahaku ei onnistunut");
        }

    }

    //Haetaan käyttäjän syöttämän opiskelijan kaikki opintosuoritukset.
    public static void opiskelijanSuoritukset() throws SQLException {
        Scanner input = new Scanner(System.in);
        Connection db = DriverManager.getConnection("jdbc:sqlite:kurssit.db");

        System.out.print("Anna opiskelijan nimi: ");
        String opiskelija = input.nextLine();

        try {
            PreparedStatement p1 = db.prepareStatement("SELECT COUNT(*) AS opiskelijaTietokannassa FROM Opiskelijat WHERE nimi = ?");
            p1.setString(1, opiskelija);
            ResultSet r1 = p1.executeQuery();

            if (r1.getInt("opiskelijaTietokannassa") < 1) {
                System.out.println("Opiskelijaa ei löytynyt");
            } else {
                
                PreparedStatement p2 = db.prepareStatement("SELECT K.nimi, K.laajuus, S.paivays, S.arvosana\n"
                        + "FROM Opiskelijat O, Kurssit K, Suoritukset S\n"
                        + "WHERE O.id = S.opiskelija_id AND K.id = S.kurssi_id AND O.nimi = ? ORDER BY S.paivays");
                p2.setString(1, opiskelija);
                ResultSet r2 = p2.executeQuery();

                while (r2.next()) {
                    System.out.println(r2.getString("nimi") + " " + r2.getInt("laajuus") + " "
                            + r2.getString("paivays") + " " + r2.getInt("arvosana"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Tietokantahaku ei onnistunut");
        }

    }

    //Haetaan keskiarvo käyttäjän syöttämältä kurssilta. Mikäli kurssia ei ole,
    //käyttäjä saa tästä tulosteen, ja ohjelma siirtyy takaisin main-metodin alkuun.
    public static void keskiarvoKurssilta() throws SQLException {
        Scanner input = new Scanner(System.in);
        Connection db = DriverManager.getConnection("jdbc:sqlite:kurssit.db");

        System.out.print("Anna kurssin nimi: ");
        String kurssi = input.nextLine();

        try {

            //Tässä tarkistetaan, onko käyttäjän antama syöte tietokannassa oleva kurssi.
            //Mikäli syötettyä kurssia ei löydy tietokannasta, tulostetaan tästä tieto käyttäjälle
            //Mikäli kurssi löytyy, siirrytään ohjelman osaan, jossa tehdään syötetyn kurssin
            //keskiarvon laskeva tietokantakysely.
            
            PreparedStatement p1 = db.prepareStatement("SELECT COUNT(*) AS syoteListalla FROM Kurssit WHERE nimi = ?");
            p1.setString(1, kurssi);
            ResultSet r1 = p1.executeQuery();

            if (r1.getInt("syoteListalla") < 1) {
                System.out.println("Kurssia ei löytynyt");
            } else {
                PreparedStatement p2 = db.prepareStatement(
                        "SELECT ROUND(1.0*SUM(S.arvosana)/COUNT(S.arvosana),2) AS keskiarvo"
                        + " FROM Kurssit K, Suoritukset S "
                        + "WHERE K.id=S.kurssi_id AND K.nimi = ?");
                p2.setString(1, kurssi);
                ResultSet r = p2.executeQuery();
                if (r.next()) {
                    System.out.println("Keskiarvo: " + r.getString("keskiarvo"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Keskiarvoa ei pystytty laskemaan");
        }

    }

    //Metodissa haetaan top-lista opettajista annettujen opintopisteiden mukaan järjestettynä.
    //Pyydetään käyttäjältä tulostettavien opettajien lukumäärää. Jos lkm < 1, pyydetään
    //syöttämään sellainen lukumäärä (yksi tai suurempi), jotta toiminnon mukainen
    //tulostus olisi mielekäs.
    public static void topOpettajatOpMukaan() throws SQLException {
        Scanner input = new Scanner(System.in);
        Connection db = DriverManager.getConnection("jdbc:sqlite:kurssit.db");

        int lkm = 0;
        while (true) {
            System.out.print("Anna opettajien määrä: ");
            lkm = Integer.valueOf(input.nextLine());
            if (lkm > 0) {
                break;
            } else {
                System.out.println("Opettajien määrän pitää olla yksi tai suurempi");
            }
        }

        try {
            PreparedStatement p = db.prepareStatement(
                    "SELECT O.nimi AS opettaja, SUM(K.laajuus) AS op FROM Opettajat O, Suoritukset S, Kurssit K "
                    + "WHERE O.id = K.opettaja_id AND S.kurssi_id = K.id GROUP BY O.id "
                    + "ORDER BY SUM(K.laajuus) DESC LIMIT ?");
            p.setInt(1, lkm);
            ResultSet r = p.executeQuery();

            
            //Tulostetaan otsikkorivi + käyttäjän syöttämä määrä (x) eniten opintopisteitä antaneita opettajia
            int rivi = 0;
            while (r.next()) {
                if (rivi == 0) {
                    ResultSetMetaData rsmd = r.getMetaData();
                    System.out.println(rsmd.getColumnName(1) + "\t" + rsmd.getColumnName(2));
                }
                System.out.println(r.getString("opettaja") + "\t" + r.getInt("op"));
                rivi++;
            }

        } catch (SQLException e) {
            System.out.println("Tietokantahaku ei onnistunut");
        }
    }
}
