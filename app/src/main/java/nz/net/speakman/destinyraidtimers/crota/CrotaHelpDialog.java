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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import nz.net.speakman.destinyraidtimers.LicensesFragment;
import nz.net.speakman.destinyraidtimers.R;

/**
 * Created by Adam on 15-02-22.
 */
public class CrotaHelpDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.crota_help, null);
        TextView description = (TextView) view.findViewById(R.id.app_about_description);
        description.setMovementMethod(LinkMovementMethod.getInstance());
        view.findViewById(R.id.app_about_licenses).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LicensesFragment.displayLicensesFragment(getFragmentManager());
            }
        });
        builder.setView(view)
                .setTitle(R.string.crota_help_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }
}
