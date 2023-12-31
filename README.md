# Market Notifier (Android)
A Market Notifier for selecting, storing and reading individual RSS feeds. The current implemntation supports Kijiji feeds only but could potentially support any RSS feed due to the generic implentation of the reader. 

# Examples 
- Main Feed Activity 


<img width="424" alt="Screen Shot 2022-04-04 at 7 09 42 PM" src="https://user-images.githubusercontent.com/93284303/161640395-69df2c2a-2fa2-4c3a-a29b-16c72ef212a8.png">




- Feed Items List 


![Screen Shot 2022-04-02 at 4 22 31 PM](https://user-images.githubusercontent.com/93284303/161398184-387f4913-4761-4038-bc26-2a29b08f2c2f.png)


# Features 
- Support for multiple Kijiji Feeds to be stored and viewed individually.
- Additional feeds can be added using our custom webview with quick add support.  
- Feeds can be removed with a swipe gesture.  
- Indicator displays if a feed item has been viewed or not. Viewing is logged for each feed individually.  
- Indicators can be globally added/removed for all items within a feed using the action bar dropdown.  
- Feed items for each feed are grouped and contain relative distance, cost, representative image, and description from the unique Kijiji feed item metadata.  
- Selecting a feed item launches an installed web view application taking the user to the Kijiji item page that allows viewing of different information and purchase of the item.
- Feeds are automatically scanned, and notifications are sent to the user regarding the latest items that are added to each unique feed.  
- Feed images are downloaded on a background thread to ensure smooth user interaction and reduce wait times while loading feed information.  

# How To Use
The application was developed in android studio and Java. To use or contribute to the application clone this repository and open the project in Android Studio. 

# Contribution 
If you would like to contribute, report bugs or request features please contact 12345@unb.ca. 

# Supported Android APIs
- API 31 Android 11  
- API 30 Android 10  
- API 28 Android 8 

# Roadmap 
- Add additioanl RSS parsers outside of the current Kijiji only implementation.  
- Add prefecting to reduce power consumption and data usage. 
