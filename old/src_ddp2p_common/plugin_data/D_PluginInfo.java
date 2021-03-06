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

package plugin_data;

import hds.ASNPluginInfo;

import java.util.ArrayList;
import java.util.Hashtable;

import util.P2PDDSQLException;
import config.Application;
import config.DD;
import data.ASN64String_2_ASNPluginInfoArray;
import data.D_Peer;
import data.D_PeerInstance;
import ASN1.ASN1DecoderFail;
import util.Util;

public
class D_PluginInfo{
	private static final boolean DEBUG = false;
	public static ArrayList<String> plugins= new ArrayList<String>();
	public static Hashtable<String, D_PluginInfo> plugin_data = new Hashtable<String, D_PluginInfo>();

	/** javax.swing.table.TableCellEditor */
	public Object mTableCellEditor; 
	/** javax.swing.table.TableCellRenderer */
	public Object mTableCellRenderer; 
	public String plugin_name;
	public String plugin_info;
	public String plugin_GID;
	public String plugin_url;
	//public PeerPlugin plugin;
	/**
	 * Used only to unpack data coming from Plugin, internally. remote communication is based on ASNPluginInfo
	 */
	public D_PluginInfo(){}
	/**
	 * Used only to unpack data coming from Plugin, internally. remote communication is based on ASNPluginInfo
	 * @param plugin_GID
	 * @param plugin_name
	 * @param plugin_info
	 * @param plugin_url
	 * @param editor
	 * @param renderer
	 */
	public D_PluginInfo(
			String plugin_GID,
			String plugin_name,
			String plugin_info,
			String plugin_url,
			Object editor, // TableCellEditor
			Object renderer // TableCellRenderer
			//plugin_data.PeerPlugin plugin
			){
		this.mTableCellEditor = editor;
		this.mTableCellRenderer = renderer;
		this.plugin_GID = plugin_GID;
		this.plugin_name = plugin_name;
		this.plugin_info = plugin_info;
		this.plugin_url = plugin_url;
		//this.plugin = plugin;
	}
	public String toString() {
		return "D_PluginInfo: (name="+plugin_name+", info="+Util.trimmed(plugin_info)+", url="+plugin_url+", GID="+Util.trimmed(plugin_GID)+") ";
	}
	public D_PluginInfo instance() {return new D_PluginInfo();}
	public static D_PluginInfo getPluginInfo(String gid){
		if(D_PluginInfo.plugin_data == null) return null;
		D_PluginInfo i = D_PluginInfo.plugin_data.get(gid);
		return i;
	}
	
	public static ASNPluginInfo[] getRegisteredPluginInfo(){
		if(plugin_data.size()==0) return null;
		ArrayList<ASNPluginInfo> api = new ArrayList<ASNPluginInfo>();
		for(D_PluginInfo pd : plugin_data.values()) {
			api.add(new ASNPluginInfo(pd.plugin_GID,pd.plugin_name,pd.plugin_info,pd.plugin_url));
		}
		return api.toArray(new ASNPluginInfo[0]);
	}

	public static void recordPluginInfo(String peer_instance, ASNPluginInfo[] plugins,
			String _global_peer_ID, String _peer_ID) throws P2PDDSQLException {
		//boolean DEBUG = true;
		if(DEBUG || DD.DEBUG_PLUGIN) System.out.println("\nD_PluginInfo: recordPluginInfo: start from peer_ID="+_peer_ID+" gid="+Util.trimmed(_global_peer_ID));
		if(plugins==null) {
			if(DEBUG || DD.DEBUG_PLUGIN) System.out.println("\nD_PluginInfo: recordPluginInfo: no plugin info received");
			return;
		}
		if(_peer_ID==null) {
			if(DEBUG || DD.DEBUG_PLUGIN) System.out.println("\nD_PluginInfo: recordPluginInfo: no plugin remote peer info received");
			return;
		}
		if (Application.peers!=null) Application.peers.setPluginsInfo(plugins, _global_peer_ID, _peer_ID);
		/*
		Application.db.update(
				table.peer.TNAME,
				new String[]{table.peer.plugin_info},
				new String[]{table.peer.peer_ID},
				new String[]{info, _peer_ID},
				DEBUG || DD.DEBUG_PLUGIN);
		*/
		D_Peer peer = D_Peer.getPeerByLID(Util.lval(_peer_ID), true, true);
		if (peer != null) {
			peer.setPluginInfo(peer_instance, plugins);
			if (peer.dirty_any())
				peer.storeRequest();
			peer.releaseReference();
		}
	}
	public Hashtable<String,Object> getHashtable() {
		Hashtable<String,Object> pd = new Hashtable<String,Object>();
		pd.put("plugin_GID", plugin_GID);
		pd.put("plugin_name", plugin_name);
		pd.put("plugin_info", plugin_info);
		pd.put("plugin_url", plugin_url);
		pd.put("editor",mTableCellEditor);
		pd.put("renderer",mTableCellRenderer);
		return pd;		
	}
	public D_PluginInfo setHashtable(Hashtable<String,Object> pd) {
		plugin_GID = (String)pd.get("plugin_GID");
		plugin_name = (String)pd.get("plugin_name");
		plugin_info = (String)pd.get("plugin_info");
		plugin_url = (String)pd.get("plugin_url");
		mTableCellEditor = (Object)pd.get("editor");  // TableCellEditor
		mTableCellRenderer = (Object)pd.get("renderer"); // TableCellRenderer
		return this;
	}
	public static boolean contains(ASNPluginInfo[] info, String pluginID) {
		if((info == null)||(pluginID == null)) return false;
		for(ASNPluginInfo i : info) {
			if(!pluginID.equals(i.gid)) continue;
			return true;
		}
		return false;
	}
	public static ASNPluginInfo[] getPluginInfoArray(String _plugin_info) {
		if (_plugin_info == null) return null;
		try {
			ASNPluginInfo[] _info = new ASN64String_2_ASNPluginInfoArray(_plugin_info).get_PluginInfo();
			//new Decoder(Util.byteSignatureFromString(_plugin_info)).getSequenceOf(ASNPluginInfo.getASN1Type(), new ASNPluginInfo[0], new ASNPluginInfo());
			return _info;
		} catch (ASN1DecoderFail e) {
			e.printStackTrace();
		}
		return null;
	}
	public static String getPluginInfoFromArray(ASNPluginInfo[] _plugin_info) {
		return new ASN64String_2_ASNPluginInfoArray(_plugin_info).get_PluginInfoString();
		//Util.stringSignatureFromByte(Encoder.getEncoder(_plugin_info).getBytes());
	}
}