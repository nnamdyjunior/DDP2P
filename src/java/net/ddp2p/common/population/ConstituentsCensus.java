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
package net.ddp2p.common.population;
import java.util.ArrayList;
import java.util.HashSet;
import static net.ddp2p.common.util.Util.__;
import net.ddp2p.common.config.Application_GUI;
import net.ddp2p.common.data.D_Constituent;
import net.ddp2p.common.data.D_Neighborhood;
import net.ddp2p.common.util.P2PDDSQLException;
import net.ddp2p.common.util.Util;
public
class ConstituentsCensus extends net.ddp2p.common.util.DDP2P_ServiceThread {
	private static final boolean DEBUG = false;
	private static final boolean _DEBUG = true;
	public ConstituentsAddressNode root;
	public ConstituentsInterfaceInput model;
	public ConstituentsInterfaceDone done_model;
	boolean running = true;
	public ConstituentsCensus(ConstituentsInterfaceInput constituentsModel, ConstituentsInterfaceDone done,
			ConstituentsAddressNode _root) {
		super("ConstituentsCensus", true);
		done_model =  done;
		model = constituentsModel;
		root = _root;
	}
	public void _run() {
		if(DEBUG) System.err.println("ConstituentsModel:run: start");
		long result = 0;
		try {
			result = censusRoot();
		} catch (P2PDDSQLException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		done(result);
		if(DEBUG) System.err.println("ConstituentsModel:run: done #="+result);
	}
	private long censusRoot() throws P2PDDSQLException {
		if(DEBUG) System.err.println("ConstituentsModel:censusRoot: start");
		long result = 0;
		if(root==null) return 0;
		if(root.getChildren() == null) return 0;
		for(int k=0; k<root.getChildren().length; k++) {
			if(!running){
				if(DEBUG) System.err.println("ConstituentsModel:censusRoot: end abandon");
				return 0;
			}
			ConstituentsNode crt = root.getChildren()[k];
			if(crt instanceof ConstituentsAddressNode) 
				result += census((ConstituentsAddressNode)crt);
			if(crt instanceof ConstituentsIDNode)
				result ++;
		}
		if(DEBUG) System.err.println("ConstituentsModel:censusRoot: end #="+result);
		return result;
	}
	/**
	 * Compute inhabitants of a node as sum of inhabitants in his sub-neighborhoods
	 * @param crt
	 * @return
	 * @throws P2PDDSQLException 
	 */
	private long census(ConstituentsAddressNode crt) throws P2PDDSQLException {
		if(DEBUG) System.err.println("ConstituentsModel:census: start");
		if((crt==null)||(crt.getNeighborhoodData()==null)){
			if(DEBUG) System.err.println("ConstituentsModel:census: end no ID");
			return 0;
		}
		long n_ID = crt.getNeighborhoodData().neighborhoodID;
		if(DEBUG) System.err.println("ConstituentsModel:census: start nID="+n_ID);
		if(n_ID <= 0){
			if(DEBUG) System.err.println("ConstituentsModel:census: start nID="+n_ID+" abandon");		
			return 0;
		}
		long result = 0;
		int neighborhoods[] = {0};
		if (crt.isColapsed()) {
			if(DEBUG) System.err.println("ConstituentsModel:census: this is colapsed");
			result = censusColapsed(crt, neighborhoods);
		} else {
			for(int k=0; k<crt.getChildren().length; k++) {
				if(!running){
					if(DEBUG) System.err.println("ConstituentsModel:census: start nID="+n_ID+" abandon request");		
					return 0;
				}
				ConstituentsNode child = crt.getChildren()[k];
				if(child instanceof ConstituentsAddressNode)  {
					result += census((ConstituentsAddressNode)child);
					neighborhoods[0]++;
				}
				if(child instanceof ConstituentsIDNode)
					result ++;
			}
		}
		crt.setNeighborhoods(neighborhoods[0]);
		crt.getLocation().inhabitants = (int)result;
		crt.getLocation().censusDone = true;
		announce(crt);
		if(DEBUG) System.err.println("ConstituentsModel:censusColapsed: start nID="+n_ID+" got="+result);		
		return result;
	}
	/**
	 * Compute the number of inhabitants and immediate neighborhoods in a visible but collapsed node
	 * @param crt
	 * @param neighborhoods an initialized array of 1 int, to increment by the # of neighborhoods
	 * @return
	 * @throws P2PDDSQLException 
	 */
	private long censusColapsed(ConstituentsAddressNode crt, int[]neighborhoods) throws P2PDDSQLException {
		if(DEBUG) System.err.println("ConstituentsModel:censusColapsed: start");
		long result = 0;
		if((crt==null)||(crt.getNeighborhoodData()==null)) return 0;
		long n_ID = crt.getNeighborhoodData().neighborhoodID;
		if(DEBUG) System.err.println("ConstituentsModel:censusColapsed: start nID="+n_ID);
		if(n_ID <= 0) return 0;
		result = D_Constituent.getConstNBinNeighborhood(n_ID);
		ArrayList<Long> n = D_Neighborhood.getNeighborhoodChildrenIDs(n_ID);
		neighborhoods[0] += n.size();
		HashSet<String> visited = new HashSet<String>();
		visited.add(""+n_ID);
		for(int k = 0; k < n.size(); k ++) {
			if (! running) return 0;
			result += censusHiddenNeighborhoods(Util.lval(n.get(k), -1), visited);
		}
		return result;
	}
	private long censusHiddenNeighborhoods(long n_ID, HashSet<String> visited) throws P2PDDSQLException {
		if(DEBUG) System.err.println("ConstituentsModel:censusHiddenNeighborhoods: start n_ID="+n_ID);
		if(n_ID<=0) return 0;
		if(visited.contains(""+n_ID)){
			Application_GUI.warning(__("Circular Neighborhood: "+n_ID), __("Circular Neighborhood"));
			return 0;
		}
		long result = 0;
		visited.add(""+n_ID);
		result = D_Constituent.getConstNBinNeighborhood(n_ID);
		ArrayList<Long> n = D_Neighborhood.getNeighborhoodChildrenIDs(n_ID);
		for(int k=0; k<n.size(); k++) {
			if(!running) return 0;
			result += censusHiddenNeighborhoods(Util.lval(n.get(k), -1), visited);
		}
		return result;
	}
	/**
	 * announce census intermediary result only if it is still relevant
	 * @param crt
	 */
	private void announce(ConstituentsAddressNode crt) {
		if(DEBUG) System.err.println("ConstituentsModel:announce: start");
		if(DEBUG) if((crt!=null) && (crt.getNeighborhoodData()!=null)) System.err.println("ConstituentsModel:announce: start nID="+crt.getNeighborhoodData().neighborhoodID);
		Object[] path=null;
		synchronized(this) {
			if(!running || (model==null)){
				if(DEBUG) System.err.println("ConstituentsModel:announce: irrelevant");
				return;
			}
			path = crt.getPath();
		}
		model.updateCensus(this, path);
	}
	public void giveUp() {
		if(DEBUG) System.err.println("ConstituentsModel:giveUp: start");
		synchronized(this) {
			model = null;
			running = false;
		}
	}
	private void done(long result){
		if (done_model != null)
			done_model.censusDone(this, result);
		done_model = null;
	}
}
