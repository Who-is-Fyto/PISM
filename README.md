# Fyto PIMS — Pharmacy Inventory Management System

**Fyto PIMS** is a multi-user Java Swing desktop application designed for modern retail and community pharmacies. It provides end-to-end management of pharmaceutical inventory, fast point-of-sale (POS) dispensing, supplier directories, cashier accountability, and real-time business intelligence reports with thermal receipt printing and CSV data export.

The application features a modern, clean graphical user interface styled with the **FlatLaf** Look and Feel, coupled with a resilient Model-View-Controller (MVC) and Data Access Object (DAO) architecture that seamlessly integrates with a **MySQL** relational database or gracefully falls back to an in-memory mock cache when offline.

---

## Table of Contents

1. [Key Features](#key-features)
2. [Architecture & Code Structure](#architecture--code-structure)
3. [System Requirements & Dependencies](#system-requirements--dependencies)
4. [Database Setup (MySQL Workbench)](#database-setup-mysql-workbench)
5. [Configuration](#configuration)
6. [How to Run the Application](#how-to-run-the-application)
7. [Default User Accounts](#default-user-accounts)
8. [Automated Testing & Quality Verification](#automated-testing--quality-verification)
9. [Resources Used](#resources-used)

---

## Key Features

### 1. Role-Based Access Control (RBAC) & Secure Login
- **Unified Login Screen** (`LoginFrame`): Clean split-card interface with user credentials validation, password reveal toggle, and clear buttons.
- **Admin vs. Cashier Roles**: Dynamically directs users to their dedicated dashboard based on their database-assigned role. Cashiers cannot access sensitive administrative or financial settings.
- **Session Management** (`UserSession`): Thread-safe singleton storing the currently authenticated user's ID, username, and role, enabling safe multi-window transitions and audit logging.

### 2. Master Administrator Dashboard (`AdminDashboard`)
- **CardLayout Sidebar Navigation**: Smoothly switch between workspace views without opening multiple disjointed windows.
- **Medicine Catalog Management**: Add, update, search, and delete medications with fields for SKU/barcode, generic name, brand name, category, dosage form, unit price, stock quantity, minimum threshold, and expiry date.
- **Visual Stock Health Indicators**: Color-coded badges (`In Stock`, `Low Stock`, `Out of Stock`) to immediately spot shortages.
- **Supplier Directory**: Manage pharmaceutical distributors, contact persons, phone numbers, email addresses, and physical locations.
- **Cashier Account Provisioning**: Create and manage staff accounts directly within the UI.

### 3. Cashier Point-of-Sale (POS) Station (`CashierDashboard`)
- **Fast Product Search**: Instant filtering by medicine name, SKU, or category.
- **Live Cart System**: Add items, adjust dispensing quantities, and inspect line-item subtotals in real time.
- **Stock Overflow Prevention**: Guards prevent dispensing more units than currently exist in stock.
- **Atomic Checkout**: Deducts inventory counts across `medicines` and registers sales records in `sales` and `sale_items` within an atomic transaction.
- **Thermal Receipt Generation** (`BillReceiptDialog`): Formats a 58mm/80mm style itemized receipt displaying pharmacy details, invoice number, cashier name, timestamp, tax breakdown, and total, ready for printing or preview.

### 4. Business Intelligence & Reporting (`ReportsPanel`)
- **Metric KPI Cards**: Top-level overview of Total Gross Sales, Low-Stock Warnings, Active Inventory Value, and Total Prescriptions Filled.
- **Date Range Filters**: Filter sales reports by Today, Past 7 Days, This Month, or Custom Date Ranges.
- **Expiry Risk Engine**: Alerts the pharmacy team to all medications expiring within 30 days.
- **Top-Selling Drugs**: Aggregated volume and revenue breakdowns of top medicines.
- **One-Click RFC 4180 CSV Export** (`CSVExporter`): Export tabular data directly to standard CSV spreadsheets for external accounting and audits.

---

## Architecture & Code Structure

The project follows the standard **Model-View-Controller (MVC)** and **Data Access Object (DAO)** patterns to ensure complete separation of concerns between user interface components, business logic, and database persistence.

```
pharmacyIMS/
├── build.xml                           # Apache Ant build configuration
├── workbench_schema.sql                # Complete MySQL schema & seed data
├── resources/
│   └── db.properties                   # External database configuration
├── lib/                                # Project dependencies (JARs)
│   ├── flatlaf-3.5.4.jar               # Modern Look and Feel library
│   └── mysql-connector-j-9.2.0.jar     # Official MySQL JDBC driver
├── src/
│   ├── db.properties                   # Fallback classpath database configuration
│   └── pharmacyims/
│       ├── PharmacyIMS.java            # Main application launcher & FlatLaf init
│       ├── LoginFrame.java             # Login interface & credential validation
│       │
│       ├── model/                      # POJO Domain Entities
│       │   ├── User.java               # User account entity
│       │   ├── Medicine.java           # Pharmaceutical product entity
│       │   ├── Supplier.java           # Supplier entity
│       │   ├── Sale.java               # Transaction master entity
│       │   └── SaleItem.java           # Transaction line-item entity
│       │
│       ├── dao/                        # Data Access Objects (SQL + Mock fallback)
│       │   ├── UserDAO.java            # User retrieval & authentication
│       │   ├── MedicineDAO.java        # Inventory CRUD & stock decrement
│       │   ├── SupplierDAO.java        # Supplier management CRUD
│       │   └── SaleDAO.java            # Checkout transactions & reporting queries
│       │
│       ├── session/
│       │   └── UserSession.java        # Current user session singleton
│       │
│       ├── util/
│       │   └── DBConnection.java       # JDBC connection manager & config loader
│       │
│       └── ui/                         # Presentation Layer
│           ├── common/                 # Reusable UI widgets
│           │   ├── HeaderBar.java      # Branded top navigation bar
│           │   ├── MetricCard.java     # KPI summary card component
│           │   ├── StatusBadgeRenderer.java   # Low/In/Out stock badge renderer
│           │   └── CurrencyTableCellRenderer.java # Currency ($) table formatting
│           ├── admin/                  # Administrative screens & modals
│           │   ├── AdminDashboard.java     # Master administrative tab container
│           │   ├── MedicineFormDialog.java # Modal for adding/editing medicines
│           │   ├── SupplierFormDialog.java # Modal for adding/editing suppliers
│           │   └── UserFormDialog.java     # Modal for creating staff accounts
│           ├── cashier/                # Cashier POS screens & components
│           │   ├── CashierDashboard.java   # Split-pane POS interface
│           │   ├── StockLookupPanel.java   # Live medicine search component
│           │   └── BillReceiptDialog.java  # Thermal receipt preview & print dialog
│           └── reports/                # Analytics & reports
│               ├── ReportsPanel.java       # Analytics dashboard & date filtering
│               └── CSVExporter.java        # CSV spreadsheet export utility
└── test/
    └── pharmacyims/
        ├── EndToEndSystemTest.java     # Complete 18-step system test suite
        ├── AdminModuleTest.java        # Admin workflow unit tests
        ├── CashierModuleTest.java      # POS checkout unit tests
        ├── LoginFrameTest.java         # Authentication unit tests
        └── ReportingModuleTest.java    # Analytics & CSV export unit tests
```

---

## System Requirements & Dependencies

- **Java Development Kit (JDK)**: JDK 17, 21, or 26.
- **Build Tool / IDE**: Apache NetBeans IDE (17+) or standard Apache Ant CLI.
- **Relational Database**: MySQL Server 8.0+ (optional; the application gracefully operates in offline mock mode if MySQL is not running).
- **Libraries (included in `lib/`)**:
  - `flatlaf-3.5.4.jar` — Modern, lightweight Swing Look and Feel.
  - `mysql-connector-j-9.2.0.jar` — MySQL official JDBC driver.

---

## Database Setup (MySQL Workbench)

To configure the live relational database:

1. **Launch MySQL Workbench** and connect to your local MySQL instance.
2. Open the schema file:
   - Click **File** > **Open SQL Script...** and select [`workbench_schema.sql`](file:///home/michael/NetBeansProjects/pharmacyIMS/workbench_schema.sql) located in the project root directory.
3. Click the **Execute (Lightning Bolt)** icon to run the script.
   - The script creates the `pims_db` database, configures foreign key constraints with cascade rules, and seeds initial data (suppliers, medicines, users, and past sales transactions).
4. Verify the database tables under the schemas panel:
   - `users`
   - `suppliers`
   - `medicines`
   - `sales`
   - `sale_items`

---

## Configuration

Database credentials are read dynamically from `resources/db.properties` (or from `src/db.properties` as a fallback):

```properties
db.url=jdbc:mysql://localhost:3306/pims_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=fyto
db.password=#Chipapamike16
```

> **Resilient Offline Fallback**:
> If the MySQL database service is unreachable or credentials fail, `DBConnection` issues a clean warning and all DAOs automatically switch to an in-memory cache populated with sample pharmaceutical data. The application will continue running without crashing.

---

## How to Run the Application

### Option A: Using Apache NetBeans IDE
1. Open Apache NetBeans.
2. Select **File** > **Open Project...** and select `/home/michael/NetBeansProjects/pharmacyIMS`.
3. Right-click the project in the Projects explorer and click **Run** (or press `F6`).

### Option B: Using the Command Line (Ant)
From the project root:
```bash
ant run
```

### Option C: Running the Built JAR
```bash
java -jar dist/pharmacyIMS.jar
```

---

## Default User Accounts

When launching the application, you can log in with either of the following seeded accounts:

| Username | Password | Assigned Role | Accessible Dashboards |
| :--- | :--- | :--- | :--- |
| `admin` | `admin123` | **ADMIN** | Medicine Catalog, Suppliers, Cashier Staff Management, Financial Reports |
| `cashier1` | `cashier123` | **CASHIER** | Cashier POS, Medicine Stock Lookup, Dispensing Cart, Thermal Receipt Printing |

---

## Automated Testing & Quality Verification

Fyto PIMS includes an automated integration test suite covering all 18 functional system requirements:

```bash
# Run using the compiled test classes
java -cp "build/classes:build/test/classes:lib/*" pharmacyims.EndToEndSystemTest
```

### Test Coverage Highlights
- **TC-01 to TC-05 (Authentication)**: Driver loading, empty credential handling, invalid login rejection, Admin & Cashier role authentication.
- **TC-06 to TC-10 (Admin Module)**: Registering suppliers, creating medicines with foreign keys, live searching, low-stock threshold flags, and staff provisioning.
- **TC-11 to TC-15 (Cashier POS)**: Product lookup, stock overflow boundary guard, subtotal calculations, atomic checkout with inventory decrement, and receipt dialog creation.
- **TC-16 to TC-18 (Reporting & Export)**: Date-range revenue calculations, expiry warning engine, and RFC 4180 CSV generation.

---

## Resources Used

The following resources, guides, and documentation were referenced during the design and development of Fyto PIMS, along with explanations of how each helped in building the codebase:

### 1. Concurrency & Threading in Swing
- **Link**: [Oracle Java Tutorials — Concurrency in Swing](https://docs.oracle.com/javase/tutorial/uiswing/concurrency/index.html)
- **How it helped**:
  I used this tutorial to understand how threading works in Java Swing. It explained why you have to launch UI windows on the Event Dispatch Thread (EDT) using `SwingUtilities.invokeLater()` so the interface doesn't lag or crash. It also helped me use `javax.swing.Timer` for simulated authentication delays and smooth transitions without freezing the UI.

---

### 2. Core Java Swing Component Reference
- **Link**: [Oracle Java Tutorials — Creating a GUI With JFC/Swing](https://docs.oracle.com/javase/tutorial/uiswing/index.html)
- **How it helped**:
  This was my go-to reference for all basic Swing controls. It helped me learn how to construct and customize components like `JFrame`, `JPanel`, `JButton`, `JLabel`, `JTable`, `JScrollPane`, and modal `JDialog` popups across the application.

---

### 3. Desktop GUI Layout & Visual Structure
- **Link**: [JetBrains Guide — Design GUI Using Swing](https://www.jetbrains.com/help/idea/design-gui-using-swing.html)
- **How it helped**:
  This helped me visualize how component hierarchies work in desktop applications and how container nesting allows complex screens (like the Admin and Cashier dashboards) to stay organized and responsive when resized.

---

### 4. Beginner's Guide to Swing Components
- **Link**: [Medium Guide — A Beginner's Guide to Java Swing Components](https://medium.com/@neerajrs124/a-beginners-guide-to-java-swing-components-bed7a8dca96f)
- **How it helped**:
  A simple, straightforward walkthrough that helped me quickly review event listeners (`ActionListener`, `KeyAdapter`) and how to read user input from `JTextField` and `JPasswordField` with clean error handling.

---

### 5. Multi-Screen Navigation in Swing
- **Link**: [Oracle Forums — Multiple Screens in a Single Swing Application](https://forums.oracle.com/ords/apexds/post/multiple-screens-in-a-single-swing-application-7220)
- **How it helped**:
  This discussion showed me how to transition between different application screens (moving from the `LoginFrame` to either `AdminDashboard` or `CashierDashboard`) cleanly, properly disposing of the previous window instead of letting hidden windows consume memory in the background.

---

### 6. Swing GUI Coding Best Practices
- **Link**: [Stack Overflow — Java Swing GUI Best Practices From a Code Standpoint](https://stackoverflow.com/questions/5473828/java-swing-gui-best-practices-from-a-code-standpoint)
- **How it helped**:
  This post gave me great tips on keeping the code clean and maintainable: separating reusable UI components (like `HeaderBar` and `MetricCard`), avoiding putting all UI logic into a single giant class, and closing database resources safely.

---

### 7. Swing Layouts & Container Spacing
- **Link**: [Hyperskill — Java Swing Containers & Layouts](https://hyperskill.org/learn/step/11299)
- **How it helped**:
  This helped me understand how to use `EmptyBorder` and `CompoundBorder` to add modern padding and breathing room around components so buttons and input boxes don't look cramped against window borders.

---

### 8. Visual Guide to Layout Managers
- **Link**: [Oracle Java Tutorials — A Visual Guide to Layout Managers](https://docs.oracle.com/javase/tutorial/uiswing/layout/visual.html)
- **How it helped**:
  I referenced this guide to pick the best layout manager for each section of the app:
  - `BorderLayout` for root panels and dashboards.
  - `GridLayout` for side-by-side split screens and KPI card rows.
  - `FlowLayout` for toolbar action buttons and pill badge tags.
  - `GridBagLayout` for form dialogs that require aligned labels and input fields.

---

### 9. Switching Views with CardLayout
- **Link**: [Oracle Java Tutorials — How to Use CardLayout](https://docs.oracle.com/javase/tutorial/uiswing/layout/card.html)
- **How it helped**:
  This was essential for the Master Admin Dashboard. It showed me how to use `CardLayout` to switch between tabs (Medicine Inventory, Supplier Directory, Cashier Accounts, and Financial Reports) when clicking sidebar buttons without opening separate windows.

---

### 10. Modern Swing Component Styling
- **Link**: [Stack Overflow — Please Recommend Pretty Java Swing Components Library](https://stackoverflow.com/questions/12322296/please-recommend-pretty-java-swing-components-library)
- **How it helped**:
  This thread helped me discover modern styling solutions for Java desktop applications so that Fyto PIMS wouldn't look like an old 1990s application. It pointed me toward FlatLaf as the premier modern Look and Feel.

---

### 11. Curated List of Swing Tools & Libraries
- **Link**: [GitHub — Awesome Swing](https://github.com/parubok/awesome-swing)
- **How it helped**:
  A helpful collection of modern Java Swing tools, libraries, renderers, and themes that helped me discover best practices for rendering tables and custom badges.

---

### 12. Modern Java Swing UI Design Video
- **Link**: [YouTube — Java Swing UI Design Tutorial](https://www.youtube.com/watch?v=4DBaDoBCpcA)
- **How it helped**:
  Watched this tutorial to see practical examples of how to customize colors, round corners, style tables, and build clean desktop application layouts.

---

### 13. Setting Up FlatLaf in Swing
- **Link**: [Stack Overflow — How to Use FlatLaf Library in Swing Application](https://stackoverflow.com/questions/66822118/how-to-use-flatlaf-library-in-swing-application)
- **How it helped**:
  This showed me the exact code needed to initialize FlatLaf (`FlatLightLaf.setup()`) before launching any GUI frames, and how to put global defaults into `UIManager` for consistent corner radii and row heights.

---

### 14. Official FlatLaf Documentation
- **Link**: [FormDev — FlatLaf Official Documentation](https://www.formdev.com/flatlaf/)
- **How it helped**:
  I used the official documentation to apply FlatLaf client properties throughout the codebase, such as:
  - `arc: 10` for modern rounded buttons and text fields.
  - `placeholderText` for intuitive field hints.
  - `showClearButton: true` and `showRevealButton: true` on password and search fields.
  - `outline: error` to highlight invalid input fields in red.

---

### 15. Modern Dashboard Design with Sidebar Navigation
- **Link**: [YouTube — Modern Java Swing Dashboard UI Design](https://www.youtube.com/watch?v=vMnyhzaWIEU)
- **How it helped**:
  Gave me inspiration for designing the Admin and Cashier dashboards with a persistent top navigation bar, quick status indicators, and clean cards.

---

### 16. MVC Architecture in Swing
- **Link**: [Wikibooks — Java Swings / MVC](https://en.wikibooks.org/wiki/Java_Swings/MVC)
- **How it helped**:
  Helped me understand how to implement the Model-View-Controller (MVC) pattern in a Java desktop project:
  - **Models** (`User`, `Medicine`, `Supplier`, `Sale`, `SaleItem`) hold the raw data.
  - **DAOs / Controllers** (`UserDAO`, `MedicineDAO`, `SupplierDAO`, `SaleDAO`) manage business logic and SQL queries.
  - **Views** (`LoginFrame`, `AdminDashboard`, `CashierDashboard`) handle only presentation and user input.

---

### 17. Practical Swing MVC Architecture Example
- **Link**: [Stack Overflow — Java Swing MVC Architecture with MCVE Example](https://stackoverflow.com/questions/34089447/java-swing-mvc-architecture-q-with-mcve-example)
- **How it helped**:
  Provided a concrete code example of separating database calls from button event listeners so that UI code doesn't get cluttered with SQL queries.

---

### 18. Organizing a Swing Project Codebase
- **Link**: [Stack Overflow — How to Organize a Swing GUI Application](https://stackoverflow.com/questions/6269851/how-to-organize-a-swing-gui-application)
- **How it helped**:
  Guided the package organization of Fyto PIMS into clean, dedicated packages: `pharmacyims.model`, `pharmacyims.dao`, `pharmacyims.ui`, `pharmacyims.util`, and `pharmacyims.session`.

---

### 19. Using Nested Layout Managers
- **Link**: [Oracle Java Tutorials — Using Layout Managers](https://docs.oracle.com/javase/tutorial/uiswing/layout/using.html)
- **How it helped**:
  Showed me how to nest different layout managers together to build complex screens—such as putting `FlowLayout` action bars at the bottom of `BorderLayout` dialogs and using `GridBagLayout` for data entry forms.

---

### 20. NetBeans GUI & Project Quickstart
- **Link**: [Apache NetBeans — Java GUI Applications Quickstart](https://netbeans.apache.org/tutorial/main/kb/docs/java/quickstart-gui/)
- **How it helped**:
  Helped me understand how NetBeans manages Ant-based Java projects, includes external JAR libraries in the `lib/` directory, and compiles bytecode into the `build/` and `dist/` folders.
