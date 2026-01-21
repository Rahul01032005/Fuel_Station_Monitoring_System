package com.fuel;

import java.sql.*;
import java.util.Scanner;

public class Main {

    static final String URL = "jdbc:mysql://localhost:3306/fuel_station";
    static final String USER = "root";
    static final String PASS = "parmi@2004";   // change if needed

    public static void main(String[] args) {

        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             Scanner sc = new Scanner(System.in)) {

            while (true) {
                try {
                    System.out.println("\n1. Admin");
                    System.out.println("2. Worker");
                    System.out.println("3. Exit");
                    System.out.print("Choice: ");

                    int ch = Integer.parseInt(sc.nextLine());

                    if (ch == 1) adminMenu(con, sc);
                    else if (ch == 2) workerMenu(con, sc);
                    else if (ch == 3) break;
                    else System.out.println("❌ Invalid option.");

                } catch (Exception e) {
                    System.out.println("❌ Enter valid number.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- ADMIN ----------------

    static void adminMenu(Connection con, Scanner sc) {

        while (true) {
            try {
                System.out.println("\n--- ADMIN MENU ---");
                System.out.println("1. View Fuel");
                System.out.println("2. Refill Fuel");
                System.out.println("3. Set Price");
                System.out.println("4. Exit");
                System.out.print("Choice: ");

                int ch = Integer.parseInt(sc.nextLine());

                if (ch == 1) viewFuel(con);
                else if (ch == 2) refillFuel(con, sc);
                else if (ch == 3) setPrice(con, sc);
                else if (ch == 4) break;
                else System.out.println("❌ Invalid option.");

            } catch (Exception e) {
                System.out.println("❌ Invalid input.");
            }
        }
    }

    static void viewFuel(Connection con) throws Exception {
        ResultSet rs = con.createStatement()
                .executeQuery("SELECT * FROM fuel_tank");

        System.out.println("\nID  Fuel   Price   Available");
        while (rs.next()) {
            System.out.println(
                    rs.getInt("id") + "   " +
                    rs.getString("fuel_type") + "   " +
                    rs.getDouble("price_per_liter") + "   " +
                    rs.getDouble("available_liters")
            );
        }
    }

    static void refillFuel(Connection con, Scanner sc) {
        try {
            System.out.print("Enter Fuel ID: ");
            int id = Integer.parseInt(sc.nextLine());

            System.out.print("Enter Refill Liters: ");
            double liters = Double.parseDouble(sc.nextLine());

            PreparedStatement ps = con.prepareStatement(
                "UPDATE fuel_tank SET available_liters = available_liters + ? WHERE id=?");
            ps.setDouble(1, liters);
            ps.setInt(2, id);
            ps.executeUpdate();

            System.out.println("✅ Refill Successful.");

        } catch (Exception e) {
            System.out.println("❌ Invalid input.");
        }
    }

    static void setPrice(Connection con, Scanner sc) {
        try {
            System.out.print("Enter Fuel ID: ");
            int id = Integer.parseInt(sc.nextLine());

            System.out.print("Enter New Price: ");
            double price = Double.parseDouble(sc.nextLine());

            PreparedStatement ps = con.prepareStatement(
                "UPDATE fuel_tank SET price_per_liter=? WHERE id=?");
            ps.setDouble(1, price);
            ps.setInt(2, id);
            ps.executeUpdate();

            System.out.println("✅ Price Updated.");

        } catch (Exception e) {
            System.out.println("❌ Invalid input.");
        }
    }

    // ---------------- WORKER ----------------

    static void workerMenu(Connection con, Scanner sc) {

        try {
            // ✅ Show fuel only once
            viewFuel(con);
        } catch (Exception e) {
            System.out.println("Unable to load fuel data.");
        }

        while (true) {
            try {
                System.out.print("\nEnter Fuel ID (or 'q' to exit): ");
                String input = sc.nextLine();

                if (input.equalsIgnoreCase("q")) {
                    System.out.println("👋 Worker Mode Closed.");
                    break;
                }

                int id = Integer.parseInt(input);

                System.out.print("Enter Amount (₹): ");
                double amount = Double.parseDouble(sc.nextLine());

                PreparedStatement ps1 = con.prepareStatement(
                    "SELECT price_per_liter, available_liters FROM fuel_tank WHERE id=?");
                ps1.setInt(1, id);
                ResultSet rs = ps1.executeQuery();

                if (!rs.next()) {
                    System.out.println("❌ Invalid Fuel ID.");
                    continue;
                }

                double price = rs.getDouble(1);
                double available = rs.getDouble(2);

                if (available <= 0) {
                    System.out.println("❌ Tank Empty!");
                    continue;
                }

                double liters = amount / price;

                if (liters > available) {
                    System.out.println("❌ Not enough fuel for this amount.");
                    continue;
                }

                PreparedStatement ps2 = con.prepareStatement(
                    "UPDATE fuel_tank SET available_liters = available_liters - ? WHERE id=?");
                ps2.setDouble(1, liters);
                ps2.setInt(2, id);
                ps2.executeUpdate();

                System.out.println("✅ Fuel Filled Successfully.");
                System.out.println("Liters Dispensed: " + String.format("%.2f", liters));
                System.out.println("Amount Paid: ₹" + amount);

            } catch (Exception e) {
                System.out.println("❌ Invalid input. Try again.");
            }
        }
    }
}
