
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
                System.out.println("Ei kyseistä toimintoa. Ohjelma loppuu.");
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

            if (r.next()) {
                System.out.println("Opintopisteiden määrä: " + r.getString("summa"));
            } else {
                System.out.println("Annetulta vuodelta ei ole suorituksia tietokannassa");
            }

        } catch (SQLException e) {
            System.out.println("Annetulta vuodelta ei ole suorituksia tietokannassa");
        }

    }

    public static void toiminto2() throws SQLException {
        Scanner input = new Scanner(System.in);
        Connection db = DriverManager.getConnection("jdbc:sqlite:kurssit.db");

        System.out.print("Anna opiskelijan nimi: ");
        String opiskelija = input.nextLine();

        try {
            PreparedStatement p = db.prepareStatement("SELECT K.nimi, K.laajuus, S.paivays, S.arvosana\n"
                    + "FROM Opiskelijat O, Kurssit K, Suoritukset S\n"
                    + "WHERE O.id = S.opiskelija_id AND K.id = S.kurssi_id AND O.nimi = ? ORDER BY S.paivays");
            p.setString(1, opiskelija);

            ResultSet r = p.executeQuery();

            while (r.next()) {
                System.out.println(r.getString("nimi") + " " + r.getInt("laajuus") + " "
                        + r.getString("paivays") + " " + r.getInt("arvosana"));
            }

        } catch (SQLException e) {
            System.out.println("Opiskelijaa ei löytynyt");
        }

    }

    public static void toiminto3() throws SQLException {
        Scanner input = new Scanner(System.in);
        Connection db = DriverManager.getConnection("jdbc:sqlite:kurssit.db");

        System.out.print("Anna kurssin nimi: ");
        String kurssi = input.nextLine();

        try {
            PreparedStatement p = db.prepareStatement(
                    "SELECT ROUND(1.0*SUM(S.arvosana)/COUNT(S.arvosana),2) AS keskiarvo"
                    + " FROM Kurssit K, Suoritukset S "
                    + "WHERE K.id=S.kurssi_id AND K.nimi = ?");
            p.setString(1, kurssi);

            ResultSet r = p.executeQuery();

            if (r.next()) {
                System.out.println("Keskiarvo: " + r.getString("keskiarvo"));
            } else {
                System.out.println("Keskiarvoa ei pystytty laskemaan");
            }

        } catch (SQLException e) {
            System.out.println("Keskiarvoa ei pystytty laskemaan");
        }

    }

    public static void toiminto4() throws SQLException {
        Scanner input = new Scanner(System.in);
        Connection db = DriverManager.getConnection("jdbc:sqlite:kurssit.db");

        int lkm=0;
        while (true) {
            System.out.print("Anna opettajien määrä: ");
            lkm = Integer.valueOf(input.nextLine());
            if (lkm > 0) {
                break;
            } else {
                System.out.println("Anna opettajien määrä positiivisena kokonaislukuna.");
            }
        }

        try {
            PreparedStatement p = db.prepareStatement(
                    "SELECT O.nimi, SUM(K.laajuus) AS summa FROM Opettajat O, Suoritukset S, Kurssit K "
                    + "WHERE O.id = K.opettaja_id AND S.kurssi_id = K.id GROUP BY O.id "
                    + "ORDER BY SUM(K.laajuus) DESC LIMIT ?");
            p.setInt(1, lkm);

            ResultSet r = p.executeQuery();

            while (r.next()) {
                System.out.println(r.getString("nimi") + " " + r.getString("summa"));
            }

        } catch (SQLException e) {
            System.out.println("Tietoa annetulle määrälle opettajia ei pystytty hakemaan");
        }

    }

}
