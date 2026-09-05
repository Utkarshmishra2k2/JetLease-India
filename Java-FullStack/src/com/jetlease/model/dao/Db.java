package com.jetlease.model.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.jetlease.model.service.IdGen;

public class Db {

    private static final String DB_FILE = "jetlease.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
        }
        return connection;
    }

    public static void initSchema() throws SQLException {
        Statement st = getConnection().createStatement();

        st.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id TEXT PRIMARY KEY, full_name TEXT, email TEXT UNIQUE, phone TEXT, " +
                "dob TEXT, emergency_contact TEXT, password TEXT, country TEXT, " +
                "role TEXT, status TEXT, membership TEXT, loyalty_points INTEGER, created_at TEXT)");

        st.execute("CREATE TABLE IF NOT EXISTS aircraft (" +
                "id TEXT PRIMARY KEY, reg TEXT, model TEXT, manufacturer TEXT, category TEXT, " +
                "capacity INTEGER, speed INTEGER, range_km INTEGER, hourly_rate INTEGER, " +
                "status TEXT, type_rating TEXT)");

        st.execute("CREATE TABLE IF NOT EXISTS routes (" +
                "code TEXT PRIMARY KEY, city TEXT, lat REAL, lng REAL)");

        st.execute("CREATE TABLE IF NOT EXISTS pilots (" +
                "id TEXT PRIMARY KEY, name TEXT, license_number TEXT, flying_hours INTEGER, " +
                "remaining_hours INTEGER, type_ratings TEXT, certifications TEXT, available INTEGER)");

        st.execute("CREATE TABLE IF NOT EXISTS crew (" +
                "id TEXT PRIMARY KEY, name TEXT, role TEXT, duty_hours INTEGER, " +
                "remaining_hours INTEGER, available INTEGER)");

        st.execute("CREATE TABLE IF NOT EXISTS pilot_license_registry (" +
                "license_number TEXT PRIMARY KEY, holder_name TEXT, license_class TEXT, " +
                "hours_on_record INTEGER, status TEXT)");

        st.execute("CREATE TABLE IF NOT EXISTS aadhaar_registry (" +
                "aadhaar_number TEXT PRIMARY KEY, holder_name TEXT, dob TEXT, gender TEXT, status TEXT)");

        st.execute("CREATE TABLE IF NOT EXISTS bank_ledger (" +
                "transaction_id TEXT, booking_id TEXT, amount INTEGER, status TEXT, cleared_at TEXT)");

        st.execute("CREATE TABLE IF NOT EXISTS bookings (" +
                "id TEXT PRIMARY KEY, user_email TEXT, type TEXT, trip_type TEXT, origin TEXT, " +
                "destination TEXT, date TEXT, time TEXT, return_date TEXT, return_time TEXT, " +
                "pax INTEGER, aircraft_id TEXT, aircraft_model TEXT, self_fly INTEGER, " +
                "license_number TEXT, license_class TEXT, flying_hours INTEGER, " +
                "certificate_file_name TEXT, dgca_declaration INTEGER, license_verified INTEGER, " +
                "hours REAL, aircraft_cost INTEGER, pilot_cost INTEGER, crew_cost INTEGER, " +
                "airport_charges INTEGER, fuel_surcharge INTEGER, gst INTEGER, total INTEGER, " +
                "status TEXT, assigned_pilot_id TEXT, assigned_crew_ids TEXT, created_at TEXT)");

        st.execute("CREATE TABLE IF NOT EXISTS passengers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, booking_id TEXT, name TEXT, dob TEXT, " +
                "gender TEXT, aadhaar TEXT, verification_status TEXT, no_aadhaar INTEGER, alt_document_id TEXT)");

        st.execute("CREATE TABLE IF NOT EXISTS payments (" +
                "id TEXT PRIMARY KEY, booking_id TEXT, user_email TEXT, amount INTEGER, " +
                "transaction_id TEXT, status TEXT, submitted_at TEXT, cancellation_fee INTEGER, refund_amount INTEGER)");

        st.execute("CREATE TABLE IF NOT EXISTS leases (" +
                "id TEXT PRIMARY KEY, booking_id TEXT, user_email TEXT, status TEXT, " +
                "signed_by TEXT, signed_date TEXT, approval_date TEXT, created_at TEXT)");

        st.execute("CREATE TABLE IF NOT EXISTS notifications (" +
                "id TEXT PRIMARY KEY, user_email TEXT, title TEXT, message TEXT, type TEXT, " +
                "is_read INTEGER, created_at TEXT)");

        st.execute("CREATE TABLE IF NOT EXISTS audit_log (" +
                "id TEXT PRIMARY KEY, actor TEXT, category TEXT, action TEXT, details TEXT, timestamp TEXT)");

        st.execute("CREATE TABLE IF NOT EXISTS contact_messages (" +
                "id TEXT PRIMARY KEY, name TEXT, phone TEXT, email TEXT, message TEXT, status TEXT, created_at TEXT)");

        st.execute("CREATE TABLE IF NOT EXISTS reports (" +
                "id TEXT PRIMARY KEY, booking_id TEXT, user_email TEXT, subject TEXT, details TEXT, " +
                "status TEXT, created_at TEXT)");

        st.execute("CREATE TABLE IF NOT EXISTS testimonials (name TEXT, role TEXT, quote TEXT)");
        st.execute("CREATE TABLE IF NOT EXISTS faq (question TEXT, answer TEXT)");

        st.close();
    }

    public static void seedIfNeeded() throws SQLException {
        Statement check = getConnection().createStatement();
        ResultSet rs = check.executeQuery("SELECT COUNT(*) AS c FROM aircraft");
        rs.next();
        int count = rs.getInt("c");
        rs.close();
        check.close();
        if (count > 0) return;

        seedAircraft();
        seedRoutes();
        seedPilots();
        seedCrew();
        seedPilotLicenseRegistry();
        seedAadhaarRegistry();
        seedTestimonials();
        seedFaq();
        seedUsers();
    }

    private static void seedAircraft() throws SQLException {
        String sql = "INSERT INTO aircraft (id,reg,model,manufacturer,category,capacity,speed,range_km,hourly_rate,status,type_rating) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        Object[][] rows = {
            {"AC-101","VT-JLA","Cessna Citation CJ3+","Cessna","Light Jet",7,770,3480,185000,"Available","CJ3-TR"},
            {"AC-102","VT-JLB","Phenom 300E","Embraer","Light Jet",8,839,3650,210000,"Available","PH300-TR"},
            {"AC-103","VT-JLC","Citation Latitude","Cessna","Mid Jet",9,841,5460,320000,"Available","CLAT-TR"},
            {"AC-104","VT-JLD","Challenger 350","Bombardier","Mid Jet",10,870,5926,410000,"Booked","CH350-TR"},
            {"AC-105","VT-JLE","Gulfstream G650ER","Gulfstream","Heavy Jet",14,956,13890,890000,"Available","G650-TR"},
            {"AC-106","VT-JLF","Global 7500","Bombardier","Heavy Jet",16,1050,14260,950000,"Maintenance","GL7500-TR"},
            {"AC-107","VT-JLG","AgustaWestland AW139","Leonardo","Helicopter",12,306,1061,260000,"Available","AW139-TR"},
            {"AC-108","VT-JLH","Bell 429","Bell","Helicopter",6,278,722,150000,"Available","B429-TR"},
            {"AC-109","VT-JLI","Pilatus PC-12 NGX","Pilatus","Turboprop",9,528,3341,120000,"Available","PC12-TR"},
            {"AC-110","VT-JLJ","King Air 350i","Beechcraft","Turboprop",11,578,3336,135000,"Grounded","KA350-TR"},
            {"AC-111","VT-JLK","Learjet 45XR Air Ambulance","Bombardier","Air Ambulance",4,862,3688,275000,"Available","LJ45-TR"},
            {"AC-112","VT-JLL","Falcon 2000LXS","Dassault","Heavy Jet",10,862,7546,520000,"Retired","F2000-TR"}
        };
        for (Object[] r : rows) {
            for (int i = 0; i < r.length; i++) ps.setObject(i + 1, r[i]);
            ps.executeUpdate();
        }
        ps.close();
    }

    private static void seedRoutes() throws SQLException {
        String sql = "INSERT INTO routes (code,city,lat,lng) VALUES (?,?,?,?)";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        Object[][] rows = {
            {"BOM","Mumbai",19.0896,72.8656},
            {"DEL","Delhi",28.5562,77.1000},
            {"BLR","Bangalore",13.1986,77.7066},
            {"HYD","Hyderabad",17.2403,78.4294},
            {"MAA","Chennai",12.9941,80.1709},
            {"GOI","Goa",15.3808,73.8314},
            {"AMD","Ahmedabad",23.0772,72.6347},
            {"PNQ","Pune",18.5822,73.9197}
        };
        for (Object[] r : rows) {
            for (int i = 0; i < r.length; i++) ps.setObject(i + 1, r[i]);
            ps.executeUpdate();
        }
        ps.close();
    }

    private static void seedPilots() throws SQLException {
        String sql = "INSERT INTO pilots (id,name,license_number,flying_hours,remaining_hours,type_ratings,certifications,available) VALUES (?,?,?,?,?,?,?,?)";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        Object[][] rows = {
            {"PLT-01","Capt. Rohan Verma","DGCA-ATPL-2291",8200,60,"G650-TR,GL7500-TR","ATPL,CAT-II",1},
            {"PLT-02","Capt. Neha Kulkarni","DGCA-ATPL-3387",6100,45,"CH350-TR,CLAT-TR","ATPL",1},
            {"PLT-03","Capt. Imran Sheikh","DGCA-ATPL-4410",5400,38,"AW139-TR,B429-TR","ATPL,Helicopter Rating",1},
            {"PLT-04","Capt. Leela Menon","DGCA-ATPL-5522",9100,0,"CJ3-TR,PH300-TR","ATPL",0}
        };
        for (Object[] r : rows) {
            for (int i = 0; i < r.length; i++) ps.setObject(i + 1, r[i]);
            ps.executeUpdate();
        }
        ps.close();
    }

    private static void seedCrew() throws SQLException {
        String sql = "INSERT INTO crew (id,name,role,duty_hours,remaining_hours,available) VALUES (?,?,?,?,?,?)";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        Object[][] rows = {
            {"CRW-01","Sanya Kapoor","Cabin Crew",120,50,1},
            {"CRW-02","Devika Rao","Cabin Crew",95,42,1},
            {"CRW-03","Farhan Ali","Flight Engineer",140,36,1},
            {"CRW-04","Meera Iyer","Cabin Crew",60,0,0}
        };
        for (Object[] r : rows) {
            for (int i = 0; i < r.length; i++) ps.setObject(i + 1, r[i]);
            ps.executeUpdate();
        }
        ps.close();
    }

    private static void seedPilotLicenseRegistry() throws SQLException {
        String sql = "INSERT INTO pilot_license_registry (license_number,holder_name,license_class,hours_on_record,status) VALUES (?,?,?,?,?)";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        Object[][] rows = {
            {"DGCA-CPL-70011","Karan Shah","Commercial Pilot License (CPL)",340,"Active"},
            {"DGCA-PPL-51204","Ritika Bose","Private Pilot License (PPL)",150,"Active"},
            {"DGCA-ATPL-2291","Rohan Verma","Airline Transport Pilot License (ATPL)",8200,"Active"},
            {"DGCA-CPL-88231","Aditya Rao","Commercial Pilot License (CPL)",410,"Active"},
            {"DGCA-PPL-11190","Sameer Qureshi","Private Pilot License (PPL)",95,"Suspended"}
        };
        for (Object[] r : rows) {
            for (int i = 0; i < r.length; i++) ps.setObject(i + 1, r[i]);
            ps.executeUpdate();
        }
        ps.close();
    }

    private static void seedAadhaarRegistry() throws SQLException {
        String sql = "INSERT INTO aadhaar_registry (aadhaar_number,holder_name,dob,gender,status) VALUES (?,?,?,?,?)";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        Object[][] rows = {
            {"123456789012","Demo Customer","1990-06-15","Male","Active"},
            {"234567890123","Karan Shah","1988-03-22","Male","Active"},
            {"345678901234","Priya Nair","1992-11-08","Female","Active"},
            {"456789012345","Arjun Malhotra","1985-01-30","Male","Active"},
            {"567890123456","Ritika Bose","1995-07-19","Female","Active"},
            {"678901234567","Sameer Qureshi","1979-09-05","Male","Suspended"}
        };
        for (Object[] r : rows) {
            for (int i = 0; i < r.length; i++) ps.setObject(i + 1, r[i]);
            ps.executeUpdate();
        }
        ps.close();
    }

    private static void seedTestimonials() throws SQLException {
        String sql = "INSERT INTO testimonials (name,role,quote) VALUES (?,?,?)";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        Object[][] rows = {
            {"Arjun Malhotra","MD, Malhotra Textiles","JetLease turned a same-day Mumbai board crisis into a non-event. Wheels up in ninety minutes."},
            {"Priya Nair","Founder, Nair Health Group","The lease desk handled our aircraft agreement end-to-end online - no paperwork chased across three cities."},
            {"Karan Shah","Self-fly Member","Verification of my hours and license was fast, and the cost breakdown before I signed was completely transparent."}
        };
        for (Object[] r : rows) {
            for (int i = 0; i < r.length; i++) ps.setObject(i + 1, r[i]);
            ps.executeUpdate();
        }
        ps.close();
    }

    private static void seedFaq() throws SQLException {
        String sql = "INSERT INTO faq (question,answer) VALUES (?,?)";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        Object[][] rows = {
            {"How fast can I book a charter?","Domestic charters can be confirmed in as little as 90 minutes once payment and passenger details are verified."},
            {"Can I fly the aircraft myself?","Yes, through Self-Fly. You need a minimum of 100 logged flying hours, a valid license number and class, and an uploaded certificate. Bookings below 100 hours are automatically rejected."},
            {"What documents do passengers need?","Full name, date of birth, gender, and Aadhaar number for mock verification. Aadhaar is not required for children under 5 years and 15 days old."},
            {"How is the total charter cost calculated?","Aircraft cost + pilot cost + crew cost + airport charges + fuel surcharge, with GST applied on the subtotal. The full breakdown is shown before you pay."},
            {"How do I pay?","Bank transfer to the account shown on the payment page. After transferring, submit your transaction ID - our team verifies it and updates your booking status."},
            {"What is a lease agreement and how do I sign it?","For qualifying bookings, a lease agreement is generated automatically. You can view, digitally sign, and download it from your dashboard; admin then approves it to finalize the booking."}
        };
        for (Object[] r : rows) {
            for (int i = 0; i < r.length; i++) ps.setObject(i + 1, r[i]);
            ps.executeUpdate();
        }
        ps.close();
    }

    private static void seedUsers() throws SQLException {
        String sql = "INSERT INTO users (id,full_name,email,phone,dob,emergency_contact,password,country,role,status,membership,loyalty_points,created_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement ps = getConnection().prepareStatement(sql);

        ps.setString(1, "ADM-0001"); ps.setString(2, "JetLease Admin"); ps.setString(3, "admin@jetlease.in");
        ps.setString(4, "9800000000"); ps.setString(5, "1985-01-01"); ps.setString(6, "");
        ps.setString(7, "Admin@123"); ps.setString(8, "India"); ps.setString(9, "admin");
        ps.setString(10, "active"); ps.setString(11, "none"); ps.setInt(12, 0); ps.setString(13, IdGen.nowIso());
        ps.executeUpdate();

        ps.setString(1, "CUS-0001"); ps.setString(2, "Demo Customer"); ps.setString(3, "demo@jetlease.in");
        ps.setString(4, "9123456780"); ps.setString(5, "1990-06-15"); ps.setString(6, "9988776655");
        ps.setString(7, "Demo@123"); ps.setString(8, "India"); ps.setString(9, "customer");
        ps.setString(10, "active"); ps.setString(11, "gold"); ps.setInt(12, 1250); ps.setString(13, IdGen.nowIso());
        ps.executeUpdate();

        ps.close();
    }
}
