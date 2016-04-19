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
package net.ddp2p.common.streaming;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.Decoder;
import net.ddp2p.ASN1.Encoder;
import net.ddp2p.common.config.Application;
import net.ddp2p.common.config.Application_GUI;
import net.ddp2p.common.config.DD;
import net.ddp2p.common.data.D_Constituent;
import net.ddp2p.common.data.D_Justification;
import net.ddp2p.common.data.D_Motion;
import net.ddp2p.common.data.D_Neighborhood;
import net.ddp2p.common.data.D_News;
import net.ddp2p.common.data.D_Organization;
import net.ddp2p.common.data.D_Peer;
import net.ddp2p.common.data.D_PeerInstance;
import net.ddp2p.common.data.D_PluginData;
import net.ddp2p.common.data.D_Translations;
import net.ddp2p.common.data.D_Vote;
import net.ddp2p.common.data.D_Witness;
import net.ddp2p.common.hds.ASNDatabase;
import net.ddp2p.common.hds.ASNSyncPayload;
import net.ddp2p.common.hds.ASNSyncRequest;
import net.ddp2p.common.hds.ClientSync;
import net.ddp2p.common.hds.ResetOrgInfo;
import net.ddp2p.common.hds.SyncAnswer;
import net.ddp2p.common.hds.Table;
import net.ddp2p.common.plugin_data.D_PluginInfo;
import net.ddp2p.common.util.CommEvent;
import net.ddp2p.common.util.P2PDDSQLException;
import net.ddp2p.common.util.Util;
import static java.lang.System.out;
import static java.lang.System.err;
import static net.ddp2p.common.util.Util.__;
public class UpdateMessages {
	public static boolean DEBUG = false;
	private static final boolean _DEBUG = true;
	private static final int LIMIT_PEERS_LOW = 50;
	private static final int LIMIT_PEERS_MAX = 200;
	private static final int LIMIT_NEWS_LOW = 10;
	private static final int LIMIT_NEWS_MAX = 200;
	private static final int LIMIT_ORG_LOW = 10;
	private static final int LIMIT_ORG_MAX = 300;
	public static byte[] getFieldData(Object obj){
		if (obj == null) return null;
		if (obj instanceof String) return obj.toString().getBytes();
		return obj.toString().getBytes();
	}
	public static SyncAnswer buildAnswer(ASNSyncRequest asr, String peerID) throws P2PDDSQLException {
		if (DEBUG) System.out.println("\n\nUpdateMessages:buildAnswer<top>: pID="+peerID+" asr="+asr);
		SyncAnswer sa=null;
		String[] _maxDate = new String[1];
		HashSet<String> orgs = new HashSet<String>();
		String peer_GID = null;
		if ((peerID == null) && (asr.address != null)) {
			peer_GID = asr.address.component_basic_data.globalID;
			peerID = D_Peer.getLocalPeerIDforGID(peer_GID);
		} else {
		}
		if (asr.address != null) {
			asr.address.setLID(peerID);
		}
		buildAnswer(asr, _maxDate, true, orgs);
		if (_maxDate[0] == null) {
			_maxDate[0] = Util.getGeneralizedTime();
			if (DEBUG) System.out.println("UpdateMessages:buildAnswer<top>: null maxDate->now="+_maxDate[0]);
		}
		if (DEBUG) System.out.println("UpdateMessages:buildAnswer<top>: interm maxDate="+_maxDate[0]);
		sa = buildAnswer(asr, _maxDate, false, orgs);
		if (DEBUG) System.out.println("UpdateMessages:buildAnswer<top>: done maxDate="+_maxDate[0]+" -> "+sa);
		sa.changed_orgs = ClientSync.getChangedOrgs(peerID);
		sa.requested = WB_Messages.getRequestedData(asr,sa);
		if (peerID != null) {
			if (DEBUG || DD.DEBUG_PLUGIN) System.out.println("UpdateMessages: buildAnswer: get plugin Data for peerID="+peerID);
			sa.plugin_data_set = D_PluginData.getPluginMessages(peerID);
		} else {
			if (DEBUG || DD.DEBUG_PLUGIN) System.out.println("UpdateMessages: buildAnswer: skip plugin_data null peerID");			
		}
		sa.plugins = D_PluginInfo.getRegisteredPluginInfo();
		if (sa.upToDate != null) {
			Calendar now = Util.CalendargetInstance();
			if (now.before(sa.upToDate)) {
				if (_DEBUG) out.println("UpdateMessages:buildAnswer<top>: replace data date: "+Encoder.getGeneralizedTime(sa.upToDate)+
						" with: "+Encoder.getGeneralizedTime(now));
				sa.upToDate = now;
			}
		}
		if (DEBUG) System.out.println("UpdateMessages:buildAnswer<top>: exit sa="+sa);
		return sa;
	}
	public static SyncAnswer buildAnswer(ASNSyncRequest asr, String[] _maxDate, boolean justDate, HashSet<String> orgs) throws P2PDDSQLException {
		SyncAnswer sa = new SyncAnswer();
		if(DEBUG) System.out.println("UpdateMessages:buildAnswers: orgs start="+" :"+Util.nullDiscrimArray(orgs.toArray(new String[0])," : "));
		String maxDate =_maxDate[0];
		if(!justDate && (maxDate==null) && (orgs.size()==0)) {
			if(DEBUG) out.println("UpdateMessages:buildAnswer: START-EXIT Nothing new to send!");
			return new SyncAnswer(asr.lastSnapshot, Application.getCurrent_Peer_ID().getPeerGID());
		}
		if(DEBUG) out.println("UpdateMessages:buildAnswer: START: Date at entrance: "+_maxDate[0]+" justDate="+justDate);
		String last_sync_date="00000000000000.000Z";
		if(asr.lastSnapshot!=null) last_sync_date = Encoder.getGeneralizedTime(asr.lastSnapshot);
		try{
			if(DEBUG) out.println("UpdateMessages:buildAnswer: Server Handler has obtained a Sync Request for: "+asr);
			if(DEBUG) out.println("UpdateMessages:buildAnswer: Server building answer for request with last sync date: "+last_sync_date+" from: "+asr.address);
		}catch(Exception e){e.printStackTrace();}
		ArrayList<Table> tableslist = new ArrayList<Table>();
		int tables_nb=0;
		if(asr.tableNames!=null) tables_nb = asr.tableNames.length;
		if(DEBUG) out.println("UpdateMessages:buildAnswer: requestes tables #"+tables_nb);
		for(int k=0; k<tables_nb; k++) {
			if(DEBUG) out.println("UpdateMessages:buildAnswer: handling table ["+k+"] "+asr.tableNames[k]);
			if(net.ddp2p.common.table.peer.G_TNAME.equals(asr.tableNames[k])){
				if(DEBUG) out.println("UpdateMessages:buildAnswer: peers table from date: "+_maxDate[0]);
				Table recentPeers = UpdatePeersTable.buildPeersTable(sa, last_sync_date, _maxDate, justDate, orgs, LIMIT_PEERS_LOW, LIMIT_PEERS_MAX);
				if(DEBUG) System.out.println("UpdateMessages:buildAnswers: orgs after peers="+" :"+Util.nullDiscrimArray(orgs.toArray(new String[0])," : "));
				if(DEBUG) out.println("UpdateMessages:buildAnswer: got peers #"+recentPeers);
				if(justDate){ if(DEBUG) out.println("UpdateMessages:buildAnswer: Date after peers: "+_maxDate[0]); continue;}
				if(recentPeers.rows.length > 0) tableslist.add(recentPeers);
				if(DEBUG) out.println("UpdateMessages:buildAnswer: Date after peers:: "+_maxDate[0]);
			}else{
				if(DEBUG) out.println("UpdateMessages:buildAnswer: non peers table from date: "+_maxDate[0]);
				if(net.ddp2p.common.table.news.G_TNAME.equals(asr.tableNames[k])){
					if(DEBUG) out.println("UpdateMessages:buildAnswer: news table from date: "+_maxDate[0]);
					if(DEBUG) System.out.println("UpdateMessages:buildAnswers: orgs after news="+" :"+Util.nullDiscrimArray(orgs.toArray(new String[0])," : "));
					Table recentNews = UpdateNewsTable.buildNewsTable(last_sync_date, _maxDate, justDate, orgs, LIMIT_NEWS_LOW, LIMIT_NEWS_MAX);
					if(justDate) {if(DEBUG) out.println("UpdateMessages:buildAnswer: Date after news: "+_maxDate[0]); continue;}
					if(recentNews.rows.length > 0) tableslist.add(recentNews);							
					if(DEBUG) out.println("UpdateMessages:buildAnswer: Date after news:: "+_maxDate[0]);
				}else
					if(DEBUG) out.println("UpdateMessages:buildAnswer: Table not served: "+asr.tableNames[k]);
			}
		}
		if (!justDate) {
			orgs.remove(OrgHandling.ORG_PEERS);
			orgs.remove(OrgHandling.ORG_NEWS);
		}
		D_Organization[] orgData = OrgHandling.getOrgData(asr, last_sync_date, _maxDate, justDate, orgs, LIMIT_ORG_LOW, LIMIT_ORG_MAX);
		if(DEBUG) out.println("UpdateMessages:buildAnswer: Date after orgData: "+_maxDate[0]);
		if(justDate) return null;
		if(DEBUG) out.println("UpdateMessages:buildAnswer: Date at end: "+_maxDate[0]);
		if ((orgData != null))
			for (D_Organization dorg : orgData) {
				if ((dorg != null) && (dorg.availableHashes != null)) {
					if (sa.advertised == null) 
						sa.advertised = new SpecificRequest();
					if ((sa.advertised != null) && (sa.advertised.rd != null))						
						sa.advertised.rd.add(dorg.availableHashes);
				}
			}
		sa.orgData = orgData;
		sa.responderGID = Application.getCurrent_Peer_ID().getPeerGID();
		if (tableslist.size() > 0) {
			sa.tables = new ASNDatabase();
			sa.tables.snapshot = Util.getCalendar(_maxDate[0]); 
			sa.tables.tables=tableslist.toArray(new Table[]{});
			if(DEBUG) out.println("UpdateMessages:buildAnswer: Prepared tables: "+sa.tables.tables.length);
		}
		if (_maxDate[0] != null) sa.upToDate = Util.getCalendar(_maxDate[0]);
		else sa.upToDate = asr.lastSnapshot;
		if (DEBUG) out.println("UpdateMessages:buildAnswer: EXIT with Answer: "+sa);
		return sa;
	}
	/**
	 * If peers tables are present, integrate the payloadReceived.responderGID from there.
	 * Also return the cache of payloadReceived.responderGID
	 * @param payloadReceived
	 * @param _received_peer
	 * @return
	 * @throws P2PDDSQLException
	 */
	static long integratePeers(ASNSyncPayload payloadReceived, D_Peer _received_peer) throws P2PDDSQLException {
		long peer_ID = -1;
		if (payloadReceived.tables != null) {
			if (DEBUG) err.println("UpdateMessages:integrateUpdate: tackle peers tables");
			for (int k = 0; k < payloadReceived.tables.tables.length; k ++) {
				if (DEBUG) err.println("Client: Handling table: "+
						payloadReceived.tables.tables[k].name+", rows="+payloadReceived.tables.tables[k].rows.length);
				if (net.ddp2p.common.table.peer.G_TNAME.equals(payloadReceived.tables.tables[k].name)) {
					D_Peer p;
					p = UpdatePeersTable.integratePeersTable(payloadReceived, _received_peer, payloadReceived.tables.tables[k], payloadReceived.responderGID);
					if (p != null) {
						peer_ID = p.getLID();
					}
					continue;
				}
				if (net.ddp2p.common.table.news.G_TNAME.equals(payloadReceived.tables.tables[k].name)) continue;
				if (_DEBUG) err.println("Client: I do not handle table: "+payloadReceived.tables.tables[k].name);
			}
		}
		return peer_ID;
	}
	/**
	 * DD.ACCEPT_STREAMING_ANSWER_FROM_NEW_PEERS : to exit automatically on new peer
	 * 
	 * @param payloadReceived : arriving data
	 * @param s_address : socket of sender
	 * @param src : caller object (for debugging)
	 * @param _global_peer_ID
	 * @param _peer_ID
	 * @param address_ID : peer_address_ID to update as the one from which I got messages
	 * @param __rq
	 * @throws ASN1DecoderFail
	 * @throws P2PDDSQLException
	 */
	public static boolean integrateUpdate(ASNSyncPayload payloadReceived, InetSocketAddress s_address, Object src,
			String _global_peer_ID, D_PeerInstance instance,
			String _peer_ID, String address_ID, RequestData __rq,
			D_Peer _received_peer,
			boolean pulled)
					throws ASN1DecoderFail, P2PDDSQLException
	{ 
		if (DEBUG || DD.DEBUG_PLUGIN) err.println("UpdateMessages:integrateUpdate: start gID="+Util.trimmed(_global_peer_ID));
		D_Peer local_peer = D_Peer.getPeerByGID_or_GIDhash (
				_received_peer.getGID(),
				_received_peer.getGIDH(), 
				true, false, false, null);
		long peer_ID = -1;
		if (local_peer != null) peer_ID = local_peer.getLID(); 
		if (peer_ID <= 0 ) {
			if (DEBUG || DD.DEBUG_TODO)err.println("UpdateMessages:integrateUpdate: peer unknown but may announce self: "+_received_peer);
			if (! DD.ACCEPT_STREAMING_ANSWER_FROM_NEW_PEERS) {
				if (_DEBUG) out.println("UpdateMessages:integrateUpdate: not getting from new peers: "+peer_ID+":"+local_peer);
				return false;
			}
		}
		DD.ed.fireClientUpdate(new CommEvent(src, null, s_address, "Integrating", payloadReceived.toSummaryString()));
		if ((peer_ID <= 0) && (payloadReceived.responderGID != null)) {
			peer_ID = integratePeers(payloadReceived, _received_peer);
			_peer_ID = Util.getStringID(peer_ID);
			if (peer_ID <= 0) {
				if (DEBUG) out.println("UpdateMessages:integrateUpdate: null peerID");
				D_Peer p = D_Peer.getPeerByGID_or_GIDhash(payloadReceived.responderGID, null, false, false, false, null);
				_peer_ID = null;
				if (p != null) {
					if (DEBUG) out.println("UpdateMessages:integrateUpdate: get from peer p:"+p);
					_peer_ID = p.getLIDstr_keep_force();
					peer_ID = p.getLID_keep_force();
					if (peer_ID <= 0) {
						D_Peer __p = D_Peer.getPeerByPeer_Keep(p);
						if (__p != null) {
							peer_ID = __p.storeRequest_getID();
							__p.releaseReference();
						}
						if (DEBUG) out.println("UpdateMessages:integrateUpdate: reget from peer p:"+__p);
					}
				} else {
					if (DEBUG) out.println("UpdateMessages:integrateUpdate: get from null peer p");
					peer_ID = -1; 
				}
				if (peer_ID <= 0) {
					if (DEBUG || DD.DEBUG_TODO) err.println("UpdateMessages:integrateUpdate: peer unknown GID not annouced in message:"+payloadReceived.responderGID);
					if (!DD.ACCEPT_STREAMING_ANSWER_FROM_ANONYMOUS_PEERS) {
						if (_DEBUG) out.println("UpdateMessages:integrateUpdate: not accepting from anonymous");
						return false;
					}
				}
			}
		}
		D_Peer __received_peer = D_Peer.getPeerByPeer_Keep(_received_peer);
		if (__received_peer != null) { __received_peer.releaseReference(); _received_peer = __received_peer;}
		{
			if (payloadReceived.tables != null) {
				if (DEBUG) err.println("UpdateMessages:integrateUpdate: tackle peers tables");
			  for (int k = 0; k < payloadReceived.tables.tables.length; k ++) {
				if (DEBUG) err.println("Client: Handling table: "+
						payloadReceived.tables.tables[k].name+", rows="+payloadReceived.tables.tables[k].rows.length);
				if (net.ddp2p.common.table.peer.G_TNAME.equals(payloadReceived.tables.tables[k].name)) {
						UpdatePeersTable.integratePeersTable(payloadReceived, _received_peer, payloadReceived.tables.tables[k], null); continue;
				}
				if (net.ddp2p.common.table.news.G_TNAME.equals(payloadReceived.tables.tables[k].name)) continue;
				if (_DEBUG) err.println("Client: I do not handle table: "+payloadReceived.tables.tables[k].name);
			  }
			}
		}
		if (payloadReceived.tables != null) {
			if (DEBUG) err.println("UpdateMessages:integrateUpdate: tackle news tables");
		  for (int k = 0; k < payloadReceived.tables.tables.length; k ++) {
			if (DEBUG) err.println("Client: Handling table: "+
					payloadReceived.tables.tables[k].name+", rows="+payloadReceived.tables.tables[k].rows.length);
			if (net.ddp2p.common.table.peer.G_TNAME.equals(payloadReceived.tables.tables[k].name)) continue;
			if (net.ddp2p.common.table.news.G_TNAME.equals(payloadReceived.tables.tables[k].name)) {
				UpdateNewsTable.integrateNewsTable(payloadReceived.tables.tables[k]); continue;
			}
			if (_DEBUG) err.println("Client: I do not handle table: "+payloadReceived.tables.tables[k].name);
		  }
		}
		if (payloadReceived.plugin_data_set!=null){
			if (DEBUG || DD.DEBUG_PLUGIN) System.out.println("\nUpdateMessages: integrateUpdate: will distribute to plugins");
			payloadReceived.plugin_data_set.distributeToPlugins(_global_peer_ID);
		} else {
			if(DEBUG || DD.DEBUG_PLUGIN) System.out.println("\nUpdateMessages: integrateUpdate: nothing for plugins");
		}
		boolean future_requests = false;
		if (payloadReceived.changed_orgs != null) { 
			if (DEBUG || DD.DEBUG_CHANGED_ORGS) err.println("UpdateMessages:integrateUpdate: changed_orgs="+Util.nullDiscrimArray(payloadReceived.changed_orgs.toArray(new ResetOrgInfo[0]),"--"));
			handleChangedOrgs(payloadReceived, _peer_ID);
		} else {
			if (DEBUG || DD.DEBUG_CHANGED_ORGS) err.println("UpdateMessages:integrateUpdate: changed_orgs=null");
		}
		if (instance != null)
			D_PluginInfo.recordPluginInfo(instance.peer_instance, payloadReceived.plugins, _global_peer_ID, _peer_ID);
		else
			D_PluginInfo.recordPluginInfo(null, payloadReceived.plugins, _global_peer_ID, _peer_ID);
		Calendar snapshot_date = payloadReceived.upToDate;
		HashSet<String> orgs = new HashSet<String>();
		Hashtable<String, RequestData> obtained_sr = new Hashtable<String, RequestData>();
		Hashtable<String, RequestData> missing_sr = new Hashtable<String, RequestData>();
		if (DEBUG || DD.DEBUG_CHANGED_ORGS) err.println("UpdateMessages: integrateUpdate: srs: obtained="+obtained_sr.size()+" - missing="+missing_sr.size());
		String crt_date = Util.getGeneralizedTime();
		WB_Messages.store(payloadReceived, _received_peer, payloadReceived.requested, missing_sr, obtained_sr, orgs, " from:"+peer_ID+" ");
		if (DEBUG || DD.DEBUG_CHANGED_ORGS || DD.DEBUG_TMP_GIDH_MANAGEMENT) err.println("UpdateMessages: integrateUpdate: srs:"
				+ "\n obtained=#["+obtained_sr.size()+"]"+Util.concat(obtained_sr, ":")+" -"
				+ "\n missing=#["+missing_sr.size()+"]"+Util.concat(missing_sr, ":"));
		integrate_GIDs_accounting(missing_sr, obtained_sr, _global_peer_ID, peer_ID, _received_peer);
		Hashtable<String, String> missing_peers = new Hashtable<String, String>();
		if (payloadReceived.orgData != null) {
			if (DEBUG) err.println("UpdateMessages:integrateUpdate: tackle org");
			String[] _orgID = new String[1];
			Calendar arrival__time = Util.CalendargetInstance();
			String arrival_time;
			for (int i = 0; i < payloadReceived.orgData.length; i ++) {
				if (DEBUG) out.println("Will integrate ["+i+"]: "+payloadReceived.orgData[i]);
				if (payloadReceived.orgData[i] == null) continue;
				String org_gidh = payloadReceived.orgData[i].getGIDH_or_guess();
				String org_gid  = payloadReceived.orgData[i].getGID();
				boolean b = D_Organization.verifyGIDhash(org_gid, org_gidh, false);
				if (! b || (org_gidh == null)) {
					System.err.println("UpdateMessages: integrateUpdate: wrong pair of GIDs: "+ b+" gID="+
							org_gid+" GIDH="+ org_gidh + " from peer="+ _received_peer);
					continue;
				}
				if (DEBUG) err.println("UpdateMessages:integrateUpdate: org_gidh: "+org_gidh);
				D_Organization org = 
						D_Organization.getOrgByGID_or_GIDhash_NoCreate(null, org_gidh, true, false);
				payloadReceived.orgData[i].getLocalIDfromGIDandBlock(); 
				OrgPeerDataHashes opdh = null;
				if (org != null) 
					opdh = org.getSpecificRequest(); 
				else if (DEBUG) System.out.println("UpdateMessages: integrateUpdates: org["+i+"] new");
				/**
				 * From the following set of hashes we remove what we received now fully
				 *  
				 */
				RequestData old_rq;
				if (opdh != null)
					old_rq = opdh.getRequestData();
				else
					old_rq = new RequestData();
				/**
				 * Here we add what is newly advertised and needed
				 */
				RequestData _new_rq = new RequestData();
				/**
				 * Here we add what is newly fully received (solved rq)
				 */
				RequestData _sol_rq = new RequestData();
				arrival_time = Encoder.getGeneralizedTime(Util.incCalendar(arrival__time, 1));
				OrgHandling.updateOrg(payloadReceived, payloadReceived.orgData[i], _orgID, arrival_time, _sol_rq, _new_rq, _received_peer); 
				org = D_Organization.getOrgByGID_or_GIDhash_NoCreate(null, org_gidh, true, false);
				for (String p : _new_rq.peers.keySet()) {
					if (! missing_peers.containsKey(p)) missing_peers.put(p, _new_rq.peers.get(p));
				}
				String fdate = payloadReceived.orgData[i]._last_sync_date;
				if ((_orgID[0] != null) && (_peer_ID != null)) {
					if (DEBUG) out.println("UpdateMessages:integrateUpdate: peer update lastsd");
					_received_peer.updateLastSyncDate(fdate, _orgID[0]);
				}
				if ((peer_ID <= 0) || org == null) {
					if (_DEBUG) out.println("UpdateMessages: integrateUpdate: NOT SAVING maybe blocked ["+i+"]: "+payloadReceived.orgData[i]+" from "+peer_ID);
					continue;
				}
				if (! payloadReceived.orgData[i].blocked) {
					if (DEBUG) out.println("UpdateMessages:integrateUpdate: org not blocked");
					if (org != null) {
						opdh = org.getSpecificRequest();
						if (opdh != null) {
							if (DEBUG || DD.DEBUG_TMP_GIDH_MANAGEMENT) out.println("UpdateMessages: integrateUpdate: initial specReq: "+opdh);
							opdh.updateAfterChanges(old_rq, _sol_rq, _new_rq, peer_ID, crt_date);
							if (!opdh.empty()) future_requests = true;
							opdh.save(org.getLID(), peer_ID, _received_peer);
							if (DEBUG || DD.DEBUG_TMP_GIDH_MANAGEMENT) out.println("UpdateMessages: integrateUpdate: final specReq: "+opdh);
						}
					}
				} else {
					if(_DEBUG) out.println("UpdateMessages: integrateUpdate: blocked ["+i+"]: "+payloadReceived.orgData[i]);					
				}
			}
		}
		integrate_peer_GIDs_accounting(missing_peers, _global_peer_ID, peer_ID, _received_peer, Util.getGeneralizedTime());
		_received_peer = D_Peer.getPeerByPeer_Keep(_received_peer);
		SpecificRequest sp = new SpecificRequest();
		evaluate_interest(payloadReceived.advertised, sp); 
		if (_DEBUG) out.println("UpdateMessages:integrateUpdate: peer="+_received_peer+"\n sp="+sp);
		if (store_detected_interests(sp, peer_ID, crt_date, _received_peer)) {
			if (DEBUG) out.println("UpdateMessages:integrateUpdate: have new future requests");
			future_requests = true;
		}
		String gdate = Encoder.getGeneralizedTime(snapshot_date);
		try {
			if (instance != null && pulled) {
				if (DEBUG) System.out.println("UpdateMessages: integrateUpdate: gdate="+gdate+" inst=["+instance+"]");
				_received_peer.setLastSyncDate_dirty(instance, gdate, false);
			} else {
				if (DEBUG) System.out.println("UpdateMessages: integrateUpdate: gdate="+gdate+" null inst=["+instance+"]");		
			}
			Calendar crtDate = Util.CalendargetInstance();
			String _crtDate = Encoder.getGeneralizedTime(crtDate);
			if (address_ID != null) {
				if (DEBUG) out.println("UpdateMessages:integrateUpdate: update conn: "+address_ID);
				_received_peer.updateAddress_LastConnection(_crtDate, address_ID);
			} else {
				if (DEBUG) out.println("UpdateMessages:integrateUpdate: update conn dir= "+s_address);
				_received_peer.updateAddress_LastConnection(_crtDate, s_address);
			}
			if (instance != null) {
				_received_peer.updateAddress_LastConnection_Instance(crtDate, instance.get_peer_instance());
			}
			if (_received_peer.dirty_any()) _received_peer.storeRequest();
		} catch (Exception e){e.printStackTrace();}
		_received_peer.releaseReference();
		if (DEBUG) out.println("UpdateMessages:integrateUpdate: done");
		return future_requests;
	}
	private static void handleChangedOrgs(ASNSyncPayload asa, String _peer_ID) throws P2PDDSQLException {
		Calendar reset = null;
		/**
		 * Here we take the earlier among the reset dates for the orgs of this peer
		 */
		for (ResetOrgInfo roi: asa.changed_orgs) {
			if (reset == null) {
				reset = roi.reset_date;
			}
			if (reset.before(roi.reset_date)){
				reset = roi.reset_date;
			}
		}
		if (reset != null) {
			String old = D_Peer.getLastResetDate(_peer_ID, asa.get_peer_instance());
			if (DEBUG || DD.DEBUG_CHANGED_ORGS) err.println("UpdateMessages:integrateUpdate: changed_orgs: old reset="+old);
			Calendar _old = null;
			if (old != null) 
				_old = Util.getCalendar(old);
			if ((_old == null) || _old.before(reset)) {
				D_Peer.reset(_peer_ID, asa.get_peer_instance(), reset);
				if (DEBUG || DD.DEBUG_CHANGED_ORGS) err.println("UpdateMessages:integrateUpdate: changed_orgs: called reset="+old+" new="+Encoder.getGeneralizedTime(reset));
			}
		} else {
			if (DEBUG || DD.DEBUG_CHANGED_ORGS) err.println("UpdateMessages:integrateUpdate: changed_orgs: called reset=null");
		}
	}
	/**
	 * return true if not empty
	 * @param sp
	 * @param _peer_ID
	 * @param generalizedTime
	 * @return
	 * @throws P2PDDSQLException
	 */
	private static boolean store_detected_interests(SpecificRequest sp,
			long _peer_ID, String generalizedTime, D_Peer peer) throws P2PDDSQLException {
		boolean result = false;
		if (DEBUG) out.println("UpdateMessages: store_detected_interests: start");
		for (RequestData rq: sp.rd) {
			String org = rq.global_organization_ID_hash;
			long orgID = Util.lval(D_Organization.getLocalOrgID_fromHashIfNotBlocked(org), -1);
			if (orgID <= 0) {
				if (_DEBUG) out.println("UpdateMessages:store_detected_interests: blocked="+org);
				continue;
			}
			if (DEBUG) out.println("UpdateMessages:store_detected_interests: not blocked="+rq);
			OrgPeerDataHashes old = OrgPeerDataHashes.get(orgID);
			if (old != null) {
				if (DEBUG || DD.DEBUG_TMP_GIDH_MANAGEMENT) out.println("UpdateMessages: store_detected_interests: start stored old ["+orgID+"]->"+old);
				old.add(rq, _peer_ID, generalizedTime);
				if ( ! rq.empty()) result |= true;
				if (_DEBUG) out.println("UpdateMessages: store_detected_interests: save orgID="+orgID + " peer=" + peer);
				old.save(orgID, _peer_ID, peer);
				if (DEBUG || DD.DEBUG_TMP_GIDH_MANAGEMENT) out.println("UpdateMessages: store_detected_interests: final stored got ["+orgID+"]->"+old);
			} else {
				if (_DEBUG) out.println("UpdateMessages:store_detected_interests: not storing interests for orgid ="+orgID);
			}
		}
		GlobalClaimedDataHashes old = GlobalClaimedDataHashes.get();
		old.add(sp, _peer_ID, generalizedTime);
		if ( ! sp.empty()) result |= true;
		old.save(); 
		return result;
	}
	/**
	 *  // check existing/non-blocked data and insert wished one into sp, store sp in orgs
	 * @param advertised
	 * @param sp
	 * @throws P2PDDSQLException 
	 */
	private static void evaluate_interest(SpecificRequest advertised, SpecificRequest sp) throws P2PDDSQLException {
		if (DEBUG) System.out.println("UpdateMessages: evaluate_interests: enter");
		if (advertised == null) return;
		if (advertised.rd != null) {
			for (RequestData rq : advertised.rd) {
				if (DEBUG) System.out.println("UpdateMessages: evaluate_interests: consider "+rq);
				RequestData sp_rq = new RequestData();
				sp_rq.global_organization_ID_hash = rq.global_organization_ID_hash;
				evaluate_interest(rq, sp_rq);
				if (DEBUG) System.out.println("UpdateMessages: evaluate_interests: retain "+sp_rq);
				if (! sp_rq.empty()) {
					sp.rd.add(sp_rq);
				}
			}
		}
		if (DEBUG) System.out.println("UpdateMessages: evaluate_interests: got interest: "+sp);
		evaluate_interest_global(advertised, sp);			
		if (DEBUG) System.out.println("UpdateMessages: evaluate_interests: got interest gb: "+sp);
	}
	private static void evaluate_interest_global(SpecificRequest advertised, SpecificRequest sp_rq) throws P2PDDSQLException {
		sp_rq.peers = D_Peer.checkAvailability(advertised.peers, DEBUG);
		sp_rq.news = D_News.checkAvailability(advertised.news, null, DEBUG);
		sp_rq.tran = D_Translations.checkAvailability(advertised.tran, null, DEBUG);
	}
	/**
	 * // check existing/non-blocked data and insert wished one into sp, store sp in orgs
	 * @param rq
	 * @param sp_rq
	 * @throws P2PDDSQLException 
	 */
	private static void evaluate_interest(RequestData advertised, RequestData sp_rq) throws P2PDDSQLException {
		String orgHash = advertised.global_organization_ID_hash;
		String orgID = D_Organization.getLocalOrgID_fromHashIfNotBlocked(orgHash);
		if (orgID == null) {
			if(_DEBUG) System.out.println("UpdateMessages: evaluate_interest: failure no local unblocked orgID for "
					+orgHash+" have blocked:"+D_Organization.getOrgByGID_or_GIDhash_NoCreate(null, orgHash, true, false));
			return;
		}
		sp_rq.cons = D_Constituent.checkAvailability(advertised.cons, orgID, DEBUG);
		sp_rq.witn = D_Witness.checkAvailability(advertised.witn, orgID, DEBUG);
		sp_rq.neig = D_Neighborhood.checkAvailability(advertised.neig, orgID, DEBUG);
		sp_rq.moti = D_Motion.checkAvailability(advertised.moti, orgID, DEBUG);
		sp_rq.just = D_Justification.checkAvailability(advertised.just, orgID, null, DEBUG);
		sp_rq.sign = D_Vote.checkAvailability(advertised.sign, orgID, DEBUG); 
		sp_rq.news = D_News.checkAvailability(advertised.news, orgID, DEBUG);
		sp_rq.tran = D_Translations.checkAvailability(advertised.tran, orgID, DEBUG);
	}
	private static void integrate_peer_GIDs_accounting(Hashtable<String, String> missing_peers, String _global_peer_ID, long _peer_ID, D_Peer peer,
			String date) throws P2PDDSQLException {
		GlobalClaimedDataHashes _opdh = GlobalClaimedDataHashes.get(); 
		_opdh.addPeers(missing_peers, _peer_ID, date);
		_opdh.save();
	}
	/**
	 * For these orgs in keys, purge obtained from their store requests
	 * @param missing_sr   : new unknown detected
	 * @param obtained_sr :  obtained entities
	 * @param _global_peer_ID : not used now, but eventually may be saved to know from where we have learned and where we should ask a dependency
	 * @param _peer_ID
	 * @throws P2PDDSQLException
	 */
	private static void integrate_GIDs_accounting(Hashtable<String, RequestData> missing_sr,
			Hashtable<String, RequestData> obtained_sr, String _global_peer_ID, long _peer_ID, D_Peer peer) throws P2PDDSQLException {
		Set<String> toreq = missing_sr.keySet();
		Set<String> got = obtained_sr.keySet();
		Set<String> orgs =	new HashSet<String>();
		orgs.addAll(toreq);
		orgs.addAll(got);
		String date = Util.getGeneralizedTime();
		if (DEBUG) System.out.println("Updateessages: integrate_GIDs_accounting: #"+orgs.size());
		for (String o : orgs) {
			if (WB_Messages.PEER_POOL.equals(o)) {
				if (DEBUG) System.out.println("Updateessages: integrate_GIDs_accounting: "+o);
				GlobalClaimedDataHashes _opdh = GlobalClaimedDataHashes.get(); 
				_opdh.addPeers(missing_sr.get(o).peers, _peer_ID, date);
				_opdh.purge(obtained_sr.get(o));
				_opdh.save();
				if (_DEBUG) System.out.println("Updateessages: integrate_GIDs_accounting: done peers:"+_opdh);
				continue;
			}
			if (WB_Messages.NEWS_POOL.equals(o)) {
				GlobalClaimedDataHashes _opdh = GlobalClaimedDataHashes.get(); 
				_opdh.addNews(missing_sr.get(o).news, _peer_ID, date);
				_opdh.purge(obtained_sr.get(o));
				_opdh.save();
				continue;
			}
			if (WB_Messages.TRAN_POOL.equals(o)) {
				GlobalClaimedDataHashes _opdh = GlobalClaimedDataHashes.get(); 
				_opdh.addTran(missing_sr.get(o).tran, _peer_ID, date);
				_opdh.purge(obtained_sr.get(o));
				_opdh.save();
				continue;
			}
			D_Organization org = D_Organization.getOrgByGID_or_GIDhash(o, o, true, false, false, null);
			if (org == null) {
				Util.printCallPath("UpdateMessages: integrate_GIDs_accounting: unknown org: \""+o+"\"");
				System.out.println("UpdateMessages: integrate_GIDs_accounting: missing: "+ missing_sr.get(o));
				System.out.println("UpdateMessages: integrate_GIDs_accounting: obtained: "+ obtained_sr.get(o));
				continue;
			}
			long oID = org.getLID(); 
			if (oID <= 0) {
				Util.printCallPath("UpdateMessages: integrate_GIDs_accounting: unknown org: \""+o+"\"");
				continue;
			}
			OrgPeerDataHashes _opdh = org.getSpecificRequest(); 
			if (DEBUG || DD.DEBUG_TMP_GIDH_MANAGEMENT) System.out.println("UpdateMessages: integrate_GIDs_accounting: start: "+ _opdh);
			_opdh.add(missing_sr.get(o), _peer_ID, date);
			_opdh.purge(obtained_sr.get(o));
			_opdh.save(oID, _peer_ID, peer);
			if (DEBUG || DD.DEBUG_TMP_GIDH_MANAGEMENT) System.out.println("UpdateMessages: integrate_GIDs_accounting: final: "+ _opdh);
		}
	}
	/**
	 * For these orgs, purge obtained from their store requests
	 * @param orgs
	 * @param obtained
	 * @throws P2PDDSQLException
	 */
	static public long get_news_ID(String global_news_ID, long constituentID, long organizationID, String date, String news, String type, String signature) throws P2PDDSQLException {
		long result=0;
		ArrayList<ArrayList<Object>> dt=Application.getDB().select("SELECT "+net.ddp2p.common.table.news.news_ID+" FROM "+net.ddp2p.common.table.news.TNAME+" WHERE "+net.ddp2p.common.table.news.global_news_ID+" = ?",
				new String[]{global_news_ID});
		if((dt.size()>=1) && (dt.get(0).size()>=1)) {
			result = Long.parseLong(dt.get(0).get(0).toString());
			return result;
		}
		result=Application.getDB().insert(net.ddp2p.common.table.news.TNAME,new String[]{net.ddp2p.common.table.news.constituent_ID,net.ddp2p.common.table.news.organization_ID,net.ddp2p.common.table.news.creation_date,net.ddp2p.common.table.news.news,net.ddp2p.common.table.news.type,net.ddp2p.common.table.news.signature,net.ddp2p.common.table.news.global_news_ID},
				new String[]{constituentID+"", organizationID+"", date, news, type, signature, global_news_ID});
		return result;
	}
	/**
	 * Get ID and/or insert temporary org entry
	 * @param global_organizationID
	 * @param org_name
	 * @param adding_date
	 * @param orgHash
	 * @return
	 * @throws P2PDDSQLException
	 */
	public static long get_organizationID(String global_organizationID, String org_name, String adding_date, String orgHash) throws P2PDDSQLException {
		long result=0;
		if(DEBUG) System.out.println("\n************\nUpdateMessages:getonly_organizationID':  start orgID_hash= = "+Util.trimmed(global_organizationID));		
		if(global_organizationID==null) return -1;
		String sql = "SELECT "+net.ddp2p.common.table.organization.organization_ID+", "+net.ddp2p.common.table.organization.name
			+" FROM "+net.ddp2p.common.table.organization.TNAME
			+" WHERE "+net.ddp2p.common.table.organization.global_organization_ID+" = ?";
		String sql_hash = "SELECT "+net.ddp2p.common.table.organization.organization_ID+", "+net.ddp2p.common.table.organization.name
			+" FROM "+net.ddp2p.common.table.organization.TNAME
			+" WHERE "+net.ddp2p.common.table.organization.global_organization_ID_hash+" = ?";
		ArrayList<ArrayList<Object>> dt;
		if(orgHash!=null) dt=Application.getDB().select(sql_hash, new String[]{orgHash}, DEBUG);
		else dt=Application.getDB().select(sql, new String[]{global_organizationID}, DEBUG);
		if((dt.size()>=1) && (dt.get(0).size()>=1)) {
			result = Long.parseLong(dt.get(0).get(0).toString());
			String oname = (String)dt.get(0).get(1);
			if(!Util.equalStrings_null_or_not(oname,org_name)) Application_GUI.warning(String.format(__("Old name for org: %1$s new name is: %2$s"),oname,org_name), __("Inconsistency"));
		}else
			result=Application.getDB().insert(net.ddp2p.common.table.organization.TNAME,
					new String[]{net.ddp2p.common.table.organization.name,net.ddp2p.common.table.organization.global_organization_ID,net.ddp2p.common.table.organization.global_organization_ID_hash},
				new String[]{org_name, global_organizationID, orgHash}, DEBUG);
		if(DEBUG) System.out.println("UpdateMessages:get_organizationID':  exit result = "+result);		
		if(DEBUG) System.out.println("****************");		
		return result;
	}
	public static OrgFilter[] getOrgFilter(String peer_ID){
		OrgFilter[] orgFilter=null;
		ArrayList<ArrayList<Object>> peers_orgs = null;
		try{
			peers_orgs = Application.getDB().select("SELECT o."+net.ddp2p.common.table.organization.global_organization_ID+", o."+net.ddp2p.common.table.organization.global_organization_ID_hash+", p."+net.ddp2p.common.table.peer_org.last_sync_date +
					" FROM " + net.ddp2p.common.table.peer_org.TNAME + " AS p " +
					" LEFT JOIN "+net.ddp2p.common.table.organization.TNAME+" AS o ON (p."+net.ddp2p.common.table.peer_org.organization_ID+"=o."+net.ddp2p.common.table.organization.organization_ID+")" +
					" WHERE p."+net.ddp2p.common.table.peer_org.served+"=1 AND p."+net.ddp2p.common.table.peer_org.peer_ID+" = ? " +
					" AND o."+ net.ddp2p.common.table.organization.requested  +"= '1'"+
							" ORDER BY p."+net.ddp2p.common.table.peer_org.last_sync_date+" ASC;",
					new String[]{peer_ID});
		} catch (P2PDDSQLException e1) {
				Application_GUI.warning(__("Database: ")+e1, __("Database"));
				return null;
		}
		orgFilter = new OrgFilter[peers_orgs.size()];
		for(int of=0; of<peers_orgs.size(); of++) {
				OrgFilter f=orgFilter[of]=new OrgFilter();
				f.orgGID = Util.getString(peers_orgs.get(of).get(0));
				f.orgGID_hash = Util.getString(peers_orgs.get(of).get(1));
				f.setGT(Util.getString(peers_orgs.get(of).get(2)));
		}
		return orgFilter;
	}
}
