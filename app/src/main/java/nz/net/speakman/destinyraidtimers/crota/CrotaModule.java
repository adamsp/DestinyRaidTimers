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

package nz.net.speakman.destinyraidtimers.crota;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import nz.net.speakman.destinyraidtimers.crota.views.CrotaMovementCountdownView;
import nz.net.speakman.destinyraidtimers.crota.views.CrotaPositionView;

/**
 * Created by Adam on 15-02-15.
 */
@Module(
        complete = false,
        injects = {
                CrotaActivity.class,
                CrotaPositionView.class,
                CrotaMovementCountdownView.class
        }
)
public class CrotaModule {
    @Singleton
    @Provides
    CrotaMovementTimer provideCrotaMovementTimer(Bus bus) {
        return new CrotaMovementTimer(bus);
    }

    @Singleton
    @Provides
    CrotaEnrageTimer provideCrotaEnrgageTimer(Bus bus) {
        return new CrotaEnrageTimer(bus);
    }
}
