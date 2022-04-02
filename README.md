# RSS Feed Reader (Android)
A RSS feed reader for selecting, storing and reading individual feeds. The current implemntation supports Kijiji feeds only but could potentially support any RSS feed due to the generic implentation of the reader. 

# Examples 
- Main Feed Activity 
![Screen Shot 2022-04-02 at 2 42 51 PM](https://user-images.githubusercontent.com/93284303/161394853-d3b56d7c-a563-4c37-98f8-3e092bf87111.png)

- Feed Items List 
<img width="487" alt="Screen Shot 2022-03-31 at 11 02 17 PM" src="https://user-images.githubusercontent.com/93284303/161394809-88ec5716-1926-40f8-91ef-01d6f3ac49ac.png">

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
