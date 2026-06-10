package com.example.solaria;

public class RecommendationEngine {




        public static String getRiskLevel(double uvIndex) {
            if (uvIndex < 3) return "Low";
            else if (uvIndex < 6) return "Moderate";
            else if (uvIndex < 8) return "High";
            else if (uvIndex < 11) return "Very High";
            else return "Extreme";
        }

        public static String getMessage(double uvIndex) {
            if (uvIndex < 3) return "UV riski düşük. Ekstra koruma gerekmez.";
            else if (uvIndex < 6) return "Orta UV riski. Güneş kremi sür.";
            else if (uvIndex < 8) return "Yüksek UV riski. Güneş kremi ve şapka kullan.";
            else if (uvIndex < 11) return "Çok yüksek UV riski. Mümkünse gölgede kal.";
            else return "Aşırı UV riski. Dışarı çıkmaktan kaçın!";
        }

        public static String getReapplyRule(double uvIndex) {
            if (uvIndex < 3) return "Tekrar uygulama gerekmez.";
            else if (uvIndex < 6) return "Her 3-4 saatte bir tekrar uygula.";
            else if (uvIndex < 9) return "Her 2 saatte bir tekrar uygula.";
            else return "Koruma zamanlayıcısını başlat ve ekstra önlem al.";
        }

        // Bildirim gönderilmeli mi?
        public static boolean shouldNotify(double uvIndex) {
            return uvIndex >= 3;
        }

        // UV'ye göre bildirim aralığı (dakika cinsinden)
        public static long getNotificationIntervalMinutes(double uvIndex) {
            if (uvIndex < 3) return -1;       // Bildirim yok
            else if (uvIndex < 6) return 210; // 3.5 saat (3-4 arası ortalama)
            else if (uvIndex < 9) return 120; // 2 saat
            else return 60;                   // 1 saat (çok yüksek)
        }
    }

