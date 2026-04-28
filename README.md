# Turkcell GYGY 5.0 - Banking Application Simulation

Bu proje, **Turkcell Geleceği Yazanlar** programı kapsamında bir ödev olarak hazırlanmıştır.

## Proje Hakkında

Bu uygulama, temel bankacılık işlemlerini (hesap oluşturma, para yatırma, para çekme, havale/EFT) bellek üzerinde (in-memory) simüle eden bir Java projesidir. 

Projenin ana odak noktalarından biri, ileride daha büyük kütüphane veya API projelerine entegre edilebilir şekilde **Hata Yönetimi (Exception Handling)** mekanizmalarını doğru kurgulamaktır. 

### Özellikler

- **Hesap Yönetimi**: Yeni banka hesabı oluşturma ve bakiye durumu görüntüleme.
- **Finansal İşlemler**: 
  - Para Yatırma
  - Para Çekme
  - Hesaptan hesaba para transferi
- **Gelişmiş Hata Yönetimi (Ödev Gereksinimi)**: 
  - Standart `RuntimeException` yerine iş kurallarına özel tasarlanmış Exception sınıfları (`BusinessException`, `AccountNotFoundException`, `InsufficientBalanceException`, `UserAlreadyExistsException`, `InvalidCredentialsException`).
  - Hataları modern sistemlerde (örneğin REST API'lerde) olduğu gibi standart bir formatta döndürmek için Response DTO yapıları (`ErrorResponse`, `ValidationErrorResponse`).

## Kurulum ve Çalıştırma

Proje yapısı standart bir Java uygulamasıdır. IntelliJ IDEA veya Eclipse gibi bir IDE ile açıp, `src/main/java/BankingApplication.java` içerisindeki `main` metodunu çalıştırarak işlem adımlarını ve simüle edilmiş hata senaryolarını konsol üzerinden görüntüleyebilirsiniz.
