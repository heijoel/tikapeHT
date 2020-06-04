package tikapeht1;

/**
 *
 * @author joel.heino95@gmail.com
 */
import java.sql.*;
import java.util.*;

public class TikapeHT1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException {

        Scanner input = new Scanner(System.in);

        while (true) {
            System.out.print("Valitse toiminto: ");
            String toiminto = input.nextLine();

            if (toiminto.equals("1")) {
                toiminto1();
                System.out.println("");
            } else if (toiminto.equals("2")) {
                toiminto2();
                System.out.println("");
            } else if (toiminto.equals("3")) {
                toiminto3();
                System.out.println("");
            } else if (toiminto.equals("4")) {
                toiminto4();
                System.out.println("");
            } else {
                break;
            }
        }

    }

    public static void toiminto1() throws SQLException {
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
            } else {
                System.out.println("Annetulta vuodelta ei ole suorituksia tietokannassa");
            }

        } catch (SQLException e) {
            System.out.println("Tietokantahaku ei onnistunut");
        }

    }

    public static void toiminto2() throws SQLException {
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

                //if (r.next() == false) {
                //   System.out.println("Opiskelijaa ei löytynyt");
                //}
                while (r2.next()) {
                    System.out.println(r2.getString("nimi") + " " + r2.getInt("laajuus") + " "
                            + r2.getString("paivays") + " " + r2.getInt("arvosana"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Tietokantahaku ei onnistunut");
        }

    }

    public static void toiminto3() throws SQLException {
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

    public static void toiminto4() throws SQLException {
        Scanner input = new Scanner(System.in);
        Connection db = DriverManager.getConnection("jdbc:sqlite:kurssit.db");

        //Pyydetään käyttäjältä tulostettavien opettajien lukumäärää. Jos lkm < 1, pyydetään
        //syöttämään sellainen lukumäärä (yksi tai suurempi), jotta toiminnon mukainen
        //tulostus olisi mielekäs.
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
            
            //Tulostetaan top-listalle otsikot
            if (r.next()) {
                ResultSetMetaData rsmd = r.getMetaData();
                System.out.println(rsmd.getColumnName(1) + " " + rsmd.getColumnName(2));
            }
            
            //Tulostetaan käyttäjän syöttämä määrä (x) eniten opintopisteitä antaneita opettajia
            while (r.next()) {
                System.out.println(r.getString("opettaja") + " " + r.getInt("op"));
            }

        } catch (SQLException e) {
            System.out.println("Tietokantahaku ei onnistunut");
        }
    }
}
