/* ------------------------------------------------------------------------- */
/*   Copyright (C) 2012
		Authors: Khalid Alhamed, Marius Silaghi
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
package data;

import ASN1.ASN1DecoderFail;
import ASN1.ASNObj;
import ASN1.Decoder;
import ASN1.Encoder;
import ciphersuits.Cipher;
import ciphersuits.PK;
import ciphersuits.SK;
import config.DD;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

import util.P2PDDSQLException;
import util.Summary;
import config.Application;
import util.Util;

public class D_Tester extends ASNObj implements Summary{
	private static final boolean _DEBUG = true;
	public static boolean DEBUG = false;
	public long tester_ID = -1;
	public String name;
	public String testerGID;
	public String email;
	public String url; // just for ads
	public String description;
	public long peer_source_LID;
	public boolean revoked;
	public String revoked_info;
	public String revoked_GIDH;
	public String version;
	public String my_name;
	public String testerGIDH;
	public byte[] signature;
	public boolean trustedAsMirror;
	public boolean trustedAsTester;
	public String trustWeight="-1"; //as default not rated yet.
	public boolean referenceTester;
	public Calendar creation_date;
	public Calendar preference_date;

	@Override
	public String toString() {
		return "Tester: name="+name+"\n"+
			"email="+email+"\n"+
			"url="+url+"\n"+
			"description="+description+"\n"+
			"PK="+testerGID;
	}

	@Override
	public String toSummaryString() {
		return "Tester: name="+name+"\n"+
			"email="+email+"\n"+
			"url="+url+"\n"+
			"description="+description;
	}
	final static String sql_get_tester_by_GID = "SELECT "+table.tester.fields_tester+
			" FROM  " + table.tester.TNAME +
			" WHERE "+table.tester.public_key + " = ?";

	private D_Tester( String gID) {
		if (gID == null) return;
//		String sql = "SELECT "+table.tester.fields_tester+
//				" FROM  " +table.tester.TNAME +
//				" WHERE "+ table.tester.public_key+"=?;";
		ArrayList<ArrayList<Object>> result=null;
		try {
			result = Application.db.select(D_Tester.sql_get_tester_by_GID,new String[]{gID},DEBUG);
		} catch (util.P2PDDSQLException e) {
			System.out.println(e);
		}
		if (result.size() > 0) init(result.get(0));
	}
	private D_Tester(long id) {
		if (id < 0) return;
		String sql = "SELECT "+table.tester.fields_tester+
				" FROM  " +table.tester.TNAME +
				" WHERE "+ table.tester.tester_ID+"=?;";
		ArrayList<ArrayList<Object>> result=null;
		try{
			result = Application.db.select(sql,new String[]{Util.getStringID(id)},DEBUG);
		}catch(util.P2PDDSQLException e){
			System.out.println(e);
		}
		if(result.size()>0){
			init(result.get(0));
			if(DEBUG)System.out.println("D_TesterDefinition:<init>:Got: "+this);
		}else{
			if(DEBUG)System.out.println("D_TesterDefinition:<init>:Not found: "+id);
		}
	}
	public D_Tester() {
		
	}
	public D_Tester(ArrayList<Object> _u) {
		init(_u);
	}
	public void init(ArrayList<Object> _u){
		if (DEBUG) System.out.println("D_TesterDefinition: <init>: start");
		tester_ID = Util.lval(_u.get(table.tester.F_ID),-1);
		name = Util.getString(_u.get(table.tester.F_ORIGINAL_TESTER_NAME));
		my_name = Util.getString(_u.get(table.tester.F_MY_TESTER_NAME));
		testerGID = Util.getString(_u.get(table.tester.F_PUBLIC_KEY));
		testerGIDH = Util.getString(_u.get(table.tester.F_PUBLIC_KEY_HASH));
		email = Util.getString(_u.get(table.tester.F_EMAIL));
		url = Util.getString(_u.get(table.tester.F_URL));
		revoked = Util.stringInt2bool(_u.get(table.tester.F_REVOKED), false);
		revoked_info = Util.getString(_u.get(table.tester.F_REVOKED_INFO));
		revoked_GIDH = Util.getString(_u.get(table.tester.F_REVOKED_GID_HASH));
		signature = Util.byteSignatureFromString(Util.getString(_u.get(table.tester.F_SIGNATURE)));
		trustedAsMirror = Util.stringInt2bool(_u.get(table.tester.F_USED_MIRROR), false);
		trustedAsTester = Util.stringInt2bool(_u.get(table.tester.F_USED_TESTER), false);
		trustWeight = Util.getString(_u.get(table.tester.F_WEIGHT));
		peer_source_LID = Util.lval(_u.get(table.tester.F_PEER_SOURCE_LID),-1);
		creation_date = Util.getCalendar(Util.getString(_u.get(table.tester.F_CREATION_DATE)));
		preference_date = Util.getCalendar(Util.getString(_u.get(table.tester.F_PREFERENCE_DATE)));
		referenceTester = Util.stringInt2bool(_u.get(table.tester.F_REFERENCE), false);
		description = Util.getString(_u.get(table.tester.F_DESCRIPTION));
		if (DEBUG) System.out.println("D_TesterDefinition: <init>: done");
	
	}
	
	public Encoder getSignatureEncoder() {
		Encoder enc = new Encoder().initSequence();
		enc.addToSequence(new Encoder(version).setASN1Type(DD.TAG_AP0));
		enc.addToSequence(new Encoder(name));
		enc.addToSequence(new Encoder(email));
		enc.addToSequence(new Encoder(url));
		enc.addToSequence(new Encoder(description));
		enc.addToSequence(new Encoder(testerGID, false));
		if (revoked) {
			enc.addToSequence(new Encoder(revoked).setASN1Type(DD.TAG_AP6));
			enc.addToSequence(new Encoder(revoked_info).setASN1Type(DD.TAG_AP7));
			enc.addToSequence(new Encoder(revoked_GIDH).setASN1Type(DD.TAG_AP8));
			
		}
		//if (signature != null) enc.addToSequence(new Encoder(signature).setASN1Type(DD.TAG_AP9));
		if (creation_date != null) enc.addToSequence(new Encoder(creation_date).setASN1Type(DD.TAG_AP10));
		enc.setASN1Type(getASNType());
		return enc;
	}
	@Override
	public Encoder getEncoder() {
		Encoder enc = new Encoder().initSequence();
		enc.addToSequence(new Encoder(version).setASN1Type(DD.TAG_AP0));
		enc.addToSequence(new Encoder(name));
		enc.addToSequence(new Encoder(email));
		enc.addToSequence(new Encoder(url));
		enc.addToSequence(new Encoder(description));
		enc.addToSequence(new Encoder(testerGID, false));
		if (revoked) {
			enc.addToSequence(new Encoder(revoked).setASN1Type(DD.TAG_AP6));
			enc.addToSequence(new Encoder(revoked_info).setASN1Type(DD.TAG_AP7));
			enc.addToSequence(new Encoder(revoked_GIDH).setASN1Type(DD.TAG_AP8));
			
		}
		if (signature != null) enc.addToSequence(new Encoder(signature).setASN1Type(DD.TAG_AP9));
		if (creation_date != null) enc.addToSequence(new Encoder(creation_date).setASN1Type(DD.TAG_AP10));
		enc.setASN1Type(getASNType());
		return enc;
	}
	@Override
	public D_Tester decode(Decoder dec) throws ASN1DecoderFail {
		Decoder d = dec.getContent();
		if (d.isFirstObjectTagByte(DD.TAG_AP0))
			version = d.getFirstObject(true).getString(DD.TAG_AP0);
		name = d.getFirstObject(true).getString();
		email = d.getFirstObject(true).getString();
		url = d.getFirstObject(true).getString();
		description = d.getFirstObject(true).getString();
		testerGID = d.getFirstObject(true).getString();
		if (d.isFirstObjectTagByte(DD.TAG_AP6))
			revoked = d.getFirstObject(true).getBoolean();
		if (d.isFirstObjectTagByte(DD.TAG_AP7))
			revoked_info = d.getFirstObject(true).getString();
		if (d.isFirstObjectTagByte(DD.TAG_AP8))
			revoked_GIDH = d.getFirstObject(true).getString();
		if (d.isFirstObjectTagByte(DD.TAG_AP9))
			signature = d.getFirstObject(true).getBytes();
		if (d.isFirstObjectTagByte(DD.TAG_AP10))
			creation_date = d.getFirstObject(true).getGeneralizedTimeCalenderAnyType();
		return this;
	}
	@Override
	public D_Tester instance() throws CloneNotSupportedException{return new D_Tester();}
	public static byte getASNType() {
		if (DEBUG) System.out.println("DD.TAG_AC23= "+ DD.TAG_AC23);
		return DD.TAG_AC23;
	}
	public void store() throws P2PDDSQLException {
		boolean update;
		
		D_Tester t = new D_Tester(this.testerGID);
		if (t.tester_ID > 0) tester_ID = t.tester_ID;
		
		update = tester_ID > 0;
		
		String params[] = new String[update? table.tester.F_FIELDS : table.tester.F_FIELDS_NOID];

		params[table.tester.F_DATA_VERSION] = this.version;
		params[table.tester.F_ORIGINAL_TESTER_NAME] = this.name;
		params[table.tester.F_MY_TESTER_NAME] = this.my_name;
		params[table.tester.F_PUBLIC_KEY] = this.testerGID;
		params[table.tester.F_PUBLIC_KEY_HASH] = this.testerGIDH;
		params[table.tester.F_EMAIL] = this.email;
		params[table.tester.F_URL] = this.url;
		params[table.tester.F_REVOKED] = Util.bool2StringInt(this.revoked);
		params[table.tester.F_REVOKED_INFO] = this.revoked_info;
		params[table.tester.F_REVOKED_GID_HASH] = this.revoked_GIDH;
		params[table.tester.F_DESCRIPTION] = this.description;
		params[table.tester.F_SIGNATURE] = Util.stringSignatureFromByte(this.signature);
		params[table.tester.F_USED_MIRROR] = Util.bool2StringInt(this.trustedAsMirror);
		params[table.tester.F_USED_TESTER] = Util.bool2StringInt(this.trustedAsTester);
		params[table.tester.F_WEIGHT] = this.trustWeight;
		params[table.tester.F_REFERENCE] = Util.bool2StringInt(this.referenceTester);
		params[table.tester.F_PEER_SOURCE_LID] = Util.getStringID(this.peer_source_LID);
		params[table.tester.F_CREATION_DATE] = Encoder.getGeneralizedTime(this.creation_date);
		params[table.tester.F_PREFERENCE_DATE] = Encoder.getGeneralizedTime(this.preference_date);

		if (update) {
			params[table.tester.F_ID] = Util.getStringID(this.tester_ID);
//			if (t.description != null) params[table.tester.F_DESCRIPTION] = t.description;
//			if (t.email != null) params[table.tester.F_EMAIL] = t.email;
//			if (t.url != null) params[table.tester.F_URL] = t.url;
			//if(t.name != null) params[table.tester.F_NAME] = t.name;
			
			Application.db.update(table.tester.TNAME, table.tester._fields_tester_no_ID,
					new String[]{table.tester.tester_ID},
					params, DEBUG);
		} else {
//			//params[table.tester.F_ID] = Util.getStringID(this.tester_ID);
//			String params2[]=new String[table.tester.F_FIELDS_NOID];
//			System.arraycopy(params,0,params2,0,params2.length);
//			if(DEBUG)System.out.println("params2[last]: "+ params2[table.tester.F_FIELDS_NOID-1]);
			this.tester_ID = Application.db.insert(table.tester.TNAME,
					table.tester._fields_tester_no_ID,
					params,
					DEBUG);
		}
	}
	final static String sql_get_all_testers_LIDs = "SELECT "+table.tester.tester_ID+
				" FROM  " + table.tester.TNAME+";";
	public static ArrayList<D_Tester> retrieveTesterDefinitions() {
		ArrayList<D_Tester> result = new ArrayList<D_Tester>();
		ArrayList<ArrayList<Object>> list = null;
		try {
			list = Application.db.select(sql_get_all_testers_LIDs, new String[]{}, DEBUG);
		} catch(util.P2PDDSQLException e){
			System.out.println(e);
		}
		if(list == null ) {
			return null;
		}
		for(ArrayList<Object> id : list){
			long _id = Util.lval(id.get(0));
			if(DEBUG)System.out.println("D_TesterDefinition:<init>:Found: "+_id);
			D_Tester td = D_Tester.getTesterInfoByLID(_id);
			result.add(td);
		}
		return result;
	}
	/*
	final static String sql_get_a_tester_by_GID = "SELECT "+table.tester.fields_tester+
				" FROM  " + table.tester.TNAME +
				" WHERE "+table.tester.F_ORIGINAL_TESTER_NAME + " = ? AND "+table.tester.public_key+"= ?";
	public static D_Tester retrieveTesterDefinition(String name, String pubKey){
		String[] params = new String[]{name, pubKey};
		ArrayList<ArrayList<Object>> result=null;
		try{
			result = Application.db.select(sql_get_a_tester_by_GID, params, DEBUG);
		}catch(util.P2PDDSQLException e){
			System.out.println(e);
		}
		if (result == null ) {
			return null;
		}
		return new D_Tester(result.get(0));
	}
	*/
	final static String sql_get_used_testers = "SELECT "+table.tester.fields_tester+
				" FROM  " + table.tester.TNAME +
				" WHERE "+table.tester.trusted_as_tester + " = 1";
	public static ArrayList<D_Tester> retrieveAllUsedTesters(){
		ArrayList<ArrayList<Object>> result = null;
		try {
			result = Application.db.select(sql_get_used_testers, new String[]{}, DEBUG);
		} catch(util.P2PDDSQLException e){
			System.out.println(e);
		}
		if (result == null ) {
			return null;
		}
		ArrayList<D_Tester> testersList = new ArrayList<D_Tester>();
		for(int i=0; i<result.size(); i++)
			testersList.add(new D_Tester(result.get(i)));
		return testersList ;
	}
	
//	/**
//	 * To be replaced by getTesterInfoByGID
//	 * @param pubKey
//	 * @return
//	 */
//	@Deprecated
//	public static D_Tester retrieveTesterDefinition_ByGID(String pubKey){
//		String[] params = new String[]{pubKey};
//		ArrayList<ArrayList<Object>> result=null;
//		try{
//			result = Application.db.select(sql_get_tester_by_GID, params, DEBUG);
//		}catch(util.P2PDDSQLException e){
//			System.out.println(e);
//		}
//		if (result == null) {
//			return null;
//		}
//		return new D_Tester(result.get(0));
//	}
	final static Object factory_monitor = new Object(); 
	/**
	 * GetTeser by GID
	 * @param testerGID
	 * @param create
	 * @param peer_source
	 * @param url
	 * @return
	 */
	public static D_Tester getTesterInfoByGID(String testerGID, boolean create, D_Peer peer_source, String url) {
		synchronized (factory_monitor) {
			D_Tester tester = new D_Tester(testerGID);
			if (create && tester.tester_ID <= 0) {
				tester.testerGID = testerGID;
				tester.url = url;
				if (url != null) {
					try {
						String data = Util.readAll(new URL(url));
						if(data==null){
							throw new Exception("Cannot read URL");
						}
						byte[] testerData = Util.byteSignatureFromString(data);
						D_Tester newtester = new D_Tester().decode(new Decoder(testerData));
						if (testerGID.equals(newtester.testerGID)
								&& url.equals(newtester.url)
								&& newtester.verifySignature()) {
							tester = newtester;
							tester.peer_source_LID = peer_source.getLID();
							tester.store();
							return tester;
						}
					} catch (MalformedURLException | ASN1DecoderFail | P2PDDSQLException e1) {
						if (DEBUG) e1.printStackTrace();
						if (_DEBUG) System.out.println("D_Tester: getTesterIngoByGID: Bad url: "+url);
					} catch( Exception e2){
						if (DEBUG) e2.printStackTrace();
						if (_DEBUG) System.out.println("D_Tester: getTesterIngoByGID: Bad url: "+url);
					}
					
				}
				if (peer_source != null)
					tester.peer_source_LID = peer_source.getLID();
				try {
					tester.store();
				} catch (P2PDDSQLException e) {
					e.printStackTrace();
				}
			}
			return tester;
		}
	}
	public boolean verifySignature() {
		PK pk = Cipher.getPK(testerGID);
		if (pk == null) return false;
		return Util.verifySign(this.getSignatureEncoder().getBytes(), pk, signature);
	}
	public byte[] sign() {
		SK sk = Util.getStoredSK(testerGID);
		if (sk == null) return null;
		return Util.sign(this.getSignatureEncoder().getBytes(), sk);
	}

	public static D_Tester getTesterInfoByLID(long testerLID) {
		synchronized (factory_monitor) {
			D_Tester tester = new D_Tester(testerLID);
			return tester;
		}
	}

	public long getLID() {
		return this.tester_ID;
	}

	public static void initAllTestersRecommendations() {
		try {
			Application.db.update(false, table.tester.TNAME,
					new String[] {table.tester.trust_weight 
					,table.tester.reference_tester 
					,table.tester.trusted_as_tester
					//,table.tester.trusted_as_mirror
					},
					new String[]{}, new String[] {null, "0", "0"
							//,"0"
					}, DEBUG);
			
		} catch (P2PDDSQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets this tester as trusted (tester) having given weight
	 * @param testerID
	 * @param score
	 */
	public static void setUsedTester(long testerID, String score) {
		
		try {
			Application.db.update(false, table.tester.TNAME,
					new String[]{table.tester.trust_weight
					,table.tester.trusted_as_tester
					//,table.tester.trusted_as_mirror
					},
					new String[]{table.tester.tester_ID}, 
					new String[]{score, "1", //"1",
					Util.getStringID(testerID)},
					DEBUG);
		} catch (P2PDDSQLException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Changes score (weight) of a tester
	 * @param testerID
	 * @param score
	 */
	public static void setKnownTester(long testerID, String score) {
		
		try {
			Application.db.update(false, table.tester.TNAME, new String[] {table.tester.trust_weight},
					new String[]{table.tester.tester_ID},
					new String[]{score, Util.getStringID(testerID)},
					DEBUG);
		} catch (P2PDDSQLException e) {
			e.printStackTrace();
		}
		
	}

	public static long getTesterLIDbyGID(String testerGID2) {
		D_Tester tester = D_Tester.getTesterInfoByGID(testerGID2, false, null, null);
		if (tester == null) return -1;
		return tester.getLID();
	}

	
}