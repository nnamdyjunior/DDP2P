package com.HumanDecisionSupportSystemsLaboratory.DD_P2P;

import java.io.File;

import net.ddp2p.common.util.DD_SK;
import net.ddp2p.common.util.P2PDDSQLException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import net.ddp2p.ciphersuits.KeyManagement;
import net.ddp2p.common.config.DD;
import net.ddp2p.common.data.D_Peer;

public class SafeProfileActivity extends FragmentActivity {

	private static final String TAG = "SafeProfile";
	// private int safe_id;
	private String safe_lid;
	private int SELECT_PROFILE_PHOTO = 10;
	private int SELECT_PPROFILE_PHOTO_KITKAT = 11;
	private String[] drawerContent;
	public static boolean[] drawerState;
	private D_Peer peer;
	private ActionBarDrawerToggle drawerToggle;
	private DrawerLayout drawerLayout = null;
	private SafeProfileFragment safeProfileFragment = null;
	private ImageView imgbut;
	private String selectedImagePath;
	private File selectImageFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.safe_profile_drawer);

		Intent i = this.getIntent();
		Bundle b = i.getExtras();

		safe_lid = b.getString(Safe.P_SAFE_LID);

		peer = D_Peer.getPeerByLID(safe_lid, true, false);

		if (getSupportFragmentManager().findFragmentById(
				R.id.safe_profile_drawer_content) == null) {
			showSafeProfile();
		}
		drawerContent = prepareDrawer(peer.getSK() != null);

		Log.d(TAG, drawerContent[0]);
		drawerLayout = (DrawerLayout) findViewById(R.id.safe_profile_drawer_layout);

		// enable action bar home button
		android.app.ActionBar actBar = getActionBar();
		actBar.setDisplayHomeAsUpEnabled(true);

		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
				R.drawable.ic_drawer, R.string.safe_profile_drawer_open,
				R.string.safe_profile_drawer_close);

		ListView drawer = (ListView) findViewById(R.id.safe_profile_drawer_listview);

		drawer.setAdapter(new SafeProfileAdapter(this, drawerContent, drawerState));

		SafeProfileOnItemClickListener drawerListener = new SafeProfileOnItemClickListener();
		drawer.setOnItemClickListener(drawerListener);
		
		drawerLayout.setDrawerListener(drawerToggle);

	}

	private void showSafeProfile() {
		if (safeProfileFragment == null)
			safeProfileFragment = new SafeProfileFragment();

		if (!safeProfileFragment.isVisible())
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.safe_profile_drawer_content,
							safeProfileFragment).commit();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
		Log.d(TAG, "onConfigChanged: ?");
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
		Log.d(TAG, "onPostCreate: ?");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.action_setNameMy) {
			Bundle id = new Bundle();
			// id.putInt(Safe.P_SAFE_ID, safe_id);
			id.putString(Safe.P_SAFE_LID, safe_lid);

			// update name my dialog
			FragmentManager fm = getSupportFragmentManager();
			UpdateSafeNameMy nameDialog = new UpdateSafeNameMy();
			nameDialog.setArguments(id);
			nameDialog.show(fm, "fragment_edit_name_my");
		}

		else if (item.getItemId() == R.id.action_setEmail) {
			Bundle id = new Bundle();
			// id.putInt("Safe_ID", safe_id);
			id.putString(Safe.P_SAFE_LID, safe_lid);

			// update email dialog
			FragmentManager fm = getSupportFragmentManager();
			UpdateSafeEmail emailDialog = new UpdateSafeEmail();
			emailDialog.setArguments(id);
			emailDialog.show(fm, "fragment_edit_email");

		}

		else if (item.getItemId() == R.id.action_setSlogan) {
			Bundle id = new Bundle();
			// id.putInt("Safe_ID", safe_id);
			id.putString(Safe.P_SAFE_LID, safe_lid);

			// update slogan dialog
			FragmentManager fm = getSupportFragmentManager();
			UpdateSafeSlogan sloganDialog = new UpdateSafeSlogan();
			sloganDialog.setArguments(id);
			sloganDialog.show(fm, "fragment_edit_slogan");
		}

		else if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		// /* else if (item.getItemId() == R.id.export_peer_pk) {
		// Bundle id = new Bundle();
		// id.putInt("Safe_ID", safe_id);
		//
		// FragmentManager fm = getSupportFragmentManager();
		// SendPK sendPKDialog = new SendPK();
		// sendPKDialog.setArguments(id);
		// sendPKDialog.show(fm, "fragment_send_public_key");
		//
		//
		// }*/

		else
			Toast.makeText(this, "To be implement", Toast.LENGTH_SHORT).show();
		return super.onOptionsItemSelected(item);
	}

	/*
	 * @TargetApi(Build.VERSION_CODES.KITKAT)
	 * 
	 * @Override protected void onActivityResult(int requestCode, int
	 * resultCode, Intent resultData) { if (resultCode == RESULT_OK &&
	 * resultData != null) { Uri uri = null;
	 * 
	 * if (requestCode == SELECT_PHOTO) { uri = resultData.getData();
	 * Log.i("Uri", "Uri: " + uri.toString()); } else if (requestCode ==
	 * SELECT_PHOTO_KITKAT) { uri = resultData.getData(); Log.i("Uri_kitkat",
	 * "Uri: " + uri.toString()); final int takeFlags = resultData.getFlags() &
	 * (Intent.FLAG_GRANT_READ_URI_PERMISSION |
	 * Intent.FLAG_GRANT_WRITE_URI_PERMISSION); // Check for the freshest data.
	 * getContentResolver().takePersistableUriPermission(uri, takeFlags); }
	 * 
	 * 
	 * selectedImagePath = FileUtils.getPath(this,uri); Log.i("path", "path: " +
	 * selectedImagePath);
	 * 
	 * selectImageFile = new File(selectedImagePath); //File testFile = new
	 * File("file://storage/emulated/0/DCIM/hobbit.bmp");
	 * 
	 * boolean success;
	 * 
	 * 
	 * String[] selected = new String[1]; DD_Address adr = new DD_Address(peer);
	 * try { //util.EmbedInMedia.DEBUG = true; Log.i("success_embed",
	 * "success_embed 1: "+selectImageFile); success =
	 * DD.embedPeerInBMP(selectImageFile, selected, adr); Log.i("success_embed",
	 * "success_embed 2: " + success); if (success == true) {
	 * Toast.makeText(this, "Export success!", Toast.LENGTH_SHORT).show(); }
	 * else Toast.makeText(this, "Unable to export:"+selected[0],
	 * Toast.LENGTH_SHORT).show(); } catch (Exception e) { Toast.makeText(this,
	 * "Unable to export!", Toast.LENGTH_SHORT).show(); e.printStackTrace(); }
	 * 
	 * }
	 * 
	 * if (resultCode == RESULT_OK && resultData != null) { Uri uri = null;
	 * 
	 * if (requestCode == PK_SELECT_PHOTO) { uri = resultData.getData();
	 * Log.i("Uri", "Uri: " + uri.toString()); } else if (requestCode ==
	 * PK_SELECT_PHOTO_KITKAT) { uri = resultData.getData(); Log.i("Uri_kitkat",
	 * "Uri: " + uri.toString()); final int takeFlags = resultData.getFlags() &
	 * (Intent.FLAG_GRANT_READ_URI_PERMISSION |
	 * Intent.FLAG_GRANT_WRITE_URI_PERMISSION); // Check for the freshest data.
	 * getContentResolver().takePersistableUriPermission(uri, takeFlags); }
	 * 
	 * 
	 * selectedImagePath = FileUtils.getPath(this,uri); Log.i("path", "path: " +
	 * selectedImagePath);
	 * 
	 * selectImageFile = new File(selectedImagePath); //File testFile = new
	 * File("file://storage/emulated/0/DCIM/hobbit.bmp");
	 * 
	 * boolean success;
	 * 
	 * try { //util.EmbedInMedia.DEBUG = true; success = saveSK(peer,
	 * selectImageFile); Log.i("success_embed", "success_embed: " + success); if
	 * (success == true) { Toast.makeText(this, "Export success!",
	 * Toast.LENGTH_SHORT).show(); } else Toast.makeText(this,
	 * "Unable to export!", Toast.LENGTH_SHORT).show(); } catch (Exception e) {
	 * Toast.makeText(this, "Unable to export!", Toast.LENGTH_SHORT).show();
	 * e.printStackTrace(); }
	 * 
	 * } super.onActivityResult(requestCode, resultCode, resultData); }
	 */
	public boolean saveSK(D_Peer pk, File f) {
		DD_SK dsk = new DD_SK();
		try {
			if (! KeyManagement.fill_sk(dsk, pk.getGID()))
				return false;
		} catch (P2PDDSQLException e) {
			e.printStackTrace();
		}
		String[] selected = new String[1];
		boolean success = DD.embedPeerInBMP(f, selected, dsk);
		return success;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.profile_main, menu);
		return true;
	}

	private class SafeProfileOnItemClickListener implements
			AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Log.d(TAG, "SafeProfileAct: onClick: position="+position+" id="+id+" peer="+peer.getName());
			FragmentManager fm = getSupportFragmentManager();
			Bundle s_id = new Bundle();
			s_id.putString(Safe.P_SAFE_LID, safe_lid);

			if (peer.getSK() != null) {
				switch (position) {
				case SafeProfileActivity.SAFE_DRAWER_SK_SET_NAME: // 0
					// update name dialog
					UpdateSafeName nameDialog = new UpdateSafeName();
					nameDialog.setArguments(s_id);
					nameDialog.show(fm, "fragment_edit_name");
					break;

				case SafeProfileActivity.SAFE_DRAWER_SK_SET_MYSELF: // 1:
                    net.ddp2p.common.data.HandlingMyself_Peer
							.setMyself_currentIdentity_announceDirs(peer, true,
									false);
					Toast.makeText(SafeProfileActivity.this,
							"Successfully set this instance myself!",
							Toast.LENGTH_SHORT).show();
					break;

				case SafeProfileActivity.SAFE_DRAWER_SK_EXPORT_ADDR: // 2:
					// update name dialog
					SendPK sendPKDialog = new SendPK();
					sendPKDialog.setArguments(s_id);
					sendPKDialog.show(fm, "fragment_send_public_key");
					break;

				case SafeProfileActivity.SAFE_DRAWER_SK_SELECT_DIRS: // 3:
					Intent i = new Intent();
					i.setClass(SafeProfileActivity.this,
							SelectDirectoryServer.class);
					startActivity(i);
					break;

				case SafeProfileActivity.SAFE_DRAWER_SK_SERVE: // 4
				case SafeProfileActivity.SAFE_DRAWER_SK_SET_INSTANCE: // 5
				case SafeProfileActivity.SAFE_DRAWER_SK_HIDE_TOGGLE: // 6
				default:
					break;
				}
			} else {
				/*
				switch (position) {
				case SafeProfileActivity.SAFE_DRAWER____RESET_SYNC: // 0:
					Switch resetThisSafe = (Switch) view
					.findViewById(R.id.safe_profile_drawer_switch);
					peer = D_Peer.getPeerByPeer_Keep(peer);
					peer.setLastSyncDate(null);
					peer.storeRequest();
					peer.releaseReference();

					for (D_PeerInstance i : peer._instances.values()) {
						Calendar date = i.get_last_sync_date();
						Log.i(TAG, "last sync date: " + date);
					}
					resetThisSafe.setChecked(false);
					break;

				case SafeProfileActivity.SAFE_DRAWER____HIDE_TOGGLE: // 1:
					Switch hideThisSafe = (Switch) view
							.findViewById(R.id.safe_profile_drawer_switch);
					D_Peer.setHidden(peer, hideThisSafe.isChecked());

					hideThisSafe
							.setOnCheckedChangeListener(new OnCheckedChangeListener() {

								@Override
								public void onCheckedChanged(
										CompoundButton buttonView,
										boolean isChecked) {
									D_Peer.setHidden(peer, isChecked);
								}							
							});
					
					break;

				case SafeProfileActivity.SAFE_DRAWER____ACCESS: // 2:
					Switch accessIt = (Switch) view
							.findViewById(R.id.safe_profile_drawer_switch);
					D_Peer.setUsed(peer, accessIt.isChecked());

					accessIt.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							D_Peer.setUsed(peer, isChecked);
						}
					});
					break;

				case SafeProfileActivity.SAFE_DRAWER____BLOCK: // 3:
					Switch blockIt = (Switch) view
							.findViewById(R.id.safe_profile_drawer_switch);
					boolean chstate = blockIt.isChecked();
					D_Peer.setBlocked(peer, chstate);
					Log.d(TAG, "SafeProfileAct: onClick: block="+peer.getBlocked()+" after setting "+chstate);

					blockIt.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							D_Peer.setBlocked(peer, isChecked);
							Log.d(TAG, "SafeProfileAct: onChecked: block="+peer.getBlocked()+" after setting "+isChecked);
						}
					});
					break;

				case SafeProfileActivity.SAFE_DRAWER____SERVE: // 4:
					Switch serveIt = (Switch) view
							.findViewById(R.id.safe_profile_drawer_switch);
					D_Peer.setUsed(peer, serveIt.isChecked());

					serveIt.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							D_Peer.setUsed(peer, isChecked);
						}
					});
					break;
				default:
					break;
				}
				*/
			}

		}

	}
	public final static int SAFE_DRAWER_SK_SET_NAME = 0;
	public final static int SAFE_DRAWER_SK_SET_MYSELF = 1;
	public final static int SAFE_DRAWER_SK_EXPORT_ADDR = 2;
	public final static int SAFE_DRAWER_SK_SELECT_DIRS = 3;
	public final static int SAFE_DRAWER_SK_SERVE = 4;
	public final static int SAFE_DRAWER_SK_SET_INSTANCE = 5;
	public final static int SAFE_DRAWER_SK_HIDE_TOGGLE = 6;
	public final static int SAFE_DRAWER____RESET_SYNC = 0;
	public final static int SAFE_DRAWER____HIDE_TOGGLE = 1;
	public final static int SAFE_DRAWER____ACCESS = 2;
	public final static int SAFE_DRAWER____BLOCK = 3;
	public final static int SAFE_DRAWER____SERVE = 4;
	private String[] prepareDrawer(boolean hasSafeSk) {
		String[] drawer = new String[7];
		drawerState = new boolean[7];

		if (hasSafeSk) {
			drawer[SAFE_DRAWER_SK_SET_NAME] = "Set Name";
			drawer[SAFE_DRAWER_SK_SET_MYSELF] = "Set Myself";
			drawer[SAFE_DRAWER_SK_EXPORT_ADDR] = "Export Address";
			drawer[SAFE_DRAWER_SK_SELECT_DIRS] = "Select DirectoryServer";
			drawer[SAFE_DRAWER_SK_SERVE] = "Serve It";
			drawer[SAFE_DRAWER_SK_SET_INSTANCE] = "Set Instance";
			drawer[SAFE_DRAWER_SK_HIDE_TOGGLE] = "Hide This Safe";
			
			drawerState[SAFE_DRAWER_SK_HIDE_TOGGLE] = this.peer.getHidden();
			drawerState[SAFE_DRAWER_SK_SERVE] = this.peer.getBroadcastable();
		}

		if (! hasSafeSk) {
			drawer[SAFE_DRAWER____RESET_SYNC] = "Reset Last Sync Date";
			drawer[SAFE_DRAWER____HIDE_TOGGLE] = "Hide This Safe";
			drawer[SAFE_DRAWER____ACCESS] = "Access It";
			drawer[SAFE_DRAWER____BLOCK] = "Block It";
			drawer[SAFE_DRAWER____SERVE] = "Serve It";
			drawer[5] = " ";
			drawer[6] = " ";
			
			drawerState[SAFE_DRAWER____RESET_SYNC] = false;
			drawerState[SAFE_DRAWER____HIDE_TOGGLE] = this.peer.getHidden();
			drawerState[SAFE_DRAWER____ACCESS] = this.peer.getUsed();
			drawerState[SAFE_DRAWER____BLOCK] = this.peer.getBlocked();
			drawerState[SAFE_DRAWER____SERVE] = this.peer.getBroadcastableMyOrDefault();
			
			Log.d(TAG, "SafeProfileAct: prepareDrawer: block="+drawerState[SAFE_DRAWER____BLOCK]);
		}

		return drawer;

	}
    static int cnt_try_false = 0;

	private class SafeProfileAdapter extends BaseAdapter {

		private Activity activity;
		private LayoutInflater inflater = null;
		private String[] data;
		private boolean[] state;

		public SafeProfileAdapter(Activity _activity, String[] _data, boolean[] _state) {
			activity = _activity;
			data = _data;
			state = _state;
			inflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		}

		@Override
		public int getCount() {
			return data.length;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;

			if (convertView == null)
				v = inflater.inflate(R.layout.safe_profile_drawer_row, null);


			TextView content = (TextView) v
					.findViewById(R.id.safe_profile_drawer_row_text);
			if (peer.getSK() != null) {
				content.setText(data[position]);
				//s.setPressed(state[position]);
			} else {
				content.setText(data[position]);
				if (position > 0) {
					v = inflater.inflate(
							R.layout.safe_profile_drawer_row_switch, null);
					Switch s = (Switch) v
							.findViewById(R.id.safe_profile_drawer_switch);
					s.setText(data[position]);
					//s.setPressed(state[position]);
					s.setChecked(state[position]);
					Log.d(TAG, "SafeProfileAdapt: getView: state="+state[position]);

                    Switch blockIt = (Switch) v
                            .findViewById(R.id.safe_profile_drawer_switch);
                    boolean chstate = blockIt.isChecked();

					switch (position) {
					case SafeProfileActivity.SAFE_DRAWER____BLOCK: // 3:
						//D_Peer.setBlocked(peer, chstate);
						Log.d(TAG, "SafeProfileAdapt: getView: block="+peer.getBlocked()+" after setting "+chstate);
						//blockIt.setOnClickListener(new View.OnClickListener() {
						blockIt.setOnCheckedChangeListener(new OnCheckedChangeListener() {


//							@Override
//							public void onClick(View v) {
//								Switch blockIt = (Switch) v
//										.findViewById(R.id.safe_profile_drawer_switch);
//								final CompoundButton buttonView_ = blockIt;
//								
//							boolean isChecked = true;
////							}
							@Override
							public void onCheckedChanged(CompoundButton buttonView,
									boolean isChecked) {
                                //Toast.makeText(getApplicationContext(), "Setting Block = "+isChecked, Toast.LENGTH_SHORT).show();
								final CompoundButton buttonView_ = buttonView;
						    	boolean _isChecked = ! peer.getBlocked();
                                if (isChecked)
                                    cnt_try_false = 0;
                                if (! _isChecked) {
                                    if (cnt_try_false++ % 5 != 0) {
                                        buttonView_.setChecked(!_isChecked);
                                        Toast.makeText(getApplicationContext(), "Try again "+(5-((cnt_try_false-1)%5))+" times to set blocking to " + _isChecked, Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                                Toast.makeText(getApplicationContext(), "Setting Block = "+isChecked, Toast.LENGTH_SHORT).show();

				                AlertDialog.Builder confirm = new AlertDialog.Builder(SafeProfileActivity.this);
				                confirm.setTitle("Do you wish to change blocking?");
				                confirm.setMessage("Do you want to set blocking of \""+peer.getName()+"\" to " + _isChecked + ((isChecked != _isChecked)?"!":""))
				                    .setCancelable(false)
								    .setPositiveButton("Yes", new MyDialog_OnClickListener("Dia 2") {
									    public void _onClick(DialogInterface dialog, int id) {
									    	
									    	boolean isChecked = ! peer.getBlocked();
											D_Peer.setBlocked(peer, isChecked);
											Log.d(TAG, "SafeProfileAdapt: getView onChecked: block="+peer.getBlocked()+" after setting "+isChecked);
								    		buttonView_.setChecked(isChecked);
								
									    	dialog.cancel();
									    }
								    })
								    .setNegativeButton("No",new DialogInterface.OnClickListener() {
								    	public void onClick(DialogInterface dialog,int id) {
									    	boolean isChecked = peer.getBlocked();
								    		buttonView_.setChecked(isChecked);
								    		dialog.cancel();
								    		return;
								    	}
								    });
			                
				                AlertDialog confirmDialog = confirm.create();
				                confirmDialog.show();
								
								//Util.printCallPath("");
							}
						});
						break;

                        case SafeProfileActivity.SAFE_DRAWER____ACCESS: // 2:
                            //D_Peer.setUsed(peer, chstate);
                            Log.d(TAG, "SafeProfileAdapt: getView: access="+peer.getUsed()+" after setting "+chstate);
                            blockIt.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView,
                                                             boolean isChecked) {
                                    //Toast.makeText(getApplicationContext(), "Setting Access = "+isChecked, Toast.LENGTH_SHORT).show();
                                    final CompoundButton buttonView_ = buttonView;
                                    boolean _isChecked = ! peer.getUsed();
                                    if (isChecked)
                                        cnt_try_false = 0;
                                    if (! _isChecked) {
                                        if (cnt_try_false++ % 5 != 0) {
                                            buttonView_.setChecked(!_isChecked);
                                            Toast.makeText(getApplicationContext(), "Try again "+(5-((cnt_try_false-1)%5))+" times to set access to " + _isChecked, Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                    }
                                    Toast.makeText(getApplicationContext(), "Setting Access = "+isChecked, Toast.LENGTH_SHORT).show();

                                    AlertDialog.Builder confirm = new AlertDialog.Builder(SafeProfileActivity.this);
                                    confirm.setTitle("Do you wish to change access?");
                                    confirm.setMessage("Do you want to set access of \""+peer.getName()+"\" to " + _isChecked + ((isChecked != _isChecked)?"!":""))
                                            .setCancelable(false)
                                            .setPositiveButton("Yes", new MyDialog_OnClickListener("Dia 2") {
                                                public void _onClick(DialogInterface dialog, int id) {

                                                    boolean isChecked = ! peer.getUsed();
                                                    D_Peer.setUsed(peer, isChecked);
                                                    Log.d(TAG, "SafeProfileAdapt: getView onChecked: access="+peer.getUsed()+" after setting "+isChecked);
                                                    buttonView_.setChecked(isChecked);

                                                    dialog.cancel();
                                                }
                                            })
                                            .setNegativeButton("No",new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,int id) {
                                                    boolean isChecked = peer.getUsed();
                                                    buttonView_.setChecked(isChecked);
                                                    dialog.cancel();
                                                    return;
                                                }
                                            });

                                    AlertDialog confirmDialog = confirm.create();
                                    confirmDialog.show();

                                    //Util.printCallPath("");
                                }
                            });
                            break;

                        case SafeProfileActivity.SAFE_DRAWER____SERVE: // 2:
                            //D_Peer.setBroadcastableMy(peer, chstate);
                            Log.d(TAG, "SafeProfileAdapt: getView: serve="+peer.getBroadcastableMyOrDefault()+" after setting "+chstate);
                            blockIt.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView,
                                                             boolean isChecked) {
                                    //Toast.makeText(getApplicationContext(), "Setting Serve = "+isChecked, Toast.LENGTH_SHORT).show();
                                    final CompoundButton buttonView_ = buttonView;
                                    boolean _isChecked = ! peer.getBroadcastableMyOrDefault();
                                    if (isChecked)
                                        cnt_try_false = 0;
                                    if (! _isChecked) {
                                        if (cnt_try_false++ % 5 != 0) {
                                            buttonView_.setChecked(!_isChecked);
                                            Toast.makeText(getApplicationContext(), "Try again "+(5-((cnt_try_false-1)%5))+" times to set serve to " + _isChecked, Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                    }
                                    Toast.makeText(getApplicationContext(), "Setting Serve = "+isChecked, Toast.LENGTH_SHORT).show();

                                    AlertDialog.Builder confirm = new AlertDialog.Builder(SafeProfileActivity.this);
                                    confirm.setTitle("Do you wish to change serve?");
                                    confirm.setMessage("Do you want to set serve of \""+peer.getName()+"\" to " + _isChecked + ((isChecked != _isChecked)?"!":""))
                                            .setCancelable(false)
                                            .setPositiveButton("Yes", new MyDialog_OnClickListener("Dia 3") {
                                                public void _onClick(DialogInterface dialog, int id) {

                                                    boolean isChecked = ! peer.getBroadcastableMyOrDefault();
                                                    D_Peer.setBroadcastableMy(peer, isChecked);
                                                    Log.d(TAG, "SafeProfileAdapt: getView onChecked: serve="+peer.getBroadcastableMyOrDefault()+" after setting "+isChecked);
                                                    buttonView_.setChecked(isChecked);

                                                    dialog.cancel();
                                                }
                                            })
                                            .setNegativeButton("No",new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,int id) {
                                                    boolean isChecked = peer.getBroadcastableMyOrDefault();
                                                    buttonView_.setChecked(isChecked);
                                                    dialog.cancel();
                                                    return;
                                                }
                                            });

                                    AlertDialog confirmDialog = confirm.create();
                                    confirmDialog.show();

                                    //Util.printCallPath("");
                                }
                            });
                            break;

                        case SafeProfileActivity.SAFE_DRAWER____HIDE_TOGGLE: // 0:
                            //D_Peer.setHidden(peer, chstate);
                            Log.d(TAG, "SafeProfileAdapt: getView: hidden="+peer.getHidden()+" after setting "+chstate);
                            blockIt.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView,
                                                             boolean isChecked) {
                                    //Toast.makeText(getApplicationContext(), "Setting Hidden = "+isChecked, Toast.LENGTH_SHORT).show();
                                    final CompoundButton buttonView_ = buttonView;
                                    boolean _isChecked = ! peer.getHidden();
                                    if (isChecked)
                                        cnt_try_false = 0;
                                    if (! _isChecked) {
                                        if (cnt_try_false ++ % 5 != 0) {
                                            buttonView_.setChecked(!_isChecked);
                                            Toast.makeText(getApplicationContext(), "Try again "+(5-((cnt_try_false-1)%5))+" times to set hidden to " + _isChecked, Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                    }
                                    Toast.makeText(getApplicationContext(), "Setting Hidden = "+isChecked, Toast.LENGTH_SHORT).show();

                                    AlertDialog.Builder confirm = new AlertDialog.Builder(SafeProfileActivity.this);
                                    confirm.setTitle("Do you wish to change hidden?");
                                    confirm.setMessage("Do you want to set hidden of \""+peer.getName()+"\" to " + _isChecked + ((isChecked != _isChecked)?"!":""))
                                            .setCancelable(false)
                                            .setPositiveButton("Yes", new MyDialog_OnClickListener("Dia 0") {
                                                public void _onClick(DialogInterface dialog, int id) {

                                                    boolean isChecked = ! peer.getHidden();
                                                    D_Peer.setHidden(peer, isChecked);
                                                    Log.d(TAG, "SafeProfileAdapt: getView onChecked: hidden="+peer.getHidden()+" after setting "+isChecked);
                                                    buttonView_.setChecked(isChecked);

                                                    dialog.cancel();
                                                }
                                            })
                                            .setNegativeButton("No",new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,int id) {
                                                    boolean isChecked = peer.getHidden();
                                                    buttonView_.setChecked(isChecked);
                                                    dialog.cancel();
                                                    return;
                                                }
                                            });

                                    AlertDialog confirmDialog = confirm.create();
                                    confirmDialog.show();

                                    //Util.printCallPath("");
                                }
                            });
                            break;
					}
					
				}
			}

			return v;
		}

	}

}