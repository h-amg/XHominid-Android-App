# ![alt text](logo_trans.png "logo") [XHominid](http://xhominid.com/) Android App
### **A platform for nutrition coaching and meal planning by expert nutritionist**
Check it out on [Google play](https://play.google.com/store/apps/details?id=com.main.android.activium&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1)

---


## Introduction

XHomind is a platform that helps people looking for nutrition coaching and meal planning get connected with a qualified professional nutritionist. The platform enables professional nutritionists to conduct nutrition assessment interviews through XHomind video call feature. After this assessment the user then gets access to weekly meal plans prepared by the nutritionist, they are then able to directly message the nutritionist for further questions via XHominid messaging feature. In order to maintain continuous progress the users are able to book weekly one to one coaching sessions with their nutritionist via the video call booking feature. The user is able to view meal information, recipes and cooking instructions as well as mark meals eaten throughout the day. The daily nutritional intake logs are uploaded instantly and made accessible to the user nutritionist so that the can advice and guide them when needed and make improvements to the upcoming weeks' plan.

---

## Components

### [Main Activity](app/src/main/java/com/main/android/activium/MainActivity.java)
 - Fetches the user's current macro nutrients intake status from DB.
 - Displays the remian allowed calories for the day.
 - Check if there are available planned meals for the day and displays them.
 - Displays and allows the browsing of planned meals recipes and cooking instructions.
 - Enables the user to mark meals as eaten to be logged and substracted from remaining calories.
 - Notifies the user when they have no planned meals available for the week.
 - Enables the user to book a consultations session with their nutritionsist when no meals are planned.
 - Requests information used to design user diet plan on intial launch of the app after sign up up
 - Verifies user subscription when the app is launched.
 - Verifies that the user is authenticated when the app is launched.

### [Meal Plans Activity](app/src/main/java/com/main/android/activium/MealPlansActivity.java)
- Displays and allows the browsing the previous days logged meals.
- Displays and allows the browsing of planned meals for upcoming days.

### [Shopping list Activity](app/src/main/java/com/main/android/activium/ShoppingListActivity.java)
- Displays and allows the browsing of weekly shopping list specified by the nutritionist each week.

### [Messages Activity](app/src/main/java/com/main/android/activium/MessagesActivity.java)
- Allows the user to message their nutritionists directly with their inquiries.

### [Consultation sessions Activity](app/src/main/java/com/main/android/activium/ConsultationActivity.java)
- Allows the user to view upcoming consultation sessions.
- Allows the user to sechedule next week's consulations sessions.
- Allow the user to view the status of whether their secheduled session is approved by the nutritionists.
- Allows the user to view declined sessions by the nutritionists.
- Allows the user to re-sechedule declined consultation sessions.
- Allows the user to enter consultations sessions and join the video call.

### [Settings Activity](app/src/main/java/com/main/android/activium/SettingsActivity.java)
- Allows the user to configutre the app notififcations
- Allows the user to signout from the app

### [Subscription Activity](app/src/main/java/com/main/android/activium/SubscriptionActivity.java)
- Allows the user to purchase their subscriptoin and process payment through Google Play billing.

### [Intro Activity](app/src/main/java/com/main/android/activium/IntroActivity.java)
- Walk the user through explanatory slides that highlights how the app works and how to use it.

### [Login Activity](app/src/main/java/com/main/android/activium/LoginActivity.java)
- Allows the user to loging with their email address.

### [Sign up Activity](app/src/main/java/com/main/android/activium/SignupActivity.java)
- Allows the user to signup with email, Google account or Facebook account.

### [Email confirmation Activity](app/src/main/java/com/main/android/activium/EmailConfirmActivity.java)
- Confirms and verifies the user email upon signing up.

### [Request Reset Password Activity](app/src/main/java/com/main/android/activium/reqPassResetActivity.java)
- Allows the user to request reseting their password.
- Send a password reset link to the user's registered account.

### [New Password Activity](app/src/main/java/com/main/android/activium/NewPassActivity.java)
- Launches when the user clikcks on the link in the password reset email.
- Allows the user to change their password.

### [Video Activity](app/src/main/java/com/main/android/activium/VideoActivity.java)
-  Displays the viedo feed from the nutrtionist when the user enters their scheduled consultation session.

### [Video Settings Activity](app/src/main/java/com/main/android/activium/VideoSettingsActivity.java) - inactive
- Allows the user to modify the video call settings

### [Diet Stats Activity](app/src/main/java/com/main/android/activium/DietStatsActivity.java) - inactive
- Displays statistical information about the user logged meals and caloric intake.

### [Meals Record Activity](app/src/main/java/com/main/android/activium/MealsRecordActivity.java) - inactive
- Displays the meals logged and their macro nutrients contents informations

## Stack

#### User authentication
- [MongoDB Stitch](https://www.mongodb.com/cloud/stitch) email, google and facebook user authentication and email verfication.

#### Data storage
- [MongoDB Atlas](https://www.mongodb.com/cloud/atlas) cloud hosted Non-relational distributed databas.

#### Push notifications
- [Firebase Cloud Messaging](https://firebase.google.com/products/cloud-messaging/) cross platform push notifications.

#### Auxiliary services
- [Firebase Cloud storage](https://firebase.google.com/products/storage/) for media files storage.
- [Twilio Video](https://www.twilio.com/video) for video chat.
- [Twilio Chat](https://www.twilio.com/chat) for instant messaging.
- [Google play billing](https://developer.android.com/google/play/billing/billing_overview) for handling app subscription payment processing.

#### [Back-end](https://github.com/h-amg/XHominid-website-and-backend)
- Flask/Python
- JavaScript
- [Twilio Video](https://www.twilio.com/video)
- [Twilio Chat](https://www.twilio.com/chat)
- [MongoDB Stitch](https://www.mongodb.com/cloud/stitch)
- [MongoDB Atlas](https://www.mongodb.com/cloud/atlas)
- [Firebase Cloud storage](https://firebase.google.com/products/storage/) 
- [Firebase Cloud Messaging](https://firebase.google.com/products/cloud-messaging/)
- [Google cloud App engine](https://cloud.google.com/appengine/)

## Screenshots
<img src=".\screenshots\sc_1.png" width="150">  <img src=".\screenshots\sc_2.png" width="150"> <img src=".\screenshots\sc_3.png" width="150">  <img src=".\screenshots\sc_4.png" width="150" height="265">  <img src=".\screenshots\sc_5.png" width="150">  <img src=".\screenshots\sc_6.png" width="150">
