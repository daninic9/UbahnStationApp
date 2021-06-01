[[_TOC_]]

#Ubahn Station Android App

## Introduction
This app fetches a list of Ubahn stations from a Graphql server and shows the user the first 10 (configurable in config.properties file). Once user scrolls down, it will load 10 more and so on each time it scrolls.
Scrolling up at the start of the list will cause the list to refresh (clear the list and re-fetch from server)

### Features
* Apollo client management
* Scroll management
* List management

## Software Info

### Requirements
Android Min SDK: 24
Android Target SDK: 30

### Components
#### Libraries used:
[Apollo client](https://www.apollographql.com/docs/android/): Library to send query requests to Graphql server and manage response
[Orhanobut Logger](https://github.com/orhanobut/logger): Library for custom logging
[Scalable Dp](https://github.com/intuit/sdp): Library to help make an app responsive.
[SwipeRefreshLayout](https://developer.android.com/jetpack/androidx/releases/swiperefreshlayout): Library to allow a comfortable swipe up or down on lists

#### Server
https://bahnql.herokuapp.com/graphql GraphQl server


