# Google Play Data Safety Form Cheat-Sheet for Pro GPS Cam

Use this cheat-sheet when filling out the **Data Safety** form in the Google Play Console for **Pro GPS Cam** (`com.pankaj.progpscam.ly2`).

---

## Overview Summary Table

| Data Category | Data Type | Collected? | Shared? | Purpose | Optional? | Encrypted in Transit? |
| :--- | :--- | :---: | :---: | :--- | :---: | :---: |
| **Location** | Precise Location | **Yes** | **Yes** | App Functionality (Reverse geocoding & photo stamping) | No | **Yes** (HTTPS) |
| **Location** | Approximate Location | **Yes** | **Yes** | App Functionality & Advertising | No | **Yes** (HTTPS) |
| **Photos and Videos** | Photos | **Yes** | **No** | App Functionality (Saved locally on device) | No | N/A (On-Device) |
| **Photos and Videos** | Videos | **Yes** | **No** | App Functionality (Saved locally on device) | Optional | N/A (On-Device) |
| **Audio Files** | Voice or sound recordings | **Yes** | **No** | App Functionality (Video audio recording) | Optional | N/A (On-Device) |
| **Device or Other IDs**| Device or other IDs (Advertising ID) | **Yes** | **Yes** | Advertising / Marketing, Analytics | Optional | **Yes** (HTTPS) |

---

## Detailed Play Console Form Responses

### 1. Location
* **Precise Location**
  * **Collected?** Yes
  * **Shared?** Yes (Transmitted to OpenStreetMap Nominatim for address reverse-geocoding)
  * **Purpose:** App Functionality (stamping GPS coordinates and address onto survey photos and videos)
  * **Is this data required or optional?** Required for geotagging features (can enter manual coordinates if denied)
  * **Is data encrypted in transit?** Yes (HTTPS)

* **Approximate Location**
  * **Collected?** Yes
  * **Shared?** Yes (Transmitted to IP-API for fallback IP-based location)
  * **Purpose:** App Functionality, Advertising / Marketing

---

### 2. Photos and Videos
* **Photos**
  * **Collected?** Yes (Captured via camera and stored on user device)
  * **Shared?** No (Photos remain 100% on user device unless shared manually by the user)
  * **Purpose:** App Functionality

* **Videos**
  * **Collected?** Yes (Recorded via camera and stored on user device)
  * **Shared?** No (Videos remain 100% on user device)
  * **Purpose:** App Functionality

---

### 3. Audio Files
* **Voice or sound recordings**
  * **Collected?** Yes (Audio recorded during video survey capture)
  * **Shared?** No (Audio remains embedded in video file on user device)
  * **Purpose:** App Functionality
  * **Is this data required or optional?** Optional (Requires microphone permission)

---

### 4. Device or Other IDs
* **Device or other IDs (Advertising ID)**
  * **Collected?** Yes (Collected by Google AdMob SDK)
  * **Shared?** Yes (Shared with Google AdMob for ad serving)
  * **Purpose:** Advertising / Marketing, Analytics
  * **Is this data required or optional?** Optional (Managed via UMP consent prompt)
  * **Is data encrypted in transit?** Yes (HTTPS)

---

## Security Practices Declarations in Play Console
* **Data Encryption:** Yes, all data in transit is encrypted using HTTPS (TLS/SSL).
* **Data Deletion Mechanism:** Yes, users can delete all photos and videos directly from their device storage.
