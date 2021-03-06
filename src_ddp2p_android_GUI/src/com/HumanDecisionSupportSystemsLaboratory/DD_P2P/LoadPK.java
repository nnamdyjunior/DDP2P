/* Copyright (C) 2014,2015 Authors: Hang Dong <hdong2012@my.fit.edu>, Marius Silaghi <silaghi@fit.edu>
Florida Tech, Human Decision Support Systems Laboratory
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation; either the current version of the License, or
(at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.
You should have received a copy of the GNU Affero General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA. */
/* ------------------------------------------------------------------------- */

package com.HumanDecisionSupportSystemsLaboratory.DD_P2P;

import java.util.regex.Pattern;

import net.ddp2p.common.config.DD;

import net.ddp2p.common.util.DD_Address;
import net.ddp2p.common.util.DD_SK;
import net.ddp2p.common.util.P2PDDSQLException;
import net.ddp2p.common.util.StegoStructure;
import net.ddp2p.common.util.Util;
import net.ddp2p.ASN1.Decoder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoadPK extends DialogFragment {

	protected static final boolean _DEBUG = true;
	protected static final boolean DEBUG = false;
	protected static final String TAG = null;
	private Button load;
	private EditText address;
	private String strAddress;
	Activity activity;
	StegoStructure message;

	interface  LoadPKListener {
		void getPKResult(StegoStructure parameter);
	}
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		//activity.onDismiss();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.dialog_load_pk,
				container);

		
		load = (Button) view.findViewById(R.id.dialog_load_pk_load);
		load.setText(Util.__("Import"));
		
		address = (EditText) view.findViewById(R.id.dialog_load_pk_editText);
		
		getDialog().setTitle("Import from Text");


		load.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				strAddress = address.getText().toString();
				
				//Interpret
				String body = DD.extractMessage(strAddress); //extractMessage(strAddress);
				
				if (body == null) {
					if (_DEBUG) Log.d(TAG, "LoadPK: Extraction of body failed");
					Toast.makeText(getActivity(), "Separators not found: \""+DD.SAFE_TEXT_MY_HEADER_SEP+DD.SAFE_TEXT_ANDROID_SUBJECT_SEP+DD.SAFE_TEXT_MY_BODY_SEP+"\"", Toast.LENGTH_SHORT).show();
			        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
			        ft.detach(LoadPK.this);
			        ft.commit();
					return;
				}

                net.ddp2p.common.util.StegoStructure imported_object = DD.interpreteASN1B64Object(body); //interprete(body);
				
				if (imported_object == null) {
					if (_DEBUG) Log.d(TAG, "LoadPK: Decoding failed");
					Toast.makeText(getActivity(), "Failed to decode", Toast.LENGTH_SHORT).show();
			        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
			        ft.detach(LoadPK.this);
			        ft.commit();
					return;
				}
				
				String interpretation = imported_object.getNiceDescription();
				//ask confirm
                address.setText(interpretation);
                

                AlertDialog.Builder confirm = new AlertDialog.Builder(getActivity());
                confirm.setTitle(getResources().getString(R.string.LoadQuestion));
                confirm.setMessage(interpretation).setIcon(android.R.drawable.ic_input_add)
                    //.setCancelable(false)
				    .setPositiveButton(getResources().getString(R.string.Yes), new MyDialog_OnClickListener(imported_object) {
						public void _onClick(DialogInterface dialog, int id) {
							Log.d("PK", "LoadPK: Trying to save");
							StegoStructure imported_object = (StegoStructure) ctx;
							LoadPK.this.message = imported_object;
							if (message != null)
								((Main) activity).getPKResult(message);
							try {
								//imported_object.saveSync(); //.save();
								//Toast.makeText(getActivity(), getResources().getString(R.string.SaveSuccess), Toast.LENGTH_SHORT).show();
							} catch (Exception e) {
								e.printStackTrace();
								Log.d("PK", "LoadPK: Failed to save: " + e.getLocalizedMessage());
							}

							FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
							ft.detach(LoadPK.this);
							ft.commit();
							dialog.cancel();
						}
					})
				    .setNegativeButton(getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
							ft.detach(LoadPK.this);
							ft.commit();
							dialog.cancel();
						}
					});
                
                AlertDialog confirmDialog = confirm.create();
                confirmDialog.show();
			}

/*
			private String extractMessage(String strAddress) {
				//boolean DEBUG = true;
				String addressASN1B64;
				try {
					if (strAddress == null) {
						if (DEBUG) Log.d(TAG, "LoadPK: Address = null");
				        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
				        ft.detach(LoadPK.this);
				        ft.commit();
						return null;
					}
					//strAddress = strAddress.trim();
					if (DEBUG) Log.d(TAG, "LoadPK: Address="+strAddress);
					
					String[] __chunks = strAddress.split(Pattern.quote(DD.SAFE_TEXT_MY_BODY_SEP));
					if (__chunks.length == 0 || __chunks[__chunks.length - 1] == null) {
						if (DEBUG) Log.d(TAG, "LoadPK: My top Body chunk = null");
				        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
				        ft.detach(LoadPK.this);
				        ft.commit();
						return null;
					}
					if (__chunks.length > 1) {
						addressASN1B64 = __chunks[__chunks.length - 1];
						addressASN1B64 = addressASN1B64.trim();
						if (DEBUG) Log.d(TAG, "LoadPK: got Body=" + addressASN1B64);
						addressASN1B64 = Util.B64Join(addressASN1B64);
						if (DEBUG) Log.d(TAG, "LoadPK: got Body=" + addressASN1B64);
						return addressASN1B64;
					}
					
					String[] chunks = strAddress.split(Pattern.quote(DD.SAFE_TEXT_MY_HEADER_SEP));
					if (chunks.length == 0 || chunks[chunks.length - 1] == null) {
						if (DEBUG) Log.d(TAG, "LoadPK: My Body chunk = null");
				        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
				        ft.detach(LoadPK.this);
				        ft.commit();
						return null;
					}
					
					String body = chunks[chunks.length - 1];
					if (DEBUG) Log.d(TAG, "LoadPK: Body="+body);
					
					String[] _chunks = strAddress.split(Pattern.quote(DD.SAFE_TEXT_ANDROID_SUBJECT_SEP));
					if (_chunks.length == 0 || _chunks[_chunks.length - 1] == null) {
						if (DEBUG) Log.d(TAG, "LoadPK: Android Body chunk = null");
				        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
				        ft.detach(LoadPK.this);
				        ft.commit();
						return null;
					}
	
					addressASN1B64 = _chunks[_chunks.length - 1];
					addressASN1B64 = addressASN1B64.trim();
					if (DEBUG) Log.d(TAG, "LoadPK: Body=" + addressASN1B64);
					addressASN1B64 = Util.B64Join(addressASN1B64);
					if (DEBUG) Log.d(TAG, "LoadPK: Body=" + addressASN1B64);
					return addressASN1B64;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
//			public  StegoStructure getStegoStructure(BigInteger ASN1TAG) {
//				Log.d("Import", "BN = "+ ASN1TAG);
//				for (StegoStructure ss : DD.getAvailableStegoStructureInstances()) {
//					Log.d("Import", "Available = "+ ss+" ID="+""+ss.getSignShort());
//					if (ASN1TAG.equals (new BigInteger(""+ss.getSignShort()))) {
//						try {
//							Log.d("Import", "Match");
//							return (StegoStructure) ss.getClass().newInstance();
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//					}
//				}
//				return null;
//			}
*/

			private net.ddp2p.common.util.StegoStructure interprete(String addressASN1B64) {
				byte[] msg = null;
				StegoStructure ss = null;
				try {
					Log.d("Import", addressASN1B64);
					msg = Util.byteSignatureFromString(addressASN1B64);
					
					Decoder dec = new Decoder(msg);
					
//					StegoStructure s2s = getStegoStructure(dec.getTagValueBN());
					
					ss = DD.getStegoStructure(dec);
					if (ss == null) {
						Log.d("Import", "LoadPK. Use default stego");
						ss = new DD_Address();
					}
					//DD_Address da =
					ss.setBytes(msg);
					return ss;
				} catch (Exception e) {
					e.printStackTrace();
					DD_SK dsk = new DD_SK();
					Log.d("Import", "LoadPK. Try SK");
				
					try {
						dsk.setBytes(msg);
						Log.d("Import", "LoadPK. got="+dsk);
						return dsk;
					} catch (Exception e2) {
						e2.printStackTrace();
						Log.d("Import", "LoadPK. err sk="+e2.getLocalizedMessage());
					}
				}
				return null;
			}
	    });

		return view;
	}
}
