Destiny Raid Timers
============

<a href="https://play.google.com/store/apps/details?id=nz.net.speakman.destinyraidtimers">
  <img alt="Get it on Google Play"
       src="https://developer.android.com/images/brand/en_generic_rgb_wo_60.png" />
</a>

Raid timers for [Destiny](http://www.destinythegame.com/ca/en). Currently, the app only has timers for the Crota fight of the Crotas End raid. Timers and guides for more fights are expected to arrive soon. Need to do something while we wait for House of Wolves, right?

Release names are drawn from the list of Destiny related terms on the [Destiny Wikia site](http://destiny.wikia.com/wiki/Special:AllPages).

The only permission required currently is the Internet permission, used for reporting crashes to Crashlytics. Not that I expect there to be any crashes.

## Libraries

This app makes use of the following libraries:

- [Timber](https://github.com/JakeWharton/Timber) (for logging)
- [Crashlytics](http://fabric.io) (for crash reporting - only included in `debugCrashlytics` and `release` builds)
- [Otto](http://square.github.io/otto/) (for event publishing & handling)
- [Dagger](http://square.github.io/dagger/) (dependency injection)
- [ButterKnife](http://jakewharton.github.io/butterknife/) (for view and click handler injection)
- [CircularProgressDrawable](https://github.com/Sefford/CircularProgressDrawable) (currently used for displaying progress on the Crota fight)
- [android-floating-action-button](https://github.com/futuresimple/android-floating-action-button) (for the timer reset button)

## Build types

There are 3 build types; `debug`, `debugAnalytics` and `release`. The `debug` build should work for anyone - you can select it from the Build Variants window of Android Studio. The other two require a `crashlytics.properties` file included in the root of the app directory with an apiKey and apiSecret defined. If you really want to build a release build you'll need your own keystore, obviously.

## License

    Copyright 2015 Adam Speakman

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
