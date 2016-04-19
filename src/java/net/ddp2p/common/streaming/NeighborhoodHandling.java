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
import static java.lang.System.out;
import java.util.ArrayList;
import java.util.HashSet;
import net.ddp2p.ASN1.Encoder;
import net.ddp2p.common.config.Application;
import net.ddp2p.common.config.DD;
import net.ddp2p.common.data.ASNNeighborhoodOP;
import net.ddp2p.common.data.D_Constituent;
import net.ddp2p.common.data.D_Neighborhood;
import net.ddp2p.common.data.D_Organization;
import net.ddp2p.common.data.D_Peer;
import net.ddp2p.common.hds.ASNSyncRequest;
import net.ddp2p.common.util.P2PDDSQLException;
import net.ddp2p.common.util.Util;
public class NeighborhoodHandling {
	static String sql_max=
		"SELECT n."+net.ddp2p.common.table.neighborhood.arrival_date+
		", o."+net.ddp2p.common.table.organization.global_organization_ID +", n."+net.ddp2p.common.table.neighborhood.organization_ID +
		" FROM "+net.ddp2p.common.table.neighborhood.TNAME +" AS n "+
		" LEFT JOIN  "+net.ddp2p.common.table.organization.TNAME+" as o ON (o."+net.ddp2p.common.table.organization.organization_ID+" = n."+net.ddp2p.common.table.neighborhood.organization_ID+")  "+
		" WHERE n."+net.ddp2p.common.table.neighborhood.signature+" IS NOT NULL " +
		 " AND o."+net.ddp2p.common.table.organization.broadcasted+" <> '0' " +
				" AND n."
		+net.ddp2p.common.table.neighborhood.arrival_date+">? AND n."+net.ddp2p.common.table.neighborhood.arrival_date+"<=?" +
		" ORDER BY n."+net.ddp2p.common.table.neighborhood.arrival_date;
	static String sql_nomax=
		"SELECT n."+net.ddp2p.common.table.neighborhood.arrival_date+
		", o."+net.ddp2p.common.table.organization.global_organization_ID +", n."+net.ddp2p.common.table.neighborhood.organization_ID +
		" FROM "+net.ddp2p.common.table.neighborhood.TNAME+" AS n "+
		" LEFT JOIN  "+net.ddp2p.common.table.organization.TNAME+" as o ON (o."+net.ddp2p.common.table.organization.organization_ID+" = n."+net.ddp2p.common.table.neighborhood.organization_ID+")  "+
		" WHERE n."+net.ddp2p.common.table.neighborhood.signature+" IS NOT NULL " +
		 " AND o."+net.ddp2p.common.table.organization.broadcasted+" <> '0' " +
				" AND n."
		+net.ddp2p.common.table.neighborhood.arrival_date+">?" +
		" ORDER BY n."+net.ddp2p.common.table.neighborhood.arrival_date;
	public static boolean DEBUG = false;
	private static final boolean _DEBUG = true;
	public static String getNextNeighborhoodDate(String last_sync_date,
			String _maxDate, OrgFilter ofi, HashSet<String> orgs, int limitNeighLow) throws P2PDDSQLException {
		if(DEBUG) out.println("OrgHandling:getNextNeighborhoodDate: start: between: "+last_sync_date+" : "+_maxDate);
		String sql = (_maxDate==null)?sql_nomax:sql_max;
		String[] param=(_maxDate==null)?new String[]{last_sync_date}:new String[]{last_sync_date, _maxDate};
		ArrayList<ArrayList<Object>> n = Application.getDB().select(sql+" ASC LIMIT "+(1+limitNeighLow), param, DEBUG);
		for (ArrayList<Object> ae : n) {
			orgs.add(Util.getString(ae.get(2)));
		}
		if(n.size() <= limitNeighLow) return _maxDate;
		_maxDate = Util.getString(n.get(n.size()-1).get(0));
		if(DD.WARN_BROADCAST_LIMITS_REACHED || DEBUG) System.out.println("NeighborhoodHandling: getNeighborhoodOPsDate: limits reached: "+limitNeighLow+" date="+_maxDate);
		return _maxDate;
	}
	static String sql_get_hashes=
		"SELECT n."+net.ddp2p.common.table.neighborhood.global_neighborhood_ID+
		" FROM "+net.ddp2p.common.table.neighborhood.TNAME+" AS n "+
		" JOIN "+net.ddp2p.common.table.constituent.TNAME+" AS c ON(n."+net.ddp2p.common.table.neighborhood.submitter_ID+"=c."+net.ddp2p.common.table.constituent.constituent_ID+") "+
		" WHERE " +
		" n."+net.ddp2p.common.table.neighborhood.organization_ID+"=? "+ 
		" AND n."+net.ddp2p.common.table.neighborhood.organization_ID+"= c."+net.ddp2p.common.table.constituent.organization_ID 
		+" AND n."+net.ddp2p.common.table.neighborhood.arrival_date+">? "
		+" AND n."+net.ddp2p.common.table.neighborhood.arrival_date+"<=? "
		+" AND c."+net.ddp2p.common.table.constituent.broadcasted+" <> '0' "
		+" AND n."+net.ddp2p.common.table.neighborhood.signature+" IS NOT NULL "
		+" AND n."+net.ddp2p.common.table.neighborhood.global_neighborhood_ID+" IS NOT NULL "
		+" ORDER BY n."+net.ddp2p.common.table.neighborhood.arrival_date
		;
	public static ArrayList<String> getNeighborhoodHashes(String last_sync_date, String org_id, String[] _maxDate, int BIG_LIMIT) throws P2PDDSQLException {
		String maxDate;
		if((_maxDate==null)||(_maxDate.length<1)||(_maxDate[0]==null)) maxDate = Util.getGeneralizedTime();
		else{ maxDate = _maxDate[0]; if((_maxDate!=null)&&(_maxDate.length>0)) _maxDate[0] = maxDate;}
		ArrayList<ArrayList<Object>> result = Application.getDB().select(sql_get_hashes+" LIMIT "+BIG_LIMIT+";",
				new String[]{org_id, last_sync_date, maxDate}, DEBUG);
		return Util.AL_AL_O_2_AL_S(result);
	}
	static String sql_neigh = "SELECT  n."+net.ddp2p.common.table.neighborhood.neighborhood_ID +//Util.setDatabaseAlias(table.neighborhood.fields_neighborhoods, "n")+
		" FROM "+net.ddp2p.common.table.neighborhood.TNAME+" AS n "+
		" LEFT JOIN "+net.ddp2p.common.table.constituent.TNAME+" AS c ON(n."+net.ddp2p.common.table.neighborhood.submitter_ID+"=c."+net.ddp2p.common.table.constituent.constituent_ID+") "+
		" LEFT JOIN "+net.ddp2p.common.table.organization.TNAME+" AS o ON(n."+net.ddp2p.common.table.neighborhood.organization_ID+"=o."+net.ddp2p.common.table.organization.organization_ID+") "+
		" WHERE n."+net.ddp2p.common.table.neighborhood.organization_ID+"=? AND n."+net.ddp2p.common.table.neighborhood.arrival_date+">? "+
		 " AND o."+net.ddp2p.common.table.organization.broadcasted+" <> '0' "+
		 " AND c."+net.ddp2p.common.table.constituent.broadcasted+" <> '0' "
			+" AND n."+net.ddp2p.common.table.neighborhood.signature+" IS NOT NULL "
			+" AND n."+net.ddp2p.common.table.neighborhood.global_neighborhood_ID+" IS NOT NULL "
		;
	public static ASNNeighborhoodOP[] getNeighborhoodOPs(ASNSyncRequest asr,
			String last_sync_date, String gid, String org_id, String[] _maxDate) throws P2PDDSQLException {
		if(DEBUG) out.println("NeighborhoodHandling:getNeighborhoodOPs: from="+last_sync_date+" to="+_maxDate[0]+" for orgID="+org_id+" orgGID="+gid);
		ASNNeighborhoodOP[] result = null;
		ArrayList<ArrayList<Object>> a;
		String order = " ORDER BY n."+net.ddp2p.common.table.neighborhood.arrival_date+";";
		if (_maxDate[0] == null) {
			a = Application.getDB().select(sql_neigh+order, new String[]{org_id,last_sync_date}, DEBUG);
		} else {
			String cond = " AND n."+net.ddp2p.common.table.neighborhood.arrival_date+"<=? ";
			a = Application.getDB().select(sql_neigh+cond+order, new String[]{org_id,last_sync_date, _maxDate[0]}, DEBUG);			
		}
		if (a.size() == 0) return null;
		result = new ASNNeighborhoodOP[a.size()];
		for (int k=0; k<result.length; k++) 
			result[k]=getNeighborhoodOP(a.get(k));
		if(DEBUG) out.println("NeighborhoodHandling:getNeighborhoodOPs: neighborhoods="+((result!=null)?result.length:"null"));
		return result;
	}
	private static ASNNeighborhoodOP getNeighborhoodOP(ArrayList<Object> N) {
		ASNNeighborhoodOP result = new ASNNeighborhoodOP();
		result.neighborhood = 
				D_Neighborhood.getNeighByLID(Util.getString(0), true, false);
		return result;
	}
	public static boolean integrateNewData(ASNNeighborhoodOP[] neighborhoods,
			String orgGID, String org_local_ID, String arrival_time, D_Organization orgData,
			RequestData sol_rq, RequestData new_rq, D_Peer __peer) throws P2PDDSQLException {
		boolean default_blocked = false;
		if(neighborhoods == null) return false;
		boolean result = false;
		for (int k = 0; k < neighborhoods.length; k ++) {
			String submit_ID;
			boolean existingConstituentSigned[] = new boolean[]{false};
			if (neighborhoods[k].neighborhood.getSubmitterLIDstr() != null) submit_ID = neighborhoods[k].neighborhood.getSubmitterLIDstr();
			else {
				D_Constituent c = D_Constituent.getConstByGID_or_GIDH(neighborhoods[k].neighborhood.getSubmitter_GID(), null, true, false, Util.Lval(org_local_ID));
				submit_ID = c.getLIDstr();
				existingConstituentSigned[0] = c.isTemporary();
			}
			if (submit_ID == null) {
				submit_ID = ""+D_Constituent.insertTemporaryGID(neighborhoods[k].neighborhood.getSubmitter_GID(), null, Util.lval(org_local_ID), __peer, default_blocked);
				new_rq.cons.put(neighborhoods[k].neighborhood.getSubmitter_GID(), DD.EMPTYDATE);
			} else {
				if((existingConstituentSigned[0]) || (1==D_Constituent.isGID_available(neighborhoods[k].neighborhood.getSubmitter_GID(), Util.lval(org_local_ID), DEBUG)))
					sol_rq.cons.put(neighborhoods[k].neighborhood.getSubmitter_GID(), DD.EMPTYDATE);
			}
			neighborhoods[k].neighborhood.setSubmitterLIDstr(submit_ID);
			if (neighborhoods[k].neighborhood.getParent_GID() != null) {
				boolean existingNeighborhoodSigned[] = new boolean[]{false};
				String parent_neighborhood_ID = null;
				String parent_neighborhood_GID = neighborhoods[k].neighborhood.getParent_GID();
				D_Neighborhood dn = D_Neighborhood.getNeighByGID(parent_neighborhood_GID, true, false, Util.Lval(org_local_ID));
				if (neighborhoods[k].neighborhood.getParentLIDstr() != null) {
					parent_neighborhood_ID = neighborhoods[k].neighborhood.getParentLIDstr();
				} else {
					if (dn != null) {
						parent_neighborhood_ID = dn.getLIDstr();
						existingNeighborhoodSigned[0] = !dn.isTemporary();
					}
				}
				if(DEBUG) System.out.println("\nNeighborhoodHandling: integrateNewData: no local parent: \n "+neighborhoods[k].neighborhood);
				if (parent_neighborhood_ID == null) {
					if (parent_neighborhood_GID != null) {
						if(DEBUG) System.out.println("\nNeighborhoodHandling: integrateNewData: will temp neigh: \n "+neighborhoods[k].neighborhood);
						parent_neighborhood_ID = Util.getStringID(D_Neighborhood.insertTemporaryGID(parent_neighborhood_GID, Util.lval(org_local_ID), __peer, default_blocked));
						new_rq.neig.add(parent_neighborhood_GID);
					}
				} else {
					if ((existingNeighborhoodSigned[0]) || (1 == D_Neighborhood.isGIDavailable(neighborhoods[k].neighborhood.getParent_GID(), Util.lval(org_local_ID), DEBUG))){
						sol_rq.neig.add(neighborhoods[k].neighborhood.getParent_GID());
					} else {
						if (DEBUG) System.out.println("\nNeighborhoodHandling: integrateNewData: needed???: \n "+neighborhoods[k].neighborhood);						
					}
				}
				neighborhoods[k].neighborhood.setParentLIDstr(parent_neighborhood_ID);
			} else {
				if (DEBUG) System.out.println("\nNeighborhoodHandling: integrateNewData: no parent for: \n "+neighborhoods[k].neighborhood);
			}
			if(DEBUG) System.out.println("\nNeighborhoodHandling: integrateNewData: will integrate: \n "+neighborhoods[k].neighborhood);						
			sol_rq.neig.add(neighborhoods[k].neighborhood.getGID());
			result |= integrateNewNeighborhoodOPData(neighborhoods[k],
					orgGID, org_local_ID, arrival_time, orgData, sol_rq, new_rq, __peer);
		}
		return result;
	}
	private static boolean integrateNewNeighborhoodOPData(
			ASNNeighborhoodOP neighborhood, String orgGID,
			String org_local_ID, String arrival_time, D_Organization orgData,
			RequestData sol_rq, RequestData new_rq, D_Peer __peer) throws P2PDDSQLException {
		if (DEBUG) System.out.println("\nNeighborhoodHandling:integrateNewNeighborhoodData: start on "+neighborhood);
		if (neighborhood == null) return false;
		boolean result = false;
		D_Neighborhood wn = neighborhood.neighborhood;
		wn.setOrgGID(orgGID);
		wn.setOrgLID(Util.lval(org_local_ID,-1));
		long id = wn.storeRemoteThis(orgGID, Util.lval(org_local_ID), arrival_time, sol_rq, new_rq, __peer);
		result = (id > 0);
		if(DEBUG) System.out.println("NeighborhoodHandling:integrateNewNeighborhoodData: exit");
		return result;
	}
}
