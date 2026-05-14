
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

# 1. Yağmur — User Interface and MainActivity

Yağmur is responsible for designing and implementing the visible part of the application. This part includes the main screen that the user interacts with.

## Main Responsibilities

- Create the main screen of the application.
- Design a clean and simple user interface.
- Display the current UV index.
- Show the UV risk level.
- Display SPF recommendation.
- Display reapplication time.
- Add notification on/off switch.
- Show offline mode status or warning.
- Connect UI elements with the application logic.

## Expected Output

Yağmur will deliver the main user interface where users can clearly see the UV information and recommendations.

The screen should include:

```text
SOLARIA

Current UV Index: 7.2
Risk Level: High
Recommended SPF: SPF 50
Reapply Every: 2 hours

Notifications: ON
Offline Mode: Not active
````

## Related Files

```text
MainActivity.java
activity_main.xml
```

## Branch Name

```text
ui-yagmur
```

---

# 2. Ayşe — API, Location, and Network Control

Ayşe is responsible for retrieving real UV index data by using the user’s location and an external API.

This part is important because SOLARIA needs UV index data to generate accurate sunscreen recommendations.

## Main Responsibilities

* Request location permission from the user.
* Get the user’s latitude and longitude from the device.
* Check whether internet connection is available.
* Send a request to a UV index API.
* Receive UV index data from the API.
* Parse the JSON response.
* Send the UV index value to the recommendation system.
* Handle possible API or internet connection errors.

## Expected Output

Ayşe will deliver the data retrieval part of the application.

When the user opens the app, the system should:

```text
Check internet connection
↓
Get user location
↓
Send API request
↓
Receive UV index value
↓
Send UV value to recommendation system
```

If internet connection is not available, the app should inform the offline/cache system.

## Related Files

```text
ApiService.java
LocationHelper.java
NetworkHelper.java
```

## Example Logic

```text
If internet is available:
    Get current location
    Send request to UV API
    Get UV index
    Return UV index to MainActivity

If internet is not available:
    Inform the app to use cached UV data
```

## Branch Name

```text
api-ayse
```

---

# 3. Helin — Recommendation, Notification, and Offline Cache

Helin is responsible for the decision-making, notification, and offline support parts of the application.

This part converts the UV index value into useful information for the user.

## Main Responsibilities

* Generate risk level according to the UV index.
* Generate SPF recommendation.
* Calculate reapplication interval.
* Create notification reminders.
* Manage notification on/off status.
* Save the last UV index value locally.
* Display the last saved UV data when internet is not available.
* Support offline mode with cached data.

## Expected Output

Helin will deliver the logic that gives sunscreen advice based on the UV index.

Example output:

```text
UV Index: 7
Risk Level: High
Recommended SPF: SPF 50
Reapply Every: 2 hours
```

## Recommendation Rules

```text
UV 0-2:
Risk Level: Low
Recommended SPF: SPF 15 or optional protection
Reapplication: No frequent reapplication needed

UV 3-5:
Risk Level: Moderate
Recommended SPF: SPF 30
Reapplication: Every 3 hours

UV 6-7:
Risk Level: High
Recommended SPF: SPF 50
Reapplication: Every 2 hours

UV 8+:
Risk Level: Very High / Extreme
Recommended SPF: SPF 50+
Reapplication: Every 1-2 hours
```

## Related Files

```text
RecommendationEngine.java
NotificationHelper.java
CacheManager.java
```

## Example Logic

```text
Receive UV index
↓
Determine risk level
↓
Generate SPF recommendation
↓
Calculate reapplication time
↓
Display recommendation to user
↓
Send notification if notifications are enabled
```

## Branch Name

```text
notification-helin
```

---

# Branch Plan

Each team member should work on their own branch to avoid conflicts.

```text
main
├── ui-yagmur
├── api-ayse
└── notification-helin
```

---

# Development Workflow

1. Each team member creates their own branch.
2. Everyone works on their assigned files.
3. After completing a task, the team member commits the changes.
4. The branch is pushed to GitHub.
5. A pull request is opened.
6. The team reviews the code.
7. If there is no problem, the branch is merged into `main`.

---

# Suggested Project Structure

```text
com.example.solaria

├── MainActivity.java
├── api
│   └── ApiService.java
├── location
│   └── LocationHelper.java
├── network
│   └── NetworkHelper.java
├── recommendation
│   └── RecommendationEngine.java
├── notification
│   └── NotificationHelper.java
├── storage
│   └── CacheManager.java
└── model
    └── UVData.java
```

---

# Minimum Prototype Goals

The first working version of SOLARIA should include:

* Display current UV index.
* Show UV risk level.
* Provide SPF recommendation.
* Show reapplication time.
* Support notification on/off option.
* Show offline mode warning.
* Save and display last UV data when offline.

---

# Notes

The first goal is not to build a perfect application.

The first goal is to build a clean and working prototype.

If the real API connection is not ready, the application can first be tested with a fake UV value such as:

```java
double uvIndex = 7.5;
```

After the UI and recommendation system work correctly, the real API can be connected.

````

Bunu repo içinde tek dosya olarak tutman yeterli. README’ye de şunu eklersen daha profesyonel görünür:

```md
## Project Documents

- [Task Distribution](docs/task-distribution.md)
````
