package ru.asander.ws.service;

import ru.asander.ws.uploaddoc.req.UploadDocReqType;
import ru.asander.ws.uploaddoc.resp.UploadDocRespType;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Action;

/**
 * ========== StreamService.java ==========
 * <p/>
 * $Revision:  $<br/>
 * $Author:  $<br/>
 * $HeadURL:  $<br/>
 * $Id:  $
 * <p/>
 * 07.06.2019 17:03: Original version (AAVolkov)<br/>
 */
@Local
@WebService(targetNamespace = "http://www.asander.ru/ws/service/")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface StreamService {

    @Action(input = "http://asander.ru/ws/UploadDoc_req", output = "http://asander.ru/ws/UploadDoc_req")
    @WebMethod(operationName = "UploadDoc")
    UploadDocRespType uploadDoc(
            @WebParam(name = "UploadDocReq", targetNamespace = "http://asander.ru/ws/UploadDoc/req")
                    UploadDocReqType req);
}
