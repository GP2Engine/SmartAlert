# SmartAlert #

SmartAlert is an Android application designed to be helpful and provide alerts to dangerous situations, such as User Falling Down, Earthquakes and Fire Reports.

* # Fall Detection #
In the main menu and when the device isn't charging, SmartAlert can detect possible falls by using data from the device's accelerometer. If it detects a fall then it gets in a
confirmation mode and keeps reading the accelerometer's values for an extra half a minute. If it detects further normal movement then it cancels the alert, otherwise if it confirms
a possible fall (for example if the devices stays still could indicate that user is unconcious) then it shows a warning message, rings an alarm (ringtone set by the user), plays TTS and
gives him a reaction time (also set by the user). If he doesn't still abort the alarm, then an SMS message with a link to his location and event's timestamp will be sent to a
recipient contact (already set by the user again). All kind of events are getting logged both in SQLite and Firebase.

* # Earthquake Detection #
If the device is in the main menu and charging, then it constantly scans for possible earthquake vibrations using the device's accelerometer. If it detects an earthquake vibration,
then it logs the event both in SQLite and Firebase and discretly informs the user. Everytime our device detects a possible earthquake vibration, then it looks 30 seconds earlier
or 30 seconds after the detection for possible earthquakes recorded by other devices within a 500km radius of the user. If it does, then it rings an alarm (also sound set by the user),
plays a TTS and informs the user. All events of course are logged both in SQLite and Firebase.

* # Fire Report #
If a user observes a fire then he can quickly report it by using the Fire Report function of this app. He can upload a picture of the fire that is imported either from the phone's
Gallery or from a straight Camera snapshot. If done, then an SMS is sent (to a preset contact) and the event is logged both in SQLite and Firebase and the picture is also uploaded
to the Firebase Storage.

* # View All Logs #
The user can view any kind of events logged (fall detections, fire reports, earthquake detections) at any time by selecting the appropriate buttons from the main menu. All logs
are available to see by their timestamp and after we tap on each timestamp we can see in depth details about each log.

* # Languages #
The whole app's UI (excluding only the TTS language version for compatibility purposes) is offered in 3 languages: English, Greek and Dutch. Dutch translation could be improved.

* # Settings #
Many parts of the app are configurable such as: the alert ringtone sounds, the SMS alert recipients, the user's available response time in case of all and the app's language.

* # Quality Control #
Most aspects of the app are materialized in a way that they offer good usability and reliability by implementing multiple checks in vital areas. For example, since the GPS location
fix is very important in this app, we are using a broader strategy for acquiring a more reliable GPS fix by using both GPS and Network and in case both fail after a predetermined
timeout, then seek of the device's last location.

* # To be done #
- Various optimizations in the code and small reduction on the app's size.

- Better authentication system.

- Direct connection to the firebase also for the logs.

More details to follow.

