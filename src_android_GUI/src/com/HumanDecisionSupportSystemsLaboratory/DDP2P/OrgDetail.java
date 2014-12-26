/*   Copyright (C) 2014 Authors: Hang Dong <hdong2012@my.fit.edu>, Marius Silaghi <silaghi@fit.edu>
Florida Tech, Human Decision Support Systems Laboratory

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation; either the current version of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.              */
/* ------------------------------------------------------------------------- */
package com.HumanDecisionSupportSystemsLaboratory.DDP2P;


import data.D_Organization;
import util.Util;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class OrgDetail extends FragmentActivity{
	private static String organization_lid;
	private static String organization_gidh;
	private static int organization_position;

	private String orgName;
	private TextView orgNameTextView;
	private ImageView motion;
	private ImageView constituent;
	private ImageView set;	
	private ImageView profile;
	private ImageView news;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);		
		setContentView(R.layout.org_detail);
		
		orgName = null;
		
		Intent i = this.getIntent();
		Bundle b = i.getExtras();		
		orgName = b.getString(Orgs.O_NAME);
		organization_lid = b.getString(Orgs.O_LID);
		organization_gidh = b.getString(Orgs.O_GIDH);
		organization_position = b.getInt(Orgs.O_ID);
		
		D_Organization org = D_Organization.getOrgByLID(organization_lid, true, false);
		if (org == null) {
			Toast.makeText(this, "Organization not found!", Toast.LENGTH_SHORT).show();
			return;
		}
		
		orgNameTextView = (TextView) findViewById(R.id.org_name_display_on_org_detail);
		orgNameTextView.setText(orgName);
		
		motion = (ImageView) findViewById(R.id.motion_img);
		constituent = (ImageView) findViewById(R.id.constituent_img);
		set = (ImageView) findViewById(R.id.org_set_img);
		profile = (ImageView) findViewById(R.id.org_profile_img);
		news = (ImageView) findViewById(R.id.news_img);

		if (org.getNamesConstituent() != null && org.getNamesConstituent().length > 0) {
			TextView orgDetailConstituentTextView = (TextView) findViewById(R.id.constituent_icon_text);
			orgDetailConstituentTextView.setText(org.getNamesConstituent()[0]);
		}
		
		if (org.getNamesMotion() != null && org.getNamesMotion().length > 0) {
			TextView orgDetailMotionTextView = (TextView) findViewById(R.id.motion_icon_text);
			orgDetailMotionTextView.setText(org.getNamesMotion()[0]);
		}
		
		motion.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(OrgDetail.this, Motion.class);
				Bundle b = new Bundle();
				b.putInt(Orgs.O_ID, organization_position);
				b.putString(Orgs.O_GIDH, organization_gidh);
				b.putString(Orgs.O_LID, organization_lid);
				b.putString(Orgs.O_NAME, orgName);
				intent.putExtras(b);
		        startActivity(intent);
		        
		        
			}
		});
		
		constituent.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				//intent.setClass(OrgDetail.this, Constituent.class);
				intent.setClass(OrgDetail.this, ConstituentFurtherLayer.class);
				Bundle b = new Bundle();
				b.putInt(Orgs.O_ID, organization_position);
				b.putString(Orgs.O_GIDH, organization_gidh);
				b.putString(Orgs.O_LID, organization_lid);
				b.putString(Orgs.O_NAME, orgName);
				intent.putExtras(b);
		        startActivity(intent);
			}
		});
		
		set.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(OrgDetail.this, OrgSetCategoryName.class);
		        startActivity(intent);
			}
		});
		
		profile.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(OrgDetail.this, OrgProfile.class);
				Bundle b = new Bundle();
				b.putInt(Orgs.O_ID, organization_position);
				b.putString(Orgs.O_GIDH, organization_gidh);
				b.putString(Orgs.O_LID, organization_lid);
				b.putString(Orgs.O_NAME, orgName);
				intent.putExtras(b);
		        startActivity(intent);
			}
		});
		
		news.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(OrgDetail.this, News.class);
		        startActivity(intent);
			}
		});
			
		
	}
	
}
