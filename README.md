NullArchive 📚🌐
Preserving the world's digital heritage through decentralized curation.

NullArchive is a digital library platform dedicated to the philosophy of Open Access. It serves as a resilient repository for public domain works and open-license content (Creative Commons CC0, CC BY, etc.), ensuring that human knowledge and culture are never lost to digital decay.

🏛️ The Philosophy
The internet is fragile. Sites go down, and information disappears. NullArchive was built to:

Prevent Digital Loss: Allow anyone to upload public domain or open-licensed works.

Democratize Access: Provide a fast, simple, and clean interface for reading.

Respect Privacy: Prioritize anonymity, allowing users to maintain a library without intrusive data collection.

👥 User Roles & Access Control
The system is divided into three distinct levels of interaction:

Anonymous User: No login required. NullArchive respects your privacy by using a Permanent Session Token. This token is stored in the browser and automatically extends its expiration every time you visit. It allows you to save "Favorites" and keep books "On the Bench" (Currently Reading) without ever creating an account.

Editor: Users who wish to contribute to the archive. By providing a nickname, email, and password, Editors can upload books (PDFs and Covers).

Administrator: The gatekeepers of quality. Admins have access to a Curatorship Panel where they review, approve, or reject pending submissions to ensure the archive remains reliable and high-quality.

🛠️ Technical Stack
Built from the ground up without heavy frameworks to demonstrate core engineering principles:

Frontend: Semantic HTML5, Modern CSS3 (Responsive Design), and Vanilla JavaScript.

Backend: Pure Java utilizing the native HttpServer for a lightweight, high-performance web server.

Database: MySQL with JDBC for robust data persistence.

Infrastructure: Custom routing and static file handling implemented in Java.

🛡️ Security Architecture
Security was a primary pillar during development, following OWASP Top 10 guidelines:

Cryptography: Passwords are never stored in plain text. We use BCrypt with a high cost factor (Salted Hashing) to ensure resistance against rainbow table and brute-force attacks.

Authentication: Secure token-based access control.

Data Integrity: Complete sanitization of user inputs and the use of Prepared Statements to eliminate the risk of SQL Injection.

Protection against XSS & IDOR: Strict validation of user permissions per request, ensuring a user cannot modify or access data (like the Admin Panel) without the proper authorization level.

Stealth Security: Implementation of "Security by Obscurity" where sensitive administrative routes return 404 Not Found to unauthorized users to prevent directory enumeration.

📺 System Demonstration
[PLACE YOUR VIDEO HERE] > Instruction: Insert a video showing the flow from an Anonymous user saving a book, to an Editor uploading a new title, and finally the Admin approving it in the Curatorship Panel.

📸 Project Gallery
🖥️ Main Interface & Catalog
[PLACE PRINT OF HOME PAGE HERE] > Instruction: Show the main grid with the book covers and the search/filter tags.

🔐 Security & Directory Structure
[PLACE PRINT OF PROJECT DIRECTORY STRUCTURE HERE] > Instruction: Show your folder organization (src, resources, etc.) to demonstrate clean code architecture.

🗄️ Database Schema
[PLACE PRINT OF YOUR MYSQL TABLES/ERD HERE] > Instruction: A screenshot of your MySQL Workbench or terminal showing the books and users tables.

🚀 How to Run
Clone the repository.

Import the schema.sql into your MySQL instance.

Configure your config.properties file with your DB credentials.

Run App.java.

Access http://localhost:8081/home.

Developed by Arthur Frade. Bridging the gap between the past and the digital future.
