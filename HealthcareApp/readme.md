# skype-android-app-sdk- Healthcare app sample


Before you can run the Android samples in the [skype-android-app-sdk-samples](https://github.com/OfficeDev/skype-android-app-sdk-samples) repository, 
you need to have two things:

1. The **Skype for Business App SDK** libraries which you can download from [Skype for Business App SDK download](http://aka.ms/sfbappsdkdownload_android). 
2. The join meeting URL for an established **Skype for Business** meeting.

## Save a join meeting URL in the string resources XML file

This sample sample requires that you update the strings.xml file found in:  _[your local repository]\\HealthcareApp\app\src\main\res\values\strings.xml_. Replace the 
bracketed values in the following string with the join meeting URL of the meeting to join..

>Note: the URL resource for a Skype Online meeting is different than the URL for an on premise meeting. Be sure that you choose the URL that is appropriate for your topology.

* Skype server hosted on premise    ``` <string name="meeting_url">https://meet.[yourdomain]/[youralias]/[somevalue]</string> ``` 
* Skype Online server ``` <string name="meeting_url">https://meet.lync.com/[yourdomain]/[youralias]/[somevalue]</string> ```

  

>Note: The join meeting URL in the strings.xml file is only for use in development. If you use any code from the Healthcare App sample, you need to get 
a meeting URL at runtime. Read [Get a Skype for Business meeting URL](https://msdn.microsoft.com/en-us/skype/appsdk/getmeetingurl) to learn about getting a meeting URL in a production app.

## Copy the Skype for Business App SDK libraries into the project

The samples already include local references to the **Skype for Business** libraries. After you clone the repository, you need to copy the libraries into
the sample project folders at:  _[your local repository]\skype-android-app-sdk-samples\HealthcareApp\app\libs_. 

## Copy the ConversationHelper class into the project

The Healthcare app sample uses the ConversationHelper.java file that is found in the _Helpers_ folder in the App SDK download package. Copy the ConversationHelper.java file into the _[your local repository]\skype-android-app-sdk-samples\HealthcareApp\app\src\main\java\com\microsoft\office\sfb\healthcare_ folder.

## Replace the package statement in ConversationHelper.java
The default package statement for the ConversationHelper.java must be replaced with: ```package com.microsoft.office.sfb.healthcare;```

## Start or join the Skype for Business meeting
Use your Skype for Business client to join the Skype for Business meeting whose URL you are using in the sample strings.xml file. Once you have connected to the meeting, turn on your video camera so that a video stream is available to the sample. 

## Run the sample

Attach your Android phone to your development computer and verify that USB debugging is turned on. In Android Studio, click the Run button to build the .apk, install it on your device, and launch the application.

>Note: The **Skype for Business App SDK** does not support Android Studio emulators. You need to run the sample on a armeabi-v7a mobile device. See [Platform Considerations](https://msdn.microsoft.com/en-us/skype/appsdk/platformconsiderations) for 
more information.

## The running sample

The sign in screen of the sample has an entry field for a user Id and a password. You can leave these fields blank. Press the **Sign in or Register** button at the bottom of the sign in screen. The well baby report activity opens. 

### Making a video call

Press the **Call Doctor** button to start a video call to join the **Skype for Business** meeting that you've gotten a meeting URL for. When the sample has joined the meeting, you will see the video stream from the Skype for Business client that you joined from earlier. Outgoing video from your Android phone is shown in the lower right quadrant of the video fragment. 

## Copyright
Copyright (c) 2016 Microsoft. All rights reserved.

