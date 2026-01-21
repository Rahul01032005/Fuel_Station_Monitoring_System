package com.fuel;

import java.sql.*;
import java.util.Scanner;

public class Main {

    static final String URL = "jdbc:mysql://localhost:3306/fuel_station";
    static final String USER = "root";
    static final String PASS = "parmi@2004";

    public static void main(String[] args) {

        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             Scanner sc = new Scanner(System.in)) {

            while (true) {
                System.out.println("\n1. Admin");
                System.out.println("2. Worker");
                System.out.println("3. Exit");
                System.out.print("Choice: ");
                int ch = sc.nextInt();

                if (ch == 1) adminMenu(con, sc);
                else if (ch == 2) workerMenu(con, sc);
                else break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- ADMIN ----------------

    static void adminMenu(Connection con, Scanner sc) throws Exception {
        System.out.println("\n1. View Fuel");
        System.out.println("2. Refill Fuel");
        System.out.println("3. Set Price");
        System.out.print("Choice: ");
        int ch = sc.nextInt();

        if (ch == 1) viewFuel(con);
        else if (ch == 2) refillFuel(con, sc);
        else if (ch == 3) setPrice(con, sc);
    }

    static void viewFuel(Connection con) throws Exception {
        ResultSet rs = con.createStatement()
                .executeQuery("SELECT * FROM fuel_tank");

        System.out.println("\nID  Fuel   Price   Available   Last Updated");
        while (rs.next()) {
            System.out.println(
                    rs.getInt("id") + "  " +
                    rs.getString("fuel_type") + "  " +
                    rs.getDouble("price_per_liter") + "  " +
                    rs.getDouble("available_liters") + "  " +
                    rs.getTimestamp("last_updated")
            );
        }
    }

    static void refillFuel(Connection con, Scanner sc) throws Exception {
        System.out.print("Enter Fuel ID: ");
        int id = sc.nextInt();

        System.out.print("Enter Refill Liters: ");
        double liters = sc.nextDouble();

        con.setAutoCommit(false);

        try {
            PreparedStatement ps1 = con.prepareStatement(
                    "UPDATE fuel_tank SET available_liters = available_liters + ? WHERE id=?");
            ps1.setDouble(1, liters);
            ps1.setInt(2, id);
            ps1.executeUpdate();

            PreparedStatement ps2 = con.prepareStatement(
                    "INSERT INTO refill_log(fuel_id, refill_liters) VALUES (?,?)");
            ps2.setInt(1, id);
            ps2.setDouble(2, liters);
            ps2.executeUpdate();

            con.commit();
            System.out.println("Refill successful.");

        } catch (Exception e) {
            con.rollback();
            System.out.println("Refill failed.");
        } finally {
            con.setAutoCommit(true);
        }
    }

    static void setPrice(Connection con, Scanner sc) throws Exception {
        System.out.print("Enter Fuel ID: ");
        int id = sc.nextInt();

        System.out.print("Enter New Price: ");
        double price = sc.nextDouble();

        PreparedStatement ps = con.prepareStatement(
                "UPDATE fuel_tank SET price_per_liter=? WHERE id=?");
        ps.setDouble(1, price);
        ps.setInt(2, id);
        ps.executeUpdate();

        System.out.println("Price updated.");
    }

    // ---------------- WORKER ----------------

    static void workerMenu(Connection con, Scanner sc) throws Exception {
        viewFuel(con);

        System.out.print("Select Fuel ID: ");
        int id = sc.nextInt();

        System.out.print("Enter Liters to Fill: ");
        double liters = sc.nextDouble();

        con.setAutoCommit(false);

        try {
            PreparedStatement ps1 = con.prepareStatement(
                    "SELECT price_per_liter, available_liters FROM fuel_tank WHERE id=?");
            ps1.setInt(1, id);
            ResultSet rs = ps1.executeQuery();

            if (!rs.next()) {
                System.out.println("Invalid Fuel ID.");
                return;
            }

            double price = rs.getDouble(1);
            double available = rs.getDouble(2);

            if (available <= 0) {
                System.out.println("Tank Empty! Cannot dispense fuel.");
                return;
            }

            if (liters > available) {
                System.out.println("Not enough fuel available.");
                return;
            }

            double bill = liters * price;

            PreparedStatement ps2 = con.prepareStatement(
                    "UPDATE fuel_tank SET available_liters = available_liters - ? WHERE id=?");
            ps2.setDouble(1, liters);
            ps2.setInt(2, id);
            ps2.executeUpdate();

            PreparedStatement ps3 = con.prepareStatement(
                    "INSERT INTO sales_log(fuel_id, sold_liters, total_amount) VALUES (?,?,?)");
            ps3.setInt(1, id);
            ps3.setDouble(2, liters);
            ps3.setDouble(3, bill);
            ps3.executeUpdate();

            con.commit();
            System.out.println("Fuel Filled Successfully.");
            System.out.println("Total Bill = ₹" + bill);

        } catch (Exception e) {
            con.rollback();
            System.out.println("Transaction Failed.");
        } finally {
            con.setAutoCommit(true);
        }
    }
}
