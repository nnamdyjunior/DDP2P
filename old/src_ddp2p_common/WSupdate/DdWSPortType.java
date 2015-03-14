
package WSupdate;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.4-b01
 * Generated source version: 2.2
 * 
 */
@WebService(name = "ddWSPortType", targetNamespace = "urn:ddWS")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@XmlSeeAlso({
    ObjectFactory.class
})
public interface DdWSPortType {


    /**
     * Get version information of the DD system
     * 
     * @param in
     * @return
     *     returns WSupdate.VersionInfo
     */
    @WebMethod(action = "urn:ddWS#getVersionInfo")
    @WebResult(partName = "return")
    public VersionInfo getVersionInfo(
        @WebParam(name = "in", partName = "in")
        History in);

}