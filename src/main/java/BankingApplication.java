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

        try {
            // Ekstra: Hata Senaryosu Simülasyonu
            System.out.println("\n[5] Hata Senaryosu: Yetersiz Bakiye ile İşlem Denemesi");
            bank.withdraw(account1, 5000.0);
        } catch (BusinessException e) {
            // Fırlatılan exception'ı yakalayıp istemciye döneceğimiz standart ErrorResponse formatına çeviriyoruz
            ErrorResponse error = new ErrorResponse("İşlem Başarısız", e.getClass().getSimpleName(), e.getMessage());
            System.out.println(" -> " + error);
        }

        try {
            System.out.println("\n[6] Hata Senaryosu: Olmayan Hesaptan Para Çekme");
            bank.withdraw("999999", 100.0);
        } catch (BusinessException e) {
            ErrorResponse error = new ErrorResponse("Kayıt Bulunamadı", e.getClass().getSimpleName(), e.getMessage());
            System.out.println(" -> " + error);
        }
        
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

    public void withdraw(double amount) {
        // İş kurallarını kontrol edip, hata durumunda özel exception fırlatıyoruz
        if (amount <= 0) {
            throw new BusinessException("Çekilecek tutar sıfırdan büyük olmalıdır.");
        }
        if (balance < amount) {
            throw new InsufficientBalanceException("Bakiyeniz bu işlem için yetersiz. Mevcut bakiye: " + balance + " TL");
        }
        balance -= amount;
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
        if (account == null) {
            // Hata mesajı basmak yerine özel exception fırlatıyoruz
            throw new AccountNotFoundException("Para yatırılacak hesap bulunamadı: " + accountNumber);
        }
        account.deposit(amount);
        System.out.println(" -> Başarılı: " + accountNumber + " nolu hesaba " + amount + " TL yatırıldı.");
    }

    public void withdraw(String accountNumber, double amount) {
        Account account = repository.findById(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("Para çekilecek hesap bulunamadı: " + accountNumber);
        }
        
        // Account sınıfı kendi içinde yetersiz bakiye durumunda exception fırlatacak
        account.withdraw(amount);
        System.out.println(" -> Başarılı: " + accountNumber + " nolu hesaptan " + amount + " TL çekildi.");
    }

    public void transfer(String fromAccountStr, String toAccountStr, double amount) {
        Account fromAccount = repository.findById(fromAccountStr);
        Account toAccount = repository.findById(toAccountStr);

        if (fromAccount == null) {
            throw new AccountNotFoundException("Gönderici hesap bulunamadı: " + fromAccountStr);
        }
        if (toAccount == null) {
            throw new AccountNotFoundException("Alıcı hesap bulunamadı: " + toAccountStr);
        }

        // Çekme işlemi hata verirse (örn: bakiye yetersiz) exception fırlayacağı için aşağıdaki yatırma kodu çalışmaz (Atomic gibi davranır)
        fromAccount.withdraw(amount);
        toAccount.deposit(amount);
        System.out.println(" -> Başarılı: " + fromAccountStr + " nolu hesaptan " + toAccountStr + " nolu hesaba " + amount + " TL transfer edildi.");
    }

    public void printAccountDetails(String accountNumber) {
        Account acc = repository.findById(accountNumber);
        if (acc != null) {
            System.out.println("    [Hesap Özeti] No: " + acc.getAccountNumber() + " | İsim: " + acc.getOwnerName() + " | Güncel Bakiye: " + acc.getBalance() + " TL");
        } else {
            throw new AccountNotFoundException("Hesap bulunamadı: " + accountNumber);
        }
    }
}

// --- EXCEPTION YÖNETİMİ ---

// Temel iş kuralı hata sınıfımız. Diğer özel exception'lar bundan türeyecek.
class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}

// Örnek: Kullanıcı zaten var hatası
class UserAlreadyExistsException extends BusinessException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}

// Örnek: Geçersiz kimlik bilgileri hatası
class InvalidCredentialsException extends BusinessException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}

// Banka uygulamasına özel hatalar
class AccountNotFoundException extends BusinessException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}

class InsufficientBalanceException extends BusinessException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}

// --- DTO: STANDART HATA YANITLARI ---

// Standart Hata Cevabı Formatı
class ErrorResponse {
    private String title;
    private String type;
    private String message;

    public ErrorResponse(String title, String type, String message) {
        this.title = title;
        this.type = type;
        this.message = message;
    }

    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getMessage() { return message; }

    @Override
    public String toString() {
        return String.format("{title: '%s', type: '%s', message: '%s'}", title, type, message);
    }
}

// Doğrulama Hataları Formatı
class ValidationErrorResponse {
    private String argument;
    private List<String> messages;

    public ValidationErrorResponse(String argument, List<String> messages) {
        this.argument = argument;
        this.messages = messages;
    }

    public String getArgument() { return argument; }
    public List<String> getMessages() { return messages; }

    @Override
    public String toString() {
        return String.format("{argument: '%s', messages: %s}", argument, messages);
    }
}
