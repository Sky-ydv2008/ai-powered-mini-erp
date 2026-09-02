package com.example.intellierp.util;

import com.example.intellierp.entity.*;
import com.example.intellierp.entity.enums.*;
import com.example.intellierp.repository.*;
import com.example.intellierp.service.AiInsightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private StockTransactionRepository stockTransactionRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AiInsightService aiInsightService;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            seedDatabase();
        }
    }

    @Transactional
    public void seedDatabase() {
        System.out.println(">>> SEEDING INTELLIERP DATABASE WITH REALISTIC DATA...");

        // 1. Seed Users
        User admin = new User("admin", passwordEncoder.encode("admin123"), "Sarah Jenkins (Admin)", "admin@intellierp.com", RoleType.ROLE_ADMIN);
        User manager = new User("manager", passwordEncoder.encode("manager123"), "Vikram Malhotra (Operations)", "manager@intellierp.com", RoleType.ROLE_MANAGER);
        User employee = new User("employee", passwordEncoder.encode("employee123"), "Ananya Sharma (POS Staff)", "employee@intellierp.com", RoleType.ROLE_EMPLOYEE);
        userRepository.saveAll(List.of(admin, manager, employee));

        // 2. Seed Categories
        Category catElectronics = categoryRepository.save(new Category("Electronics & Gadgets", "Microcontrollers, peripherals, accessories, and cables", "cpu"));
        Category catOffice = categoryRepository.save(new Category("Office Supplies & Stationery", "Paper, organizers, writing equipment, and ergonomic tools", "clipboard"));
        Category catIndustrial = categoryRepository.save(new Category("Industrial Hardware & Tools", "Heavy-duty machinery, power tools, drills, and bearings", "wrench"));
        Category catPackaging = categoryRepository.save(new Category("Packaging & Shipping", "Corrugated cartons, bubble wrap, poly-mailers, and tapes", "box"));
        Category catPolymer = categoryRepository.save(new Category("Raw Materials & Polymers", "Industrial grade resins, thermoplastic pellets, and aluminum sheets", "layers"));
        Category catApparel = categoryRepository.save(new Category("Safety Wear & Uniforms", "Hi-vis vests, steel-toe boots, helmets, and protective gloves", "shield"));

        // 3. Seed 10 Suppliers with realistic metrics
        Supplier sApex = new Supplier("Apex Logistics & Hardware", "Rajesh Kumar", "rajesh@apexlogistics.in", "+91-98765-43210", "Sector 18, Gurugram, Haryana", 5);
        sApex.setOnTimeDeliveryRate(new BigDecimal("97.50"));
        sApex.setDefectRate(new BigDecimal("1.20"));
        sApex.setReturnRate(new BigDecimal("0.80"));
        sApex.setDelayedOrders(1);
        sApex.setTotalPurchases(new BigDecimal("680000.00"));
        sApex.setTotalOrders(18);

        Supplier sBharat = new Supplier("Bharat Polymer Solutions", "Sunil Verma", "sverma@bharatpolymer.com", "+91-98111-22334", "Industrial Area, Faridabad, Haryana", 10);
        sBharat.setOnTimeDeliveryRate(new BigDecimal("65.00")); // Intentional delay anomaly
        sBharat.setDefectRate(new BigDecimal("12.50")); // Intentional defect rate
        sBharat.setReturnRate(new BigDecimal("8.00"));
        sBharat.setDelayedOrders(7); // High delays
        sBharat.setTotalPurchases(new BigDecimal("520000.00"));
        sBharat.setTotalOrders(14);
        sBharat.setEstimatedLoss(new BigDecimal("64500.00"));

        Supplier sQuantum = new Supplier("Quantum Microelectronics", "David Chen", "chen@quantum-micro.com", "+91-98222-33445", "Electronic City, Bengaluru, Karnataka", 4);
        sQuantum.setOnTimeDeliveryRate(new BigDecimal("98.00"));
        sQuantum.setDefectRate(new BigDecimal("0.90"));
        sQuantum.setReturnRate(new BigDecimal("0.50"));
        sQuantum.setDelayedOrders(0);
        sQuantum.setTotalPurchases(new BigDecimal("890000.00"));
        sQuantum.setTotalOrders(22);

        Supplier sZenith = new Supplier("Zenith Packaging Corp", "Meera Nair", "meera@zenithpack.in", "+91-98333-44556", "MIDC Andheri, Mumbai, Maharashtra", 6);
        sZenith.setOnTimeDeliveryRate(new BigDecimal("94.00"));
        sZenith.setDefectRate(new BigDecimal("2.10"));
        sZenith.setTotalPurchases(new BigDecimal("340000.00"));
        sZenith.setTotalOrders(12);

        Supplier sTitan = new Supplier("Titan Industrial Tools", "Amitabh Sen", "asen@titantools.com", "+91-98444-55667", "Peenya Industrial Area, Bengaluru", 7);
        sTitan.setOnTimeDeliveryRate(new BigDecimal("92.50"));
        sTitan.setDefectRate(new BigDecimal("2.80"));
        sTitan.setTotalPurchases(new BigDecimal("450000.00"));
        sTitan.setTotalOrders(15);

        Supplier sSunrise = new Supplier("Sunrise Textile & Safety", "Preeti Deshmukh", "pdeshmukh@sunrisetextile.in", "+91-98555-66778", "Surat Textile Hub, Gujarat", 5);
        sSunrise.setOnTimeDeliveryRate(new BigDecimal("96.00"));
        sSunrise.setDefectRate(new BigDecimal("1.50"));
        sSunrise.setTotalPurchases(new BigDecimal("280000.00"));
        sSunrise.setTotalOrders(10);

        Supplier sEverest = new Supplier("Everest Raw Materials", "Karan Singhal", "ksinghal@everestraw.in", "+91-98666-77889", "RIICO Industrial Area, Jaipur, Rajasthan", 8);
        sEverest.setOnTimeDeliveryRate(new BigDecimal("89.00"));
        sEverest.setDefectRate(new BigDecimal("3.40"));
        sEverest.setTotalPurchases(new BigDecimal("410000.00"));
        sEverest.setTotalOrders(11);

        Supplier sBlueStar = new Supplier("BlueStar Office Dynamics", "Rohit Bhatia", "rbhatia@bluestaroffice.in", "+91-98777-88990", "Connaught Place, New Delhi", 3);
        sBlueStar.setOnTimeDeliveryRate(new BigDecimal("99.00"));
        sBlueStar.setDefectRate(new BigDecimal("0.50"));
        sBlueStar.setTotalPurchases(new BigDecimal("210000.00"));
        sBlueStar.setTotalOrders(9);

        Supplier sPinnacle = new Supplier("Pinnacle Precision Parts", "Manoj Tiwari", "mtiwari@pinnacleparts.com", "+91-98888-99001", "Pimpri-Chinchwad, Pune, Maharashtra", 6);
        sPinnacle.setOnTimeDeliveryRate(new BigDecimal("93.00"));
        sPinnacle.setDefectRate(new BigDecimal("2.20"));
        sPinnacle.setTotalPurchases(new BigDecimal("390000.00"));
        sPinnacle.setTotalOrders(13);

        Supplier sGlobal = new Supplier("Global Cargo & Supply Co", "Arjun Reddy", "areddy@globalcargo.in", "+91-98999-00112", "Sri City, Andhra Pradesh", 7);
        sGlobal.setOnTimeDeliveryRate(new BigDecimal("91.00"));
        sGlobal.setDefectRate(new BigDecimal("3.00"));
        sGlobal.setTotalPurchases(new BigDecimal("310000.00"));
        sGlobal.setTotalOrders(10);

        List<Supplier> allSuppliers = supplierRepository.saveAll(List.of(
                sApex, sBharat, sQuantum, sZenith, sTitan, sSunrise, sEverest, sBlueStar, sPinnacle, sGlobal
        ));

        // 4. Seed 50+ Products across categories
        List<Product> products = new ArrayList<>();

        // Electronics
        Product pMouse = new Product("Quantum Gaming Mouse X1", "EL-GM-001", catElectronics, sQuantum, new BigDecimal("1200.00"), new BigDecimal("2499.00"), 80, 150, 40);
        pMouse.setLeadTimeDays(7);
        pMouse.setBcgClassification(BcgClassification.STAR);
        products.add(pMouse);

        Product pKeyboard = new Product("Mechanical RGB Keyboard Pro", "EL-KB-002", catElectronics, sQuantum, new BigDecimal("2200.00"), new BigDecimal("4199.00"), 120, 80, 25);
        pKeyboard.setBcgClassification(BcgClassification.STAR);
        products.add(pKeyboard);

        Product pHub = new Product("UltraHD 8-in-1 USB-C Hub", "EL-HUB-003", catElectronics, sQuantum, new BigDecimal("1400.00"), new BigDecimal("3299.00"), 45, 50, 15);
        pHub.setBcgClassification(BcgClassification.QUESTION_MARK); // High margin (57%), low volume candidate
        products.add(pHub);

        Product pCable = new Product("Braided Thunderbolt 4 Cable 2M", "EL-CB-004", catElectronics, sQuantum, new BigDecimal("350.00"), new BigDecimal("999.00"), 350, 100, 30);
        pCable.setBcgClassification(BcgClassification.CASH_COW);
        products.add(pCable);

        Product pWebcam = new Product("4K Ultra-Wide Conference Webcam", "EL-CAM-005", catElectronics, sQuantum, new BigDecimal("3800.00"), new BigDecimal("6999.00"), 65, 40, 10);
        products.add(pWebcam);

        Product pHeadset = new Product("Active Noise Cancelling Headset", "EL-HS-006", catElectronics, sQuantum, new BigDecimal("2800.00"), new BigDecimal("5499.00"), 90, 50, 15);
        products.add(pHeadset);

        Product pPowerBank = new Product("20000mAh 65W Fast Power Bank", "EL-PB-007", catElectronics, sQuantum, new BigDecimal("1100.00"), new BigDecimal("2199.00"), 140, 60, 20);
        products.add(pPowerBank);

        Product pAdapter = new Product("65W GaN Dual USB-C Charger", "EL-CHG-008", catElectronics, sQuantum, new BigDecimal("650.00"), new BigDecimal("1499.00"), 220, 80, 25);
        products.add(pAdapter);

        // Office Supplies
        Product pPaper = new Product("A4 Premium Copy Paper 80GSM (500 Sheets)", "OF-PPR-101", catOffice, sBlueStar, new BigDecimal("180.00"), new BigDecimal("299.00"), 850, 300, 100);
        pPaper.setBcgClassification(BcgClassification.CASH_COW);
        products.add(pPaper);

        Product pChair = new Product("Ergonomic Mesh High-Back Executive Chair", "OF-CHR-102", catOffice, sBlueStar, new BigDecimal("5500.00"), new BigDecimal("11999.00"), 35, 30, 10);
        pChair.setBcgClassification(BcgClassification.STAR);
        products.add(pChair);

        Product pDesk = new Product("Motorized Dual-Motor Standing Desk Frame", "OF-DSK-103", catOffice, sBlueStar, new BigDecimal("14000.00"), new BigDecimal("24999.00"), 18, 20, 5);
        products.add(pDesk);

        Product pWhiteboard = new Product("Magnetic Dry Erase Whiteboard 4x3 Ft", "OF-WB-104", catOffice, sBlueStar, new BigDecimal("850.00"), new BigDecimal("1899.00"), 75, 40, 12);
        products.add(pWhiteboard);

        Product pPenBox = new Product("Gel Ink Pens 0.5mm (Box of 50)", "OF-PEN-105", catOffice, sBlueStar, new BigDecimal("220.00"), new BigDecimal("499.00"), 400, 150, 40);
        products.add(pPenBox);

        Product pFilingCab = new Product("Heavy Duty 3-Drawer Steel Filing Cabinet", "OF-CAB-106", catOffice, sBlueStar, new BigDecimal("3200.00"), new BigDecimal("6499.00"), 25, 20, 6);
        products.add(pFilingCab);

        Product pShredder = new Product("Cross-Cut 12-Sheet Heavy Duty Paper Shredder", "OF-SHR-107", catOffice, sBlueStar, new BigDecimal("2400.00"), new BigDecimal("4999.00"), 40, 25, 8);
        products.add(pShredder);

        Product pLaminator = new Product("A3 Thermal Document Laminating Machine", "OF-LAM-108", catOffice, sBlueStar, new BigDecimal("1300.00"), new BigDecimal("2799.00"), 55, 30, 10);
        products.add(pLaminator);

        // Industrial Tools
        Product pGrinder = new Product("Heavy Duty Angle Grinder 900W 100mm", "ID-GRN-201", catIndustrial, sTitan, new BigDecimal("1650.00"), new BigDecimal("3199.00"), 110, 60, 20);
        products.add(pGrinder);

        Product pDrill = new Product("Cordless Brushless Impact Drill Kit 20V", "ID-DRL-202", catIndustrial, sTitan, new BigDecimal("3400.00"), new BigDecimal("6999.00"), 70, 40, 15);
        pDrill.setBcgClassification(BcgClassification.STAR);
        products.add(pDrill);

        Product pWrenchSet = new Product("Combination Ratcheting Wrench Set 14-Piece", "ID-WRN-203", catIndustrial, sTitan, new BigDecimal("850.00"), new BigDecimal("1899.00"), 180, 70, 20);
        products.add(pWrenchSet);

        Product pRotaryTool = new Product("Precision Multi-Speed Rotary Tool 180W", "ID-ROT-204", catIndustrial, sTitan, new BigDecimal("950.00"), new BigDecimal("2199.00"), 95, 45, 12);
        products.add(pRotaryTool);

        Product pLaserLevel = new Product("Self-Leveling 360-Degree Green Beam Laser", "ID-LSR-205", catIndustrial, sTitan, new BigDecimal("2100.00"), new BigDecimal("4899.00"), 50, 30, 10);
        products.add(pLaserLevel);

        Product pMultimeter = new Product("True-RMS Digital Industrial Multimeter", "ID-MM-206", catIndustrial, sTitan, new BigDecimal("1100.00"), new BigDecimal("2499.00"), 130, 50, 15);
        products.add(pMultimeter);

        // Packaging
        Product pCarton12 = new Product("Eco-Corrugated Box 12x12x12 (Bundle of 25)", "PK-BOX-301", catPackaging, sZenith, new BigDecimal("420.00"), new BigDecimal("799.00"), 650, 250, 80);
        pCarton12.setBcgClassification(BcgClassification.CASH_COW);
        products.add(pCarton12);

        Product pCarton18 = new Product("Heavy Duty Double-Wall Box 18x18x18 (20 Pcs)", "PK-BOX-302", catPackaging, sZenith, new BigDecimal("680.00"), new BigDecimal("1299.00"), 380, 150, 50);
        products.add(pCarton18);

        Product pBubbleWrap = new Product("Air Bubble Packaging Roll 100M x 1M", "PK-BBL-303", catPackaging, sZenith, new BigDecimal("550.00"), new BigDecimal("1099.00"), 420, 150, 50);
        products.add(pBubbleWrap);

        Product pStretchFilm = new Product("Industrial Pallet Stretch Wrap Film 500mm", "PK-STR-304", catPackaging, sZenith, new BigDecimal("320.00"), new BigDecimal("649.00"), 500, 200, 60);
        products.add(pStretchFilm);

        Product pTape = new Product("Brown Packaging Tape 3-Inch (Pack of 12)", "PK-TPE-305", catPackaging, sZenith, new BigDecimal("260.00"), new BigDecimal("549.00"), 700, 250, 70);
        products.add(pTape);

        Product pThermalLabel = new Product("Direct Thermal Shipping Labels 4x6 (1000 Roll)", "PK-LBL-306", catPackaging, sZenith, new BigDecimal("310.00"), new BigDecimal("699.00"), 450, 180, 50);
        products.add(pThermalLabel);

        // Raw Materials & Polymers (Supplier: Bharat Polymer Solutions - Intentional defect/delay source)
        Product pResin = new Product("High Density Polyethylene Granules HDPE-5502", "RM-PL-401", catPolymer, sBharat, new BigDecimal("85.00"), new BigDecimal("140.00"), 2500, 1000, 300);
        pResin.setUnit("kg");
        products.add(pResin);

        Product pPolypropylene = new Product("Polypropylene Homopolymer Pellets PP-H110", "RM-PL-402", catPolymer, sBharat, new BigDecimal("92.00"), new BigDecimal("155.00"), 1800, 800, 250);
        pPolypropylene.setUnit("kg");
        products.add(pPolypropylene);

        Product pMasterbatch = new Product("Black Carbon Masterbatch Pellets 40%", "RM-PL-403", catPolymer, sBharat, new BigDecimal("120.00"), new BigDecimal("210.00"), 800, 400, 120);
        pMasterbatch.setUnit("kg");
        products.add(pMasterbatch);

        Product pAluminum = new Product("Industrial Aluminum Sheet 6061-T6 (2mm)", "RM-AL-404", catPolymer, sEverest, new BigDecimal("450.00"), new BigDecimal("799.00"), 350, 150, 40);
        pAluminum.setUnit("sheet");
        products.add(pAluminum);

        Product pStainlessRod = new Product("Stainless Steel Round Bar 304 Grade (12mm)", "RM-SS-405", catPolymer, sEverest, new BigDecimal("280.00"), new BigDecimal("499.00"), 450, 200, 50);
        pStainlessRod.setUnit("pcs");
        products.add(pStainlessRod);

        // Safety Wear & Uniforms
        Product pSafetyBoots = new Product("Steel-Toe Industrial Safety Boots Grade-1", "SF-BOT-501", catApparel, sSunrise, new BigDecimal("850.00"), new BigDecimal("1799.00"), 160, 80, 25);
        products.add(pSafetyBoots);

        Product pHiVisVest = new Product("High-Visibility Reflective Safety Vest (Pack of 10)", "SF-VST-502", catApparel, sSunrise, new BigDecimal("350.00"), new BigDecimal("799.00"), 280, 100, 30);
        products.add(pHiVisVest);

        Product pSafetyGoggles = new Product("Anti-Fog UV400 Industrial Safety Goggles", "SF-GOG-503", catApparel, sSunrise, new BigDecimal("120.00"), new BigDecimal("299.00"), 500, 200, 50);
        products.add(pSafetyGoggles);

        Product pGloves = new Product("Cut-Resistant Nitrile Coated Gloves (12 Pairs)", "SF-GLV-504", catApparel, sSunrise, new BigDecimal("240.00"), new BigDecimal("549.00"), 350, 150, 40);
        products.add(pGloves);

        Product pHelmet = new Product("Hard Hat Ventilated Construction Safety Helmet", "SF-HLM-505", catApparel, sSunrise, new BigDecimal("210.00"), new BigDecimal("499.00"), 220, 90, 25);
        products.add(pHelmet);

        // Add slow-moving dead-stock candidate
        Product pDeadStock = new Product("Legacy Serial Port RS-232 PCI Expansion Card", "EL-LEG-999", catElectronics, sApex, new BigDecimal("450.00"), new BigDecimal("599.00"), 15, 10, 5);
        pDeadStock.setBcgClassification(BcgClassification.DEAD_STOCK);
        products.add(pDeadStock);

        List<Product> savedProducts = productRepository.saveAll(products);

        // 5. Seed 100+ Customers across VIP, Returning, Regular, Churn Risk tiers
        List<Customer> customers = new ArrayList<>();
        String[] firstNames = {"Aarav", "Aditi", "Rohan", "Sneha", "Kavita", "Deepak", "Pooja", "Manoj", "Ananya", "Rahul",
                "Divya", "Suresh", "Priya", "Nikhil", "Megha", "Alok", "Swati", "Gaurav", "Sunita", "Harsh"};
        String[] lastNames = {"Sharma", "Patel", "Verma", "Mehta", "Iyer", "Nair", "Reddy", "Chopra", "Gupta", "Kapoor",
                "Bhatia", "Deshmukh", "Singhal", "Tiwari", "Sen", "Joshi", "Bansal", "Saxena", "Malik", "Pandey"};
        String[] companies = {"TechCorp Solutions", "Apex Infrastructure", "Sunrise Manufacturing", "Zenith Retailers",
                "Matrix Logistics", "Nexus Trading Co", "BlueChip Enterprises", "Vanguard Industries", "Delta Supplies", "Prime Builders"};

        Random rand = new Random(42);

        for (int i = 0; i < 100; i++) {
            String name = (i % 3 == 0) ? companies[i % companies.length] + " (" + firstNames[i % firstNames.length] + ")"
                    : firstNames[i % firstNames.length] + " " + lastNames[(i * 3 + 1) % lastNames.length];
            String email = "customer" + (i + 1) + "@example.com";
            String phone = "+91-98" + String.format("%08d", 10000000 + i * 739);
            String address = "Suite " + (100 + i) + ", Cyber City, Phase " + ((i % 5) + 1) + ", Sector " + ((i % 60) + 1);

            CustomerTier tier;
            if (i < 15) tier = CustomerTier.VIP;
            else if (i < 45) tier = CustomerTier.RETURNING;
            else if (i < 80) tier = CustomerTier.REGULAR;
            else tier = CustomerTier.CHURN_RISK;

            Customer c = new Customer(name, email, phone, address, tier);
            if (tier == CustomerTier.CHURN_RISK) {
                c.setLastPurchaseDate(LocalDateTime.now().minusDays(50 + rand.nextInt(30)));
                c.setTotalSpend(new BigDecimal(35000 + rand.nextInt(40000)));
                c.setTotalOrders(6 + rand.nextInt(8));
            } else if (tier == CustomerTier.VIP) {
                c.setLastPurchaseDate(LocalDateTime.now().minusDays(1 + rand.nextInt(10)));
                c.setTotalSpend(new BigDecimal(75000 + rand.nextInt(150000)));
                c.setTotalOrders(15 + rand.nextInt(25));
            } else {
                c.setLastPurchaseDate(LocalDateTime.now().minusDays(2 + rand.nextInt(25)));
                c.setTotalSpend(new BigDecimal(5000 + rand.nextInt(30000)));
                c.setTotalOrders(1 + rand.nextInt(5));
            }
            customers.add(c);
        }
        List<Customer> savedCustomers = customerRepository.saveAll(customers);

        // 6. Seed Purchases & Stock Ledger
        List<Purchase> purchases = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 35; i++) {
            Supplier supp = allSuppliers.get(i % allSuppliers.size());
            LocalDate pDate = today.minusDays(90 - (i * 2));
            LocalDate expDate = pDate.plusDays(supp.getLeadTimeDays());
            LocalDate actDate = supp.getName().contains("Bharat") ? expDate.plusDays(4 + rand.nextInt(5)) : expDate;

            Purchase po = new Purchase();
            po.setOrderNumber("PO-2024-" + String.format("%04d", 1000 + i));
            po.setSupplier(supp);
            po.setOrderDate(pDate);
            po.setExpectedDeliveryDate(expDate);
            po.setActualDeliveryDate(actDate);
            po.setStatus(PurchaseStatus.RECEIVED);
            po.setNotes("Procurement batch #" + (i + 1));

            Product p1 = savedProducts.get((i * 3) % savedProducts.size());
            Product p2 = savedProducts.get((i * 3 + 1) % savedProducts.size());

            PurchaseItem item1 = new PurchaseItem(po, p1, 50 + rand.nextInt(100), p1.getPurchasePrice());
            PurchaseItem item2 = new PurchaseItem(po, p2, 40 + rand.nextInt(80), p2.getPurchasePrice());
            po.addItem(item1);
            po.addItem(item2);

            purchases.add(po);
        }
        purchaseRepository.saveAll(purchases);

        // 7. Seed 500+ Sales Transactions across 90 days with realistic weekly and seasonal patterns
        List<Sale> sales = new ArrayList<>();
        PaymentMethod[] paymentMethods = {PaymentMethod.UPI, PaymentMethod.CARD, PaymentMethod.BANK_TRANSFER, PaymentMethod.CASH};

        for (int i = 0; i < 520; i++) {
            Customer cust = savedCustomers.get(rand.nextInt(savedCustomers.size()));
            User seller = (i % 2 == 0) ? employee : manager;

            // Distribute over last 90 days, with higher concentration recently
            int daysAgo = 90 - (int) Math.sqrt(rand.nextInt(8100)); // quadratic distribution towards today
            LocalDateTime sDate = today.minusDays(daysAgo).atTime(9 + rand.nextInt(10), rand.nextInt(60), rand.nextInt(60));

            Sale sale = new Sale();
            sale.setInvoiceNumber("INV-2024-" + String.format("%05d", 20000 + i));
            sale.setCustomer(cust);
            sale.setUser(seller);
            sale.setSaleDate(sDate);
            sale.setPaymentMethod(paymentMethods[rand.nextInt(paymentMethods.length)]);
            sale.setStatus(SaleStatus.COMPLETED);

            // 1 to 3 items per sale
            int numItems = 1 + rand.nextInt(3);
            for (int k = 0; k < numItems; k++) {
                Product pr = savedProducts.get((i + k * 7) % savedProducts.size());
                int qty = 1 + rand.nextInt(4);
                BigDecimal disc = (i % 7 == 0) ? pr.getSellingPrice().multiply(new BigDecimal("0.10")) : BigDecimal.ZERO;

                SaleItem sItem = new SaleItem(sale, pr, qty, pr.getSellingPrice(), pr.getPurchasePrice(), disc);
                sale.addItem(sItem);

                // Stock transaction
                StockTransaction stx = new StockTransaction(
                        pr,
                        -qty,
                        StockTransactionType.SALE,
                        sale.getInvoiceNumber(),
                        "POS Sale Invoice " + sale.getInvoiceNumber(),
                        seller,
                        pr.getCurrentStock()
                );
                stx.setTimestamp(sDate);
                stockTransactionRepository.save(stx);
            }

            sales.add(sale);
        }
        saleRepository.saveAll(sales);

        // 8. Seed 100+ Expenses across categories with intentional Transportation anomaly
        List<Expense> expenses = new ArrayList<>();
        ExpenseCategory[] expCats = ExpenseCategory.values();

        for (int i = 0; i < 110; i++) {
            int daysAgo = 90 - (i * 90 / 110);
            LocalDate eDate = today.minusDays(daysAgo);
            ExpenseCategory cat = expCats[i % expCats.length];

            BigDecimal amt;
            String desc;
            if (cat == ExpenseCategory.RENT) {
                amt = new BigDecimal("45000.00");
                desc = "Warehouse & Office Lease Rental";
            } else if (cat == ExpenseCategory.SALARY) {
                amt = new BigDecimal("85000.00");
                desc = "Payroll Disbursements & Operator Wages";
            } else if (cat == ExpenseCategory.TRANSPORTATION) {
                // Intentional Anomaly: Transport expenses in last 30 days are 35% higher due to emergency courier routing
                amt = daysAgo < 30 ? new BigDecimal("18500.00") : new BigDecimal("9500.00");
                desc = daysAgo < 30 ? "Expedited Air Freight & Last-Mile Surcharges (Spike)" : "Standard Freight & Logistics Distribution";
            } else if (cat == ExpenseCategory.ELECTRICITY) {
                amt = new BigDecimal("12000.00");
                desc = "Industrial Power Grid Utility Bill";
            } else if (cat == ExpenseCategory.MARKETING) {
                amt = new BigDecimal("15000.00");
                desc = "Digital Ad Campaigns & B2B Catalogs";
            } else if (cat == ExpenseCategory.SOFTWARE) {
                amt = new BigDecimal("8000.00");
                desc = "Cloud Infrastructure & SaaS Subscriptions";
            } else {
                amt = new BigDecimal(2000 + rand.nextInt(5000));
                desc = "General Maintenance & Consumables";
            }

            Expense exp = new Expense(cat, amt, eDate, desc, PaymentMethod.BANK_TRANSFER, admin);
            expenses.add(exp);
        }
        expenseRepository.saveAll(expenses);

        // 9. Seed Audit Logs
        for (int i = 0; i < 20; i++) {
            auditLogRepository.save(new AuditLog(
                    admin,
                    "SYSTEM_INIT",
                    "Database",
                    (long) (i + 1),
                    "System initialized and ledger balances synced",
                    "127.0.0.1"
            ));
        }

        // 10. Generate AI Insights Engine Output
        aiInsightService.generateAllInsights();

        System.out.println(">>> INTELLIERP DATABASE SEEDING COMPLETED SUCCESSFULLY!");
        System.out.println(">>> Users: admin/admin123, manager/manager123, employee/employee123");
    }
}
