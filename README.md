# intelligentDataSyncAndroidApp
Local to Live Server Data Sync Example
# A brief overview of your architectural choices
We have used Android Java for building this application and the application design have 2 screens, a splash screen and a dashboard screen having 2 textfield to get User inputs of Name, City name and a Command Button for Saving data into Local SqlLite shared preference. Below the button, there is list box for displaying saved data, and below the list box, there is one Command Button for Sync with Server, for pushing data to server once internet is turned on.

# Instructions on how to build and run the app
Just Clone git repo in your studio, after all files cloned, Just click on build, select device either simulator or debug devices, click on build. After building, graddle will be started to build, and studio will ask confirmation box for install to your physical device, otherwise, directly simulator will be opened and app icon will be visible on your phone device.

# A short explanation of how you handled the "Smart Sync" logic
As per our requirements, we have decided to use SQLlite as local memory, a web REST api for transfer data to Server. Now, User will give their own input as name, city name etc. and click on Save button, data will be saved in local memory, and will dispay in list box, Now, if user will turn on Internet of mobile device, than user have to click on Sync to Server button for sending local user input to server.

# Thank you.