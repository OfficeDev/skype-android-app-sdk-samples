# skype-android-app-sdk-samples

Before you can run the Android samples in the [skype-android-app-sdk-samples](https://github.com/OfficeDev/skype-android-app-sdk-samples) repository, 
you need to have two things:

1. The **Skype for Business App SDK** libraries which you can download from [](). 
2. The join meeting URL for an established **Skype for Business** meeting.


## Healthcare App Sample
### Update the string resource file

The [Healthcare app](https://github.com/OfficeDev/skype-android-app-sdk-samples/tree/master/HealthcareApp) sample requires
that you update the strings.xml file found in:  _[your local repository]\\HealthcareApp\app\src\main\res\values\strings.xml_. Replace the 
bracketed values in the following string.    ``` <string name="meeting_url">https://meet.lync.com/[yourdomain]/[youralias]/[somevalue]</string> ``` with 
the join meeting URL of the meeting to join. 

>Note: The join meeting URL in the strings.xml file is only for use in development. If you use any code from the Healthcare App sample, you need to get 
a meeting URL at runtime. Read [Get a Skype for Business meeting URL](GetMeetingURL.md) to learn about getting a meeting URL in a production app.

### Copy the Skype for Business App SDK libraries into the project

The samples already include local references to the **Skype for Business** libraries. After you clone the repository, you need to copy the libraries into
the sample project folders at: _[your local repository]\HealthcareApp\app\libs_. 

### Copy the ConversationHelper class into the project

The Healthcare app sample uses the ConversationHelper.java file that is found in the _Helpers_ folder in the App SDK download package.

### Run the sample

The **Skype for Business App SDK** does not support Android Studio emulators. You need to run the sample on a armeabi-v7a mobile device. See [Platform Considerations](PlatformConsiderations.md) for 
more information.

## GuestMeetingJoin sample
the [Guest Meeting Join sample](https://github.com/OfficeDev/skype-android-app-sdk-samples/tree/master/GuestMeetingJoin) uses the core **Skype for Business App SDK** API to
join meetings, chat, and share video. It prompts you for a join meeting URL at runtime. The sample has local references to the SDK libraries that you download to 
your development computer. 

## Copyright
Copyright (c) 2016 Microsoft. All rights reserved.
