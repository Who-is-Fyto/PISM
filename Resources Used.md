# Resources Used — Fyto PIMS

This document lists all the online tutorials, documentation, forum discussions, and guides referenced during the development of the **Fyto Pharmacy Inventory Management System (Fyto PIMS)**, along with short student explanations of how each resource helped with the codebase.

---

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
