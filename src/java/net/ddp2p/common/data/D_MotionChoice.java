/* ------------------------------------------------------------------------- */
/*   Copyright (C) 2012 Marius C. Silaghi
		Author: Marius Silaghi: msilaghi@fit.edu
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

package net.ddp2p.common.data;

import java.util.ArrayList;

import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.ASNObj;
import net.ddp2p.ASN1.Decoder;
import net.ddp2p.ASN1.Encoder;
import net.ddp2p.common.config.Application;
import net.ddp2p.common.config.Application_GUI;
import net.ddp2p.common.config.DD;
import net.ddp2p.common.util.P2PDDSQLException;
import net.ddp2p.common.util.Util;
public
class D_MotionChoice extends ASNObj{
	private static final boolean DEBUG = false;
	private static final boolean _DEBUG = true;
	public String name;
	public String short_name; // name used in votes to save space 

	String motionID;
	String choice_ID;
	
	public String toStringDump() {
		return "WB_Choice: short="+short_name+" name="+name;
	}
	public String toString() {
		return name;
	}
	
	public D_MotionChoice(){}
	
	/**
	 *  An array with table.motion_choice.fields
	 *  table.motion_choice.CH_NAME
	 *   and 
	 *  table.motion_choice.CH_SHORT_NAME
	 */
	public D_MotionChoice(ArrayList<Object> l) {
		name = Util.getString(l.get(net.ddp2p.common.table.motion_choice.CH_NAME));
		short_name = Util.getString(l.get(net.ddp2p.common.table.motion_choice.CH_SHORT_NAME));
	}
	public D_MotionChoice(String _name, String _short_name) {
		name = _name;
		short_name = _short_name;
	}
	@Override
	public Encoder getEncoder() {
		Encoder enc = new Encoder().initSequence();
		enc.addToSequence(new Encoder(name).setASN1Type(DD.TAG_AC0));
		enc.addToSequence(new Encoder(short_name).setASN1Type(DD.TAG_AC1));
		return enc;
	}
	@Override
	public D_MotionChoice decode(Decoder dec) throws ASN1DecoderFail {
		Decoder d = dec.getContent();
		if(d.getTypeByte()==DD.TAG_AC0) name = d.getFirstObject(true).getString(DD.TAG_AC0);
		if(d.getTypeByte()==DD.TAG_AC1) short_name = d.getFirstObject(true).getString(DD.TAG_AC1);
		return this;
	}
	public D_MotionChoice instance() throws CloneNotSupportedException{
		return new D_MotionChoice();
	}
	public static final String sql_choice_IDs =
			"SELECT "+net.ddp2p.common.table.motion_choice.choice_ID+
			" FROM "+net.ddp2p.common.table.motion_choice.TNAME+
			" WHERE "+net.ddp2p.common.table.motion_choice.motion_ID+"=? AND ( "+net.ddp2p.common.table.motion_choice.choice_Name+"=? OR "+net.ddp2p.common.table.motion_choice.shortName+"=?);";
	/**
	 * Saves a choice for motionID, assuming no other choice exists with the same name OR GID
	 * @param motionID
	 * @return
	 * @throws P2PDDSQLException
	 */
	public long save(String motionID, boolean sync) throws P2PDDSQLException {
		long result = -1;
		ArrayList<ArrayList<Object>> c = Application.getDB().select(sql_choice_IDs, new String[]{motionID, name, short_name}, DEBUG);
		if(c.size()!=0){
			String old = Util.getString(c.get(0).get(0));
			Application_GUI.warning(Util.__("Duplicate motion choice:")+" "+old+"\n"+this, Util.__("Duplicate motion choice"));
			return result;
		}else{
			String[] params = new String[net.ddp2p.common.table.motion_choice.CH_FIELDS];
			params[net.ddp2p.common.table.motion_choice.CH_MOTION_ID] = motionID;
			params[net.ddp2p.common.table.motion_choice.CH_NAME] = name;
			params[net.ddp2p.common.table.motion_choice.CH_SHORT_NAME] = short_name;
			if(sync){
				result = Application.getDB().insert(net.ddp2p.common.table.motion_choice.TNAME, net.ddp2p.common.table.motion_choice.fields_array,params,DEBUG);
			}else{
				result = Application.getDB().insertNoSync(net.ddp2p.common.table.motion_choice.TNAME, net.ddp2p.common.table.motion_choice.fields_array,params,DEBUG);
			}
			choice_ID = result+"";
		}
		return result;
	}
	public static void save(D_MotionChoice[] choices, String motionID, boolean sync) throws P2PDDSQLException {
		if(choices==null) return;
		for(D_MotionChoice c: choices){
			c.save(motionID, sync);
		}
	}
	public static final String sql_get_choices = 
			"SELECT "+net.ddp2p.common.table.motion_choice.fields +
			" FROM "+net.ddp2p.common.table.motion_choice.TNAME +
			" WHERE "+net.ddp2p.common.table.motion_choice.motion_ID+"=? ORDER BY "+net.ddp2p.common.table.motion_choice.shortName+";";
	public static D_MotionChoice[] getChoices(String motionID) throws P2PDDSQLException {
		D_MotionChoice[] result;
		String[] params=new String[]{motionID};
		ArrayList<ArrayList<Object>> ch = Application.getDB().select(sql_get_choices, params, DEBUG);
		if (ch.size() == 0) return null;
		result = new D_MotionChoice[ch.size()];
		for(int k=0; k<result.length; k++) {
			result[k]=new D_MotionChoice(ch.get(k));
		}
		return result;
	}
	public static D_MotionChoice[] getChoices(String[] val) {
		if(DEBUG) System.out.println("D_MotionChoice: getChoices: in= "+Util.concat(val, ":"));
		D_MotionChoice[] result;
		if(val == null) return null;
		result = new D_MotionChoice[val.length];
		for ( int k=0; k<val.length; k++) {
			result[k] = new D_MotionChoice(val[k], ""+k);
		}
		if(DEBUG) System.out.println("D_MotionChoice: getChoices: out= "+Util.concat(result, ":"));
		return result;
	}
	public static String[] getNames(D_MotionChoice[] val) {
		String[] result;
		if(val == null) return new String[0];
		result = new String[val.length];
		for ( int k=0; k<val.length; k++) {
			result[k] = val[k].name;
		}
		return result;
	}
}