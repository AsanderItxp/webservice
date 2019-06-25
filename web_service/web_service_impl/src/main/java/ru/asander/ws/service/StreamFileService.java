package ru.asander.ws.service;

import com.sun.xml.ws.developer.StreamingAttachment;
import com.sun.xml.ws.developer.StreamingDataHandler;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.asander.ws.common.result.IntegrationSimpleResultDataType;
import ru.asander.ws.uploaddoc.resp.UploadDocRespType;
import ru.asander.ws.uploaddoc.req.UploadDocReqType;

import javax.jws.WebService;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.MTOM;
import javax.xml.ws.soap.SOAPBinding;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * ========== asander.java ==========
 * <p/>
 * $Revision:  $<br/>
 * $Author:  $<br/>
 * $HeadURL:  $<br/>
 * $Id:  $
 * <p/>
 * 07.06.2019 15:14: Original version (AAVolkov)<br/>
 */
@WebService(portName = "StreamFileServicePort",
        serviceName = "StreamFileService",
        targetNamespace = "http://www.asander.ru/ws/service/",
        endpointInterface = "ru.asander.ws.service.StreamService")

@StreamingAttachment(
        parseEagerly = true,      //TRUE - Сперва ждем загрузки файла. FALSE - Наоборот сначала вход в метод потом качаем стрим.
        memoryThreshold = 40000L  //Размер буфера для хранения в памяти.
        //dir = "U:\\temp"        //Временный каталог сохранения данных.
)
@BindingType(value = SOAPBinding.SOAP12HTTP_MTOM_BINDING)
@MTOM
public class StreamFileService implements StreamService {
    private static final Logger LOG = LoggerFactory.getLogger(StreamFileService.class);

    public UploadDocRespType uploadDoc(UploadDocReqType req) {
        LOG.info("\n =============================== ");
        LOG.debug("\nin uploadDoc started...");

        try (

                StreamingDataHandler sdh = (StreamingDataHandler) req.getDocument().getData();
                InputStream in = sdh.readOnce(); // после прочтения файл автоматически удаляется. Поток более не доступен.
                OutputStream out = new FileOutputStream(req.getDocument().getDocName());
        ) {
            LOG.debug("in uploadDoc finished received");
            LOG.debug("in uploadDoc dataHandler ...{}\n", req.getDocument().getData());


            LOG.info("StreamingDataHandler: name: {}", sdh.getName());
            LOG.info("StreamingDataHandler: contentType: {}", sdh.getContentType());

            IOUtils.copy(in, out);
            LOG.debug("in uploadDoc file moved to {}", req.getDocument().getDocName());
            LOG.info("\n===============================");
        } catch (Exception e) {
            throw new WebServiceException(e);
        }

        UploadDocRespType response = new UploadDocRespType();
        response.setDocID(req.getDocument().getDocName());
        IntegrationSimpleResultDataType rData = new IntegrationSimpleResultDataType();
        rData.setResultCode("Ok");
        response.setResultData(rData);
        return response;
    }


}
