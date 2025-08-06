Personal-Finance-Tracker-JavaFX-SQLite-

A secure desktop finance tracker built using JavaFX and SQLite that allows users to log in, track deposits and withdrawals, persist transactions, and export data to CSV.

User Login & Registration:
  Secure login system that uses password hashing (SHA-256).
  Used SQLite to create a table for usernames (userTable) to manage credentials.

Deposit & Withdrawal Tracking:
  Real time balance updates.
  Input validation and error handling.
  The transaction history is stored in SQLite.

Persistent Transaction History:
  All transactions are saved in financeTrackerTable.
  Reloads previous transactions from the database upon login.

Utility Functions:
  Clears all user data.
  Clear only the transaction history.
  Able to export transactions to a .csv file.

Interface:
  Built with JavaFX.
  Clean and user friendly layout with custom styling.

Database Structure:
  userTable: Stores usernames and hashed passwords.
  financeTrackerTable: Stores each transaction with fields:
    transactionNumber, username, typeOfTransaction, amount, dateAndTime.

Technologies Used:
  Java 17+
  JavaFX
  SQLite (via JDBC)
  SHA-256 (for password hashing)

How to Run
  Clone the repository
  Ensure JavaFX is set up in your IDE, VSC in my case.
  Make sure the SQLite JDBC driver is included in your project (Add to referenced libraries).
  Update the path to finance_tracker.db in the code if needed:
    private final String url = "jdbc:sqlite:/your/path/to/finance_tracker.db"; (Line 35).
  Run the FinanceTrackerApp class
  
