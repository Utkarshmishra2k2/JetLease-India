package com.jetlease.config;

import com.jetlease.entity.*;
import com.jetlease.repository.*;
import com.jetlease.util.IdGen;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Ported from Db.seedIfNeeded() - seeds demo fleet, routes, pilots, crew, users, registries, FAQ & testimonials. */
@Component
public class DataSeeder implements CommandLineRunner {

    @Value("${jetlease.seed.enabled:true}")
    private boolean enabled;

    private final AircraftRepository aircraftRepository;
    private final RouteRepository routeRepository;
    private final PilotRepository pilotRepository;
    private final CrewRepository crewRepository;
    private final UserRepository userRepository;
    private final PilotLicenseRegistryRepository licenseRegistryRepository;
    private final AadhaarRegistryRepository aadhaarRegistryRepository;
    private final FaqRepository faqRepository;
    private final TestimonialRepository testimonialRepository;

    public DataSeeder(AircraftRepository aircraftRepository, RouteRepository routeRepository,
                       PilotRepository pilotRepository, CrewRepository crewRepository,
                       UserRepository userRepository, PilotLicenseRegistryRepository licenseRegistryRepository,
                       AadhaarRegistryRepository aadhaarRegistryRepository, FaqRepository faqRepository,
                       TestimonialRepository testimonialRepository) {
        this.aircraftRepository = aircraftRepository;
        this.routeRepository = routeRepository;
        this.pilotRepository = pilotRepository;
        this.crewRepository = crewRepository;
        this.userRepository = userRepository;
        this.licenseRegistryRepository = licenseRegistryRepository;
        this.aadhaarRegistryRepository = aadhaarRegistryRepository;
        this.faqRepository = faqRepository;
        this.testimonialRepository = testimonialRepository;
    }

    @Override
    public void run(String... args) {
        if (!enabled) return;
        if (aircraftRepository.count() > 0) return; // already seeded

        seedRoutes();
        seedAircraft();
        seedPilotsAndCrew();
        seedUsers();
        seedRegistries();
        seedFaqAndTestimonials();
    }

    private void seedRoutes() {
        save(route("DEL", "New Delhi", "Indira Gandhi Intl", 28.5562, 77.1000));
        save(route("BOM", "Mumbai", "Chhatrapati Shivaji Intl", 19.0896, 72.8656));
        save(route("BLR", "Bengaluru", "Kempegowda Intl", 13.1986, 77.7066));
        save(route("HYD", "Hyderabad", "Rajiv Gandhi Intl", 17.2403, 78.4294));
        save(route("MAA", "Chennai", "Chennai Intl", 12.9941, 80.1709));
        save(route("CCU", "Kolkata", "Netaji Subhas Chandra Bose Intl", 22.6547, 88.4467));
        save(route("GOI", "Goa", "Dabolim", 15.3808, 73.8314));
        save(route("JAI", "Jaipur", "Jaipur Intl", 26.8242, 75.8122));
        save(route("COK", "Kochi", "Cochin Intl", 10.1520, 76.4019));
        save(route("PNQ", "Pune", "Pune Airport", 18.5822, 73.9197));
    }

    private Route route(String code, String city, String airport, double lat, double lon) {
        Route r = new Route();
        r.setCode(code);
        r.setCity(city);
        r.setAirport(airport);
        r.setLat(lat);
        r.setLon(lon);
        return r;
    }

    private void save(Route r) { routeRepository.save(r); }

    private void seedAircraft() {
        aircraftRepository.save(aircraft("Cessna Citation CJ3+", "Textron", "Light Jet", 6, 750, 3700, 220000, "N-CJ3"));
        aircraftRepository.save(aircraft("Embraer Legacy 500", "Embraer", "Mid Jet", 9, 850, 5900, 340000, "N-EMB500"));
        aircraftRepository.save(aircraft("Bombardier Global 6000", "Bombardier", "Heavy Jet", 13, 900, 11000, 680000, "N-GLB6000"));
        aircraftRepository.save(aircraft("Airbus H145", "Airbus Helicopters", "Helicopter", 8, 245, 680, 145000, "N-H145"));
        aircraftRepository.save(aircraft("Bell 429", "Bell", "Helicopter", 6, 260, 722, 130000, "N-B429"));
        aircraftRepository.save(aircraft("Beechcraft King Air 350i", "Beechcraft", "Turboprop", 9, 578, 3300, 165000, "N-KA350"));
        aircraftRepository.save(aircraft("Pilatus PC-24", "Pilatus", "Light Jet", 8, 815, 3610, 260000, "N-PC24"));
        aircraftRepository.save(aircraft("Gulfstream G650", "Gulfstream", "Heavy Jet", 14, 956, 13890, 850000, "N-G650"));
    }

    private Aircraft aircraft(String model, String manufacturer, String category, int capacity, int speed,
                               int rangeKm, long hourlyRate, String typeRating) {
        Aircraft a = new Aircraft();
        a.setId(IdGen.uid("AC"));
        a.setReg("VT-" + typeRating.replaceAll("[^A-Za-z0-9]", "").toUpperCase());
        a.setModel(model);
        a.setManufacturer(manufacturer);
        a.setCategory(category);
        a.setCapacity(capacity);
        a.setSpeed(speed);
        a.setRangeKm(rangeKm);
        a.setHourlyRate(hourlyRate);
        a.setStatus("Available");
        a.setTypeRating(typeRating);
        return a;
    }

    private void seedPilotsAndCrew() {
        pilotRepository.save(pilot("Capt. Arjun Mehta", "DGCA-ATPL-10234", 900));
        pilotRepository.save(pilot("Capt. Neha Kapoor", "DGCA-ATPL-10567", 750));
        pilotRepository.save(pilot("Capt. Rohan Verma", "DGCA-ATPL-10890", 1000));
        pilotRepository.save(pilot("Capt. Sara Iyer", "DGCA-ATPL-11123", 620));

        crewRepository.save(crew("Priya Nair", "Flight Attendant", 900));
        crewRepository.save(crew("Karan Malhotra", "Flight Attendant", 850));
        crewRepository.save(crew("Divya Menon", "Flight Engineer", 700));
        crewRepository.save(crew("Aditya Rao", "Flight Attendant", 950));
    }

    private Pilot pilot(String name, String license, double hours) {
        Pilot p = new Pilot();
        p.setId(IdGen.uid("PLT"));
        p.setName(name);
        p.setLicenseNumber(license);
        p.setRemainingHours(hours);
        p.setAvailable(true);
        return p;
    }

    private Crew crew(String name, String role, double hours) {
        Crew c = new Crew();
        c.setId(IdGen.uid("CRW"));
        c.setName(name);
        c.setRole(role);
        c.setRemainingHours(hours);
        c.setAvailable(true);
        return c;
    }

    private void seedUsers() {
        User admin = new User();
        admin.setId(IdGen.uid("ADM"));
        admin.setFullName("JetLease Admin");
        admin.setEmail("admin@jetlease.in");
        admin.setPhone("9000000000");
        admin.setDob("1985-01-01");
        admin.setEmergencyContact("9000000001");
        admin.setPassword("Admin@123");
        admin.setCountry("India");
        admin.setRole("admin");
        admin.setStatus("active");
        admin.setMembership("none");
        admin.setLoyaltyPoints(0);
        admin.setCreatedAt(IdGen.nowIso());
        userRepository.save(admin);

        User demo = new User();
        demo.setId(IdGen.uid("CUS"));
        demo.setFullName("Demo Customer");
        demo.setEmail("demo@jetlease.in");
        demo.setPhone("9123456780");
        demo.setDob("1990-05-15");
        demo.setEmergencyContact("9123456781");
        demo.setPassword("Demo@123");
        demo.setCountry("India");
        demo.setRole("customer");
        demo.setStatus("active");
        demo.setMembership("gold");
        demo.setLoyaltyPoints(120);
        demo.setCreatedAt(IdGen.nowIso());
        userRepository.save(demo);
    }

    private void seedRegistries() {
        licenseRecord("DGCA-ATPL-10234", "Arjun Mehta", "ATPL", 900, "Active");
        licenseRecord("DGCA-ATPL-10567", "Neha Kapoor", "ATPL", 750, "Active");
        licenseRecord("DGCA-CPL-99871", "Demo Customer", "CPL", 145, "Active");
        licenseRecord("DGCA-CPL-55021", "Rahul Singh", "CPL", 60, "Active");

        aadhaarRecord("123456789012", "Demo Customer", "1990-05-15", "Other");
        aadhaarRecord("234567890123", "Priya Sharma", "1988-11-02", "Female");
        aadhaarRecord("345678901234", "Ankit Gupta", "1979-03-21", "Male");
    }

    private void licenseRecord(String number, String holder, String cls, int hours, String status) {
        PilotLicenseRegistry r = new PilotLicenseRegistry();
        r.setLicenseNumber(number);
        r.setHolderName(holder);
        r.setLicenseClass(cls);
        r.setHoursOnRecord(hours);
        r.setStatus(status);
        licenseRegistryRepository.save(r);
    }

    private void aadhaarRecord(String number, String holder, String dob, String gender) {
        AadhaarRegistry r = new AadhaarRegistry();
        r.setAadhaarNumber(number);
        r.setHolderName(holder);
        r.setDob(dob);
        r.setGender(gender);
        r.setStatus("Active");
        aadhaarRegistryRepository.save(r);
    }

    private void seedFaqAndTestimonials() {
        faq("How far in advance should I book a charter?", "We recommend booking at least 48 hours in advance, though urgent charters can often be accommodated with shorter notice depending on aircraft availability.");
        faq("Can I bring pets on board?", "Yes, most of our aircraft are pet-friendly. Please mention this during booking so we can prepare accordingly.");
        faq("What documents do passengers need?", "A valid government photo ID and Aadhaar (or an alternate ID document) are required for all passengers.");
        faq("Can I self-fly the aircraft?", "Yes, if you hold a valid DGCA license with at least 100 logged flying hours on the relevant type rating, you may opt for a self-fly charter during booking.");
        faq("What is your cancellation policy?", "Cancellations incur a 20% fee on the amount paid; the remaining 80% is refunded.");

        testimonial("Ritika Sharma", "Business Executive", "JetLease made our Mumbai-Delhi commute effortless. Professional crew and immaculate aircraft.", 5);
        testimonial("Vikram Oberoi", "Film Producer", "Booked a last-minute helicopter charter for a shoot - seamless from booking to landing.", 5);
        testimonial("Ananya Rao", "Entrepreneur", "The self-fly option was a great touch. Loved having full control on my own terms.", 4);
    }

    private void faq(String q, String a) {
        Faq f = new Faq();
        f.setQuestion(q);
        f.setAnswer(a);
        faqRepository.save(f);
    }

    private void testimonial(String name, String role, String quote, int rating) {
        Testimonial t = new Testimonial();
        t.setName(name);
        t.setRole(role);
        t.setQuote(quote);
        t.setRating(rating);
        testimonialRepository.save(t);
    }
}
