/*
 * Copyright 2015 Adam Speakman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nz.net.speakman.destinyraidtimers.consumables;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import nz.net.speakman.destinyraidtimers.consumables.views.Consumables10CountdownView;
import nz.net.speakman.destinyraidtimers.consumables.views.Consumables30CountdownView;

/**
 * Created by Adam on 15-03-28.
 */
@Module(
        complete = false,
        injects = {
                ConsumablesActivity.class,
                Consumables10CountdownView.class,
                Consumables30CountdownView.class
        }
)
public class ConsumablesModule {
    @Singleton
    @Provides
    Consumables10Timer provideConsumables10Timer(Bus bus) {
        return new Consumables10Timer(bus);
    }

    @Singleton
    @Provides
    Consumables30Timer provideConsumables30Timer(Bus bus) {
        return new Consumables30Timer(bus);
    }
}
