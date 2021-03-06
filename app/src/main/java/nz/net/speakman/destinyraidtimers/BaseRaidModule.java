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

package nz.net.speakman.destinyraidtimers;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import nz.net.speakman.destinyraidtimers.consumables.ConsumablesModule;
import nz.net.speakman.destinyraidtimers.crota.CrotaModule;

/**
 * Created by Adam on 15-02-15.
 */
@Module(
        complete = false,
        injects = {
                MainActivity.class,
                RaidApplication.class,
                NotifyService.class
        },
        includes = {
                CrotaModule.class,
                ConsumablesModule.class
        }
)
public class BaseRaidModule {
    @Singleton
    @Provides
    Bus providesEventBus() {
        return new Bus();
    }

    @Singleton
    @Provides
    Preferences providePreferences() {
        return new Preferences(RaidApplication.getApplication());
    }
}
