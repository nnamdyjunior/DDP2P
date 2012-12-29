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
/*
 * Describes an address as sent between a directory and a client
 */
package hds;

import java.util.regex.Pattern;

import ASN1.ASN1DecoderFail;
import ASN1.ASNObj;
import ASN1.Decoder;
import ASN1.Encoder;

public class Address extends ASNObj{
	public static final String ADDR_TYPE_SEP = "^";//DirectoryServer.ADDR_SEP;
	public static final String ADDR_PART_SEP = ":";
	private static final boolean _DEBUG = true;
	private static final boolean DEBUG = false;
	public static final String SOCKET = "Socket";
	public static final String DIR = "DIR";
	public String domain;
	String protocol;
	int tcp_port;
	public int udp_port;
	public String toString(){
		return ((protocol!=null)?(protocol+"://"):"")+domain+ADDR_PART_SEP+tcp_port+((udp_port>0)?(ADDR_PART_SEP+udp_port):"");
	}
	public Address(String domain_port){
		if(domain_port == null) return;
		protocol = getProtocol(domain_port);
		domain = getDomain(domain_port);
		tcp_port = getTCP(domain_port);
		udp_port = getUDP(domain_port);
		/*
		String dp[] = domain_port.split(":");
		if(dp.length<2) return;
		domain = dp[0];
		String[]proto = domain.split("://");
		if(proto.length>1){
			protocol = proto[0];
			domain = proto[1];
		}else{
			domain = proto[0];
		}
		tcp_port = Integer.parseInt(dp[1]);
		if(dp.length>2)
			udp_port = Integer.parseInt(dp[2]);
		else udp_port = tcp_port;
		*/
	}
	Address(String _domain, int _tcp_port, int _udp_port){
		if(_domain == null) return;
		domain = _domain; tcp_port = _tcp_port; udp_port = _udp_port;
	}
	public Encoder getEncoder() {
		Encoder enc = new Encoder().initSequence();
		enc.addToSequence(new Encoder(domain).setASN1Type(Encoder.TAG_PrintableString));
		enc.addToSequence(new Encoder(tcp_port));
		enc.addToSequence(new Encoder(udp_port));
		if(protocol!=null)
			enc.addToSequence(new Encoder(protocol).setASN1Type(Encoder.TAG_PrintableString));
		return enc;
	}
	public Address decode(Decoder dec){
		Decoder content;
		try {
			content = dec.getContent();
		} catch (ASN1DecoderFail e) {
			e.printStackTrace();
			return this;
		}
		domain = content.getFirstObject(true).getString();
		tcp_port = content.getFirstObject(true).getInteger().intValue();
		udp_port = content.getFirstObject(true).getInteger().intValue();
		if(content.getFirstObject(false)!=null)
			protocol = content.getFirstObject(true).getString();
		return this;
	}
	public static String getAddress(String _domain, int _tcp_port, int _udp_port, String _protocol) {
		String port;
		if(_udp_port>=0) port = _tcp_port+ADDR_PART_SEP+_udp_port;
		else port = _tcp_port+"";
		if(_protocol == null) return _domain+ADDR_PART_SEP+port;
		return _protocol+"://"+_domain+ADDR_PART_SEP+port;
	}
	public static String getProtocol(String address){
		String[] protos = address.split("://");
		if(protos.length > 1) return protos[0].trim();
		return null;
	}
	public static String getDomain(String address){
		String domain;
		String[] protos = address.split("://");
		if(protos.length > 1) domain = protos[1];
		else domain = protos[0];
		return domain.split(Pattern.quote(ADDR_PART_SEP))[0].trim();
	}
	public static int getTCP(String address){
		String domain;
		String[] protos = address.split("://");
		if(protos.length > 1) domain = protos[1];
		else domain = protos[0];
		String[] ports = domain.split(Pattern.quote(ADDR_PART_SEP));
		if(ports.length<2) return -1;
		if((ports[1]==null) || (ports[1].length()==0)) return 0;
		return Integer.parseInt(ports[1]);
	}
	public static int getUDP(String address){
		String domain;
		String[] protos = address.split("://");
		if(protos.length > 1) domain = protos[1];
		else domain = protos[0];
		String[] ports = domain.split(Pattern.quote(ADDR_PART_SEP));
		if(ports.length<2) return -1; 
		if(ports.length==2) return Integer.parseInt(ports[1]); 
		return Integer.parseInt(ports[2]);
	}
	public static String joinAddresses(String addresses1, String addrSep,
			String addresses2) {
		return addresses1+addrSep+addresses2;
	}
	public static String joinAddresses(String addresses1,
			String addresses2) {
		return addresses1+DirectoryServer.ADDR_SEP+addresses2;
	}
	public static String[] split(String address) {
		if(address==null) return new String[0];
		String[] result;
		if(DEBUG) System.out.println("Address:split: parsing address "+address+" BY "+DirectoryServer.ADDR_SEP);
		result = address.split(Pattern.quote(DirectoryServer.ADDR_SEP));
		if(DEBUG) System.out.println("Address:split: parsing address #="+result.length);
		for(int k=0;k<result.length; k++){
			if(DEBUG) System.out.println("Address:split: parsing address #["+k+"]="+result[k]);
		}
		return result;
	}
	
}