# 💎 Kristal - Gelişmiş Özel Para Birimi Sistemi

Kristal, Minecraft sunucunuz için optimize edilmiş, yüksek performanslı ve modern bir özel para birimi (economy) sistemidir. SQLite veritabanı altyapısı ve PlaceholderAPI desteği ile sunucunuza profesyonel bir ekonomik boyut kazandırır.

## 🚀 Öne Çıkan Özellikler

*   **Yüksek Performans:** SQLite tabanlı veritabanı yapısı sayesinde veriler güvenle saklanır ve hızlıca işlenir.
*   **PlaceholderAPI Desteği:** Bakiyeleri, sıralamaları ve formatlanmış değerleri skor tablolarında (scoreboard) veya menülerde kolayca gösterin.
*   **Akıllı Formatlama:** Büyük sayıları (1.000 -> 1k, 1.000.000 -> 1m) şeklinde otomatik formatlar.
*   **Sıralama Sistemi:** Sunucunuzdaki en zengin kristal sahiplerini anlık olarak listeleyin.
*   **Tamamen Özelleştirilebilir:** Tüm mesajlar, prefixler ve sayı formatları `config.yml` üzerinden değiştirilebilir.

## 📊 Placeholder Listesi

| Placeholder | Açıklama |
| :--- | :--- |
| `%kristal_balance%` | Saf kristal miktarını gösterir. |
| `%kristal_balance_formatted%` | Formatlanmış bakiye (örn: 15.5k). |
| `%kristal_vault_<sıra>_name%` | Sıralamadaki oyuncunun adı. |
| `%kristal_vault_<sıra>_balance%` | Sıralamadaki oyuncunun bakiyesi. |

## 🛠️ Komutlar ve Yetkiler

| Komut | Açıklama | Yetki |
| :--- | :--- | :--- |
| `/kristal view [oyuncu]` | Bakiyeyi görüntüler. | `kristal.use` |
| `/kristal give <oyuncu> <miktar>` | Oyuncuya kristal verir. | `kristal.give` |
| `/kristal take <oyuncu> <miktar>` | Oyuncudan kristal alır. | `kristal.take` |
| `/kristal set <oyuncu> <miktar>` | Bakiyeyi ayarlar. | `kristal.set` |
| `/kristal reset <oyuncu>` | Bakiyeyi sıfırlar. | `kristal.reset` |
| `/kristal reload` | Konfigürasyonu yeniler. | `kristal.admin` |

## 📦 Kurulum

1. `Kristal.jar` dosyasını `plugins` klasörüne kopyalayın.
2. Sunucunuzu başlatın ve `config.yml` dosyasını düzenleyin.
3. PlaceholderAPI kullanıyorsanız `/papi reload` komutunu çalıştırmayı unutmayın.

---
*Kristal ile sunucunuzun ekonomisini bir üst seviyeye taşıyın.*
