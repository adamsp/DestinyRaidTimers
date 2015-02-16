Destiny Raid Timers
============
This app has raid timers for the game [Destiny](http://www.destinythegame.com/ca/en).

Currently, it only has timers for the Crota fight of the Crotas End raid.

## Libraries

This app makes use of the following libraries:

- [Timber](https://github.com/JakeWharton/Timber) (for logging)
- [Crashlytics](http://fabric.io) (for crash reporting - only included in `debugCrashlytics` and `release` builds)
- [Otto](http://square.github.io/otto/) (for event publishing & handling)
- [Dagger](http://square.github.io/dagger/) (dependency injection)
- [ButterKnife](http://jakewharton.github.io/butterknife/) (for view and click handler injection)
- [CircularProgressDrawable](https://github.com/Sefford/CircularProgressDrawable) (currently used for displaying progress on the Crota fight)

## Build types

There are 3 build types; `debug`, `debugAnalytics` and `release`. The `debug` build should work for anyone - you can select it from the Build Variants window of Android Studio. The other two require a `crashlytics.properties` file included in the root of the app directory with an apiKey and apiSecret defined.

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