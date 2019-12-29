# ![alt text](logo_trans.png "logo") [XHominid](http://xhominid.com/) Android App
### **A platform for nutrition coaching and meal planning by expert nutritionist**
Check it out on [Google play](https://play.google.com/store/apps/details?id=com.main.android.activium&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1)

---


## Introduction

XHomind is a platform that helps people looking for nutrition coaching and meal planning get connected with a qualified professional nutritionist. The platform enables professional nutritionists to conduct nutrition assessment interviews through XHomind video call feature. After this assessment the user then gets access to weekly meal plans prepared by the nutritionist, they are then able to directly message the nutritionist for further questions via XHominid messaging feature. In order to maintain continuous progress the users are able to book weekly one to one coaching sessions with their nutritionist via the video call booking feature. The user is able to view meal information, recipes and cooking instructions as well as mark meals eaten throughout the day. The daily nutritional intake logs are uploaded instantly and made accessible to the user nutritionist so that the can advice and guide them when needed and make improvements to the upcoming weeks' plan.

---

## Components

### [Main Activity](app/src/main/java/com/main/android/activium/MainActivity.java)

### [Meal Plans Activity](app/src/main/java/com/main/android/activium/MealPlansActivity.java)

### [Shopping list Activity](app/src/main/java/com/main/android/activium/ShoppingListActivity.java)

### [Messages Activity](app/src/main/java/com/main/android/activium/MessagesActivity.java)

### [Consultation sessions Activity](app/src/main/java/com/main/android/activium/ConsultationActivity.java)

### [Settings Activity](app/src/main/java/com/main/android/activium/SettingsActivity.java)

### [Subscription Activity](app/src/main/java/com/main/android/activium/SubscriptionActivity.java)

### [Intro Activity](app/src/main/java/com/main/android/activium/IntroActivity.java)

### [Login Activity](app/src/main/java/com/main/android/activium/LoginActivity.java)

### [Sign up Activity](app/src/main/java/com/main/android/activium/SignupActivity.java)

### [Email confirmation Activity](app/src/main/java/com/main/android/activium/EmailConfirmActivity.java)

### [Meals Record Activity](app/src/main/java/com/main/android/activium/MealsRecordActivity.java)

### [Reset Password request Activity](app/src/main/java/com/main/android/activium/)

### [Send Password Reset Email Activity](app/src/main/java/com/main/android/activium/)

### [Video Activity](app/src/main/java/com/main/android/activium/VideoActivity.java)

### [Video Settings Activity](app/src/main/java/com/main/android/activium/VideoSettingsActivity.java) - inactive

### [Diet Stats Activity](app/src/main/java/com/main/android/activium/DietStatsActivity.java) - inactive

## Stack
- [Back-end](https://github.com/h-amg/XHominid-website-and-backend): Built with Flask and hosted on GCP App engine
- User authentication: Built with [MongoDB Stitch](https://www.mongodb.com/cloud/stitch) email, google and facebook user authentication and email verfication.
- Data storage: Uses [MongoDB Atlas](https://www.mongodb.com/cloud/atlas) cloud hosted Non-relational distributed databas.
- Push notifications: Built with [Firebase Cloud messaging](https://firebase.google.com/products/cloud-messaging/) 
- Auxiliary services: [Firebase Cloud storage](https://firebase.google.com/products/storage/) for media files storage, [Twilio Video](https://www.twilio.com/video) for video chat, [Twilio Chat](https://www.twilio.com/chat) for instant messaging, [Google play billing](https://developer.android.com/google/play/billing/billing_overview) for handling app subscription payment processing.

## Screenshots
<img src=".\screenshots\sc_1.png" width="150">  <img src=".\screenshots\sc_2.png" width="150"> <img src=".\screenshots\sc_3.png" width="150">  <img src=".\screenshots\sc_4.png" width="150" height="265">  <img src=".\screenshots\sc_5.png" width="150">  <img src=".\screenshots\sc_6.png" width="150">


## Credits