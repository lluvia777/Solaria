# SOLARIA Task Distribution

This document explains the task distribution for the SOLARIA mobile application project.

SOLARIA is an Android-based mobile application that retrieves UV index data, provides sunscreen and reapplication recommendations, sends notifications, and supports offline cached data display.

---

## Team Members and Responsibilities

| Team Member | Main Responsibility | Related Files / Modules |
|---|---|---|
| Yağmur | User Interface and MainActivity | MainActivity.java, activity_main.xml |
| Ayşe | API, Location, and Network Control | ApiService.java, LocationHelper.java, NetworkHelper.java |
| Helin | Recommendation, Notification, and Offline Cache | RecommendationEngine.java, NotificationHelper.java, CacheManager.java |

---

## 1. Yağmur — User Interface and MainActivity

Yağmur is responsible for designing and implementing the visible part of the application.

### Main Responsibilities

- Create the main screen of the application.
- Display the current UV index.
- Show the risk level based on the UV value.
- Display SPF recommendation.
- Display reapplication time.
- Add notification on/off switch.
- Show offline mode warning when internet is not available.
- Connect UI elements with the application logic.

### Expected Output

Yağmur will deliver the main user interface where users can clearly see:

- Current UV Index
- Risk Level
- Recommended SPF
- Reapplication Time
- Notification Status
- Offline Mode Status

### Related Files

```text
MainActivity.java
activity_main.xml
