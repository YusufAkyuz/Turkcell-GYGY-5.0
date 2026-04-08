import java.util.*;

public class BankingApplication {
    public static void main(String[] args) {
        System.out.println("========== BANKING APPLICATION SIMULATION ==========");
        BankService bank = new BankService();
        
        // 1. Özellik: Yeni Hesap Oluşturma
        System.out.println("\n[1] Hesaplar Oluşturuluyor...");
        String account1 = bank.createAccount("Ahmet Yılmaz", 1500.0);
        String account2 = bank.createAccount("Ayşe Demir", 3000.0);
        
        bank.printAccountDetails(account1);
        bank.printAccountDetails(account2);
        
        // 2. Özellik: Para Yatırma
        System.out.println("\n[2] Para Yatırma İşlemi...");
        System.out.println("Ahmet'in hesabına 500 TL yatırılıyor.");
        bank.deposit(account1, 500.0);
        bank.printAccountDetails(account1);
        
        // 3. Özellik: Para Çekme
        System.out.println("\n[3] Para Çekme İşlemi...");
        System.out.println("Ayşe'nin hesabından 750 TL çekiliyor.");
        bank.withdraw(account2, 750.0);
        bank.printAccountDetails(account2);
        
        // 4. Özellik: Hesaptan Hesaba Transfer (Havale/EFT)
        System.out.println("\n[4] Para Transfer İşlemi...");
        System.out.println("Ahmet'in hesabından Ayşe'nin hesabına 1000 TL transfer ediliyor.");
        bank.transfer(account1, account2, 1000.0);
        
        bank.printAccountDetails(account1);
        bank.printAccountDetails(account2);

        // Ekstra: Hata Senaryosu Simülasyonu
        System.out.println("\n[5] Hata Senaryosu: Yetersiz Bakiye ile İşlem Denemesi");
        bank.withdraw(account1, 5000.0);
        
        System.out.println("\n========== SİMÜLASYON TAMAMLANDI ==========");
    }
}

// In-Memory Repository Sınıfı
// Veriler RAM'de bir HashMap (veritabanı simülasyonu) içinde tutulur.
class AccountRepository {
    private final Map<String, Account> db = new HashMap<>();

    public void save(Account account) {
        db.put(account.getAccountNumber(), account);
    }

    public Account findById(String accountNumber) {
        return db.get(accountNumber);
    }

    public List<Account> findAll() {
        return new ArrayList<>(db.values());
    }
}

// Entity Model
class Account {
    private String accountNumber;
    private String ownerName;
    private double balance;

    public Account(String accountNumber, String ownerName, double balance) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = balance;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getOwnerName() { return ownerName; }
    public double getBalance() { return balance; }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
        }
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }
}

// Banka Servisi - İş Mantığı
class BankService {
    private final AccountRepository repository;

    public BankService() {
        // Bağımlılık burada manuel olarak enjekte ediliyor (DI simülasyonu)
        this.repository = new AccountRepository();
    }

    public String createAccount(String ownerName, double initialDeposit) {
        // Basit bir 6 haneli rastgele hesap numarası üretiyoruz
        String accountNumber = String.format("%06d", new Random().nextInt(999999));
        Account account = new Account(accountNumber, ownerName, initialDeposit);
        repository.save(account);
        System.out.println(" -> Yeni hesap oluşturuldu. Sahibi: " + ownerName + " (Hesap No: " + accountNumber + ")");
        return accountNumber;
    }

    public void deposit(String accountNumber, double amount) {
        Account account = repository.findById(accountNumber);
        if (account != null) {
            account.deposit(amount);
            System.out.println(" -> Başarılı: " + accountNumber + " nolu hesaba " + amount + " TL yatırıldı.");
        } else {
            System.out.println(" -> Hata: Hesap bulunamadı!");
        }
    }

    public void withdraw(String accountNumber, double amount) {
        Account account = repository.findById(accountNumber);
        if (account != null) {
            boolean success = account.withdraw(amount);
            if (success) {
                System.out.println(" -> Başarılı: " + accountNumber + " nolu hesaptan " + amount + " TL çekildi.");
            } else {
                System.out.println(" -> Hata: Yetersiz bakiye! İşlem gerçekleştirilemedi.");
            }
        } else {
            System.out.println(" -> Hata: Hesap bulunamadı!");
        }
    }

    public void transfer(String fromAccountStr, String toAccountStr, double amount) {
        Account fromAccount = repository.findById(fromAccountStr);
        Account toAccount = repository.findById(toAccountStr);

        if (fromAccount != null && toAccount != null) {
            if (fromAccount.withdraw(amount)) {
                toAccount.deposit(amount);
                System.out.println(" -> Başarılı: " + fromAccountStr + " nolu hesaptan " + toAccountStr + " nolu hesaba " + amount + " TL transfer edildi.");
            } else {
                System.out.println(" -> Hata: Transfer başarısız. Gönderen hesapta yeterli bakiye yok!");
            }
        } else {
            System.out.println(" -> Hata: Gönderen veya alıcı hesap bulunamadı!");
        }
    }

    public void printAccountDetails(String accountNumber) {
        Account acc = repository.findById(accountNumber);
        if (acc != null) {
            System.out.println("    [Hesap Özeti] No: " + acc.getAccountNumber() + " | İsim: " + acc.getOwnerName() + " | Güncel Bakiye: " + acc.getBalance() + " TL");
        }
    }
}
